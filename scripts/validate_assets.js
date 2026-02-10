#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

function validateManifest(manifestPath) {
  console.log('=== Asset Manifest Validation ===\n');

  if (!fs.existsSync(manifestPath)) {
    console.error(`✗ Manifest file not found: ${manifestPath}`);
    return { valid: false, assets: [] };
  }

  let manifest;
  try {
    const content = fs.readFileSync(manifestPath, 'utf8');
    manifest = JSON.parse(content);
  } catch (err) {
    console.error(`✗ Failed to read/parse manifest: ${err.message}`);
    return { valid: false, assets: [] };
  }

  if (!Array.isArray(manifest)) {
    console.error('✗ Manifest must be an array of asset objects');
    return { valid: false, assets: [] };
  }

  const errors = [];
  const warnings = [];
  const assetIds = new Set();
  const files = new Set();

  manifest.forEach((asset, index) => {
    // Check required fields
    const requiredFields = ['assetId', 'file', 'sourceUrl', 'license'];
    requiredFields.forEach(field => {
      if (!(field in asset)) {
        errors.push(`Asset ${index}: Missing required field '${field}'`);
      }
    });

    // Check for duplicate assetIds
    if (asset.assetId) {
      if (assetIds.has(asset.assetId)) {
        errors.push(`Duplicate assetId: ${asset.assetId}`);
      }
      assetIds.add(asset.assetId);
    }

    // Check for duplicate files
    if (asset.file) {
      if (files.has(asset.file)) {
        warnings.push(`Duplicate file path: ${asset.file}`);
      }
      files.add(asset.file);
    }

    // Verify file exists
    if (asset.file) {
      const assetPath = path.join(path.dirname(manifestPath), '..', asset.file);
      if (!fs.existsSync(assetPath)) {
        errors.push(`Asset ${asset.assetId || index}: File not found: ${asset.file}`);
      }
    }

    // Check license field
    if (asset.license && asset.license === 'unknown' && !asset.notes) {
      warnings.push(`Asset ${asset.assetId || index}: License is 'unknown' but no notes provided`);
    }
  });

  console.log(`Total assets in manifest: ${manifest.length}`);
  console.log(`Unique asset IDs: ${assetIds.size}`);

  if (errors.length > 0) {
    console.log(`\n✗ ${errors.length} errors:`);
    errors.slice(0, 10).forEach(err => console.log(`  - ${err}`));
    if (errors.length > 10) {
      console.log(`  ... and ${errors.length - 10} more errors`);
    }
  } else {
    console.log('✓ No errors in manifest');
  }

  if (warnings.length > 0) {
    console.log(`\n⚠ ${warnings.length} warnings:`);
    warnings.slice(0, 5).forEach(warn => console.log(`  - ${warn}`));
    if (warnings.length > 5) {
      console.log(`  ... and ${warnings.length - 5} more warnings`);
    }
  }

  return { valid: errors.length === 0, assets: manifest, assetIds };
}

function checkAssetReferences(topicsDir, assetIds) {
  console.log('\n=== Asset Reference Validation ===\n');

  if (!fs.existsSync(topicsDir)) {
    console.log('Topics directory not found, skipping reference check');
    return;
  }

  const topicFiles = fs.readdirSync(topicsDir)
    .filter(f => f.endsWith('.json'))
    .map(f => path.join(topicsDir, f));

  const referencedAssets = new Set();
  const missingAssets = new Set();

  topicFiles.forEach(file => {
    try {
      const content = fs.readFileSync(file, 'utf8');
      const questions = JSON.parse(content);

      if (Array.isArray(questions)) {
        questions.forEach(q => {
          if (q.image && q.image.assetId) {
            referencedAssets.add(q.image.assetId);
            if (!assetIds.has(q.image.assetId)) {
              missingAssets.add(q.image.assetId);
            }
          }
        });
      }
    } catch (err) {
      console.log(`Warning: Could not read ${path.basename(file)}: ${err.message}`);
    }
  });

  console.log(`Referenced assets: ${referencedAssets.size}`);
  console.log(`Assets in manifest: ${assetIds.size}`);

  if (missingAssets.size > 0) {
    console.log(`\n✗ ${missingAssets.size} referenced assets not in manifest:`);
    Array.from(missingAssets).slice(0, 10).forEach(id => console.log(`  - ${id}`));
    if (missingAssets.size > 10) {
      console.log(`  ... and ${missingAssets.size - 10} more`);
    }
  } else {
    console.log('✓ All referenced assets exist in manifest');
  }

  const unusedAssets = new Set([...assetIds].filter(id => !referencedAssets.has(id)));
  if (unusedAssets.size > 0) {
    console.log(`\n⚠ ${unusedAssets.size} assets in manifest not referenced by any questions:`);
    Array.from(unusedAssets).slice(0, 10).forEach(id => console.log(`  - ${id}`));
    if (unusedAssets.size > 10) {
      console.log(`  ... and ${unusedAssets.size - 10} more`);
    }
  } else {
    console.log('✓ All manifest assets are referenced');
  }

  return { missingAssets, unusedAssets };
}

function main() {
  const manifestPath = path.join(__dirname, '../assets/manifest.json');
  const topicsDir = path.join(__dirname, '../data/tx/topics');

  const { valid, assets, assetIds } = validateManifest(manifestPath);

  if (assetIds && assetIds.size > 0) {
    const { missingAssets, unusedAssets } = checkAssetReferences(topicsDir, assetIds);

    console.log('\n=== Summary ===');
    const allValid = valid && missingAssets.size === 0;
    console.log(allValid ? '✓ All validations passed' : '✗ Validation failed');

    process.exit(allValid ? 0 : 1);
  } else {
    process.exit(valid ? 0 : 1);
  }
}

main();
