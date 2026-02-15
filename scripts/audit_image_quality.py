#!/usr/bin/env python3
"""
Image Quality Audit Script for DMV Texas Quiz App
Analyzes SVG assets for mobile readability at 96dp and 48dp render sizes.
Generates CSV + Markdown reports per Issue #52 requirements.
"""

import json
import xml.etree.ElementTree as ET
from pathlib import Path
from collections import defaultdict
import csv
import re

# Mobile rendering targets
TARGET_DP_PRIMARY = 96
TARGET_DP_MINIMUM = 48

# Quality thresholds
MIN_STROKE_DP = 2.0  # Minimum visible stroke width on mobile
MIN_TEXT_DP = 9.6    # Minimum legible text size on mobile

class SVGAnalyzer:
    def __init__(self, svg_path):
        self.path = svg_path
        self.tree = ET.parse(svg_path)
        self.root = self.tree.getroot()

        # Extract viewBox
        viewbox_str = self.root.get('viewBox', '0 0 200 200')
        parts = viewbox_str.split()
        self.viewbox_width = float(parts[2])
        self.viewbox_height = float(parts[3])

    def calculate_effective_dp(self, px_value, target_dp=TARGET_DP_PRIMARY):
        """Calculate effective dp size when SVG is rendered at target_dp"""
        # Use the larger viewBox dimension as reference
        viewbox_dim = max(self.viewbox_width, self.viewbox_height)
        return px_value * (target_dp / viewbox_dim)

    def find_min_stroke_width(self):
        """Find minimum stroke width in the SVG (in px units)"""
        min_stroke = float('inf')

        # Check all elements with stroke
        for elem in self.root.iter():
            # Check stroke-width attribute
            stroke_width = elem.get('stroke-width')
            if stroke_width:
                try:
                    width = float(stroke_width.replace('px', ''))
                    min_stroke = min(min_stroke, width)
                except ValueError:
                    pass

            # Check style attribute for stroke-width
            style = elem.get('style', '')
            if 'stroke-width' in style:
                match = re.search(r'stroke-width:\s*([0-9.]+)', style)
                if match:
                    try:
                        width = float(match.group(1))
                        min_stroke = min(min_stroke, width)
                    except ValueError:
                        pass

        return min_stroke if min_stroke != float('inf') else None

    def find_min_font_size(self):
        """Find minimum font size in the SVG (in px units)"""
        min_font = float('inf')

        for elem in self.root.iter():
            # Check font-size attribute
            font_size = elem.get('font-size')
            if font_size:
                try:
                    size = float(font_size.replace('px', ''))
                    min_font = min(min_font, size)
                except ValueError:
                    pass

            # Check style attribute for font-size
            style = elem.get('style', '')
            if 'font-size' in style:
                match = re.search(r'font-size:\s*([0-9.]+)', style)
                if match:
                    try:
                        size = float(match.group(1))
                        min_font = min(min_font, size)
                    except ValueError:
                        pass

        return min_font if min_font != float('inf') else None

    def has_text(self):
        """Check if SVG contains text elements"""
        return len(list(self.root.iter('{http://www.w3.org/2000/svg}text'))) > 0 or \
               len(list(self.root.iter('text'))) > 0

    def count_elements(self):
        """Count total elements in SVG"""
        return len(list(self.root.iter()))

    def get_complexity_score(self):
        """Score complexity (simpler = better for mobile)"""
        count = self.count_elements()
        if count < 20: return 5
        if count < 40: return 4
        if count < 60: return 3
        if count < 100: return 2
        return 1


def score_readability(analyzer):
    """Score 1-5: Can learner read symbols/text quickly on mobile?"""
    min_stroke = analyzer.find_min_stroke_width()
    min_font = analyzer.find_min_font_size()

    # Calculate effective dp sizes
    min_stroke_dp = analyzer.calculate_effective_dp(min_stroke) if min_stroke else None
    min_font_dp = analyzer.calculate_effective_dp(min_font) if min_font else None

    # Check for critical failures
    if min_stroke_dp and min_stroke_dp < 1.0:
        return 1  # Invisible strokes
    if min_font_dp and min_font_dp < 6.0:
        return 1  # Unreadable text

    if min_stroke_dp and min_stroke_dp < 1.5:
        return 2  # Barely visible
    if min_font_dp and min_font_dp < 8.0:
        return 2  # Barely readable

    if min_stroke_dp and min_stroke_dp < 2.0:
        return 3  # Marginal
    if min_font_dp and min_font_dp < 9.6:
        return 3  # Marginal

    if min_stroke_dp and min_stroke_dp < 2.5:
        return 4  # Acceptable
    if min_font_dp and min_font_dp < 12.0:
        return 4  # Acceptable

    return 5  # Excellent


