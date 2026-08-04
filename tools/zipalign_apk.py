#!/usr/bin/env python3
"""Align uncompressed APK entries before signing.

Native libraries use 16 KiB alignment; other stored entries use the Android
default four-byte alignment. Padding is written only to local headers, matching
the layout produced by Android's zipalign tool.
"""

from __future__ import annotations

import argparse
import struct
from dataclasses import dataclass
from pathlib import Path


LOCAL_SIGNATURE = 0x04034B50
CENTRAL_SIGNATURE = 0x02014B50
EOCD_SIGNATURE = 0x06054B50


@dataclass
class Entry:
    central_start: int
    central_end: int
    local_offset: int
    method: int
    filename: bytes


def find_eocd(data: bytes) -> int:
    minimum = max(0, len(data) - 22 - 0xFFFF)
    signature = struct.pack("<I", EOCD_SIGNATURE)
    offset = data.rfind(signature, minimum)
    if offset < 0:
        raise ValueError("End-of-central-directory record not found")
    return offset


def read_entries(data: bytes, eocd_offset: int) -> tuple[list[Entry], int, int]:
    disk, central_disk, disk_count, total_count, central_size, central_offset = struct.unpack_from(
        "<HHHHII", data, eocd_offset + 4
    )
    if disk or central_disk or disk_count != total_count:
        raise ValueError("Split APK archives are not supported")

    entries: list[Entry] = []
    cursor = central_offset
    for _ in range(total_count):
        if struct.unpack_from("<I", data, cursor)[0] != CENTRAL_SIGNATURE:
            raise ValueError(f"Invalid central-directory entry at {cursor}")
        method = struct.unpack_from("<H", data, cursor + 10)[0]
        name_length, extra_length, comment_length = struct.unpack_from("<HHH", data, cursor + 28)
        local_offset = struct.unpack_from("<I", data, cursor + 42)[0]
        end = cursor + 46 + name_length + extra_length + comment_length
        filename = data[cursor + 46 : cursor + 46 + name_length]
        entries.append(Entry(cursor, end, local_offset, method, filename))
        cursor = end

    if cursor != central_offset + central_size:
        raise ValueError("Central-directory size mismatch")
    return entries, central_offset, central_size


def align_apk(source: Path, destination: Path) -> None:
    data = source.read_bytes()
    eocd_offset = find_eocd(data)
    entries, central_offset, central_size = read_entries(data, eocd_offset)
    by_local_offset = sorted(entries, key=lambda entry: entry.local_offset)

    output = bytearray()
    new_offsets: dict[int, int] = {}
    for index, entry in enumerate(by_local_offset):
        start = entry.local_offset
        end = by_local_offset[index + 1].local_offset if index + 1 < len(by_local_offset) else central_offset
        if struct.unpack_from("<I", data, start)[0] != LOCAL_SIGNATURE:
            raise ValueError(f"Invalid local-file header at {start}")

        name_length, extra_length = struct.unpack_from("<HH", data, start + 26)
        header_end = start + 30 + name_length
        data_start = header_end + extra_length
        local_name = data[start + 30 : header_end]
        if local_name != entry.filename:
            raise ValueError(f"Filename mismatch for {entry.filename!r}")

        new_start = len(output)
        new_offsets[entry.central_start] = new_start
        alignment = 1
        if entry.method == 0:
            alignment = 16384 if entry.filename.endswith(b".so") else 4
        padding = (-(new_start + 30 + name_length + extra_length)) % alignment
        if extra_length + padding > 0xFFFF:
            raise ValueError(f"Extra field too large for {entry.filename!r}")

        header = bytearray(data[start : start + 30])
        struct.pack_into("<H", header, 28, extra_length + padding)
        output.extend(header)
        output.extend(local_name)
        output.extend(data[header_end:data_start])
        output.extend(b"\0" * padding)
        output.extend(data[data_start:end])

    new_central_offset = len(output)
    for entry in entries:
        central_record = bytearray(data[entry.central_start : entry.central_end])
        struct.pack_into("<I", central_record, 42, new_offsets[entry.central_start])
        output.extend(central_record)

    if len(output) - new_central_offset != central_size:
        raise ValueError("Rebuilt central-directory size mismatch")

    eocd = bytearray(data[eocd_offset:])
    struct.pack_into("<I", eocd, 16, new_central_offset)
    output.extend(eocd)
    destination.write_bytes(output)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("destination", type=Path)
    arguments = parser.parse_args()
    align_apk(arguments.source, arguments.destination)


if __name__ == "__main__":
    main()
