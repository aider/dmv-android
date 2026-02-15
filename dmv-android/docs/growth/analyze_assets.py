#!/usr/bin/env python3
"""
Image Quality Audit Script for DMV Texas Quiz App
Analyzes SVG assets based on mobile rendering constraints and user experience criteria
"""

import xml.etree.ElementTree as ET
import json
import os
import csv
import re
from collections import defaultdict

# Constants
TARGET_DP = 96  # Primary render size
MIN_DP = 48     # Minimum distinguishable size
VIEWBOX_STANDARD = 200  # Standard viewBox dimension

# Minimum thresholds for mobile readability
MIN_STROKE_DP = 2.0   # Minimum stroke width for visibility
MIN_TEXT_DP = 9.6     # Minimum text size for readability (10sp approx)
OPTIMAL_TEXT_DP = 14.4  # Optimal text size (15sp approx)

def calculate_effective_dp(px_value, viewbox_dim, target_dp=TARGET_DP):
    """Calculate effective dp size when rendered"""
    return px_value * (target_dp / viewbox_dim)

def extract_viewbox_dims(viewbox_str):
    """Extract width and height from viewBox attribute"""
    if not viewbox_str or viewbox_str == 'MISSING':
        return None, None
    parts = viewbox_str.split()
    if len(parts) == 4:
        return float(parts[2]), float(parts[3])
    return None, None

def analyze_svg_technical(filepath):
    """Perform technical analysis of SVG file"""
    try:
        tree = ET.parse(filepath)
        root = tree.getroot()

        viewbox = root.get('viewBox', 'MISSING')
        vb_width, vb_height = extract_viewbox_dims(viewbox)

        # Collect all stroke widths
        stroke_widths = []
        for elem in root.iter():
            stroke_width = elem.get('stroke-width', '')
            if stroke_width:
                try:
                    stroke_widths.append(float(stroke_width))
                except ValueError:
                    pass

        # Collect all text elements and font sizes
        text_elements = []
        ns = {'svg': 'http://www.w3.org/2000/svg'}
        for text in root.findall('.//svg:text', ns):
            font_size_str = text.get('font-size', '')
            text_content = ''.join(text.itertext()).strip()
            try:
                font_size = float(font_size_str) if font_size_str else None
                text_elements.append({
                    'content': text_content,
                    'font_size': font_size,
                    'font_size_dp': calculate_effective_dp(font_size, vb_width, TARGET_DP) if font_size and vb_width else None
                })
            except ValueError:
                pass

        # Calculate effective stroke widths in dp
        stroke_dps = []
        if stroke_widths and vb_width:
            stroke_dps = [calculate_effective_dp(sw, vb_width, TARGET_DP) for sw in stroke_widths]

        # Check for filters, gradients, complex elements
        has_filters = len(root.findall('.//{http://www.w3.org/2000/svg}filter')) > 0
        has_complex_gradients = len(root.findall('.//{http://www.w3.org/2000/svg}linearGradient')) + \
                                len(root.findall('.//{http://www.w3.org/2000/svg}radialGradient')) > 2

        return {
            'viewbox': viewbox,
            'viewbox_width': vb_width,
            'viewbox_height': vb_height,
            'stroke_widths_px': stroke_widths,
            'stroke_widths_dp': stroke_dps,
            'min_stroke_dp': min(stroke_dps) if stroke_dps else None,
            'text_elements': text_elements,
            'has_filters': has_filters,
            'has_complex_gradients': has_complex_gradients,
        }
    except Exception as e:
        return {'error': str(e)}

def score_readability(technical_data):
    """
    Score 1-5: Can a learner read key symbols/text quickly on phone size?
    5 = Perfect, all text ≥14dp, strokes ≥2.5dp
    4 = Good, text ≥10dp, strokes ≥2dp
    3 = Acceptable, text ≥9dp, strokes ≥1.5dp
    2 = Poor, text <9dp or strokes <1.5dp
    1 = Failed, illegible text or invisible strokes
    """
    if 'error' in technical_data:
        return 1

    min_stroke = technical_data.get('min_stroke_dp')
    text_elements = technical_data.get('text_elements', [])

    # Check strokes
    stroke_score = 5
    if min_stroke is not None:
        if min_stroke < 1.0:
            stroke_score = 1
        elif min_stroke < 1.5:
            stroke_score = 2
        elif min_stroke < 2.0:
            stroke_score = 3
        elif min_stroke < 2.5:
            stroke_score = 4

    # Check text
    text_score = 5
    if text_elements:
        min_text_dp = min([t['font_size_dp'] for t in text_elements if t['font_size_dp']], default=None)
        if min_text_dp is not None:
            if min_text_dp < 6:
                text_score = 1
            elif min_text_dp < 9:
                text_score = 2
            elif min_text_dp < 10:
                text_score = 3
            elif min_text_dp < 14:
                text_score = 4

    return min(stroke_score, text_score)

