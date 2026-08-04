#!/usr/bin/env python3
"""Render compatibility PNGs for the Hermes Light icon set."""

from __future__ import annotations

import os
import subprocess
import sys
import tempfile
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path


NIGHT_COLORS = {
    "#3370FF": "#82A2FF",
    "#20C7C9": "#44D6D5",
    "#24C48E": "#4ED6A4",
    "#8B5CF6": "#AA8BFF",
    "#FFB323": "#FFC85C",
    "#F45B69": "#FF7A86",
    "#596780": "#D7DEEC",
}


def render(svg_text: str, output: Path, temporary_directory: Path) -> None:
    source = temporary_directory / (output.parent.name + "_" + output.stem + ".svg")
    source.write_text(svg_text, encoding="utf-8")
    env = os.environ.copy()
    env["XDG_CONFIG_HOME"] = str(temporary_directory / "config")
    env["XDG_CACHE_HOME"] = str(temporary_directory / "cache")
    env["XDG_DATA_HOME"] = str(temporary_directory / "data")
    subprocess.run(
        [
            "inkscape",
            str(source),
            "--export-type=png",
            "--export-width=96",
            "--export-height=96",
            "--export-background-opacity=0",
            f"--export-filename={output}",
        ],
        check=True,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
        env=env,
    )


def main() -> int:
    if len(sys.argv) != 4:
        print("Usage: generate_android_light_icon_bitmaps.py SVG_DIR DAY_DIR NIGHT_DIR", file=sys.stderr)
        return 2
    source_dir, day_dir, night_dir = map(Path, sys.argv[1:])
    icons = sorted(source_dir.glob("*.svg"))
    if len(icons) != 79:
        raise RuntimeError(f"Expected 79 approved icons, found {len(icons)}")
    day_dir.mkdir(parents=True, exist_ok=True)
    night_dir.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="hermes-icon-render-") as temp:
        temporary_directory = Path(temp)
        jobs: list[tuple[str, Path]] = []
        for svg in icons:
            name = "hermes_bitmap_" + svg.stem.replace("-", "_") + ".png"
            light = svg.read_text(encoding="utf-8")
            dark = light
            for original, replacement in NIGHT_COLORS.items():
                dark = dark.replace(original, replacement)
            jobs.append((light, day_dir / name))
            jobs.append((dark, night_dir / name))
        with ThreadPoolExecutor(max_workers=4) as executor:
            futures = [executor.submit(render, svg_text, output, temporary_directory) for svg_text, output in jobs]
            for future in futures:
                future.result()
    print(f"Rendered {len(icons)} day and {len(icons)} night PNG icons")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
