#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const TOPIC_CODES = {
  SIGNS: 'SIG',
  TRAFFIC_SIGNALS: 'TRA',
  PAVEMENT_MARKINGS: 'PAV',
  RIGHT_OF_WAY: 'ROW',
  SPEED_AND_DISTANCE: 'SPD',
  PARKING: 'PRK',
  SAFE_DRIVING: 'SAF',
  SPECIAL_SITUATIONS: 'SPC'
};

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

const ALLOWED_TOPICS = Object.keys(TOPIC_CODES);

function validateQuestion(question, index, topicFile) {
  const errors = [];
  const warnings = [];

  // Check required fields
  const requiredFields = ['id', 'topic', 'difficulty', 'text', 'choices', 'correctIndex', 'explanation', 'reference'];
  for (const field of requiredFields) {
    if (!(field in question)) {
      errors.push(`Question ${index}: Missing required field '${field}'`);
    }
  }

  // Validate ID format
  if (question.id) {
    const match = question.id.match(/^TX-([A-Z]{3})-(\d{4})$/);
    if (!match) {
      errors.push(`Question ${index}: Invalid ID format '${question.id}' (expected TX-{CODE}-{NUMBER})`);
    } else {
      const topicCode = match[1];
      const expectedCode = TOPIC_CODES[question.topic];
      if (expectedCode && topicCode !== expectedCode) {
        errors.push(`Question ${index}: ID topic code '${topicCode}' doesn't match topic '${question.topic}' (expected ${expectedCode})`);
      }
    }
  }

  // Validate topic
  if (question.topic && !ALLOWED_TOPICS.includes(question.topic)) {
    errors.push(`Question ${index}: Invalid topic '${question.topic}' (allowed: ${ALLOWED_TOPICS.join(', ')})`);
  }

  // Validate difficulty
  if (question.difficulty !== undefined) {
    if (!Number.isInteger(question.difficulty) || question.difficulty < 1 || question.difficulty > 5) {
      errors.push(`Question ${index}: Difficulty must be integer 1-5, got ${question.difficulty}`);
    }
  }

  // Validate choices
  if (question.choices) {
    if (!Array.isArray(question.choices)) {
      errors.push(`Question ${index}: Choices must be an array`);
    } else if (question.choices.length !== 4) {
      errors.push(`Question ${index}: Choices must have exactly 4 elements, got ${question.choices.length}`);
    }
  }

  // Validate correctIndex
  if (question.correctIndex !== undefined) {
    if (!Number.isInteger(question.correctIndex) || question.correctIndex < 0 || question.correctIndex > 3) {
      errors.push(`Question ${index}: correctIndex must be integer 0-3, got ${question.correctIndex}`);
    }
  }

  // Validate image if present
  if (question.image) {
    if (!question.image.type) {
      errors.push(`Question ${index}: Image missing 'type' field`);
    }
    if (!question.image.assetId) {
      errors.push(`Question ${index}: Image missing 'assetId' field`);
    }
  }

  // Check for empty strings
  if (question.text !== undefined && question.text.trim() === '') {
    warnings.push(`Question ${index}: Empty question text`);
  }
  if (question.explanation !== undefined && question.explanation.trim() === '') {
    warnings.push(`Question ${index}: Empty explanation`);
  }

  return { errors, warnings };
}

