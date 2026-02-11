#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const MANIFEST_PATH = path.join(ROOT, 'assets', 'manifest.json');
const REVIEW_PATH = path.join(ROOT, 'assets', 'review', 'reference_prompt_review.json');
const REPORT_PATH = path.join(ROOT, 'assets', 'review', 'reference_prompt_apply_report.md');
const DATE = '2026-02-10';

function readJson(filePath) {
  return JSON.parse(fs.readFileSync(filePath, 'utf8'));
}

function writeJson(filePath, data) {
  fs.writeFileSync(filePath, `${JSON.stringify(data, null, 2)}\n`, 'utf8');
}

function ensurePromptSpec(entry) {
  if (!entry.promptSpec || typeof entry.promptSpec !== 'object') {
    entry.promptSpec = {
      version: 1,
      status: 'draft',
      updatedAt: DATE,
      generator: 'scripts/apply_reference_prompt_additions.js',
      visualCategory: 'generic',
      palette: [],
      prompt: '',
    };
  }
  if (typeof entry.promptSpec.prompt !== 'string') {
    entry.promptSpec.prompt = '';
  }
}

function appendReferenceNotes(prompt, row) {
  const base = prompt.trimEnd();
  const additions = Array.isArray(row.promptAdditions) ? row.promptAdditions : [];
  const missing = additions.filter((line) => line && !base.includes(line));
  if (!missing.length) {
    return { prompt: base, addedLines: 0 };
  }

  const blocks = [];
  blocks.push(`Reference Alignment Notes (${DATE}):`);
  if (row.bestReferenceTitle && row.bestReferenceUrl) {
    blocks.push(`- Source: ${row.bestReferenceTitle} (${row.bestReferenceUrl})`);
  } else if (row.bestReferenceUrl) {
    blocks.push(`- Source: ${row.bestReferenceUrl}`);
  }
  for (const line of missing) {
    blocks.push(`- ${line}`);
  }

  const next = `${base}\n\n${blocks.join('\n')}`;
  return { prompt: next, addedLines: missing.length };
}

function main() {
  const manifest = readJson(MANIFEST_PATH);
  const review = readJson(REVIEW_PATH);

  if (!Array.isArray(manifest)) {
    throw new Error('assets/manifest.json must be an array');
  }
  if (!review || !Array.isArray(review.rows)) {
    throw new Error('assets/review/reference_prompt_review.json is invalid');
  }

  const byAssetId = new Map(review.rows.map((row) => [row.assetId, row]));

  let touched = 0;
  let totalAddedLines = 0;
  const missingInReview = [];
  const updatedAssets = [];

  for (const entry of manifest) {
    if (!entry || !entry.assetId) continue;
    const row = byAssetId.get(entry.assetId);
    if (!row) {
      missingInReview.push(entry.assetId);
      continue;
    }
    if (row.decision !== 'keep') continue;

    ensurePromptSpec(entry);

    const before = entry.promptSpec.prompt;
    const appended = appendReferenceNotes(before, row);

    entry.promptSpec.prompt = appended.prompt;
    entry.promptSpec.status = 'refined';
    entry.promptSpec.updatedAt = DATE;
    entry.promptSpec.referenceReview = {
      checkedAt: DATE,
      query: row.query || '',
      decision: row.decision || 'skip',
      reason: row.reason || '',
      sourceTitle: row.bestReferenceTitle || '',
      sourceUrl: row.bestReferenceUrl || '',
      sourceScore: row.bestReferenceScore ?? null,
      additions: Array.isArray(row.promptAdditions) ? row.promptAdditions : [],
    };
    entry.promptLastReviewedAt = DATE;

    if (appended.addedLines > 0 || entry.promptSpec.prompt !== before) {
      touched += 1;
      totalAddedLines += appended.addedLines;
      updatedAssets.push(entry.assetId);
    }
  }

  writeJson(MANIFEST_PATH, manifest);

  const lines = [];
  lines.push('# Reference Prompt Apply Report');
  lines.push('');
  lines.push(`Date: ${DATE}`);
  lines.push(`Manifest assets: ${manifest.length}`);
  lines.push(`Updated assets: ${touched}`);
  lines.push(`Total prompt additions inserted: ${totalAddedLines}`);
  lines.push(`Rows in reference review: ${review.rows.length}`);
  lines.push('');
  if (missingInReview.length) {
    lines.push('## Missing In Review');
    for (const id of missingInReview) lines.push(`- ${id}`);
    lines.push('');
  }
  lines.push('## Updated Asset IDs');
  if (!updatedAssets.length) {
    lines.push('- None');
  } else {
    for (const id of updatedAssets) lines.push(`- ${id}`);
  }
  lines.push('');

  fs.writeFileSync(REPORT_PATH, `${lines.join('\n')}\n`, 'utf8');

  console.log(`Updated assets: ${touched}`);
  console.log(`Total prompt additions inserted: ${totalAddedLines}`);
  console.log(`Report: ${path.relative(ROOT, REPORT_PATH)}`);
}

main();