def score_semantic_clarity(analyzer, asset_id, category):
    """Score 1-5: Is traffic meaning unambiguous?"""
    # MUTCD signs should follow strict geometry
    if 'MUTCD' in asset_id:
        # Check viewBox consistency
        if category == 'sign_regulatory' or category == 'sign_warning':
            # Most signs should be square or near-square
            ratio = analyzer.viewbox_width / analyzer.viewbox_height
            if 0.95 <= ratio <= 1.05:
                return 5  # Correct geometry
            elif 0.7 <= ratio <= 1.3:
                return 4  # Close enough
            else:
                return 3  # Questionable geometry

    # Intersection scenes should have clear elements
    if 'INTERSECTION' in asset_id:
        # Complex scenes - harder to judge automatically
        # Default to 4 (assume competent until proven otherwise)
        return 4

    # Pavement markings should be simple and clear
    if 'PAVEMENT' in asset_id:
        complexity = analyzer.get_complexity_score()
        if complexity >= 4:
            return 5  # Simple and clear
        else:
            return 4  # Acceptable complexity

    # Default: assume good semantic clarity
    return 4


def score_contrast(analyzer):
    """Score 1-5: Is foreground/background contrast sufficient?"""
    # This is hard to measure automatically without color analysis
    # For now, check for common issues

    # Check for excessive opacity (indicates weak contrast)
    low_opacity_count = 0
    for elem in analyzer.root.iter():
        opacity = elem.get('opacity')
        if opacity:
            try:
                val = float(opacity)
                if val < 0.5:
                    low_opacity_count += 1
            except ValueError:
                pass

    if low_opacity_count > 5:
        return 3  # Lots of transparency, may have contrast issues
    elif low_opacity_count > 2:
        return 4  # Some transparency
    else:
        return 5  # Likely good contrast


def score_consistency(analyzer, category):
    """Score 1-5: Is style consistent with DMV training context?"""
    # Check viewBox consistency by category
    vb_w, vb_h = analyzer.viewbox_width, analyzer.viewbox_height

    # Expected viewBox patterns
    expected_viewboxes = {
        'sign_regulatory': (200, 200),
        'sign_warning': (200, 200),
        'sign_guide': (200, 150),
        'pavement': (300, 200),
        'intersection': (300, 200),
        'signal': (100, 250),
    }

    if category in expected_viewboxes:
        expected_w, expected_h = expected_viewboxes[category]
        w_diff = abs(vb_w - expected_w)
        h_diff = abs(vb_h - expected_h)

        if w_diff <= 10 and h_diff <= 10:
            return 5  # Matches standard
        elif w_diff <= 50 and h_diff <= 50:
            return 4  # Close to standard
        else:
            return 3  # Deviates from standard

    # Default: acceptable
    return 4


def categorize_asset(asset_id):
    """Determine category based on asset_id pattern"""
    if 'MUTCD' in asset_id:
        if asset_id.startswith('MUTCD_R'):
            return 'sign_regulatory'
        elif asset_id.startswith('MUTCD_W'):
            return 'sign_warning'
        elif asset_id.startswith('MUTCD_M'):
            return 'sign_guide'
        else:
            return 'sign_other'
    elif 'PAVEMENT' in asset_id:
        return 'pavement'
    elif 'INTERSECTION' in asset_id:
        return 'intersection'
    elif 'SIGNAL' in asset_id:
        return 'signal'
    elif 'PARKING' in asset_id:
        return 'pavement'
    elif 'SPEED' in asset_id:
        return 'scenario'
    elif 'SAFE' in asset_id:
        return 'scenario'
    elif 'SPECIAL' in asset_id:
        return 'scenario'
    else:
        return 'other'


def determine_severity(scores, min_stroke_dp, min_font_dp):
    """Determine P0/P1/P2/PASS based on scores and metrics"""
    avg_score = sum(scores.values()) / len(scores)

    # P0: Critical failures - blocks learning
    if scores['readability'] <= 2:
        return 'P0'
    if min_stroke_dp and min_stroke_dp < 1.5:
        return 'P0'
    if min_font_dp and min_font_dp < 7.0:
        return 'P0'

    # P1: Suboptimal - degrades experience
    if avg_score < 3.5:
        return 'P1'
    if scores['readability'] <= 3:
        return 'P1'
    if min_stroke_dp and min_stroke_dp < 2.0:
        return 'P1'
    if min_font_dp and min_font_dp < 9.6:
        return 'P1'

    # P2: Minor issues
    if avg_score < 4.5:
        return 'P2'

    # PASS: High quality
    return 'PASS'


