#!/usr/bin/env node

/**
 * Build per-asset reference search review:
 * - one web search query per asset
 * - best candidate reference URL
 * - keep/skip decision
 * - concrete prompt additions to improve regeneration quality
 *
 * Output:
 *   assets/review/reference_prompt_review.json
 *   assets/review/reference_prompt_review.md
 */

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const MANIFEST_PATH = path.join(ROOT, 'assets', 'manifest.json');
const REVIEW_DIR = path.join(ROOT, 'assets', 'review');
const OUT_JSON = path.join(REVIEW_DIR, 'reference_prompt_review.json');
const OUT_MD = path.join(REVIEW_DIR, 'reference_prompt_review.md');
const CACHE_PATH = path.join(REVIEW_DIR, 'reference_search_cache.json');
const TODAY = '2026-02-10';

const REQUEST_DELAY_MS = 450;
const MAX_RESULTS = 8;

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function readJson(p, fallback = null) {
  try {
    return JSON.parse(fs.readFileSync(p, 'utf8'));
  } catch {
    return fallback;
  }
}

function writeJson(p, data) {
  fs.writeFileSync(p, `${JSON.stringify(data, null, 2)}\n`, 'utf8');
}

function category(assetId) {
  if (assetId.startsWith('MUTCD_')) return 'mutcd';
  if (assetId.startsWith('INTERSECTION_')) return 'intersection';
  if (assetId.startsWith('PAVEMENT_')) return 'pavement';
  if (assetId.startsWith('SIGNAL_')) return 'signal';
  if (assetId.startsWith('PARKING_')) return 'parking';
  if (assetId.startsWith('SAFE_')) return 'safe';
  if (assetId.startsWith('SPEED_')) return 'speed';
  if (assetId.startsWith('SPECIAL_')) return 'special';
  if (assetId.startsWith('MARKING_')) return 'marking';
  return 'generic';
}

function tokens(text) {
  return text
    .toLowerCase()
    .replace(/[^a-z0-9\-_\s]/g, ' ')
    .split(/\s+/)
    .filter(Boolean);
}

function inferMutcdCode(assetId) {
  const m = assetId.match(/^MUTCD_([^_]+)/);
  return m ? m[1] : null;
}

function readableName(assetId) {
  return assetId
    .replace(/^MUTCD_/, '')
    .replace(/_/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

function buildQuery(entry) {
  const a = entry.assetId;
  const c = category(a);
  const name = readableName(a);
  const code = inferMutcdCode(a);

  if (c === 'mutcd' && code) {
    return `${code} MUTCD traffic sign diagram`;
  }
  if (c === 'marking' && a.includes('HAND_SIGNAL')) {
    return `driver hand signal ${name.toLowerCase()} bicycle hand signals`;
  }
  if (c === 'intersection') {
    return `${name.toLowerCase()} driving intersection diagram`;
  }
  if (c === 'pavement') {
    return `${name.toLowerCase()} pavement marking MUTCD`;
  }
  if (c === 'signal') {
    return `${name.toLowerCase()} traffic signal indication`;
  }
  if (c === 'speed') {
    return `${name.toLowerCase()} safe driving distance diagram`;
  }
  if (c === 'special') {
    return `${name.toLowerCase()} driving rule diagram`;
  }
  if (c === 'parking') {
    return `${name.toLowerCase()} parking rule diagram`;
  }
  if (c === 'safe') {
    return `${name.toLowerCase()} driver safety diagram`;
  }
  return `${name.toLowerCase()} traffic sign diagram`;
}

function unwrapDuckUrl(href) {
  if (!href) return null;
  const normalized = href.startsWith('//') ? `https:${href}` : href;
  try {
    const u = new URL(normalized);
    if (u.hostname.includes('duckduckgo.com') && u.pathname === '/l/') {
      const uddg = u.searchParams.get('uddg');
      if (uddg) return decodeURIComponent(uddg);
    }
    return normalized;
  } catch {
    return null;
  }
}

function parseDuckResults(html) {
  const out = [];
  const regexes = [
    /<a[^>]*href="([^"]+)"[^>]*class="[^"]*result__a[^"]*"[^>]*>([\s\S]*?)<\/a>/gi,
    /<a[^>]*class="[^"]*result__a[^"]*"[^>]*href="([^"]+)"[^>]*>([\s\S]*?)<\/a>/gi,
    /<a[^>]*href='([^']+)'[^>]*class='[^']*result__a[^']*'[^>]*>([\s\S]*?)<\/a>/gi,
    /<a[^>]*class='[^']*result__a[^']*'[^>]*href='([^']+)'[^>]*>([\s\S]*?)<\/a>/gi,
  ];

  for (const re of regexes) {
    let m;
    while ((m = re.exec(html))) {
      const rawHref = m[1];
      const title = m[2].replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim();
      const url = unwrapDuckUrl(rawHref);
      if (!url || !title) continue;
      out.push({ title, url });
      if (out.length >= MAX_RESULTS) return dedupeResults(out);
    }
  }

  // Fallback for alternate DDG result layout.
  const h2Re = /<h2[^>]*class="[^"]*result__title[^"]*"[^>]*>[\s\S]*?<a[^>]*href="([^"]+)"[^>]*>([\s\S]*?)<\/a>[\s\S]*?<\/h2>/gi;
  let m;
  while ((m = h2Re.exec(html))) {
    const rawHref = m[1];
    const title = m[2].replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim();
    const url = unwrapDuckUrl(rawHref);
    if (!url || !title) continue;
    out.push({ title, url });
    if (out.length >= MAX_RESULTS) break;
  }

  return dedupeResults(out).slice(0, MAX_RESULTS);
}

