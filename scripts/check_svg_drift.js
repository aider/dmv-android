#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const ROOT = path.resolve(__dirname, '..');
const SOURCE_DIR = path.join(ROOT, 'assets', 'svg');
const TARGET_DIRS = [
  path.join(ROOT, 'app', 'src', 'main', 'assets', 'svg'),
  path.join(ROOT, 'dmv-android', 'app', 'src', 'main', 'assets', 'svg'),
];

function listSvg(dir) {
  if (!fs.existsSync(dir)) return [];
  return fs.readdirSync(dir).filter((f) => f.toLowerCase().endsWith('.svg')).sort();
}

function hashFile(filePath) {
  const data = fs.readFileSync(filePath);
  return crypto.createHash('sha256').update(data).digest('hex');
}

function compareDirs(sourceDir, targetDir) {
  const sourceFiles = listSvg(sourceDir);
  const targetFiles = listSvg(targetDir);

  const sourceSet = new Set(sourceFiles);
  const targetSet = new Set(targetFiles);

  const missing = sourceFiles.filter((f) => !targetSet.has(f));
  const extra = targetFiles.filter((f) => !sourceSet.has(f));
  const changed = [];

  for (const file of sourceFiles) {
    if (!targetSet.has(file)) continue;
    const srcHash = hashFile(path.join(sourceDir, file));
    const tgtHash = hashFile(path.join(targetDir, file));
    if (srcHash !== tgtHash) changed.push(file);
  }

  return { missing, extra, changed, sourceCount: sourceFiles.length, targetCount: targetFiles.length };
}

function main() {
  if (!fs.existsSync(SOURCE_DIR)) {
    console.error(`Source directory does not exist: ${SOURCE_DIR}`);
    process.exit(1);
  }

  let hasDrift = false;
  for (const target of TARGET_DIRS) {
    const result = compareDirs(SOURCE_DIR, target);
    console.log(`\nChecking: ${target}`);
    console.log(`  source count: ${result.sourceCount}`);
    console.log(`  target count: ${result.targetCount}`);
    console.log(`  missing: ${result.missing.length}`);
    console.log(`  extra: ${result.extra.length}`);
    console.log(`  changed: ${result.changed.length}`);

    if (result.missing.length || result.extra.length || result.changed.length) {
      hasDrift = true;
      for (const file of result.missing.slice(0, 10)) console.log(`    MISSING ${file}`);
      for (const file of result.extra.slice(0, 10)) console.log(`    EXTRA   ${file}`);
      for (const file of result.changed.slice(0, 10)) console.log(`    CHANGED ${file}`);
    }
  }

  if (hasDrift) {
    console.error('\nSVG drift detected.');
    process.exit(1);
  }

  console.log('\nNo SVG drift detected.');
}

main();
