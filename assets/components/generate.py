#!/usr/bin/env python3
"""Generate INTERSECTION_* SVGs from the component library."""

import os
import sys

# Allow running from any directory
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from components.scenes_intersection import ALL_SCENES

OUTPUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'svg')


def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    for filename, svg_content in ALL_SCENES.items():
        path = os.path.join(OUTPUT_DIR, filename)
        with open(path, 'w') as f:
            f.write(svg_content)
        size = os.path.getsize(path)
        print(f"  {filename} ({size} bytes)")
    print(f"\nGenerated {len(ALL_SCENES)} files in {os.path.abspath(OUTPUT_DIR)}")


if __name__ == "__main__":
    main()
