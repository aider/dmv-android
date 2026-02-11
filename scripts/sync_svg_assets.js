#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const SOURCE_SVG_DIR = path.join(ROOT, 'assets', 'svg');
const SOURCE_MANIFEST = path.join(ROOT, 'assets', 'manifest.json');

const TARGETS = [
  path.join(ROOT, 'app', 'src', 'main', 'assets', 'svg'),
  path.join(ROOT, 'dmv-android', 'app', 'src', 'main', 'assets', 'svg'),
];

const TARGET_MANIFESTS = [
  path.join(ROOT, 'app', 'src', 'main', 'assets', 'assets_manifest.json'),
  path.join(ROOT, 'dmv-android', 'app', 'src', 'main', 'assets', 'assets_manifest.json'),
];

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function listSvgFiles(dir) {
  if (!fs.existsSync(dir)) return [];
  return fs.readdirSync(dir).filter((f) => f.toLowerCase().endsWith('.svg')).sort();
}

function copySvgMirror(sourceDir, targetDir) {
  ensureDir(targetDir);

  const sourceFiles = listSvgFiles(sourceDir);
  const targetFiles = listSvgFiles(targetDir);

  for (const file of sourceFiles) {
    fs.copyFileSync(path.join(sourceDir, file), path.join(targetDir, file));
  }

  const sourceSet = new Set(sourceFiles);
  for (const file of targetFiles) {
    if (!sourceSet.has(file)) {
      fs.unlinkSync(path.join(targetDir, file));
    }
  }

  return { copied: sourceFiles.length, removed: targetFiles.filter((f) => !sourceSet.has(f)).length };
}

function buildAndroidManifestEntries(sourceManifestPath) {
  const raw = fs.readFileSync(sourceManifestPath, 'utf8');
  const entries = JSON.parse(raw);
  return entries.map((x) => ({
    assetId: x.assetId,
    description: x.description,
    file: x.file,
    sourceUrl: x.sourceUrl,
    license: x.license,
  }));
}

function writeJson(filePath, data) {
  ensureDir(path.dirname(filePath));
  fs.writeFileSync(filePath, `${JSON.stringify(data, null, 2)}\n`);
}

function main() {
  if (!fs.existsSync(SOURCE_SVG_DIR) || !fs.existsSync(SOURCE_MANIFEST)) {
    console.error('Source assets are missing.');
    process.exit(1);
  }

  const sourceFiles = listSvgFiles(SOURCE_SVG_DIR);
  if (sourceFiles.length === 0) {
    console.error('No SVG files found in source directory.');
    process.exit(1);
  }

  for (const target of TARGETS) {
    const { copied, removed } = copySvgMirror(SOURCE_SVG_DIR, target);
    console.log(`Synced ${target}`);
    console.log(`  copied/updated: ${copied}`);
    console.log(`  removed stale: ${removed}`);
  }

  const androidManifest = buildAndroidManifestEntries(SOURCE_MANIFEST);
  for (const targetManifest of TARGET_MANIFESTS) {
    writeJson(targetManifest, androidManifest);
    console.log(`Wrote ${targetManifest} (${androidManifest.length} entries)`);
  }

  console.log('Sync completed.');
}

main();
