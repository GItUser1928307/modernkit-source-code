#!/usr/bin/env node
'use strict';

const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

const ROOT = path.resolve(__dirname, '..');
const PROPS = path.join(ROOT, 'gradle.properties');
const DIST = path.join(ROOT, 'dist');
const LIBS = path.join(ROOT, 'build', 'libs');
const LOG = path.join(ROOT, 'gradle-failed-build.log');

function escapeRe(s) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function getProp(text, key) {
  const m = text.match(new RegExp('^' + key + '\\s*=\\s*(.+?)\\s*$', 'm'));
  return m ? m[1] : null;
}

function setProp(text, key, value) {
  return text.replace(new RegExp('^(' + key + '\\s*=).*$', 'm'), (m, p1) => p1 + value);
}

function compare(a, b) {
  for (let i = 0; i < 3; i++) {
    if (a[i] !== b[i]) return a[i] - b[i];
  }
  return 0;
}

// Highest version among the jars currently in dist/, or null if none.
function previousVersion(base) {
  if (!fs.existsSync(DIST)) return null;
  const re = new RegExp('^' + escapeRe(base) + '-(\\d+)\\.(\\d+)\\.(\\d+)\\.jar$');
  let best = null;
  for (const file of fs.readdirSync(DIST)) {
    const m = file.match(re);
    if (!m) continue;
    const v = [Number(m[1]), Number(m[2]), Number(m[3])];
    if (!best || compare(v, best) > 0) best = v;
  }
  return best;
}

function main() {
  const original = fs.readFileSync(PROPS, 'utf8');
  const base = getProp(original, 'archives_base_name') || 'modernkit';
  const currentVersion = getProp(original, 'mod_version') || '1.0.0';

  let prev = previousVersion(base);
  let source = 'dist/';
  if (!prev) {
    prev = currentVersion.split('.').map(Number);
    source = 'gradle.properties';
  }

  const next = prev[0] + '.' + (prev[1] + 1) + '.0';
  console.log('Previous version (' + source + '): ' + prev.join('.'));
  console.log('New version: ' + next);

  fs.writeFileSync(PROPS, setProp(original, 'mod_version', next));

  let ok = false;
  let output = '';

  try {
    const isWin = process.platform === 'win32';
    const gradlew = isWin ? 'gradlew.bat' : './gradlew';
    console.log('Running ' + gradlew + ' clean build ...');

    const res = spawnSync(gradlew, ['clean', 'build', '--console=plain'], {
      cwd: ROOT,
      encoding: 'utf8',
      shell: isWin,
      maxBuffer: 1024 * 1024 * 100
    });

    output = (res.stdout || '') + (res.stderr || '');
    if (res.error) output += '\n' + String(res.error);
    ok = res.status === 0 && !res.error;

    if (ok) {
      const jar = path.join(LIBS, base + '-' + next + '.jar');
      if (!fs.existsSync(jar)) {
        ok = false;
        output += '\nBuild reported success but the jar was not found: ' + jar + '\n';
      } else {
        fs.mkdirSync(DIST, { recursive: true });
        for (const file of fs.readdirSync(DIST)) {
          if (file.endsWith('.jar')) fs.unlinkSync(path.join(DIST, file));
        }
        fs.copyFileSync(jar, path.join(DIST, path.basename(jar)));
        if (fs.existsSync(LOG)) fs.unlinkSync(LOG);
        console.log('\nBUILD SUCCESSFUL');
        console.log('dist/' + path.basename(jar) + ' is the new jar (old jars removed).');
      }
    }
  } finally {
    // A failed build must not leave a bumped version behind.
    if (!ok) fs.writeFileSync(PROPS, original);
  }

  if (!ok) {
    const header =
      'ModernKit build failed\n' +
      'Time: ' + new Date().toISOString() + '\n' +
      'Attempted version: ' + next + ' (gradle.properties restored to ' + currentVersion + ')\n' +
      '==============================\n\n';
    fs.writeFileSync(LOG, header + output);

    const lines = output.split('\n');
    const errors = lines.filter((l) => /error|FAILED|What went wrong/i.test(l));
    console.error('\nBUILD FAILED');
    console.error((errors.length ? errors : lines).slice(-20).join('\n'));
    console.error('\nFull output saved to gradle-failed-build.log');
    process.exit(1);
  }
}

main();
