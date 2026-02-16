# TX Image-Question Screenshot Capture (Feb 2026)

## Method
- Source: `dmv-android/app/src/main/assets/packs/TX/tx_v1.json` (660 questions)
- Filter: questions with a non-null `image.assetId` field
- Sample rule: for each topic, sort image-backed questions by `id` ascending, select first `min(5, count)`
- Captured on Pixel 8 API 35 emulator via `adb exec-out screencap`

## Capture Environment
- **Emulator**: Pixel 8 API 35 (Android 15), `$ANDROID_SDK_ROOT/emulator/emulator @Pixel_8_API_35 -no-window -no-audio -no-boot-anim`
- **APK**: debug build from `dmv-android/`, installed via `adb install -r`
- **Rendering**: Canonical quiz screen layout (QuizScreen.kt chrome) via `ScreenshotCaptureActivity`

## Capture Steps (Reproducible)

### 1. Build and install
```bash
cd dmv-android && ./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. Trigger pack import
```bash
adb shell am start -n com.dmv.texas/.MainActivity
sleep 5
```

### 3. Capture each question
For each question in the sample list, launch the debug capture Activity with the canonical quiz chrome:
```bash
adb shell am start -n com.dmv.texas/.ui.screen.debug.ScreenshotCaptureActivity \
  --es questionId "TX-SIG-0001" \
  --ei questionIndex 0 \
  --ei totalQuestions 40
sleep 3
adb exec-out screencap -p > TX-SIG-0001__MUTCD_R1-1_STOP.png
```

### 4. Batch capture script
```bash
IDX=0
while IFS='|' read -r QID ASSET; do
  QID=$(echo "$QID" | xargs)
  ASSET=$(echo "$ASSET" | xargs)
  adb shell am start -n com.dmv.texas/.ui.screen.debug.ScreenshotCaptureActivity \
    --es questionId "$QID" --ei questionIndex "$IDX" --ei totalQuestions 40 < /dev/null
  sleep 3
  adb exec-out screencap -p < /dev/null > "${QID}__${ASSET}.png"
  IDX=$((IDX+1))