function dedupeResults(items) {
  const seen = new Set();
  const out = [];
  for (const it of items) {
    const key = `${it.title}::${it.url}`;
    if (seen.has(key)) continue;
    seen.add(key);
    out.push(it);
  }
  return out;
}

function domainOf(url) {
  try {
    return new URL(url).hostname.toLowerCase().replace(/^www\./, '');
  } catch {
    return '';
  }
}

const DOMAIN_WEIGHT = [
  { suffix: 'mutcd.fhwa.dot.gov', weight: 5 },
  { suffix: 'ops.fhwa.dot.gov', weight: 4 },
  { suffix: 'fhwa.dot.gov', weight: 4 },
  { suffix: 'transportation.gov', weight: 3 },
  { suffix: 'txdot.gov', weight: 4 },
  { suffix: 'dps.texas.gov', weight: 4 },
  { suffix: 'roadtrafficsigns.com', weight: 3 },
  { suffix: 'driving-tests.org', weight: 2 },
  { suffix: 'wikipedia.org', weight: 1 },
  { suffix: 'wikimedia.org', weight: 1 },
  { suffix: 'youtube.com', weight: 1 },
];

function domainWeight(domain) {
  for (const rule of DOMAIN_WEIGHT) {
    if (domain === rule.suffix || domain.endsWith(`.${rule.suffix}`)) return rule.weight;
  }
  return 0;
}

function scoreResult(result, entry, query) {
  const title = result.title.toLowerCase();
  const url = result.url.toLowerCase();
  const domain = domainOf(result.url);
  const a = entry.assetId.toLowerCase();
  const code = inferMutcdCode(entry.assetId)?.toLowerCase();
  const qTokens = new Set(tokens(query));
  const nameTokens = tokens(readableName(entry.assetId));

  let score = 0;
  score += domainWeight(domain);

  if (code && (title.includes(code) || url.includes(code))) score += 5;
  if (a.includes('stop') && (title.includes('stop') || url.includes('stop'))) score += 2;
  if (a.includes('yield') && (title.includes('yield') || url.includes('yield'))) score += 2;

  let overlap = 0;
  for (const t of nameTokens) {
    if (t.length < 3) continue;
    if (title.includes(t) || url.includes(t)) overlap += 1;
  }
  score += Math.min(overlap, 6);

  let queryOverlap = 0;
  for (const t of qTokens) {
    if (t.length < 4) continue;
    if (title.includes(t) || url.includes(t)) queryOverlap += 1;
  }
  score += Math.min(queryOverlap, 4);

  if (url.includes('pdf')) score += 1;
  if (url.includes('mutcd')) score += 2;
  if (title.includes('error') || title.includes('404')) score -= 5;

  return score;
}

function chooseBest(results, entry, query) {
  if (!results.length) return null;
  const scored = results.map((r) => ({ ...r, score: scoreResult(r, entry, query) }));
  scored.sort((a, b) => b.score - a.score);
  return scored[0];
}

function decision(best) {
  if (!best) return 'skip';
  return best.score >= 7 ? 'keep' : 'skip';
}

