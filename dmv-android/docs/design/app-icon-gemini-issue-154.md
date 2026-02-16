# App Icon Generation Spec (Issue #154)

Date: 2026-02-16  
Model: `imagen-4.0-generate-001`  
Script: `/Users/ayder/projects/dmv.tx/tools/imagen_generate.py`

## Goal
Create a clean, legible launcher icon for TX DMV Practice that remains clear at small sizes (96px and 48px).

## Final Prompt
```text
Design a clean Android launcher icon for a Texas DMV practice app. Rounded-square navy background. Centered red octagon road-sign shape with a crisp white border and a simple white checkmark in the center. Flat style, high contrast, balanced geometry, minimal details, optimized for legibility at 48px and 96px. IMPORTANT: no letters, no words, no numbers, no labels, no captions, no watermarks, no overlays, no extra symbols, no mockup frame.
```

## Generation Command
```bash
cd /Users/ayder/projects/dmv.tx
python3 tools/imagen_generate.py "<PROMPT>" 4
```

## Candidate Evidence
- `/Users/ayder/projects/dmv.tx/dmv-android/docs/evidence/icon_generation/issue-154/candidate_1.png`
- `/Users/ayder/projects/dmv.tx/dmv-android/docs/evidence/icon_generation/issue-154/candidate_2.png`
- `/Users/ayder/projects/dmv.tx/dmv-android/docs/evidence/icon_generation/issue-154/candidate_3.png`
- `/Users/ayder/projects/dmv.tx/dmv-android/docs/evidence/icon_generation/issue-154/candidate_4_selected.png` (selected)

## Exported Previews
- `/Users/ayder/projects/dmv.tx/dmv-android/docs/evidence/icon_generation/issue-154/previews/icon_1024.png`
- `/Users/ayder/projects/dmv.tx/dmv-android/docs/evidence/icon_generation/issue-154/previews/icon_96.png`
- `/Users/ayder/projects/dmv.tx/dmv-android/docs/evidence/icon_generation/issue-154/previews/icon_48.png`

## Android Integration
Generated PNG icon was resized and exported as `ic_launcher.png` and `ic_launcher_round.png` for:
- `mipmap-mdpi` (48x48)
- `mipmap-hdpi` (72x72)
- `mipmap-xhdpi` (96x96)
- `mipmap-xxhdpi` (144x144)
- `mipmap-xxxhdpi` (192x192)

Updated modules:
- `/Users/ayder/projects/dmv.tx/dmv-android/app/src/main/res/`
- `/Users/ayder/projects/dmv.tx/app/src/main/res/`