done < sample_list.txt
```

**Important**: Use `< /dev/null` on `adb` commands inside `while read` loops to prevent stdin consumption.

## What the Screenshots Show
Each screenshot displays the **canonical quiz screen layout** identical to what users see during a quiz:
- **Top bar**: "Quit" button (left), question counter "N / 40" (center), progress bar (below)
- **Content area**: topic badge, question text, SVG image, four Material OutlinedCard answer buttons (A/B/C/D)
- **Bottom bar**: horizontal divider + "Next" navigation button

## Inventory Summary

| Topic | Image Questions | Sampled |
|---|---|---|
| PARKING | 9 | 5 |
| PAVEMENT_MARKINGS | 22 | 5 |
| RIGHT_OF_WAY | 14 | 5 |
| SAFE_DRIVING | 5 | 5 |
| SIGNS | 50 | 5 |
| SPECIAL_SITUATIONS | 12 | 5 |
| SPEED_AND_DISTANCE | 9 | 5 |
| TRAFFIC_SIGNALS | 15 | 5 |
| **Total** | **136** | **40** |

## Sample List

| # | Question ID | Topic | Asset ID |
|---|---|---|---|
| 1 | TX-PRK-0001 | PARKING | PARKING_FIRE_HYDRANT_15FT |
| 2 | TX-PRK-0018 | PARKING | MUTCD_R8-3a_NO_PARKING_ANYTIME |
| 3 | TX-PRK-0020 | PARKING | MUTCD_R7-8_NO_PARKING |
| 4 | TX-PRK-0023 | PARKING | PARKING_PARALLEL_STEPS |
| 5 | TX-PRK-0031 | PARKING | PARKING_HILL_UPHILL_CURB |
| 6 | TX-PAV-0001 | PAVEMENT_MARKINGS | PAVEMENT_SOLID_WHITE_LINE |
| 7 | TX-PAV-0002 | PAVEMENT_MARKINGS | PAVEMENT_DASHED_WHITE_LINE |
| 8 | TX-PAV-0003 | PAVEMENT_MARKINGS | PAVEMENT_DOUBLE_YELLOW_SOLID |
| 9 | TX-PAV-0005 | PAVEMENT_MARKINGS | PAVEMENT_SINGLE_YELLOW_DASHED |
| 10 | TX-PAV-0006 | PAVEMENT_MARKINGS | PAVEMENT_DOUBLE_YELLOW_MIXED |
| 11 | TX-ROW-0001 | RIGHT_OF_WAY | INTERSECTION_4WAY_STOP |
| 12 | TX-ROW-0003 | RIGHT_OF_WAY | INTERSECTION_UNCONTROLLED |
| 13 | TX-ROW-0005 | RIGHT_OF_WAY | INTERSECTION_T_STOP |
| 14 | TX-ROW-0006 | RIGHT_OF_WAY | INTERSECTION_ROUNDABOUT |
| 15 | TX-ROW-0007 | RIGHT_OF_WAY | MUTCD_R1-1_STOP |
| 16 | TX-SAF-0001 | SAFE_DRIVING | SAFE_DEFENSIVE_SPACE_CUSHION |
| 17 | TX-SAF-0003 | SAFE_DRIVING | SPEED_FOLLOWING_DISTANCE_3SEC |
| 18 | TX-SAF-0005 | SAFE_DRIVING | SAFE_BLIND_SPOT_CHECK |
| 19 | TX-SAF-0066 | SAFE_DRIVING | SAFE_TIRE_TREAD_DEPTH |
| 20 | TX-SAF-0071 | SAFE_DRIVING | SAFE_MIRROR_ADJUSTMENT |
| 21 | TX-SIG-0001 | SIGNS | MUTCD_R1-1_STOP |
| 22 | TX-SIG-0002 | SIGNS | MUTCD_R1-2_YIELD |
| 23 | TX-SIG-0003 | SIGNS | MUTCD_R1-2_YIELD |
| 24 | TX-SIG-0004 | SIGNS | MUTCD_R2-1_SPEED_LIMIT_65 |
| 25 | TX-SIG-0005 | SIGNS | MUTCD_R5-1_DO_NOT_ENTER |
| 26 | TX-SPC-0001 | SPECIAL_SITUATIONS | INTERSECTION_SCHOOL_BUS_STOPPED |
| 27 | TX-SPC-0004 | SPECIAL_SITUATIONS | MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20 |
| 28 | TX-SPC-0007 | SPECIAL_SITUATIONS | MUTCD_S1-1_SCHOOL_CROSSING |
| 29 | TX-SPC-0012 | SPECIAL_SITUATIONS | PAVEMENT_SCHOOL_ZONE |
| 30 | TX-SPC-0016 | SPECIAL_SITUATIONS | SPECIAL_RAILROAD_STOP_PROCEDURE |
| 31 | TX-SPD-0001 | SPEED_AND_DISTANCE | SPEED_SCHOOL_ZONE_20MPH |
| 32 | TX-SPD-0002 | SPEED_AND_DISTANCE | SPEED_LIMIT_RESIDENTIAL_30 |
| 33 | TX-SPD-0017 | SPEED_AND_DISTANCE | MUTCD_R2-1_SPEED_LIMIT_70 |
| 34 | TX-SPD-0022 | SPEED_AND_DISTANCE | MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20 |
| 35 | TX-SPD-0030 | SPEED_AND_DISTANCE | MUTCD_R2-1_SPEED_LIMIT_65 |
| 36 | TX-TRA-0001 | TRAFFIC_SIGNALS | SIGNAL_SOLID_RED |
| 37 | TX-TRA-0002 | TRAFFIC_SIGNALS | SIGNAL_SOLID_GREEN |
| 38 | TX-TRA-0003 | TRAFFIC_SIGNALS | SIGNAL_SOLID_YELLOW |
| 39 | TX-TRA-0009 | TRAFFIC_SIGNALS | SIGNAL_RED_YELLOW_TOGETHER |
| 40 | TX-TRA-0021 | TRAFFIC_SIGNALS | SIGNAL_GREEN_ARROW_LEFT |

## Screenshots Location
All 40 screenshots are stored in `dmv-android/docs/evidence/tx-image-question-sample/` with filename format `<questionId>__<assetId>.png`.

## Full Inventory
See `docs/quality/tx-image-question-screenshot-inventory-2026-02.csv` for the complete 136-question inventory with sample flags.
