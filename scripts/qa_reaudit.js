#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const SVG_DIR = path.join(ROOT, 'assets', 'svg');
const MANIFEST_PATH = path.join(ROOT, 'assets', 'manifest.json');
const OUT = path.join(ROOT, 'assets', 'review', 'qa_reaudit_2026-02-10.md');

function parseViewBox(svg) {
  const m = svg.match(/viewBox\s*=\s*"([^"]+)"/i);
  if (!m) return null;
  const p = m[1].trim().split(/[\s,]+/).map(Number);
  if (p.length !== 4 || p.some(Number.isNaN)) return null;
  return { x: p[0], y: p[1], w: p[2], h: p[3], raw: m[1] };
}

function textWidthApprox(text, fontSize) {
  let width = 0;
  for (const ch of text) {
    if (ch === ' ') width += 0.34 * fontSize;
    else if (/[A-Z0-9]/.test(ch)) width += 0.62 * fontSize;
    else width += 0.5 * fontSize;
  }
  return width;
}

function analyzeTextRisks(svg, canvasW, canvasH) {
  const risks = [];
  const textTagRe = /<text\b([^>]*)>([\s\S]*?)<\/text>/gi;
  let m;
  while ((m = textTagRe.exec(svg))) {
    const attrs = m[1] || '';
    const content = (m[2] || '').replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim();
    if (!content) continue;

    const getNum = (name) => {
      const a = attrs.match(new RegExp(`${name}\\s*=\\s*"([^"]+)"`, 'i'));
      if (!a) return null;
      const v = Number(a[1]);
      return Number.isFinite(v) ? v : null;
    };

    const x = getNum('x');
    const y = getNum('y');
    const fsz = getNum('font-size') || 16;
    const anchorM = attrs.match(/text-anchor\s*=\s*"([^"]+)"/i);
    const anchor = anchorM ? anchorM[1].toLowerCase() : 'start';

    if (x == null || y == null) {
      risks.push(`text "${content}" missing numeric x/y`);
      continue;
    }

    const w = textWidthApprox(content, fsz);
    let left = x;
    let right = x + w;
    if (anchor === 'middle') {
      left = x - w / 2;
      right = x + w / 2;
    } else if (anchor === 'end') {
      left = x - w;
      right = x;
    }

    const top = y - 0.82 * fsz;
    const bottom = y + 0.24 * fsz;

    const margin = 2;
    if (left < margin || right > canvasW - margin || top < margin || bottom > canvasH - margin) {
      risks.push(`text "${content}" may clip (bbox≈${left.toFixed(1)},${top.toFixed(1)}-${right.toFixed(1)},${bottom.toFixed(1)})`);
    }

    if (fsz >= 24 && content.length >= 8) {
      risks.push(`large text "${content}" may overflow on some renderers (font-size ${fsz})`);
    }
  }
  return risks;
}

function analyzeSvg(filePath) {
  const svg = fs.readFileSync(filePath, 'utf8');
  const risks = [];
  const vb = parseViewBox(svg);

  if (!vb) {
    risks.push('missing/invalid viewBox');
    return { risks, score: 100, viewBox: null };
  }
  if (!(vb.x === 0 && vb.y === 0 && vb.w === 200 && vb.h === 200)) {
    risks.push(`non-standard viewBox ${vb.raw}`);
  }

  if (/transform\s*=\s*"[^"]*scale\(/i.test(svg)) {
    risks.push('contains scaled transform (can cause renderer variance)');
  }
  if (/font-family\s*=\s*"[^"]*serif/i.test(svg) && !/Arial|Helvetica|sans-serif/i.test(svg)) {
    risks.push('font-family may be unstable across platforms');
  }

  risks.push(...analyzeTextRisks(svg, vb.w, vb.h));

  let score = 0;
  for (const r of risks) {
    if (r.includes('may clip')) score += 4;
    else if (r.includes('scaled transform')) score += 3;
    else if (r.includes('large text')) score += 2;
    else score += 1;
  }

  return { risks, score, viewBox: vb.raw };
}

function main() {
  const manifest = JSON.parse(fs.readFileSync(MANIFEST_PATH, 'utf8'));
  const byId = new Map(manifest.map((x) => [x.assetId, x]));
  const files = fs.readdirSync(SVG_DIR).filter((f) => f.toLowerCase().endsWith('.svg')).sort();

  const rows = [];
  for (const f of files) {
    const assetId = path.basename(f, '.svg');
    const full = path.join(SVG_DIR, f);
    const a = analyzeSvg(full);
    rows.push({ assetId, file: `assets/svg/${f}`, ...a, status: byId.get(assetId)?.status || 'unknown' });
  }

  const risky = rows.filter((r) => r.risks.length > 0).sort((a, b) => b.score - a.score || a.assetId.localeCompare(b.assetId));
  const top = risky.slice(0, 40);

  const lines = [];
  lines.push('# QA Re-Audit (Visual Risk Precheck)');
  lines.push('');
  lines.push('Date: 2026-02-10');
  lines.push(`Total assets scanned: ${rows.length}`);
  lines.push(`Assets with heuristic risks: ${risky.length}`);
  lines.push('');
  lines.push('## Top Risk Assets (Heuristic)');
  lines.push('');
  lines.push('| Asset | Score | Risks |');
  lines.push('|---|---:|---|');
  for (const r of top) {
    lines.push(`| ${r.assetId} | ${r.score} | ${r.risks.slice(0, 3).join('; ')} |`);
  }
  lines.push('');
  lines.push('## Method');
  lines.push('- Heuristic text bounding-box checks for potential clipping.');
  lines.push('- Detect scaled transforms and non-standard viewBox usage.');
  lines.push('- Output is precheck only; final decision must be made by PNG visual review.');
  lines.push('');

  fs.writeFileSync(OUT, `${lines.join('\n')}\n`, 'utf8');
  console.log(`Wrote ${path.relative(ROOT, OUT)}`);
  console.log(`Risky assets: ${risky.length}`);
  console.log(`Top sample: ${top.slice(0, 10).map((x) => x.assetId).join(', ')}`);
}

main();
