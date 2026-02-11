#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const SVG_DIR = path.join(ROOT, 'assets', 'svg');
const MANIFEST_PATH = path.join(ROOT, 'assets', 'manifest.json');
const REVIEW_DIR = path.join(ROOT, 'assets', 'review');
const LOCKFILE_PATH = path.join(ROOT, 'assets', 'review', 'svg_approved.lock.json');
const FORCE_LOCKED = process.argv.includes('--force-locked');

const TODAY = '2026-02-10';
const CANVAS = 200;
const PAD = 12;

const POLISHED_MUTCD = new Set([
  'MUTCD_R1-1_STOP',
  'MUTCD_R1-2_YIELD',
  'MUTCD_R2-1_SPEED_LIMIT_30',
  'MUTCD_R2-1_SPEED_LIMIT_65',
  'MUTCD_R2-1_SPEED_LIMIT_70',
  'MUTCD_R5-1_DO_NOT_ENTER',
  'MUTCD_R5-1a_WRONG_WAY',
  'MUTCD_R10-6_ONE_WAY',
  'MUTCD_R4-7_KEEP_RIGHT',
  'MUTCD_W1-1_CURVE_RIGHT',
  'MUTCD_W1-2_CURVE_LEFT',
]);

const MANUAL_OK = new Set([
  'MUTCD_D1-1_EXIT_SIGN',
  'MUTCD_D3-1_DISTANCE_SIGN',
  'MUTCD_D9-1_REST_AREA',
  'MUTCD_I-5_INTERSTATE_10',
  'MUTCD_I-5_INTERSTATE_35',
  'MUTCD_M1-1_US_ROUTE_90',
  'MUTCD_M1-4_STATE_ROUTE_71',
  'MUTCD_OM1-1_KEEP_RIGHT',
  'MUTCD_OM2-1_KEEP_LEFT',
  'MUTCD_R10-7_DO_NOT_PASS',
  'MUTCD_R15-1_RAILROAD_CROSSING',
  'MUTCD_S1-1_SCHOOL_CROSSING',
]);

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function listSvgFiles(dir) {
  return fs.readdirSync(dir).filter((f) => f.toLowerCase().endsWith('.svg')).sort();
}

function parseViewBox(svgTag) {
  const m = svgTag.match(/viewBox\s*=\s*"([^"]+)"/i);
  if (!m) return { x: 0, y: 0, w: CANVAS, h: CANVAS, raw: '0 0 200 200' };
  const parts = m[1].trim().split(/[\s,]+/).map(Number);
  if (parts.length < 4 || parts.some((x) => Number.isNaN(x))) {
    return { x: 0, y: 0, w: CANVAS, h: CANVAS, raw: m[1] };
  }
  return { x: parts[0], y: parts[1], w: parts[2], h: parts[3], raw: m[1] };
}

function fmt(n) {
  if (Math.abs(n - Math.round(n)) < 1e-6) return String(Math.round(n));
  return n.toFixed(4).replace(/0+$/, '').replace(/\.$/, '');
}