function promptAdditions(entry) {
  const a = entry.assetId;
  const c = category(a);
  const adds = [];

  if (c === 'mutcd') {
    adds.push('Use official MUTCD code geometry proportions for this exact code.');
    adds.push('Set sign footprint to 76-84% of canvas width and center exactly in viewBox.');
    adds.push('Constrain border thickness and inset to consistent ratio (outer border > inner keyline).');
    adds.push('Keep all legend text inside safe area with at least 8 units side padding.');
  }

  if (a.includes('R1-1_STOP')) {
    adds.push('STOP legend: wide uppercase letters, centered, total text width about 62-70% of octagon width.');
  }
  if (a.includes('R1-2_YIELD')) {
    adds.push('Yield triangle must be apex-down with balanced red border and centered YIELD legend.');
  }
  if (a.includes('I-5_INTERSTATE')) {
    adds.push('Interstate shield: red top banner with full INTERSTATE word centered and not cropped.');
  }
  if (a.includes('M1-') || a.includes('US_ROUTE') || a.includes('STATE_ROUTE')) {
    adds.push('Route marker numerals must fill 45-55% of marker height and remain optically centered.');
  }
  if (a.includes('OM1-1') || a.includes('OM2-1')) {
    adds.push('Object marker arrow: strong vertical stem with directional head; avoid blob-like top.');
  }
  if (a.includes('R3-') || a.includes('NO_LEFT_TURN') || a.includes('NO_RIGHT_TURN') || a.includes('NO_U_TURN')) {
    adds.push('Prohibition signs: red circle + 45-degree slash must intersect black symbol cleanly.');
  }
  if (a.includes('R2-1_SPEED_LIMIT')) {
    adds.push('Speed limit layout: SPEED LIMIT top stack, horizontal separator, large numeral block centered.');
  }
  if (a.includes('R8-3a_NO_PARKING_ANYTIME')) {
    adds.push('NO PARKING ANYTIME text must fit without overlap; reduce font size before touching border.');
  }
  if (a.includes('S4-3_SCHOOL_SPEED_LIMIT_20')) {
    adds.push('Bottom legend WHEN CHILDREN PRESENT must remain fully readable at 128px preview.');
  }

  if (c === 'pavement') {
    adds.push('Use lane-centered markings with accurate line type (solid, dashed, double, shark teeth).');
    adds.push('Text markings must be road-aligned and not exceed 70% lane width.');
  }
  if (a.includes('PAVEMENT_GORE_AREA')) {
    adds.push('Gore area hatch must taper correctly and include explicit no-drive area boundary.');
  }
  if (a.includes('PAVEMENT_SHARED_CENTER_TURN_LANE')) {
    adds.push('Center two-way left-turn lane needs opposing arrows in dashed yellow envelope.');
  }
  if (a.includes('PAVEMENT_RAILROAD_CROSSING_X')) {
    adds.push('Railroad X and RR letters must follow MUTCD pavement lettering spacing.');
  }
  if (a.includes('PAVEMENT_SCHOOL_ZONE')) {
    adds.push('School zone text should be horizontally centered, stacked, and sized for mobile legibility.');
  }

  if (c === 'intersection') {
    adds.push('Top-down intersection diagrams: lane lines and control devices must be semantically unambiguous.');
    adds.push('Vehicle scale should be consistent across intersection assets (about 14-18% lane span).');
  }
  if (a.includes('INTERSECTION_SCHOOL_BUS_STOPPED')) {
    adds.push('Stopped school bus should show active red stop arm and implied no-passing context.');
  }
  if (a.includes('INTERSECTION_ROUNDABOUT')) {
    adds.push('Roundabout arrows should be evenly spaced and tangent to circular travel path.');
  }
  if (a.includes('INTERSECTION_SIGNAL_PROTECTED_LEFT')) {
    adds.push('Protected-left example must clearly show green arrow phase vs opposing traffic control.');
  }

  if (c === 'signal') {
    adds.push('Traffic signal heads should use standard light order and clear active indication emphasis.');
    adds.push('Pedestrian module text/icons must not clip and must remain legible at 128px.');
  }
  if (a.includes('SIGNAL_PED_WALK')) {
    adds.push('WALK word must fit inside white panel with uniform margins.');
  }
  if (a.includes('SIGNAL_PED_DONT_WALK')) {
    adds.push('DON’T WALK panel text should use apostrophe and balanced two-line arrangement.');
  }
  if (a.includes('SIGNAL_PED_COUNTDOWN')) {
    adds.push('Countdown numeral should dominate upper panel and remain centered.');
  }

  if (c === 'marking') {
    adds.push('Hand signals must originate from driver side window with anatomically clear arm angles.');
  }

  if (c === 'speed') {
    adds.push('Educational speed diagrams should keep one teaching point per frame and avoid text crowding.');
  }
  if (a.includes('SPEED_STOPPING_DISTANCE')) {
    adds.push('Reaction vs braking segments must be separated with clear labels and non-overlapping text.');
  }
  if (a.includes('SPEED_PASSING_CLEARANCE')) {
    adds.push('Passing clearance panel should show both vehicles and required safe gap dimension clearly.');
  }

  if (c === 'safe' || c === 'special' || c === 'parking') {
    adds.push('Use consistent icon stroke family and avoid decorative elements that reduce instructional clarity.');
    adds.push('Keep all instructional text within safe margins and center labels to prevent truncation.');
  }

  if (!adds.length) {
    adds.push('Match official/educational reference geometry and preserve mobile readability.');
  }

  return [...new Set(adds)];
}

