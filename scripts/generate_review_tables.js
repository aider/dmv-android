#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const EXPECTED_COUNTS = {
  SIGNS: 120,
  TRAFFIC_SIGNALS: 60,
  PAVEMENT_MARKINGS: 70,
  RIGHT_OF_WAY: 120,
  SPEED_AND_DISTANCE: 80,
  PARKING: 60,
  SAFE_DRIVING: 90,
  SPECIAL_SITUATIONS: 60
};

function loadTopicFiles(topicsDir) {
  const topicFiles = fs.readdirSync(topicsDir)
    .filter(f => f.endsWith('.json'))
    .map(f => path.join(topicsDir, f));

  const topics = {};
  const allQuestions = [];

  topicFiles.forEach(file => {
    try {
      const content = fs.readFileSync(file, 'utf8');
      const questions = JSON.parse(content);

      if (Array.isArray(questions) && questions.length > 0) {
        const topic = questions[0].topic;
        topics[topic] = questions;
        allQuestions.push(...questions);
      }
    } catch (err) {
      console.error(`Warning: Could not read ${path.basename(file)}: ${err.message}`);
    }
  });

  return { topics, allQuestions };
}

function loadManifest(manifestPath) {
  if (!fs.existsSync(manifestPath)) {
    return [];
  }

  try {
    const content = fs.readFileSync(manifestPath, 'utf8');
    return JSON.parse(content);
  } catch (err) {
    console.error(`Warning: Could not read manifest: ${err.message}`);
    return [];
  }
}

function generateIndexByTopic(topics, allQuestions, manifest, outputPath) {
  const lines = [];

  lines.push('# Texas DMV Question Bank - Index by Topic\n');
  lines.push(`Generated: ${new Date().toISOString().split('T')[0]}\n`);

  // Summary statistics
  lines.push('## Summary Statistics\n');
  lines.push('| Metric | Value |');
  lines.push('|--------|-------|');
  lines.push(`| Total Questions | ${allQuestions.length} |`);
  lines.push(`| Questions with Images | ${allQuestions.filter(q => q.image).length} |`);
  const avgDifficulty = (allQuestions.reduce((sum, q) => sum + (q.difficulty || 0), 0) / allQuestions.length).toFixed(2);
  lines.push(`| Average Difficulty | ${avgDifficulty} |`);
  lines.push(`| Total Assets | ${manifest.length} |`);
  lines.push('');

  // By-topic breakdown
  lines.push('## Questions by Topic\n');
  lines.push('| Topic | Total | Expected | Status | With Images | Avg Difficulty |');
  lines.push('|-------|-------|----------|--------|-------------|----------------|');

  Object.entries(EXPECTED_COUNTS).forEach(([topic, expected]) => {
    const questions = topics[topic] || [];
    const actual = questions.length;
    const status = actual === expected ? '✓' : `⚠ (${actual - expected > 0 ? '+' : ''}${actual - expected})`;
    const withImages = questions.filter(q => q.image).length;
    const avgDiff = questions.length > 0
      ? (questions.reduce((sum, q) => sum + (q.difficulty || 0), 0) / questions.length).toFixed(2)
      : 'N/A';

    lines.push(`| ${topic} | ${actual} | ${expected} | ${status} | ${withImages} | ${avgDiff} |`);
  });

  lines.push('');

  // Validation footer
  lines.push('## Validation\n');
  const expectedTotal = Object.values(EXPECTED_COUNTS).reduce((a, b) => a + b, 0);
  const uniqueIds = new Set(allQuestions.map(q => q.id)).size;

  lines.push('| Check | Status |');
  lines.push('|-------|--------|');
  lines.push(`| Unique IDs | ${uniqueIds === allQuestions.length ? '✓' : '✗'} (${uniqueIds}/${allQuestions.length}) |`);
  lines.push(`| Total Count | ${allQuestions.length === expectedTotal ? '✓' : '✗'} (${allQuestions.length}/${expectedTotal}) |`);

  // Asset usage
  const referencedAssets = new Set(allQuestions.filter(q => q.image).map(q => q.image.assetId));
  const manifestAssets = new Set(manifest.map(a => a.assetId));
  const missingAssets = [...referencedAssets].filter(id => !manifestAssets.has(id));
  const unusedAssets = [...manifestAssets].filter(id => !referencedAssets.has(id));

  lines.push(`| Missing Assets | ${missingAssets.length === 0 ? '✓' : `✗ (${missingAssets.length})`} |`);
  lines.push(`| Unused Assets | ${unusedAssets.length === 0 ? '✓' : `⚠ (${unusedAssets.length})`} |`);

  lines.push('');

  fs.writeFileSync(outputPath, lines.join('\n'));
  console.log(`✓ Generated ${outputPath}`);
}