function normalizeSvg(filePath, assetId) {
  const raw = fs.readFileSync(filePath, 'utf8').replace(/\r\n/g, '\n').trim();
  const openTagMatch = raw.match(/<svg\b[^>]*>/i);
  const closeIdx = raw.toLowerCase().lastIndexOf('</svg>');
  if (!openTagMatch || closeIdx === -1) {
    return {
      changed: false,
      error: 'invalid_svg',
      issues: ['Invalid SVG markup'],
      originalViewBox: 'unknown',
      normalizedFrom: null,
      hasText: false,
      needsFix: true,
    };
  }

  const openTag = openTagMatch[0];
  const contentStart = raw.indexOf(openTag) + openTag.length;
  const inner = raw.slice(contentStart, closeIdx).trim();

  const vb = parseViewBox(openTag);
  const sameCanvas = Math.abs(vb.x) < 1e-6 && Math.abs(vb.y) < 1e-6 && Math.abs(vb.w - CANVAS) < 1e-6 && Math.abs(vb.h - CANVAS) < 1e-6;

  let transformedInner = inner;
  let normalizedFrom = null;

  if (!sameCanvas) {
    const safeW = vb.w > 0 ? vb.w : CANVAS;
    const safeH = vb.h > 0 ? vb.h : CANVAS;
    const targetW = CANVAS - PAD * 2;
    const targetH = CANVAS - PAD * 2;
    const scale = Math.min(targetW / safeW, targetH / safeH);
    const tx = (CANVAS - safeW * scale) / 2;
    const ty = (CANVAS - safeH * scale) / 2;

    const body = inner
      .split('\n')
      .map((line) => (line.length ? `    ${line}` : line))
      .join('\n');

    transformedInner = `  <g transform="translate(${fmt(tx)} ${fmt(ty)}) scale(${fmt(scale)})">\n${body}\n  </g>`;
    normalizedFrom = vb.raw;
  }

  const normalized = [
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${CANVAS} ${CANVAS}" width="${CANVAS}" height="${CANVAS}" role="img" aria-label="${assetId}">`,
    transformedInner || '',
    '</svg>',
    '',
  ].join('\n');

  const changed = normalized !== `${raw}\n`;
  if (changed) fs.writeFileSync(filePath, normalized, 'utf8');

  const hasText = /<text\b/i.test(transformedInner);
  const issues = [];

  if (hasText) issues.push('Contains text nodes (font-dependent rendering risk)');
  if (normalizedFrom) issues.push(`Normalized from viewBox ${normalizedFrom} to 0 0 ${CANVAS} ${CANVAS}`);
  if (/font-family\s*=\s*"[^"]*times|comic|cursive/i.test(transformedInner)) {
    issues.push('Non-standard font family detected');
  }

  const needsFix = assetId.startsWith('MUTCD_') && !POLISHED_MUTCD.has(assetId)
    ? true
    : (assetId.startsWith('MUTCD_') && hasText)
      ? true
      : false;

  return {
    changed,
    issues,
    originalViewBox: vb.raw,
    normalizedFrom,
    hasText,
    needsFix,
    error: null,
  };
}

function inferTags(assetId) {
  const tags = ['svg', 'dmv', 'quiz'];
  if (assetId.startsWith('MUTCD_')) tags.push('mutcd', 'sign');
  if (assetId.startsWith('INTERSECTION_')) tags.push('intersection', 'diagram');
  if (assetId.startsWith('PAVEMENT_')) tags.push('pavement-marking');
  if (assetId.startsWith('SIGNAL_')) tags.push('signal');
  if (assetId.startsWith('PARKING_')) tags.push('parking');
  if (assetId.startsWith('SAFE_')) tags.push('safety');
  if (assetId.startsWith('SPEED_')) tags.push('speed');
  if (assetId.startsWith('SPECIAL_')) tags.push('special-case');
  if (assetId.startsWith('MARKING_')) tags.push('hand-signal');
  return [...new Set(tags)];
}

function readReferencedAssets() {
  const reviewMd = path.join(ROOT, 'data', 'tx', 'review', 'questions_with_images.md');
  if (!fs.existsSync(reviewMd)) return new Set();
  const txt = fs.readFileSync(reviewMd, 'utf8');
  const ids = new Set();
  for (const m of txt.matchAll(/\b([A-Z][A-Z0-9_\-]+)\b/g)) {
    if (m[1].includes('_')) ids.add(m[1]);
  }
  return ids;
}

function readLockedAssets() {
  if (!fs.existsSync(LOCKFILE_PATH)) return new Set();
  try {
    const lock = JSON.parse(fs.readFileSync(LOCKFILE_PATH, 'utf8'));
    if (!Array.isArray(lock.entries)) return new Set();
    return new Set(lock.entries.map((x) => x.assetId).filter(Boolean));
  } catch {
    return new Set();
  }
}