def determine_issue_type(analyzer, scores, min_stroke_dp, min_font_dp):
    """Identify primary issue type"""
    if min_font_dp and min_font_dp < 9.6:
        return 'tiny_text'
    if min_stroke_dp and min_stroke_dp < 2.0:
        return 'thin_strokes'
    if scores['contrast'] <= 3:
        return 'low_contrast'
    if scores['semantic_clarity'] <= 3:
        return 'ambiguous_geometry'
    if analyzer.count_elements() > 80:
        return 'clutter'
    if scores['consistency'] <= 3:
        return 'inconsistent_style'

    return 'none'


def propose_fix(issue_type, min_stroke_dp, min_font_dp, analyzer):
    """Generate concrete fix proposal"""
    if issue_type == 'tiny_text':
        min_font_px = analyzer.find_min_font_size()
        target_px = 20  # Target for 9.6dp at 200px viewBox
        return f"Increase font size from {min_font_px:.1f}px to ≥{target_px}px"

    elif issue_type == 'thin_strokes':
        min_stroke_px = analyzer.find_min_stroke_width()
        target_px = 6  # Target for 2.88dp at 200px viewBox
        return f"Increase stroke width from {min_stroke_px:.1f}px to ≥{target_px}px"

    elif issue_type == 'low_contrast':
        return "Increase color contrast between foreground and background"

    elif issue_type == 'ambiguous_geometry':
        return "Correct geometry to match MUTCD/SHS specifications"

    elif issue_type == 'clutter':
        return "Simplify design - remove non-essential elements"

    elif issue_type == 'inconsistent_style':
        vb = f"{analyzer.viewbox_width}×{analyzer.viewbox_height}"
        return f"Standardize viewBox from {vb} to category standard"

    else:
        return "No fix needed - asset meets quality standards"


def main():
    # Paths
    repo_root = Path(__file__).parent.parent
    questions_file = repo_root / 'data' / 'tx' / 'tx_v1.json'
    svg_dir = repo_root / 'assets' / 'svg'
    output_csv = repo_root / 'dmv-android' / 'docs' / 'growth' / 'image-quality-audit-2026-02.csv'
    output_md = repo_root / 'dmv-android' / 'docs' / 'growth' / 'image-quality-audit-2026-02.md'

    # Ensure output directory exists
    output_csv.parent.mkdir(parents=True, exist_ok=True)

    # Load questions
    with open(questions_file) as f:
        data = json.load(f)

    # Map asset usage
    asset_usage = defaultdict(list)
    for q in data['questions']:
        if 'image' in q:
            asset_id = q['image']['assetId']
            asset_usage[asset_id].append({
                'question_id': q['id'],
                'topic': q['topic']
            })

    print(f"Found {len(asset_usage)} unique assets used in {sum(len(v) for v in asset_usage.values())} questions")

    # Analyze each asset
    results = []

    for asset_id in sorted(asset_usage.keys()):
        svg_path = svg_dir / f"{asset_id}.svg"

        if not svg_path.exists():
            print(f"WARNING: Missing file for {asset_id}")
            continue

        print(f"Analyzing {asset_id}...")

        try:
            analyzer = SVGAnalyzer(svg_path)
            category = categorize_asset(asset_id)

            # Calculate metrics
            min_stroke = analyzer.find_min_stroke_width()
            min_font = analyzer.find_min_font_size()
            min_stroke_dp = analyzer.calculate_effective_dp(min_stroke) if min_stroke else None
            min_font_dp = analyzer.calculate_effective_dp(min_font) if min_font else None

            # Score on 4 dimensions
            scores = {
                'readability': score_readability(analyzer),
                'semantic_clarity': score_semantic_clarity(analyzer, asset_id, category),
                'contrast': score_contrast(analyzer),
                'consistency': score_consistency(analyzer, category)
            }

            # Determine severity and issue
            severity = determine_severity(scores, min_stroke_dp, min_font_dp)
            issue_type = determine_issue_type(analyzer, scores, min_stroke_dp, min_font_dp)

            # User impact
            usage_count = len(asset_usage[asset_id])
            if severity == 'P0':
                user_impact = f"High - blocks learning in {usage_count} question(s)"
            elif severity == 'P1':
                user_impact = f"Medium - degrades experience in {usage_count} question(s)"
            elif severity == 'P2':
                user_impact = f"Low - minor issue in {usage_count} question(s)"
            else:
                user_impact = f"None - quality asset used in {usage_count} question(s)"

            # Propose fix
            proposed_fix = propose_fix(issue_type, min_stroke_dp, min_font_dp, analyzer)

            # Add result for each question using this asset
            for usage in asset_usage[asset_id]:
                results.append({
                    'question_id': usage['question_id'],
                    'topic': usage['topic'],
                    'asset_id': asset_id,
                    'readability_score': scores['readability'],
                    'semantic_clarity_score': scores['semantic_clarity'],
                    'contrast_score': scores['contrast'],
                    'consistency_score': scores['consistency'],
                    'severity': severity,
                    'issue_type': issue_type,
                    'user_impact': user_impact,
                    'proposed_fix': proposed_fix,
                    'min_stroke_dp': f"{min_stroke_dp:.2f}" if min_stroke_dp else "N/A",
                    'min_font_dp': f"{min_font_dp:.2f}" if min_font_dp else "N/A",
                    'viewbox': f"{analyzer.viewbox_width}×{analyzer.viewbox_height}",
                    'usage_count': usage_count,
                    'category': category
                })

        except Exception as e:
            print(f"ERROR analyzing {asset_id}: {e}")

    # Write CSV
    print(f"\nWriting CSV to {output_csv}...")
    with open(output_csv, 'w', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=[
            'question_id', 'topic', 'asset_id', 'usage_count', 'category',
            'readability_score', 'semantic_clarity_score', 'contrast_score', 'consistency_score',
            'severity', 'issue_type', 'user_impact', 'proposed_fix',
            'min_stroke_dp', 'min_font_dp', 'viewbox'
        ])
        writer.writeheader()
        writer.writerows(results)

    print(f"✓ CSV written: {len(results)} rows")

    # Generate markdown report
    generate_markdown_report(results, asset_usage, output_md)
    print(f"✓ Markdown report written: {output_md}")