function validateTopicFile(filePath) {
  console.log(`\nValidating ${path.basename(filePath)}...`);

  let data;
  try {
    const content = fs.readFileSync(filePath, 'utf8');
    data = JSON.parse(content);
  } catch (err) {
    console.error(`  ✗ Failed to read/parse file: ${err.message}`);
    return { valid: false, questions: [], errors: [err.message], warnings: [] };
  }

  const allErrors = [];
  const allWarnings = [];
  const questions = Array.isArray(data) ? data : [];

  if (!Array.isArray(data)) {
    allErrors.push('File must contain an array of questions');
    return { valid: false, questions: [], errors: allErrors, warnings: allWarnings };
  }

  // Validate each question
  const ids = new Set();
  questions.forEach((question, index) => {
    const { errors, warnings } = validateQuestion(question, index, filePath);
    allErrors.push(...errors);
    allWarnings.push(...warnings);

    // Check for duplicate IDs
    if (question.id) {
      if (ids.has(question.id)) {
        allErrors.push(`Duplicate question ID: ${question.id}`);
      }
      ids.add(question.id);
    }
  });

  // Check topic count expectations
  const topic = questions[0]?.topic;
  if (topic && EXPECTED_COUNTS[topic]) {
    const expected = EXPECTED_COUNTS[topic];
    const actual = questions.length;
    if (actual !== expected) {
      allWarnings.push(`Expected ${expected} questions for topic ${topic}, got ${actual}`);
    }
  }

  const valid = allErrors.length === 0;
  console.log(`  ${valid ? '✓' : '✗'} ${questions.length} questions`);
  if (allErrors.length > 0) {
    console.log(`  ✗ ${allErrors.length} errors`);
    allErrors.slice(0, 10).forEach(err => console.log(`    - ${err}`));
    if (allErrors.length > 10) {
      console.log(`    ... and ${allErrors.length - 10} more errors`);
    }
  }
  if (allWarnings.length > 0) {
    console.log(`  ⚠ ${allWarnings.length} warnings`);
    allWarnings.slice(0, 5).forEach(warn => console.log(`    - ${warn}`));
    if (allWarnings.length > 5) {
      console.log(`    ... and ${allWarnings.length - 5} more warnings`);
    }
  }

  return { valid, questions, errors: allErrors, warnings: allWarnings };
}

function main() {
  console.log('=== Question Validation ===');

  const topicsDir = path.join(__dirname, '../data/tx/topics');

  if (!fs.existsSync(topicsDir)) {
    console.error(`Topics directory not found: ${topicsDir}`);
    process.exit(1);
  }

  const topicFiles = fs.readdirSync(topicsDir)
    .filter(f => f.endsWith('.json'))
    .map(f => path.join(topicsDir, f));

  if (topicFiles.length === 0) {
    console.log('No topic files found.');
    process.exit(0);
  }

  let totalValid = 0;
  let totalInvalid = 0;
  let totalQuestions = 0;
  const allQuestionIds = new Set();
  const duplicateIds = new Set();

  const results = topicFiles.map(file => {
    const result = validateTopicFile(file);
    if (result.valid) {
      totalValid++;
    } else {
      totalInvalid++;
    }
    totalQuestions += result.questions.length;

    // Check for duplicate IDs across files
    result.questions.forEach(q => {
      if (q.id) {
        if (allQuestionIds.has(q.id)) {
          duplicateIds.add(q.id);
        }
        allQuestionIds.add(q.id);
      }
    });

    return result;
  });

  console.log('\n=== Summary ===');
  console.log(`Total files: ${topicFiles.length}`);
  console.log(`Valid files: ${totalValid}`);
  console.log(`Invalid files: ${totalInvalid}`);
  console.log(`Total questions: ${totalQuestions}`);
  console.log(`Unique IDs: ${allQuestionIds.size}`);

  if (duplicateIds.size > 0) {
    console.log(`\n✗ Duplicate IDs across files: ${duplicateIds.size}`);
    Array.from(duplicateIds).slice(0, 10).forEach(id => console.log(`  - ${id}`));
  } else {
    console.log(`✓ All question IDs are unique`);
  }

  const expectedTotal = Object.values(EXPECTED_COUNTS).reduce((a, b) => a + b, 0);
  console.log(`\nExpected total: ${expectedTotal}`);
  console.log(`Actual total: ${totalQuestions}`);
  if (totalQuestions === expectedTotal) {
    console.log(`✓ Question count matches target`);
  } else {
    console.log(`⚠ Question count mismatch (${totalQuestions - expectedTotal > 0 ? '+' : ''}${totalQuestions - expectedTotal})`);
  }

  process.exit(totalInvalid > 0 || duplicateIds.size > 0 ? 1 : 0);
}

main();
