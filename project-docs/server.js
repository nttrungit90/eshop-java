const http = require('http');
const fs = require('fs');
const path = require('path');
const yaml = require('js-yaml');

const PORT = process.env.PORT || 3333;
const DATA_DIR = process.env.DATA_DIR || path.join(__dirname, 'data');

// ─── Data Loaders ────────────────────────────────────────────────────────────

function loadFile(name) {
  const p = path.join(DATA_DIR, name);
  return fs.existsSync(p) ? fs.readFileSync(p, 'utf8') : '';
}

function loadTasks() {
  const raw = loadFile('tasks.yaml');
  return raw ? yaml.load(raw) : { phases: [] };
}

function loadAllDocs() {
  // Load all .md files from data/ as extra doc pages
  const docs = [];
  if (!fs.existsSync(DATA_DIR)) return docs;
  const files = fs.readdirSync(DATA_DIR).filter(f => f.endsWith('.md')).sort();
  for (const file of files) {
    const content = fs.readFileSync(path.join(DATA_DIR, file), 'utf8');
    // Extract title from first # heading or filename
    const titleMatch = content.match(/^#\s+(.+)$/m);
    const title = titleMatch ? titleMatch[1] : file.replace('.md', '');
    const id = file.replace('.md', '').replace(/[^a-z0-9]/gi, '-');
    docs.push({ id, title, file, content });
  }
  return docs;
}

// ─── HTML Helpers ────────────────────────────────────────────────────────────

function escHtml(s) {
  return String(s || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function mdToHtml(md) {
  let html = escHtml(md);

  // Code blocks
  html = html.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) =>
    `<pre><code>${code.trim()}</code></pre>`);

  // Tables
  html = html.replace(/^(\|.+\|)\n(\|[-| :]+\|)\n((?:\|.+\|\n?)+)/gm, (_, header, sep, body) => {
    const headers = header.split('|').filter(c => c.trim()).map(c => `<th>${c.trim()}</th>`).join('');
    const rows = body.trim().split('\n').map(row => {
      const cells = row.split('|').filter(c => c.trim()).map(c => `<td>${c.trim()}</td>`).join('');
      return `<tr>${cells}</tr>`;
    }).join('');
    return `<table><thead><tr>${headers}</tr></thead><tbody>${rows}</tbody></table>`;
  });

  html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/^#### (.+)$/gm, '<h4>$1</h4>');
  html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>');
  html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>');
  html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>');
  html = html.replace(/^---$/gm, '<hr>');
  html = html.replace(/^- \[x\] (.+)$/gm, '<li class="cb done">$1</li>');
  html = html.replace(/^- \[ \] (.+)$/gm, '<li class="cb">$1</li>');
  html = html.replace(/^- (.+)$/gm, '<li>$1</li>');
  html = html.replace(/^\d+\. (.+)$/gm, '<li>$1</li>');
  html = html.replace(/((?:<li[^>]*>.*<\/li>\n?)+)/g, '<ul>$1</ul>');

  html = html.split('\n').map(line => {
    const t = line.trim();
    if (!t) return '';
    if (t.startsWith('<')) return line;
    return `<p>${line}</p>`;
  }).join('\n');

  return html;
}

// ─── Dashboard Builder ───────────────────────────────────────────────────────

function buildDashboardTab(data, progressMd) {
  const phases = data.phases;
  // Extract task ID from the "## Next Task" section specifically
  const nextSection = progressMd.split(/^## Next Task/m)[1] || '';
  const nextMatch = nextSection.match(/\*\*([\w]+-[\w]+)\*\*/);
  const nextTaskId = nextMatch ? nextMatch[1] : null;

  let totalTasks = 0, doneTasks = 0;
  phases.forEach(p => p.tasks.forEach(t => {
    totalTasks++;
    if (t.status === 'done') doneTasks++;
  }));
  const pct = totalTasks > 0 ? Math.round((doneTasks / totalTasks) * 100) : 0;

  let nextTask = null;
  if (nextTaskId) {
    for (const phase of phases) {
      for (const task of phase.tasks) {
        if (task.id === nextTaskId) {
          nextTask = { ...task, phaseName: phase.name };
          break;
        }
      }
      if (nextTask) break;
    }
  }

  let phasesHtml = '';
  phases.forEach(phase => {
    const pDone = phase.tasks.filter(t => t.status === 'done').length;
    const pTotal = phase.tasks.length;
    const isOpen = phase.status === 'in_progress';
    const badgeLabel = phase.status === 'in_progress' ? 'In Progress'
                     : phase.status === 'done' ? 'Done' : 'Pending';

    let tasksHtml = '';
    phase.tasks.forEach(task => {
      const icon = task.status === 'done' ? '✓' : task.status === 'in_progress' ? '●' : '○';
      const files = [...(task.java_files || []), ...(task.dotnet_files || [])];
      const isNext = task.id === nextTaskId;

      let desc = '';
      if (task.description && task.status !== 'done') {
        desc = `<div class="task-desc">${escHtml(task.description.trim().split('\n')[0])}</div>`;
      }

      let fileTags = '';
      if (files.length > 0 && task.status !== 'done') {
        fileTags = '<div class="files">' +
          files.map(f => `<span class="file-tag">${escHtml(f.split('/').pop())}</span>`).join('') +
          '</div>';
      }

      tasksHtml += `
        <div class="task ${task.status}${isNext ? ' next' : ''}">
          <span class="icon ${task.status}">${icon}</span>
          <div class="task-content">
            <div class="task-title">${escHtml(task.name)}</div>
            <div class="task-meta">${escHtml(task.id)}</div>
            ${desc}${fileTags}
          </div>
        </div>`;
    });

    phasesHtml += `
      <div class="phase${isOpen ? ' open' : ''}">
        <div class="phase-header" onclick="this.parentElement.classList.toggle('open')">
          <span class="chevron">▶</span>
          <span class="phase-name">${escHtml(phase.name)}</span>
          ${phase.description ? `<span class="phase-desc">${escHtml(phase.description)}</span>` : ''}
          <span class="phase-badge badge-${phase.status}">${badgeLabel}</span>
          <span class="phase-count">${pDone}/${pTotal}</span>
        </div>
        <div class="phase-tasks">${tasksHtml}</div>
      </div>`;
  });

  let nextTaskHtml = '';
  if (nextTask) {
    nextTaskHtml = `
      <div class="current-task">
        <div class="arrow">▶</div>
        <div class="info">
          <div class="label">Next Task</div>
          <div class="task-name">${escHtml(nextTask.name)}</div>
          <div class="task-id">${escHtml(nextTask.id)} · ${escHtml(nextTask.phaseName)}</div>
        </div>
      </div>`;
  }

  return `
    <div class="stats">
      <div class="stat-card"><div class="label">Progress</div><div class="value">${pct}%</div></div>
      <div class="stat-card"><div class="label">Completed</div><div class="value green">${doneTasks}</div></div>
      <div class="stat-card"><div class="label">Remaining</div><div class="value orange">${totalTasks - doneTasks}</div></div>
      <div class="stat-card"><div class="label">Total Tasks</div><div class="value">${totalTasks}</div></div>
    </div>
    <div class="progress-bar-container">
      <div class="progress-bar-header">
        <span>Overall Progress</span>
        <strong>${doneTasks} / ${totalTasks} tasks</strong>
      </div>
      <div class="progress-bar"><div class="fill" style="width: ${pct}%"></div></div>
    </div>
    ${nextTaskHtml}
    ${phasesHtml}`;
}

// ─── Page Builder ────────────────────────────────────────────────────────────

function buildPage() {
  const data = loadTasks();
  const progressMd = loadFile('migration-progress.md');
  const docs = loadAllDocs();

  // Build tabs: Dashboard first, then all .md files from data/
  const tabs = [{ id: 'dashboard', title: 'Dashboard' }];
  docs.forEach(d => tabs.push({ id: d.id, title: d.title }));

  const tabButtons = tabs.map((t, i) =>
    `<button class="tab${i === 0 ? ' active' : ''}" onclick="switchTab('${t.id}')">${escHtml(t.title)}</button>`
  ).join('\n    ');

  const dashboardContent = buildDashboardTab(data, progressMd);

  const docTabs = docs.map(d =>
    `<div id="tab-${d.id}" class="tab-content"><div class="md-content">${mdToHtml(d.content)}</div></div>`
  ).join('\n  ');

  return `<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>eShop Project Docs</title>
<style>
  :root {
    --bg: #0f1117;
    --surface: #1a1d27;
    --surface2: #232733;
    --border: #2e3345;
    --text: #e1e4ed;
    --text-muted: #8b8fa3;
    --green: #34d399;
    --green-bg: rgba(52, 211, 153, 0.1);
    --blue: #60a5fa;
    --blue-bg: rgba(96, 165, 250, 0.1);
    --gray: #4b5069;
    --gray-bg: rgba(75, 80, 105, 0.1);
    --orange: #fb923c;
    --orange-bg: rgba(251, 146, 60, 0.1);
    --radius: 8px;
  }
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif;
    background: var(--bg);
    color: var(--text);
    line-height: 1.6;
  }
  .container { max-width: 1000px; margin: 0 auto; padding: 2rem; }
  header { margin-bottom: 2rem; }
  header h1 { font-size: 1.5rem; font-weight: 600; margin-bottom: 0.25rem; }
  header p { color: var(--text-muted); font-size: 0.875rem; }

  /* Tabs */
  .tabs {
    display: flex;
    gap: 0;
    border-bottom: 1px solid var(--border);
    margin-bottom: 2rem;
    overflow-x: auto;
  }
  .tab {
    padding: 0.75rem 1.25rem;
    font-size: 0.875rem;
    color: var(--text-muted);
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: all 0.15s;
    user-select: none;
    background: none;
    border-top: none; border-left: none; border-right: none;
    font-family: inherit;
    white-space: nowrap;
  }
  .tab:hover { color: var(--text); }
  .tab.active { color: var(--blue); border-bottom-color: var(--blue); }
  .tab-content { display: none; }
  .tab-content.active { display: block; }

  /* Stats */
  .stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 2rem; }
  .stat-card {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    padding: 1rem;
  }
  .stat-card .label { font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.25rem; }
  .stat-card .value { font-size: 1.5rem; font-weight: 700; }
  .stat-card .value.green { color: var(--green); }
  .stat-card .value.orange { color: var(--orange); }

  /* Progress bar */
  .progress-bar-container {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    padding: 1.25rem;
    margin-bottom: 2rem;
  }
  .progress-bar-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
  .progress-bar-header span { font-size: 0.875rem; color: var(--text-muted); }
  .progress-bar-header strong { font-size: 0.875rem; }
  .progress-bar { height: 8px; background: var(--surface2); border-radius: 4px; overflow: hidden; }
  .progress-bar .fill { height: 100%; background: linear-gradient(90deg, var(--green), var(--blue)); border-radius: 4px; }

  /* Current task */
  .current-task {
    background: var(--blue-bg);
    border: 1px solid rgba(96, 165, 250, 0.3);
    border-radius: var(--radius);
    padding: 1rem 1.25rem;
    margin-bottom: 2rem;
    display: flex; align-items: flex-start; gap: 0.75rem;
  }
  .current-task .arrow { color: var(--blue); font-size: 1.25rem; line-height: 1.4; }
  .current-task .info .label { font-size: 0.75rem; color: var(--blue); text-transform: uppercase; letter-spacing: 0.05em; }
  .current-task .info .task-name { font-weight: 600; }
  .current-task .info .task-id { color: var(--text-muted); font-size: 0.8125rem; }

  /* Phases */
  .phase { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); margin-bottom: 0.75rem; overflow: hidden; }
  .phase-header {
    padding: 1rem 1.25rem; cursor: pointer;
    display: flex; align-items: center; gap: 0.75rem;
    user-select: none; transition: background 0.15s;
  }
  .phase-header:hover { background: var(--surface2); }
  .phase-header .chevron { color: var(--text-muted); font-size: 0.75rem; transition: transform 0.2s; width: 1rem; text-align: center; }
  .phase.open .phase-header .chevron { transform: rotate(90deg); }
  .phase-header .phase-name { font-weight: 600; }
  .phase-header .phase-desc { color: var(--text-muted); font-size: 0.8125rem; flex: 1; }
  .phase-header .phase-badge { font-size: 0.75rem; padding: 0.125rem 0.5rem; border-radius: 999px; font-weight: 500; }
  .badge-done { background: var(--green-bg); color: var(--green); }
  .badge-in_progress { background: var(--blue-bg); color: var(--blue); }
  .badge-pending { background: var(--gray-bg); color: var(--text-muted); }
  .phase-header .phase-count { font-size: 0.8125rem; color: var(--text-muted); }
  .phase-tasks { display: none; border-top: 1px solid var(--border); }
  .phase.open .phase-tasks { display: block; }

  /* Tasks */
  .task {
    padding: 0.75rem 1.25rem 0.75rem 2.75rem;
    display: flex; align-items: flex-start; gap: 0.625rem;
    border-bottom: 1px solid var(--border);
  }
  .task:last-child { border-bottom: none; }
  .task.next { background: var(--blue-bg); }
  .task .icon { font-size: 0.875rem; margin-top: 2px; flex-shrink: 0; }
  .task .icon.done { color: var(--green); }
  .task .icon.in_progress { color: var(--blue); }
  .task .icon.pending { color: var(--gray); }
  .task .task-content { flex: 1; min-width: 0; }
  .task .task-title { font-size: 0.875rem; font-weight: 500; }
  .task.done .task-title { color: var(--text-muted); text-decoration: line-through; text-decoration-color: var(--gray); }
  .task .task-meta { font-size: 0.75rem; color: var(--text-muted); margin-top: 0.125rem; }
  .task .task-desc { font-size: 0.8125rem; color: var(--text-muted); margin-top: 0.375rem; }
  .task .files { margin-top: 0.375rem; display: flex; flex-wrap: wrap; gap: 0.25rem; }
  .task .file-tag {
    font-size: 0.6875rem;
    background: var(--surface2); border: 1px solid var(--border);
    border-radius: 4px; padding: 0.0625rem 0.375rem;
    color: var(--text-muted);
    font-family: 'SF Mono', 'Fira Code', monospace;
  }

  /* Markdown */
  .md-content {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    padding: 1.5rem 2rem;
    font-size: 0.875rem; line-height: 1.8;
  }
  .md-content h1 { font-size: 1.375rem; margin: 1.5rem 0 0.75rem; border-bottom: 1px solid var(--border); padding-bottom: 0.5rem; }
  .md-content h1:first-child { margin-top: 0; }
  .md-content h2 { font-size: 1.125rem; margin: 1.25rem 0 0.5rem; color: var(--blue); }
  .md-content h3 { font-size: 1rem; margin: 1rem 0 0.375rem; }
  .md-content h4 { font-size: 0.9375rem; margin: 0.75rem 0 0.25rem; color: var(--text-muted); }
  .md-content p { margin: 0.5rem 0; }
  .md-content ul, .md-content ol { margin: 0.5rem 0 0.5rem 1.5rem; }
  .md-content li { margin: 0.25rem 0; }
  .md-content li.cb { list-style: none; margin-left: -1.5rem; padding-left: 0; }
  .md-content li.cb::before { content: '☐ '; color: var(--text-muted); }
  .md-content li.cb.done::before { content: '✓ '; color: var(--green); }
  .md-content li.cb.done { color: var(--text-muted); text-decoration: line-through; text-decoration-color: var(--gray); }
  .md-content code {
    background: var(--surface2); border: 1px solid var(--border);
    border-radius: 3px; padding: 0.1rem 0.35rem;
    font-size: 0.8125rem;
    font-family: 'SF Mono', 'Fira Code', monospace;
  }
  .md-content pre {
    background: var(--surface2); border: 1px solid var(--border);
    border-radius: var(--radius); padding: 1rem; margin: 0.75rem 0; overflow-x: auto;
  }
  .md-content pre code { background: none; border: none; padding: 0; font-size: 0.8125rem; color: var(--text); }
  .md-content strong { color: var(--text); }
  .md-content hr { border: none; border-top: 1px solid var(--border); margin: 1rem 0; }
  .md-content table { width: 100%; border-collapse: collapse; margin: 0.75rem 0; font-size: 0.8125rem; }
  .md-content th, .md-content td { border: 1px solid var(--border); padding: 0.5rem 0.75rem; text-align: left; }
  .md-content th { background: var(--surface2); font-weight: 600; }
  .md-content td { background: var(--surface); }

  @media (max-width: 640px) {
    .container { padding: 1rem; }
    .stats { grid-template-columns: repeat(2, 1fr); }
  }
</style>
</head>
<body>
<div class="container">
  <header>
    <h1>eShop Project Docs</h1>
    <p>.NET to Java migration — architecture, progress, and notes</p>
  </header>

  <div class="tabs">
    ${tabButtons}
  </div>

  <div id="tab-dashboard" class="tab-content active">
    ${dashboardContent}
  </div>

  ${docTabs}
</div>

<script>
function switchTab(name) {
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
  document.getElementById('tab-' + name).classList.add('active');
  event.target.classList.add('active');
}
</script>
</body>
</html>`;
}

// ─── Server ──────────────────────────────────────────────────────────────────

const server = http.createServer((req, res) => {
  if (req.url === '/' || req.url === '/index.html') {
    try {
      const html = buildPage();
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      res.end(html);
    } catch (err) {
      res.writeHead(500, { 'Content-Type': 'text/plain' });
      res.end('Error: ' + err.message + '\n' + err.stack);
    }
  } else {
    res.writeHead(404);
    res.end('Not found');
  }
});

server.listen(PORT, () => {
  console.log(`eShop Project Docs running at http://localhost:${PORT}`);
});