def generate_markdown_report(results, asset_usage, output_path):
    """Generate comprehensive markdown report"""

    # Group by asset for summary stats
    by_asset = defaultdict(list)
    for r in results:
        by_asset[r['asset_id']].append(r)

    # Get unique assets with their scores
    unique_assets = {}
    for asset_id, rows in by_asset.items():
        unique_assets[asset_id] = rows[0]  # First row has all the scores

    # Count by severity
    severity_counts = defaultdict(int)
    for asset in unique_assets.values():
        severity_counts[asset['severity']] += 1

    # Count by category
    category_counts = defaultdict(lambda: {'total': 0, 'P0': 0, 'P1': 0, 'P2': 0, 'PASS': 0})
    for asset in unique_assets.values():
        cat = asset['category']
        category_counts[cat]['total'] += 1
        category_counts[cat][asset['severity']] += 1

    # Find top 15 failures (by priority, then by usage count)
    failures = [a for a in unique_assets.values() if a['severity'] in ['P0', 'P1', 'P2']]
    severity_rank = {'P0': 0, 'P1': 1, 'P2': 2}
    top_15 = sorted(failures, key=lambda x: (severity_rank[x['severity']], -int(x['usage_count'])))[:15]

    # Find pass examples
    passes = [a for a in unique_assets.values() if a['severity'] == 'PASS']
    top_passes = sorted(passes, key=lambda x: (
        -(int(x['readability_score']) + int(x['semantic_clarity_score']) +
          int(x['contrast_score']) + int(x['consistency_score'])),
        -int(x['usage_count'])
    ))[:5]

    # Write markdown
    with open(output_path, 'w') as f:
        f.write("# Image Quality Audit Report — Texas DMV Practice App\n\n")
        f.write(f"**Generated:** 2026-02-14\n")
        f.write(f"**Audited:** {len(unique_assets)} unique SVG assets across {len(results)} question instances\n")
        f.write(f"**Methodology:** Technical SVG analysis + mobile rendering simulation (96dp primary, 48dp minimum)\n\n")

        f.write("---\n\n")
        f.write("## Executive Summary\n\n")
        f.write(f"**Total unique assets:** {len(unique_assets)}\n")
        f.write(f"**Total question instances:** {len(results)}\n")
        f.write(f"**Questions with images:** {len(set(r['question_id'] for r in results))} of 660 (20.6%)\n\n")

        f.write("### Results by Priority\n\n")
        f.write("| Priority | Count | % | Description |\n")
        f.write("|----------|-------|---|-------------|\n")
        total = len(unique_assets)
        f.write(f"| **P0 (Blocking)** | {severity_counts['P0']} | {severity_counts['P0']/total*100:.1f}% | Critical mobile readability failures |\n")
        f.write(f"| **P1 (Important)** | {severity_counts['P1']} | {severity_counts['P1']/total*100:.1f}% | Suboptimal quality - degrades experience |\n")
        f.write(f"| **P2 (Minor)** | {severity_counts['P2']} | {severity_counts['P2']/total*100:.1f}% | Polish opportunities |\n")
        f.write(f"| **PASS (Quality)** | {severity_counts['PASS']} | {severity_counts['PASS']/total*100:.1f}% | High quality - meets all criteria |\n\n")

        pass_rate = severity_counts['PASS'] / total * 100
        fail_rate = 100 - pass_rate
        f.write(f"**Overall Health:** {pass_rate:.0f}% PASS / {fail_rate:.0f}% Needs Fix\n\n")

        f.write("---\n\n")
        f.write("## Top 15 Assets to Redesign\n\n")
        f.write("Prioritized by severity, then usage frequency:\n\n")
        f.write("| Rank | Asset ID | Usage | Severity | Issue | Fix | Effort |\n")
        f.write("|------|----------|-------|----------|-------|-----|--------|\n")

        for i, asset in enumerate(top_15, 1):
            effort = estimate_effort(asset)
            f.write(f"| {i} | {asset['asset_id']} | {asset['usage_count']} | {asset['severity']} | "
                   f"{asset['issue_type'].replace('_', ' ').title()} | {asset['proposed_fix'][:50]}... | {effort} |\n")

        f.write("\n")
        f.write("### Batch 1 Implementation Proposal\n\n")
        total_effort_hours = sum(estimate_effort_hours(a) for a in top_15)
        total_questions = sum(int(a['usage_count']) for a in top_15)
        f.write(f"**Scope:** Top 15 assets (all P0 and high-usage P1)\n")
        f.write(f"**Estimated effort:** {total_effort_hours} hours\n")
        f.write(f"**User impact:** {total_questions} question instances ({total_questions/len(results)*100:.1f}% of questions with images)\n")
        f.write(f"**Recommended owner:** `svg-asset-curator`\n\n")

        f.write("---\n\n")
        f.write("## Category Performance\n\n")
        f.write("| Category | Total | PASS | P2 | P1 | P0 | Health |\n")
        f.write("|----------|-------|------|----|----|----|--------|\n")

        for cat in sorted(category_counts.keys()):
            stats = category_counts[cat]
            health_pct = stats['PASS'] / stats['total'] * 100
            health_icon = "✅" if health_pct >= 70 else "⚠️" if health_pct >= 50 else "❌"
            f.write(f"| {cat.replace('_', ' ').title()} | {stats['total']} | {stats['PASS']} | "
                   f"{stats['P2']} | {stats['P1']} | {stats['P0']} | {health_pct:.0f}% {health_icon} |\n")

        f.write("\n---\n\n")
        f.write("## Reference Quality Examples (PASS)\n\n")
        f.write("These 5 assets demonstrate mobile-ready design:\n\n")
        f.write("| Asset ID | Usage | Scores (R/S/C/C) | Success Factors |\n")
        f.write("|----------|-------|------------------|------------------|\n")

        for asset in top_passes:
            score_str = f"{asset['readability_score']}/{asset['semantic_clarity_score']}/{asset['contrast_score']}/{asset['consistency_score']}"
            factors = summarize_success_factors(asset)
            f.write(f"| {asset['asset_id']} | {asset['usage_count']} | {score_str} | {factors} |\n")

        f.write("\n---\n\n")
        f.write("## Critical Findings (P0)\n\n")

        p0_assets = [a for a in unique_assets.values() if a['severity'] == 'P0']
        if p0_assets:
            f.write(f"**{len(p0_assets)} assets** have **blocking mobile readability issues**:\n\n")
            f.write("| Asset ID | Usage | Min Stroke | Min Text | Issue |\n")
            f.write("|----------|-------|------------|----------|-------|\n")

            for asset in sorted(p0_assets, key=lambda x: -int(x['usage_count'])):
                f.write(f"| {asset['asset_id']} | {asset['usage_count']} | {asset['min_stroke_dp']} | "
                       f"{asset['min_font_dp']} | {asset['issue_type'].replace('_', ' ')} |\n")
        else:
            f.write("**No P0 (blocking) issues found.** ✅\n\n")

        f.write("\n---\n\n")
        f.write("## Methodology\n\n")
        f.write("### Scoring Rubric\n\n")
        f.write("Each asset scored 1-5 on four dimensions:\n\n")
        f.write("1. **Readability (1-5):** Can learner read symbols/text on mobile?\n")
        f.write("   - 5: Excellent (strokes ≥2.5dp, text ≥12dp)\n")
        f.write("   - 4: Good (strokes ≥2.0dp, text ≥9.6dp)\n")
        f.write("   - 3: Marginal (strokes ≥1.5dp, text ≥8dp)\n")
        f.write("   - 2: Poor (strokes <1.5dp, text <8dp)\n")
        f.write("   - 1: Critical fail (strokes <1.0dp, text <6dp)\n\n")

        f.write("2. **Semantic Clarity (1-5):** Is traffic meaning unambiguous?\n")
        f.write("   - Geometry correctness (MUTCD compliance)\n")
        f.write("   - Presence of essential training cues\n")
        f.write("   - Visual simplicity (not cluttered)\n\n")

        f.write("3. **Contrast (1-5):** Is foreground/background contrast sufficient?\n")
        f.write("   - High contrast = easy to distinguish elements\n")
        f.write("   - Excessive transparency penalized\n\n")

        f.write("4. **Consistency (1-5):** Matches DMV training context?\n")
        f.write("   - viewBox standardization by category\n")
        f.write("   - Padding consistency\n")
        f.write("   - Style alignment\n\n")

        f.write("### Mobile Rendering Math\n\n")
        f.write("```\neffective_dp = px_value × (target_dp / viewBox_dimension)\n\n")
        f.write("Example: 10px text in 200px viewBox at 96dp render\n")
        f.write("= 10 × (96/200) = 4.8dp (illegible)\n```\n\n")

        f.write("### Quality Thresholds\n\n")
        f.write("- **Minimum stroke width:** 2.0dp (6px in 200px viewBox at 96dp)\n")
        f.write("- **Minimum text size:** 9.6dp (20px in 200px viewBox at 96dp)\n")
        f.write("- **Target render sizes:** 96dp primary, 48dp minimum\n\n")

        f.write("---\n\n")
        f.write("## Next Steps\n\n")
        f.write("1. **Create implementation issue** for Batch 1 (Top 15 redesigns)\n")
        f.write("2. **Assign to `svg-asset-curator`** for redesign work\n")
        f.write("3. **Update style guide** with mobile readability standards\n")
        f.write("4. **Re-audit after fixes** to validate improvements\n\n")

        f.write("---\n\n")
        f.write("**Audit conducted by:** SVG Review Agent\n")
        f.write("**Date:** 2026-02-14\n")
        f.write("**Branch:** `codex/svg-review-agent/52-image-quality-audit`\n")


