#!/usr/bin/env python3
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
DRAWABLES = ROOT / "app/src/main/res/drawable"
OUTPUT = ROOT / "design/hermes-filled-icons-v046.svg"
ANDROID = "{http://schemas.android.com/apk/res/android}"

COLORS = {
    "hermes_icon_blue": "#4E78E8",
    "hermes_icon_mint": "#30B69A",
    "hermes_icon_amber": "#F2B84B",
    "hermes_icon_coral": "#EF7B72",
    "hermes_icon_lavender": "#8A72E8",
    "hermes_icon_ink": "#34435F",
}

LABELS = {
    "ai": "AI", "appearance": "Appearance", "archive": "Archive", "artifact": "Artifact",
    "attachment": "Add content", "chat": "Chat", "connection": "Connection", "delete": "Delete",
    "file": "File", "folder": "Folder", "information": "Information", "link": "Link",
    "microphone": "Voice", "model": "Models", "move": "Move", "new_chat": "New chat",
    "notification": "Notification", "photo": "Photo", "pinned": "Pinned", "profile": "Profile",
    "project": "Projects", "recent": "Recent", "rename": "Rename", "search": "Search",
    "space": "Space", "storage": "Storage", "task": "Tasks", "todo": "Todos",
    "verified": "Verified",
}


def resolve_color(raw: str) -> tuple[str, float]:
    if raw.startswith("@color/"):
        return COLORS[raw.removeprefix("@color/")], 1.0
    if raw.startswith("#") and len(raw) == 9:
        alpha = int(raw[1:3], 16) / 255
        return f"#{raw[3:]}", alpha
    return raw, 1.0


def render_icon(file: Path) -> str:
    root = ET.parse(file).getroot()
    rendered = []
    for path in root.findall("path"):
        color, alpha_from_color = resolve_color(path.attrib[ANDROID + "fillColor"])
        alpha = alpha_from_color * float(path.attrib.get(ANDROID + "fillAlpha", "1"))
        fill_rule = path.attrib.get(ANDROID + "fillType", "nonZero").lower()
        rule = "evenodd" if fill_rule == "evenodd" else "nonzero"
        rendered.append(
            f'<path d="{path.attrib[ANDROID + "pathData"]}" fill="{color}" '
            f'fill-opacity="{alpha:.3f}" fill-rule="{rule}"/>'
        )
    return "".join(rendered)


def main() -> None:
    files = sorted(DRAWABLES.glob("hermes_ic_*.xml"))
    width, height = 1080, 1260
    cell_w, cell_h, columns = 190, 166, 5
    left, top = 65, 210
    cards = []
    for index, file in enumerate(files):
        name = file.stem.removeprefix("hermes_ic_")
        row, col = divmod(index, columns)
        x, y = left + col * cell_w, top + row * cell_h
        icon = render_icon(file)
        cards.append(
            f'<g transform="translate({x} {y})">'
            '<rect width="166" height="142" rx="28" fill="#FFFFFF" filter="url(#shadow)"/>'
            f'<g transform="translate(45 22) scale(3.15)">{icon}</g>'
            f'<text x="83" y="122" text-anchor="middle" class="label">{LABELS[name]}</text>'
            '</g>'
        )

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(
        f'''<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">
<defs>
  <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#F8FAFE"/><stop offset="0.5" stop-color="#F1F6FD"/><stop offset="1" stop-color="#F8F3FC"/></linearGradient>
  <filter id="shadow" x="-25%" y="-25%" width="150%" height="170%"><feDropShadow dx="0" dy="7" stdDeviation="11" flood-color="#415579" flood-opacity="0.10"/></filter>
  <style>.label{{font:600 17px 'Noto Sans CJK SC','DejaVu Sans',sans-serif;fill:#344054}} .sub{{font:400 18px 'Noto Sans CJK SC','DejaVu Sans',sans-serif;fill:#748095}}</style>
</defs>
<rect width="1080" height="1260" fill="url(#bg)"/>
<circle cx="990" cy="30" r="180" fill="#DDEBFF" opacity=".56"/><circle cx="70" cy="1220" r="180" fill="#E4F7F1" opacity=".72"/>
<text x="65" y="82" font-family="DejaVu Sans,sans-serif" font-size="42" font-weight="800" fill="#182235">Hermes Filled Icon System</text>
<text x="65" y="126" class="sub">v0.4.6 · 28 semantic icons + pin · solid silhouettes · multicolor layers</text>
<rect x="65" y="155" width="286" height="36" rx="18" fill="#E5ECFF"/><text x="208" y="179" text-anchor="middle" font-family="DejaVu Sans,sans-serif" font-size="15" font-weight="700" fill="#4E6FD2">APP RESOURCE PREVIEW</text>
{''.join(cards)}
</svg>''',
        encoding="utf-8",
    )


if __name__ == "__main__":
    main()