def score_semantic_clarity(asset_id, technical_data):
    """
    Score 1-5: Is the intended traffic meaning unambiguous?
    Based on category and complexity
    """
    min_stroke = technical_data.get('min_stroke_dp')

    # MUTCD signs: highly standardized, should be 5
    if asset_id.startswith('MUTCD_'):
        if min_stroke is None or min_stroke >= 2.0:
            return 5
        else:
            return 4  # Geometry correct but too thin

    # Intersections: need vehicles, arrows, stop lines
    if asset_id.startswith('INTERSECTION_'):
        # Check if has very thin strokes (ambiguous)
        if min_stroke is not None and min_stroke < 1.5:
            return 3
        return 4

    # Pavement markings
    if asset_id.startswith('PAVEMENT_'):
        return 4

    # Signals
    if asset_id.startswith('SIGNAL_'):
        return 5

    # Safe driving / special
    if asset_id.startswith(('SAFE_', 'SPECIAL_', 'SPEED_', 'PARKING_')):
        if min_stroke is not None and min_stroke < 1.5:
            return 3
        return 4

    return 3

def score_contrast(asset_id, technical_data):
    """
    Score 1-5: Is foreground/background contrast sufficient?
    Without full color analysis, use heuristics
    """
    # Assume most assets have adequate contrast unless filters
    if technical_data.get('has_complex_gradients') or technical_data.get('has_filters'):
        return 3

    # Signs and signals have strong contrast
    if asset_id.startswith(('MUTCD_', 'SIGNAL_')):
        return 5

    # Pavement and intersections: medium contrast
    if asset_id.startswith(('PAVEMENT_', 'INTERSECTION_')):
        return 4

    return 4

def score_consistency(asset_id, technical_data, all_viewboxes):
    """
    Score 1-5: Is style consistent with other assets and DMV training context?
    Check viewBox consistency within category
    """
    category = asset_id.split('_')[0]

    # Check viewBox consistency
    vb = technical_data.get('viewbox')
    if vb and vb != 'MISSING':
        category_viewboxes = [v for aid, v in all_viewboxes.items() if aid.startswith(category)]
        if category_viewboxes:
            most_common_vb = max(set(category_viewboxes), key=category_viewboxes.count)
            if vb != most_common_vb:
                return 3

    return 4

def categorize_issue_type(readability_score, semantic_score, contrast_score):
    """Determine primary issue type"""
    if readability_score <= 2:
        return 'tiny_text'
    if semantic_score <= 2:
        return 'ambiguous_geometry'
    if contrast_score <= 2:
        return 'low_contrast'
    if readability_score <= 3:
        return 'clutter'
    return 'none'

def assign_severity(readability, semantic, contrast, consistency, usage_count):
    """
    Assign P0/P1/P2/PASS priority
    P0: High usage (≥2) + critical readability/semantic issues
    P1: Moderate usage or significant quality issues
    P2: Low usage + minor issues
    PASS: All scores ≥4
    """
    avg_score = (readability + semantic + contrast + consistency) / 4

    if readability >= 4 and semantic >= 4 and contrast >= 4 and consistency >= 4:
        return 'PASS'

    if readability <= 2 or semantic <= 2:
        if usage_count >= 2:
            return 'P0'
        else:
            return 'P1'

    if readability == 3 or semantic == 3:
        return 'P1'

    return 'P2'

def propose_fix(asset_id, technical_data, issue_type):
    """Propose specific fix based on issue"""
    fixes = []

    min_stroke = technical_data.get('min_stroke_dp')
    if min_stroke and min_stroke < MIN_STROKE_DP:
        target_stroke = 8  # 8px in 200px viewBox = 3.84dp at 96dp
        fixes.append(f"Increase minimum stroke width from {min_stroke:.1f}dp to ≥{MIN_STROKE_DP}dp (suggest 8px in viewBox)")

    text_elements = technical_data.get('text_elements', [])
    for text in text_elements:
        if text['font_size_dp'] and text['font_size_dp'] < MIN_TEXT_DP:
            fixes.append(f"Increase text '{text['content'][:20]}...' from {text['font_size_dp']:.1f}dp to ≥{MIN_TEXT_DP}dp (20px min)")

    if not fixes:
        fixes.append("Minor optimizations: verify contrast, check padding")

    return '; '.join(fixes)