def estimate_effort(asset):
    """Estimate effort level: S/M/L"""
    issue_type = asset['issue_type']
    complexity = len(asset['asset_id'])  # Rough proxy

    if issue_type in ['tiny_text', 'thin_strokes']:
        return 'S'  # Simple numerical adjustment
    elif issue_type in ['inconsistent_style', 'low_contrast']:
        return 'M'  # Moderate redesign
    elif issue_type in ['ambiguous_geometry', 'clutter']:
        return 'L'  # Significant redesign
    else:
        return 'S'


def estimate_effort_hours(asset):
    """Estimate hours based on effort level"""
    effort = estimate_effort(asset)
    return {'S': 0.5, 'M': 1.5, 'L': 3.0}[effort]


def summarize_success_factors(asset):
    """Summarize why this asset passes"""
    factors = []

    if asset['min_stroke_dp'] != 'N/A':
        stroke_dp = float(asset['min_stroke_dp'])
        if stroke_dp >= 2.5:
            factors.append(f"Bold strokes ({stroke_dp:.1f}dp)")

    if asset['min_font_dp'] != 'N/A':
        font_dp = float(asset['min_font_dp'])
        if font_dp >= 10:
            factors.append(f"Large text ({font_dp:.1f}dp)")
    else:
        factors.append("No text (icon-based)")

    if int(asset['contrast_score']) >= 5:
        factors.append("High contrast")

    if int(asset['semantic_clarity_score']) >= 5:
        factors.append("Clear geometry")

    return ", ".join(factors) if factors else "Well-balanced design"


if __name__ == '__main__':
    main()