function generateQuestionsWithImages(topics, allQuestions, manifest, outputPath) {
  const lines = [];

  lines.push('# Texas DMV Question Bank - Questions with Images\n');
  lines.push(`Generated: ${new Date().toISOString().split('T')[0]}\n`);

  const questionsWithImages = allQuestions.filter(q => q.image);

  lines.push('## Questions Using Images\n');
  lines.push(`Total: ${questionsWithImages.length}\n`);
  lines.push('| Question ID | Topic | Asset ID | Question Text (excerpt) |');
  lines.push('|-------------|-------|----------|-------------------------|');

  questionsWithImages.forEach(q => {
    const excerpt = q.text.length > 60 ? q.text.substring(0, 60) + '...' : q.text;
    const assetId = q.image.assetId;
    lines.push(`| ${q.id} | ${q.topic} | ${assetId} | ${excerpt} |`);
  });

  lines.push('');

  // Asset usage table
  lines.push('## Asset Usage Statistics\n');

  const assetUsage = new Map();
  questionsWithImages.forEach(q => {
    const assetId = q.image.assetId;
    if (!assetUsage.has(assetId)) {
      assetUsage.set(assetId, []);
    }
    assetUsage.get(assetId).push(q.id);
  });

  const manifestMap = new Map(manifest.map(a => [a.assetId, a]));

  lines.push('| Asset ID | Description | Used By (count) | Question IDs (sample) |');
  lines.push('|----------|-------------|-----------------|----------------------|');

  const sortedAssets = [...assetUsage.entries()].sort((a, b) => b[1].length - a[1].length);

  sortedAssets.forEach(([assetId, questionIds]) => {
    const asset = manifestMap.get(assetId);
    const description = asset ? (asset.description || 'N/A') : 'NOT IN MANIFEST';
    const sample = questionIds.slice(0, 3).join(', ');
    const more = questionIds.length > 3 ? ` +${questionIds.length - 3} more` : '';
    lines.push(`| ${assetId} | ${description} | ${questionIds.length} | ${sample}${more} |`);
  });

  lines.push('');

  fs.writeFileSync(outputPath, lines.join('\n'));
  console.log(`✓ Generated ${outputPath}`);
}

function main() {
  console.log('=== Generating Review Tables ===\n');

  const topicsDir = path.join(__dirname, '../data/tx/topics');
  const manifestPath = path.join(__dirname, '../assets/manifest.json');
  const reviewDir = path.join(__dirname, '../data/tx/review');

  if (!fs.existsSync(reviewDir)) {
    fs.mkdirSync(reviewDir, { recursive: true });
  }

  const { topics, allQuestions } = loadTopicFiles(topicsDir);
  const manifest = loadManifest(manifestPath);

  if (allQuestions.length === 0) {
    console.log('No questions found, skipping table generation');
    return;
  }

  const indexPath = path.join(reviewDir, 'index_by_topic.md');
  const imagesPath = path.join(reviewDir, 'questions_with_images.md');

  generateIndexByTopic(topics, allQuestions, manifest, indexPath);
  generateQuestionsWithImages(topics, allQuestions, manifest, imagesPath);

  console.log('\n✓ Review tables generated successfully');
}

main();
