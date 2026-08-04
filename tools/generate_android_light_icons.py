#!/usr/bin/env python3
"""Convert the approved Hermes 24 px SVG icon set to Android VectorDrawables."""

from __future__ import annotations

import math
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


ANDROID_NS = "http://schemas.android.com/apk/res/android"
ET.register_namespace("android", ANDROID_NS)
A = f"{{{ANDROID_NS}}}"

COLOR_RESOURCES = {
    "#3370FF": "@color/hermes_icon_blue",
    "#20C7C9": "@color/hermes_icon_cyan",
    "#24C48E": "@color/hermes_icon_green",
    "#8B5CF6": "@color/hermes_icon_purple",
    "#FFB323": "@color/hermes_icon_amber",
    "#F45B69": "@color/hermes_icon_coral",
    "#596780": "@color/hermes_icon_ink",
    "#FFFFFF": "@color/hermes_icon_on_color",
}


def number(value: float) -> str:
    text = f"{value:.4f}".rstrip("0").rstrip(".")
    return "0" if text in {"-0", ""} else text


def paint(value: str | None, default: str = "#000000") -> str:
    value = (value or default).upper()
    if value == "NONE":
        return "@android:color/transparent"
    return COLOR_RESOURCES.get(value, value)


def rounded_rect_path(element: ET.Element) -> str:
    x = float(element.get("x", "0"))
    y = float(element.get("y", "0"))
    width = float(element.get("width", "0"))
    height = float(element.get("height", "0"))
    rx = float(element.get("rx", element.get("ry", "0")))
    ry = float(element.get("ry", element.get("rx", "0")))
    rx = min(max(rx, 0), width / 2)
    ry = min(max(ry, 0), height / 2)
    if rx == 0 and ry == 0:
        return f"M{number(x)},{number(y)}h{number(width)}v{number(height)}h-{number(width)}Z"
    return (
        f"M{number(x + rx)},{number(y)} "
        f"H{number(x + width - rx)} "
        f"A{number(rx)},{number(ry)} 0 0 1 {number(x + width)},{number(y + ry)} "
        f"V{number(y + height - ry)} "
        f"A{number(rx)},{number(ry)} 0 0 1 {number(x + width - rx)},{number(y + height)} "
        f"H{number(x + rx)} "
        f"A{number(rx)},{number(ry)} 0 0 1 {number(x)},{number(y + height - ry)} "
        f"V{number(y + ry)} "
        f"A{number(rx)},{number(ry)} 0 0 1 {number(x + rx)},{number(y)} Z"
    )


def ellipse_path(cx: float, cy: float, rx: float, ry: float) -> str:
    return (
        f"M{number(cx - rx)},{number(cy)} "
        f"A{number(rx)},{number(ry)} 0 1 0 {number(cx + rx)},{number(cy)} "
        f"A{number(rx)},{number(ry)} 0 1 0 {number(cx - rx)},{number(cy)} Z"
    )


def dashed_circle_paths(element: ET.Element) -> list[str]:
    cx = float(element.get("cx", "0"))
    cy = float(element.get("cy", "0"))
    radius = float(element.get("r", "0"))
    dash, gap = (float(part) for part in element.get("stroke-dasharray", "1 1").split()[:2])
    circumference = math.tau * radius
    segment_count = max(2, round(circumference / (dash + gap)))
    dash_angle = min(360 / segment_count - 8, 360 * dash / circumference)
    step = 360 / segment_count
    paths: list[str] = []
    for index in range(segment_count):
        start_angle = -90 + index * step
        end_angle = start_angle + dash_angle
        start = math.radians(start_angle)
        end = math.radians(end_angle)
        x1, y1 = cx + radius * math.cos(start), cy + radius * math.sin(start)
        x2, y2 = cx + radius * math.cos(end), cy + radius * math.sin(end)
        large_arc = 1 if dash_angle > 180 else 0
        paths.append(
            f"M{number(x1)},{number(y1)} "
            f"A{number(radius)},{number(radius)} 0 {large_arc} 1 {number(x2)},{number(y2)}"
        )
    return paths


def path_data(element: ET.Element) -> list[str]:
    tag = element.tag.rsplit("}", 1)[-1]
    if tag == "path":
        return [element.attrib["d"]]
    if tag == "rect":
        return [rounded_rect_path(element)]
    if tag == "circle":
        if element.get("stroke-dasharray"):
            return dashed_circle_paths(element)
        cx = float(element.get("cx", "0"))
        cy = float(element.get("cy", "0"))
        radius = float(element.get("r", "0"))
        return [ellipse_path(cx, cy, radius, radius)]
    if tag == "ellipse":
        return [
            ellipse_path(
                float(element.get("cx", "0")),
                float(element.get("cy", "0")),
                float(element.get("rx", "0")),
                float(element.get("ry", "0")),
            )
        ]
    raise ValueError(f"Unsupported SVG element: {tag}")


def add_vector_path(vector: ET.Element, element: ET.Element, data: str) -> None:
    attributes = {
        f"{A}pathData": data,
        f"{A}fillColor": paint(element.get("fill")),
    }
    if element.get("stroke"):
        attributes[f"{A}strokeColor"] = paint(element.get("stroke"))
        attributes[f"{A}strokeWidth"] = element.get("stroke-width", "1")
    if element.get("stroke-linecap"):
        attributes[f"{A}strokeLineCap"] = element.attrib["stroke-linecap"]
    if element.get("stroke-linejoin"):
        attributes[f"{A}strokeLineJoin"] = element.attrib["stroke-linejoin"]
    opacity = element.get("opacity")
    if opacity:
        attributes[f"{A}fillAlpha"] = opacity
        if element.get("stroke"):
            attributes[f"{A}strokeAlpha"] = opacity
    if element.get("fill-opacity"):
        attributes[f"{A}fillAlpha"] = element.attrib["fill-opacity"]
    if element.get("stroke-opacity"):
        attributes[f"{A}strokeAlpha"] = element.attrib["stroke-opacity"]
    if element.get("fill-rule") == "evenodd":
        attributes[f"{A}fillType"] = "evenOdd"
    ET.SubElement(vector, "path", attributes)


def convert(svg_path: Path, output_path: Path) -> None:
    svg = ET.parse(svg_path).getroot()
    view_box = [float(value) for value in svg.get("viewBox", "0 0 24 24").split()]
    vector = ET.Element(
        "vector",
        {
            f"{A}width": "24dp",
            f"{A}height": "24dp",
            f"{A}viewportWidth": number(view_box[2]),
            f"{A}viewportHeight": number(view_box[3]),
        },
    )
    for element in svg:
        for data in path_data(element):
            add_vector_path(vector, element, data)
    ET.indent(vector, space="    ")
    output_path.write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n' + ET.tostring(vector, encoding="unicode") + "\n",
        encoding="utf-8",
    )


def main() -> int:
    if len(sys.argv) != 3:
        print("Usage: generate_android_light_icons.py SVG_DIR DRAWABLE_DIR", file=sys.stderr)
        return 2
    source_dir = Path(sys.argv[1])
    output_dir = Path(sys.argv[2])
    output_dir.mkdir(parents=True, exist_ok=True)
    icons = sorted(source_dir.glob("*.svg"))
    if len(icons) != 79:
        raise RuntimeError(f"Expected 79 approved icons, found {len(icons)}")
    for svg_path in icons:
        resource_name = "hermes_light_" + svg_path.stem.replace("-", "_") + ".xml"
        convert(svg_path, output_dir / resource_name)
    print(f"Generated {len(icons)} Android VectorDrawables in {output_dir}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
