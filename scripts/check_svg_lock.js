#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const ROOT = path.resolve(__dirname, '..');
const LOCKFILE_PATH = path.join(ROOT, 'assets', 'review', 'svg_approved.lock.json');

function sha256File(filePath) {
  const data = fs.readFileSync(filePath);
  return crypto.createHash('sha256').update(data).digest('hex');
}

function main() {
  if (!fs.existsSync(LOCKFILE_PATH)) {
    console.error(`Lock file not found: ${path.relative(ROOT, LOCKFILE_PATH)}`);
    process.exit(1);
  }

  const lock = JSON.parse(fs.readFileSync(LOCKFILE_PATH, 'utf8'));
  if (!Array.isArray(lock.entries)) {
    console.error('Lock file is invalid: entries must be an array.');
    process.exit(1);
  }

  const missing = [];
  const changed = [];
  for (const entry of lock.entries) {
    const filePath = path.join(ROOT, entry.file);
    if (!fs.existsSync(filePath)) {
      missing.push(entry);
      continue;
    }
    const currentHash = sha256File(filePath);
    if (currentHash !== entry.sha256) {
      changed.push({ ...entry, currentSha256: currentHash });
    }
  }

  console.log(`Checked locked assets: ${lock.entries.length}`);
  console.log(`Missing: ${missing.length}`);
  console.log(`Changed: ${changed.length}`);

  if (missing.length > 0) {
    for (const x of missing.slice(0, 20)) {
      console.log(`  MISSING ${x.assetId} -> ${x.file}`);
    }
  }
  if (changed.length > 0) {
    for (const x of changed.slice(0, 20)) {
      console.log(`  CHANGED ${x.assetId} -> ${x.file}`);
    }
  }

  if (missing.length > 0 || changed.length > 0) {
    process.exit(1);
  }

  console.log('Lock check passed.');
}

main();
