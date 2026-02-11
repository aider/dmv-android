#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const MANIFEST = path.join(ROOT, 'assets', 'manifest.json');
const DATE = '2026-02-10';

function readJson(p) {
  return JSON.parse(fs.readFileSync(p, 'utf8'));
}

function writeJson(p, data) {
  fs.writeFileSync(p, `${JSON.stringify(data, null, 2)}\n`, 'utf8');
}

function readSvgColors(svgPath) {
  if (!fs.existsSync(svgPath)) return ['#000000', '#FFFFFF'];
  const txt = fs.readFileSync(svgPath, 'utf8');
  const matches = [...txt.matchAll(/#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})\b/g)].map((m) => m[0].toUpperCase());
  const rank = new Map();
  for (const c of matches) rank.set(c, (rank.get(c) || 0) + 1);
  return [...rank.entries()].sort((a, b) => b[1] - a[1]).map((x) => x[0]).slice(0, 6);
}

function titleFromId(assetId) {
  return assetId.replace(/^MUTCD_/, '').replace(/_/g, ' ').toLowerCase();
}

function category(assetId) {
  if (assetId.startsWith('MUTCD_')) return 'mutcd';
  if (assetId.startsWith('INTERSECTION_')) return 'intersection';
  if (assetId.startsWith('PAVEMENT_')) return 'pavement';
  if (assetId.startsWith('SIGNAL_')) return 'signal';
  if (assetId.startsWith('PARKING_')) return 'parking';
  if (assetId.startsWith('SAFE_')) return 'safe';
  if (assetId.startsWith('SPEED_')) return 'speed';
  if (assetId.startsWith('SPECIAL_')) return 'special';
  if (assetId.startsWith('MARKING_')) return 'marking';
  return 'generic';
}

function symbolHint(assetId, cat) {
  const s = assetId;
  if (cat === 'pavement') {
    if (s.includes('STOP_LINE')) return 'horizontal stop bar marking across lane, high contrast with pavement';
    if (s.includes('YIELD_LINE')) return 'yield shark-tooth pattern aligned to lane approach';
    if (s.includes('ARROW_')) return 'lane-direction arrow marking with clean symmetric head and shaft';
    if (s.includes('RAILROAD')) return 'railroad crossing pavement marking with centered X and letters';
    if (s.includes('SCHOOL_ZONE')) return 'school-zone pavement message with balanced line spacing';
    return 'road surface marking composition with lane-centric alignment';
  }
  if (s.includes('_R1-1_') || s.endsWith('_STOP')) return 'octagon, 8 sides, centered, equal edge lengths';
  if (s.includes('_R1-2_') || s.endsWith('_YIELD')) return 'equilateral triangle, apex down, centered';
  if (s.includes('SPEED_LIMIT')) return 'vertical regulatory rectangle, centered text blocks';
  if (s.includes('NO_LEFT_TURN')) return 'black left-turn arrow in red prohibition circle with 45-degree slash';
  if (s.includes('NO_RIGHT_TURN')) return 'black right-turn arrow in red prohibition circle with 45-degree slash';
  if (s.includes('NO_U_TURN')) return 'black U-turn arrow in red prohibition circle with 45-degree slash';
  if (s.includes('ONE_WAY')) return 'horizontal black rectangle with white one-way text and directional arrow';
  if (s.includes('KEEP_RIGHT')) return 'vertical marker with bold black arrow keeping right of obstacle';
  if (s.includes('KEEP_LEFT')) return 'vertical marker with bold black arrow keeping left of obstacle';
  if (s.includes('PEDESTRIAN')) return 'human figure pictogram with MUTCD-style stroke rhythm';
  if (s.includes('BICYCLE')) return 'bicycle pictogram with balanced wheel diameters and frame';
  if (s.includes('TRUCK')) return 'truck pictogram silhouette centered in warning diamond';
  if (s.includes('WINDING_ROAD')) return 'single winding S-curve arrow, not rectangular panel arrow';
  if (s.includes('MERGE')) return 'lane-merge symbol with clear converging geometry';
  if (s.includes('HILL')) return 'grade curve plus vehicle icon, icon size >= 22% of sign width';
  if (s.includes('ROUNDABOUT')) return 'circular roundabout arrows with central island';
  if (s.includes('CROSSWALK')) return 'crosswalk stripes and pedestrian geometry with high contrast';
  if (s.includes('SIGNAL')) return 'traffic signal housing, evenly spaced light modules';
  return 'centered iconography consistent with MUTCD-like proportions';
}

function baseConstraints(cat) {
  const shared = [
    'Output must be a clean SVG only, no editor metadata.',
    'Use viewBox "0 0 200 200" and keep composition centered.',
    'Keep 10-14 units safe padding from all outer edges.',
    'No clipping. All strokes and text must remain inside canvas.',
    'Use consistent stroke widths (4, 6, 8, 10 family only).',
    'Use flat colors only (no gradients, shadows, filters).',
    'Typography: bold sans-serif look, centered and legible at mobile size.',
  ];

  const map = {
    mutcd: 'Follow MUTCD visual language: strict geometry, borders, and symbol clarity.',
    intersection: 'Top-down road diagram style, high-contrast lanes and markings.',
    pavement: 'Road-surface marking style, directional arrows and line types must be clear.',
    signal: 'Signal modules must read at small size (red/yellow/green hierarchy).',
    parking: 'Parking instruction diagram style with simple didactic composition.',
    safe: 'Safety concept illustration with 1 primary idea per icon.',
    speed: 'Speed instruction visual with short text and high legibility.',
    special: 'Special-case scene with one focal action and minimal clutter.',
    marking: 'Hand-signal demonstration style with clear limb orientation.',
    generic: 'Consistent DMV training icon style and spacing.',
  };
  return [map[cat] || map.generic, ...shared];
}

function buildPrompt(entry, colors) {
  const cat = category(entry.assetId);
  const title = titleFromId(entry.assetId);
  const hint = symbolHint(entry.assetId, cat);
  const palette = colors.join(', ');

  const prompt = [
    `Create a standards-aligned DMV training SVG for: ${title}.`,
    '',
    'Geometry and Composition:',
    `- Primary symbol: ${hint}.`,
    '- Center the sign/symbol block both horizontally and vertically.',
    '- Preserve symmetric spacing between border, symbol, and text.',
    '- Keep text baseline and icon axes aligned to avoid visual tilt.',
    '',
    'Color and Stroke:',
    `- Match palette priority from current asset: ${palette}.`,
    '- Use opaque fills and crisp strokes only.',
    '- Ensure border-to-fill contrast is high for mobile readability.',
    '',
    'Quality Constraints:',
    ...baseConstraints(cat).map((x) => `- ${x}`),
    '',
    'Do not:',
    '- Do not crop text or symbols.',
    '- Do not use approximate random proportions.',
    '- Do not change sign meaning or code semantics.',
    '- Do not add decorative effects.',
    '',
    'Acceptance checklist:',
    '- Passes small-size preview at 128px without unreadable text.',
    '- No element touches canvas edge.',
    '- Shape and symbol are immediately recognizable as the intended traffic sign.',
  ].join('\n');

  return {
    version: 1,
    status: 'draft',
    updatedAt: DATE,
    generator: 'scripts/build_asset_prompts.js',
    visualCategory: cat,
    palette: colors,
    prompt,
  };
}

function main() {
  const manifest = readJson(MANIFEST);
  let updated = 0;

  for (const entry of manifest) {
    const svgPath = path.join(ROOT, entry.file || `assets/svg/${entry.assetId}.svg`);
    const colors = readSvgColors(svgPath);
    entry.promptSpec = buildPrompt(entry, colors);
    entry.promptLastReviewedAt = DATE;
    updated += 1;
  }

  writeJson(MANIFEST, manifest);

  const summary = {
    date: DATE,
    totalAssets: manifest.length,
    withPromptSpec: manifest.filter((x) => x.promptSpec && x.promptSpec.prompt).length,
    categories: manifest.reduce((acc, x) => {
      const c = x.promptSpec?.visualCategory || 'unknown';
      acc[c] = (acc[c] || 0) + 1;
      return acc;
    }, {}),
  };

  const lines = [
    '# Prompt Coverage Report',
    '',
    `Date: ${DATE}`,
    `Total assets: ${summary.totalAssets}`,
    `Assets with promptSpec: ${summary.withPromptSpec}`,
    '',
    '## Category Distribution',
    ...Object.entries(summary.categories).sort((a, b) => a[0].localeCompare(b[0])).map(([k, v]) => `- ${k}: ${v}`),
    '',
    '## Editing Workflow',
    '- Edit `promptSpec.prompt` first (angles, line widths, colors, spacing, text constraints).',
    '- Regenerate or manually update SVG.',
    '- Render preview and compare with official reference.',
    '- Update `status`, `lastReviewedAt`, and `promptSpec.status`.',
    '',
  ];

  fs.writeFileSync(path.join(ROOT, 'assets', 'review', 'prompt_report.md'), lines.join('\n'), 'utf8');

  console.log(`Updated promptSpec for ${updated} assets`);
  console.log(`Report: assets/review/prompt_report.md`);
}

main();