async function searchDuck(query) {
  const url = `https://duckduckgo.com/html/?q=${encodeURIComponent(query)}`;
  const res = await fetch(url, {
    headers: { 'User-Agent': 'Mozilla/5.0 (Codex SVG audit)' },
  });
  if (!res.ok) throw new Error(`DuckDuckGo HTTP ${res.status}`);
  return res.text();
}

async function fetchJson(url) {
  const res = await fetch(url, {
    headers: { 'User-Agent': 'Mozilla/5.0 (Codex SVG audit)' },
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

async function findMutcdReference(code) {
  const titles = [
    `File:MUTCD_${code}.svg`,
    `File:MUTCD ${code}.svg`,
    `File:US_road_sign_${code}.svg`,
    `File:US road sign ${code}.svg`,
  ];

  for (const title of titles) {
    const endpoints = [
      'https://commons.wikimedia.org/w/api.php',
      'https://en.wikipedia.org/w/api.php',
    ];

    for (const endpoint of endpoints) {
      try {
        const url = `${endpoint}?action=query&titles=${encodeURIComponent(title)}&prop=imageinfo&iiprop=url&format=json`;
        const js = await fetchJson(url);
        const pages = js?.query?.pages || {};
        const page = Object.values(pages)[0];
        const img = page?.imageinfo?.[0]?.url;
        if (img) {
          return { title, url: img };
        }
      } catch {
        // Try next variant.
      }
    }
  }

  return {
    title: `MUTCD ${code} reference bundle`,
    url: 'https://mutcd.fhwa.dot.gov/htm/2009r1r2/part2/part2a.htm',
  };
}

function fallbackReference(entry) {
  const a = entry.assetId;
  const c = category(a);

  if (a === 'SPECIAL_WORK_ZONE_FLAGGER') {
    return {
      title: 'MUTCD Part 6F Temporary Traffic Control Zone Devices',
      url: 'https://mutcd.fhwa.dot.gov/htm/2009/part6/part6f.htm',
    };
  }

  if (a.includes('RAILROAD') || a === 'SPECIAL_RAILROAD_STOP_PROCEDURE') {
    return {
      title: 'MUTCD Part 8B Signs and Markings at Highway-Rail Grade Crossings',
      url: 'https://mutcd.fhwa.dot.gov/htm/2009/part8/part8b.htm',
    };
  }

  if (c === 'pavement') {
    return {
      title: 'MUTCD Part 3B Pavement and Curb Markings',
      url: 'https://mutcd.fhwa.dot.gov/htm/2009/part3/part3b.htm',
    };
  }

  if (c === 'signal') {
    if (a.includes('PED_')) {
      return {
        title: 'MUTCD Part 4E Pedestrian Control Features',
        url: 'https://mutcd.fhwa.dot.gov/htm/2009/part4/part4e.htm',
      };
    }
    return {
      title: 'MUTCD Part 4D Highway Traffic Signals',
      url: 'https://mutcd.fhwa.dot.gov/htm/2009/part4/part4d.htm',
    };
  }

  if (c === 'intersection' || c === 'marking' || c === 'parking' || c === 'safe' || c === 'speed' || c === 'special') {
    return {
      title: 'Texas Driver Handbook (DL-7) official reference',
      url: 'https://www.dps.texas.gov/internetforms/Forms/DL-7.pdf',
    };
  }

  return null;
}

function summarizeReason(best, entry) {
  if (!best) return 'No relevant web result found for this asset query.';
  const code = inferMutcdCode(entry.assetId);
  const domain = domainOf(best.url);
  const bits = [];
  bits.push(`Top result score ${best.score}.`);
  if (code && (best.title.toLowerCase().includes(code.toLowerCase()) || best.url.toLowerCase().includes(code.toLowerCase()))) {
    bits.push(`Contains code ${code}.`);
  }
  bits.push(`Domain: ${domain}.`);
  return bits.join(' ');
}

function mdEscape(text) {
  return String(text).replace(/\|/g, '\\|').replace(/\n/g, '<br>');
}

function buildMarkdown(rows) {
  const lines = [];
  const keep = rows.filter((r) => r.decision === 'keep').length;
  const skip = rows.length - keep;

  lines.push('# Reference vs Prompt Review');
  lines.push('');
  lines.push(`Date: ${TODAY}`);
  lines.push(`Total assets reviewed: ${rows.length}`);
  lines.push(`Keep references: ${keep}`);
  lines.push(`Skip (no strong match): ${skip}`);
  lines.push('');
  lines.push('This file is used to refine `promptSpec.prompt` per asset before regeneration.');
  lines.push('');
  lines.push('| Asset | Query | Best Reference | Decision | Why | Prompt Additions |');
  lines.push('|---|---|---|---|---|---|');

  for (const r of rows) {
    const ref = r.bestReferenceUrl ? `[${mdEscape(r.bestReferenceTitle)}](${r.bestReferenceUrl})` : '—';
    const adds = r.promptAdditions.map((x) => `- ${mdEscape(x)}`).join('<br>');
    lines.push(
      `| ${mdEscape(r.assetId)} | ${mdEscape(r.query)} | ${ref} | ${r.decision.toUpperCase()} | ${mdEscape(r.reason)} | ${adds} |`
    );
  }

  lines.push('');
  lines.push('## Next Step');
  lines.push('- For rows marked `KEEP`, merge `Prompt Additions` into that asset `promptSpec.prompt`.');
  lines.push('- For rows marked `SKIP`, re-search manually with narrower query or use official state manual source.');
  lines.push('');

  return `${lines.join('\n')}\n`;
}

async function main() {
  ensureDir(REVIEW_DIR);

  const manifest = readJson(MANIFEST_PATH, []);
  if (!Array.isArray(manifest) || !manifest.length) {
    throw new Error('assets/manifest.json is empty or invalid');
  }

  const cache = readJson(CACHE_PATH, {});
  const rows = [];

  for (let i = 0; i < manifest.length; i += 1) {
    const entry = manifest[i];
    const query = buildQuery(entry);
    const cacheKey = `${entry.assetId}::${query}`;

    let results = [];
    try {
      if (cache[cacheKey]?.results?.length) {
        results = cache[cacheKey].results;
      } else {
        const html = await searchDuck(query);
        results = parseDuckResults(html);
        cache[cacheKey] = { checkedAt: TODAY, results };
        writeJson(CACHE_PATH, cache);
        await sleep(REQUEST_DELAY_MS);
      }
    } catch (err) {
      results = [];
      cache[cacheKey] = { checkedAt: TODAY, error: String(err) };
      writeJson(CACHE_PATH, cache);
    }

    let best = chooseBest(results, entry, query);

    if (category(entry.assetId) === 'mutcd' && (!best || best.score < 7)) {
      const code = inferMutcdCode(entry.assetId);
      if (code) {
        const ref = await findMutcdReference(code);
        best = {
          title: ref.title,
          url: ref.url,
          score: 9,
        };
      }
    }

    if (!best || best.score < 7) {
      const fallback = fallbackReference(entry);
      if (fallback) {
        best = {
          title: fallback.title,
          url: fallback.url,
          score: 8,
        };
      }
    }
    const row = {
      assetId: entry.assetId,
      category: category(entry.assetId),
      query,
      bestReferenceTitle: best?.title || null,
      bestReferenceUrl: best?.url || null,
      bestReferenceScore: best?.score ?? null,
      decision: decision(best),
      reason: summarizeReason(best, entry),
      promptAdditions: promptAdditions(entry),
    };
    rows.push(row);

    process.stdout.write(
      `[${String(i + 1).padStart(3, '0')}/${manifest.length}] ${entry.assetId} -> ${row.decision.toUpperCase()}${best ? ` (${best.score})` : ''}\n`
    );
  }

  writeJson(OUT_JSON, {
    generatedAt: TODAY,
    total: rows.length,
    keep: rows.filter((r) => r.decision === 'keep').length,
    skip: rows.filter((r) => r.decision === 'skip').length,
    rows,
  });

  fs.writeFileSync(OUT_MD, buildMarkdown(rows), 'utf8');

  console.log(`\nSaved JSON: ${path.relative(ROOT, OUT_JSON)}`);
  console.log(`Saved MD:   ${path.relative(ROOT, OUT_MD)}`);
}

main().catch((err) => {
  console.error(err.stack || err.message || String(err));
  process.exit(1);
});
