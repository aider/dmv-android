#!/bin/bash
# pick-issue.sh - macOS version with clipboard

REPO="your-org/your-repo"  # ← ваш репо

echo "🔍 Loading issues with label 'claude-todo'..."

# Выбрать issue
SELECTED=$(gh issue list --repo "$REPO" \
    --label "claude-todo" \
    --state open \
    --json number,title \
    --jq '.[] | "#\(.number) - \(.title)"' | \
    fzf --height 40% \
        --border rounded \
        --prompt "Select issue to implement: " \
        --preview "gh issue view {1} --repo $REPO" \
        --preview-window right:60%)

if [ -z "$SELECTED" ]; then
    echo "❌ No issue selected"
    exit 0
fi

# Извлечь номер (macOS compatible)
ISSUE_NUM=$(echo "$SELECTED" | sed -n 's/^#\([0-9]*\).*/\1/p')

# Получить детали issue
echo "📥 Fetching issue details..."
ISSUE_DATA=$(gh issue view "$ISSUE_NUM" --repo "$REPO" --json title,body)
TITLE=$(echo "$ISSUE_DATA" | jq -r '.title')
BODY=$(echo "$ISSUE_DATA" | jq -r '.body')

# Создать промпт
PROMPT="Implement GitHub issue #$ISSUE_NUM: $TITLE

$BODY

Please help me implement this following our project standards in CLAUDE.md."

# Скопировать в clipboard
echo "$PROMPT" | pbcopy

# Показать что скопировали
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Issue prompt copied to clipboard!"
echo ""
echo "📋 Issue #$ISSUE_NUM: $TITLE"
echo ""
echo "🚀 Starting Claude Code..."
echo "💬 Press Cmd+V to paste the prompt"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Запустить Claude
claude
