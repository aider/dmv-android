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

function main() {
  console.log('=== Merging Topic Files ===\n');

  const topicsDir = path.join(__dirname, '../data/tx/topics');
  const outputPath = path.join(__dirname, '../data/tx/tx_v1.json');

  if (!fs.existsSync(topicsDir)) {
    console.error(`Topics directory not found: ${topicsDir}`);
    process.exit(1);
  }

  const topicFiles = fs.readdirSync(topicsDir)
    .filter(f => f.endsWith('.json'))
    .sort();

  if (topicFiles.length === 0) {
    console.error('No topic files found to merge');
    process.exit(1);
  }

  const allQuestions = [];
  const topicCounts = {};

  topicFiles.forEach(file => {
    const filePath = path.join(topicsDir, file);
    console.log(`Reading ${file}...`);

    try {
      const content = fs.readFileSync(filePath, 'utf8');
      const questions = JSON.parse(content);

      if (!Array.isArray(questions)) {
        console.error(`  ✗ File does not contain an array: ${file}`);
        process.exit(1);
      }

      const topic = questions[0]?.topic;
      if (topic) {
        topicCounts[topic] = (topicCounts[topic] || 0) + questions.length;
      }

      allQuestions.push(...questions);
      console.log(`  ✓ Added ${questions.length} questions`);
    } catch (err) {
      console.error(`  ✗ Failed to read ${file}: ${err.message}`);
      process.exit(1);
    }
  });

  const merged = {
    stateCode: 'TX',
    version: 1,
    totalQuestions: allQuestions.length,
    topics: topicCounts,
    generatedDate: new Date().toISOString().split('T')[0],
    questions: allQuestions
  };

  fs.writeFileSync(outputPath, JSON.stringify(merged, null, 2));
  console.log(`\n✓ Merged ${allQuestions.length} questions to ${outputPath}`);

  // Validation summary
  console.log('\n=== Topic Counts ===');
  Object.entries(EXPECTED_COUNTS).forEach(([topic, expected]) => {
    const actual = topicCounts[topic] || 0;
    const status = actual === expected ? '✓' : '✗';
    console.log(`${status} ${topic}: ${actual}/${expected}`);
  });

  const expectedTotal = Object.values(EXPECTED_COUNTS).reduce((a, b) => a + b, 0);
  console.log(`\nTotal: ${allQuestions.length}/${expectedTotal}`);
}

main();