def estimate_user_impact(severity, usage_count):
    """Estimate user impact"""
    if severity == 'PASS':
        return 'None'
    if severity == 'P0':
        return f'High - affects {usage_count} questions, critical learning content'
    if severity == 'P1':
        return f'Medium - affects {usage_count} question(s), readability concerns'
    return f'Low - affects {usage_count} question(s), minor polish'

def main():
    # Load questions
    questions_file = '../../../data/tx/tx_v1.json'
    with open(questions_file, 'r') as f:
        data = json.load(f)

    # Build asset usage map
    asset_usage = defaultdict(lambda: {'count': 0, 'questions': [], 'topics': set()})
    for q in data['questions']:
        if 'image' in q and q['image']:
            asset_id = q['image'].get('assetId')
            if asset_id:
                asset_usage[asset_id]['count'] += 1
                asset_usage[asset_id]['questions'].append(q['id'])
                asset_usage[asset_id]['topics'].add(q['topic'])

    # Analyze all SVG assets
    svg_dir = '../../../assets/svg'
    results = []
    all_viewboxes = {}

    for asset_id, usage_data in asset_usage.items():
        svg_path = os.path.join(svg_dir, f'{asset_id}.svg')
        if not os.path.exists(svg_path):
            print(f"WARNING: Missing SVG for {asset_id}")
            continue

        # Technical analysis
        tech_data = analyze_svg_technical(svg_path)
        all_viewboxes[asset_id] = tech_data.get('viewbox', '')

    # Second pass: scoring with full context
    for asset_id, usage_data in asset_usage.items():
        svg_path = os.path.join(svg_dir, f'{asset_id}.svg')
        if not os.path.exists(svg_path):
            continue

        tech_data = analyze_svg_technical(svg_path)

        # Score on rubric
        readability = score_readability(tech_data)
        semantic = score_semantic_clarity(asset_id, tech_data)
        contrast = score_contrast(asset_id, tech_data)
        consistency = score_consistency(asset_id, tech_data, all_viewboxes)

        usage_count = usage_data['count']
        severity = assign_severity(readability, semantic, contrast, consistency, usage_count)
        issue_type = categorize_issue_type(readability, semantic, contrast)

        # Build result row
        topics_str = ', '.join(sorted(usage_data['topics']))
        questions_sample = usage_data['questions'][0] if usage_data['questions'] else ''

        result = {
            'question_id': questions_sample,
            'topic': topics_str,
            'asset_id': asset_id,
            'usage_count': usage_count,
            'readability_score': readability,
            'semantic_clarity_score': semantic,
            'contrast_score': contrast,
            'consistency_score': consistency,
            'severity': severity,
            'issue_type': issue_type,
            'user_impact': estimate_user_impact(severity, usage_count),
            'proposed_fix': propose_fix(asset_id, tech_data, issue_type),
            'min_stroke_dp': f"{tech_data.get('min_stroke_dp', 0):.2f}" if tech_data.get('min_stroke_dp') else 'N/A',
            'viewbox': tech_data.get('viewbox', ''),
        }

        results.append(result)

    # Sort by severity (P0, P1, P2, PASS) then by usage count
    severity_order = {'P0': 0, 'P1': 1, 'P2': 2, 'PASS': 3}
    results.sort(key=lambda x: (severity_order[x['severity']], -x['usage_count']))

    # Write CSV
    csv_file = 'image-quality-audit-2026-02.csv'
    with open(csv_file, 'w', newline='') as f:
        fieldnames = ['question_id', 'topic', 'asset_id', 'usage_count',
                      'readability_score', 'semantic_clarity_score', 'contrast_score', 'consistency_score',
                      'severity', 'issue_type', 'user_impact', 'proposed_fix', 'min_stroke_dp', 'viewbox']
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(results)

    print(f"✓ Wrote {csv_file} with {len(results)} assets")

    # Print summary
    severity_counts = defaultdict(int)
    for r in results:
        severity_counts[r['severity']] += 1

    print(f"\nSummary:")
    print(f"  P0 (blocking): {severity_counts['P0']}")
    print(f"  P1 (important): {severity_counts['P1']}")
    print(f"  P2 (nice-to-have): {severity_counts['P2']}")
    print(f"  PASS: {severity_counts['PASS']}")

    print(f"\nTop 15 issues to fix:")
    for i, r in enumerate(results[:15], 1):
        print(f"  {i}. {r['asset_id']:<45} [{r['severity']}] {r['issue_type']}")

    return results

if __name__ == '__main__':
    main()