function writeGallery(entries) {
  ensureDir(REVIEW_DIR);
  const cards = entries.map((e) => {
    return [
      '<div class="card">',
      `  <img src="../svg/${e.fileName}" alt="${e.assetId}" loading="lazy"/>`,
      `  <div class="id">${e.assetId}</div>`,
      `  <div class="status ${e.status}">${e.status.toUpperCase()}</div>`,
      '</div>',
    ].join('\n');
  }).join('\n');

  const html = `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>DMV SVG Asset Gallery</title>
  <style>
    :root { --ok:#147d4f; --fix:#b54708; --bg:#eef2f7; --card:#ffffff; --text:#1f2937; }
    * { box-sizing: border-box; }
    body { margin:0; font-family: ui-sans-serif, -apple-system, Segoe UI, Roboto, sans-serif; background:var(--bg); color:var(--text); }
    header { padding:16px 20px; border-bottom:1px solid #d8dee7; background:#f8fafc; position:sticky; top:0; }
    h1 { margin:0; font-size:18px; }
    .meta { margin-top:6px; font-size:13px; color:#4b5563; }
    .grid { padding:16px; display:grid; grid-template-columns:repeat(auto-fill,minmax(180px,1fr)); gap:12px; }
    .card { background:var(--card); border:1px solid #d8dee7; border-radius:12px; padding:10px; }
    .card img { width:100%; height:140px; object-fit:contain; display:block; background:#f8fafc; border-radius:8px; }
    .id { margin-top:8px; font-size:12px; font-weight:700; word-break:break-word; }
    .status { margin-top:6px; font-size:12px; font-weight:700; }
    .status.ok { color:var(--ok); }
    .status.needs_fix { color:var(--fix); }
  </style>
</head>
<body>
<header>
  <h1>DMV SVG Asset Gallery</h1>
  <div class="meta">${entries.length} assets · Reviewed ${TODAY}</div>
</header>
<main class="grid">
${cards}
</main>
</body>
</html>
`;
  fs.writeFileSync(path.join(REVIEW_DIR, 'gallery.html'), html, 'utf8');
}

function writeReport(entries, fixedAssets, redesignAssets, issuesByAsset) {
  const ok = entries.filter((x) => x.status === 'ok').length;
  const needs = entries.length - ok;
  const fixedList = fixedAssets.map((x) => `- ${x}`).join('\n') || '- None';
  const redesignList = redesignAssets.map((x) => `- ${x}`).join('\n') || '- None';

  const remaining = entries
    .filter((x) => x.status === 'needs_fix')
    .map((x) => {
      const issues = (issuesByAsset.get(x.assetId) || []).slice(0, 3).join('; ') || 'Manual conformance review required';
      return `- ${x.assetId}: ${issues}`;
    })
    .join('\n');

  const report = `# SVG Review Report

Date: ${TODAY}

## Summary
- Total assets: ${entries.length}
- OK: ${ok}
- Needs fix: ${needs}
- Standard viewBox: \`0 0 200 200\`
- Standard canvas size: \`200x200\`
- Standard padding for normalized assets: \`${PAD}\`

## Fixed Assets
${fixedList}

## Remaining Issues
${remaining || '- None'}

## May Require Redesign
${redesignList}
`;

  fs.writeFileSync(path.join(REVIEW_DIR, 'report.md'), `${report}\n`, 'utf8');
}

function writeTodo(entries, issuesByAsset) {
  const needs = entries.filter((e) => e.status === 'needs_fix');
  const lines = [
    '# SVG Issues TODO',
    '',
    `Date: ${TODAY}`,
    `Total needs_fix: ${needs.length}`,
    '',
    '## Priority P1 (MUTCD conformance)',
  ];

  for (const e of needs.filter((x) => x.assetId.startsWith('MUTCD_'))) {
    const issue = (issuesByAsset.get(e.assetId) || [])[0] || 'Manual MUTCD geometry/typography pass required';
    lines.push(`- [ ] ${e.assetId}: ${issue}`);
  }

  lines.push('', '## Priority P2 (Visual polish / readability)');
  for (const e of needs.filter((x) => !x.assetId.startsWith('MUTCD_'))) {
    const issue = (issuesByAsset.get(e.assetId) || [])[0] || 'Visual polish required';
    lines.push(`- [ ] ${e.assetId}: ${issue}`);
  }

  fs.writeFileSync(path.join(REVIEW_DIR, 'issues_todo.md'), `${lines.join('\n')}\n`, 'utf8');
}

