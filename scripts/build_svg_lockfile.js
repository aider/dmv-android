#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const ROOT = path.resolve(__dirname, '..');
const MANIFEST_PATH = path.join(ROOT, 'assets', 'manifest.json');
const LOCKFILE_PATH = path.join(ROOT, 'assets', 'review', 'svg_approved.lock.json');

function sha256File(filePath) {
  const data = fs.readFileSync(filePath);
  return crypto.createHash('sha256').update(data).digest('hex');
}

function main() {
  const lockAll = process.argv.includes('--all');

  if (!fs.existsSync(MANIFEST_PATH)) {
    console.error(`Manifest not found: ${MANIFEST_PATH}`);
    process.exit(1);
  }

  const manifest = JSON.parse(fs.readFileSync(MANIFEST_PATH, 'utf8'));
  if (!Array.isArray(manifest)) {
    console.error('Manifest must be an array.');
    process.exit(1);
  }

  const entries = [];
  for (const asset of manifest.sort((a, b) => a.assetId.localeCompare(b.assetId))) {
    if (!lockAll && asset.status !== 'ok') continue;
    const filePath = path.join(ROOT, asset.file);
    if (!fs.existsSync(filePath)) {
      console.error(`Missing SVG file for lock: ${asset.assetId} -> ${asset.file}`);
      process.exit(1);
    }
    entries.push({
      assetId: asset.assetId,
      file: asset.file,
      sha256: sha256File(filePath),
    });
  }

  const lockfile = {
    version: 1,
    generatedAt: new Date().toISOString(),
    sourceManifest: 'assets/manifest.json',
    mode: lockAll ? 'all' : 'ok-only',
    count: entries.length,
    entries,
  };

  fs.mkdirSync(path.dirname(LOCKFILE_PATH), { recursive: true });
  fs.writeFileSync(LOCKFILE_PATH, `${JSON.stringify(lockfile, null, 2)}\n`);
  console.log(`Wrote lock file: ${path.relative(ROOT, LOCKFILE_PATH)}`);
  console.log(`Locked assets: ${entries.length}`);
}

main();