function main() {
  if (!fs.existsSync(MANIFEST_PATH)) {
    console.error(`Manifest not found: ${MANIFEST_PATH}`);
    process.exit(1);
  }

  const manifest = JSON.parse(fs.readFileSync(MANIFEST_PATH, 'utf8'));
  if (!Array.isArray(manifest)) {
    console.error('Manifest must be an array.');
    process.exit(1);
  }

  const svgFiles = listSvgFiles(SVG_DIR);
  const byId = new Map(manifest.map((x) => [x.assetId, x]));

  for (const fileName of svgFiles) {
    const assetId = path.basename(fileName, '.svg');
    if (!byId.has(assetId)) {
      manifest.push({
        assetId,
        description: assetId.replace(/_/g, ' ').toLowerCase(),
        file: `assets/svg/${fileName}`,
        sourceUrl: 'generated',
        license: 'generated',
        tags: inferTags(assetId),
        status: 'needs_fix',
        lastReviewedAt: TODAY,
      });
      byId.set(assetId, manifest[manifest.length - 1]);
    }
  }

  const referenced = readReferencedAssets();
  const lockedAssets = readLockedAssets();
  const fixedAssets = [];
  const redesignAssets = [];
  const issuesByAsset = new Map();
  let lockedSkipped = 0;

  for (const entry of manifest) {
    const fileName = path.basename(entry.file || `${entry.assetId}.svg`);
    const svgPath = path.join(SVG_DIR, fileName);
    if (!fs.existsSync(svgPath)) {
      entry.status = 'needs_fix';
      entry.lastReviewedAt = TODAY;
      entry.tags = inferTags(entry.assetId);
      issuesByAsset.set(entry.assetId, ['SVG file missing']);
      redesignAssets.push(entry.assetId);
      continue;
    }

    if (lockedAssets.has(entry.assetId) && !FORCE_LOCKED) {
      entry.file = `assets/svg/${fileName}`;
      entry.description = entry.description || entry.assetId.replace(/_/g, ' ').toLowerCase();
      entry.tags = inferTags(entry.assetId);
      issuesByAsset.set(entry.assetId, ['Locked asset: skipped automatic normalization']);
      lockedSkipped += 1;
      continue;
    }

    const result = normalizeSvg(svgPath, entry.assetId);
    const issues = [...(result.issues || [])];

    if (entry.assetId.startsWith('MUTCD_') && !POLISHED_MUTCD.has(entry.assetId)) {
      issues.push('Needs manual MUTCD-accurate redraw (shape proportions, symbol geometry, legend spacing)');
      redesignAssets.push(entry.assetId);
    }

    if (referenced.has(entry.assetId) && entry.assetId.startsWith('INTERSECTION_') && result.hasText) {
      issues.push('Referenced asset: verify small-size readability in question cards');
    }

    if (result.changed) fixedAssets.push(entry.assetId);

    issuesByAsset.set(entry.assetId, issues);

    const needsFixByRules = result.error || issues.some((x) =>
      x.includes('manual MUTCD') ||
      x.includes('font-dependent') ||
      x.includes('missing')
    );
    const needsFix = MANUAL_OK.has(entry.assetId) ? false : needsFixByRules;

    entry.file = `assets/svg/${fileName}`;
    entry.description = entry.description || entry.assetId.replace(/_/g, ' ').toLowerCase();
    entry.tags = inferTags(entry.assetId);
    entry.status = needsFix ? 'needs_fix' : 'ok';
    entry.lastReviewedAt = TODAY;
  }

  manifest.sort((a, b) => a.assetId.localeCompare(b.assetId));
  fs.writeFileSync(MANIFEST_PATH, `${JSON.stringify(manifest, null, 2)}\n`, 'utf8');

  const galleryEntries = manifest.map((x) => ({
    assetId: x.assetId,
    fileName: path.basename(x.file),
    status: x.status,
  }));

  writeGallery(galleryEntries);
  writeReport(manifest, fixedAssets.sort(), [...new Set(redesignAssets)].sort(), issuesByAsset);
  writeTodo(manifest, issuesByAsset);

  const ok = manifest.filter((x) => x.status === 'ok').length;
  const needs = manifest.length - ok;

  console.log(`Reviewed: ${manifest.length}`);
  console.log(`OK: ${ok}`);
  console.log(`Needs fix: ${needs}`);
  console.log(`Normalized SVG files changed: ${fixedAssets.length}`);
  console.log(`Locked assets skipped: ${lockedSkipped}`);
  if (lockedAssets.size > 0 && !FORCE_LOCKED) {
    console.log(`Lock file active: ${path.relative(ROOT, LOCKFILE_PATH)}`);
  }
}

main();
