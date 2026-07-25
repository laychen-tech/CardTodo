// ===== Storage =====
const STORAGE_KEY = 'cardtodo_tasks';
const THEME_KEY   = 'cardtodo_theme';

function loadTasks() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY)) || []; }
  catch { return []; }
}
function saveTasks(tasks) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(tasks));
}

const PRIORITY_ORDER = { HIGH: 0, MEDIUM: 1, LOW: 2 };
function sortTasks(tasks) {
  return [...tasks].sort((a, b) => {
    if (a.done !== b.done) return a.done ? 1 : -1;
    return (PRIORITY_ORDER[a.priority] ?? 1) - (PRIORITY_ORDER[b.priority] ?? 1);
  });
}

function genId() { return Date.now().toString(36) + Math.random().toString(36).slice(2); }

// ===== State =====
let tasks = sortTasks(loadTasks());
let currentIndex = 0;

// ===== DOM =====
const cardTrack    = document.getElementById('cardTrack');
const cardViewport = document.getElementById('cardViewport');
const counter      = document.getElementById('counter');
const emptyState   = document.getElementById('emptyState');
const fabBtn       = document.getElementById('fabBtn');
const modalOverlay = document.getElementById('modalOverlay');
const settingsOverlay = document.getElementById('settingsOverlay');
const cancelBtn    = document.getElementById('cancelBtn');
const addBtn       = document.getElementById('addBtn');
const inputTitle   = document.getElementById('inputTitle');
const inputDesc    = document.getElementById('inputDesc');
const settingsBtn  = document.getElementById('settingsBtn');
const settingsCloseBtn = document.getElementById('settingsCloseBtn');
const darkToggle   = document.getElementById('darkToggle');

// ===== Theme =====
function applyTheme(dark) {
  document.body.classList.toggle('dark', dark);
  document.body.classList.toggle('light', !dark);
  darkToggle.checked = dark;
  localStorage.setItem(THEME_KEY, dark ? 'dark' : 'light');
}
applyTheme(localStorage.getItem(THEME_KEY) === 'dark');

darkToggle.addEventListener('change', () => applyTheme(darkToggle.checked));

// ===== Priority Selector =====
let selectedPriority = 'MEDIUM';
document.querySelectorAll('.priority-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    selectedPriority = btn.dataset.p;
    document.querySelectorAll('.priority-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
  });
});

// ===== Render =====
function priorityLabel(p) {
  return { HIGH: '🔴 高优先级', MEDIUM: '🟡 中优先级', LOW: '🟢 低优先级' }[p] || '🟡 中优先级';
}
function priorityClass(p) {
  return { HIGH: 'p-high', MEDIUM: 'p-medium', LOW: 'p-low' }[p] || 'p-medium';
}
function formatDate(ts) {
  const d = new Date(ts);
  return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日`;
}

function render() {
  tasks = sortTasks(tasks);
  cardTrack.innerHTML = '';

  const isEmpty = tasks.length === 0;
  emptyState.style.display  = isEmpty ? 'flex' : 'none';
  cardViewport.style.display = isEmpty ? 'none' : 'block';
  counter.style.display      = isEmpty ? 'none' : 'block';

  if (isEmpty) { updateDots(); return; }

  if (currentIndex >= tasks.length) currentIndex = tasks.length - 1;

  // 计算 slide 宽度
  const vw = window.innerWidth;
  const slideW = Math.min(vw - 28, 480);  // max 480px
  const sideVisible = (vw - slideW) / 2;
  cardViewport.style.height = '460px';

  tasks.forEach((task, i) => {
    const slide = document.createElement('div');
    slide.className = 'card-slide' + (i !== currentIndex ? ' neighbor' : '');
    slide.style.width = slideW + 'px';

    slide.innerHTML = `
      <div class="card ${priorityClass(task.priority)}">
        <div class="card-top">
          <span class="priority-badge">${priorityLabel(task.priority)}</span>
          <span class="status-icon" data-id="${task.id}">
            ${task.done
              ? `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2A10 10 0 1 1 2 12 10 10 0 0 1 12 2zm-1.5 14.06-4.24-4.24 1.41-1.41 2.83 2.83 5.65-5.65 1.42 1.41z"/></svg>`
              : `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg>`
            }
          </span>
        </div>
        <div class="card-divider"></div>
        <div class="card-spacer"></div>
        <div class="card-title${task.done ? ' done' : ''}">${escHtml(task.title)}</div>
        <div class="card-desc">${escHtml(task.description || '暂无描述')}</div>
        <div class="card-spacer"></div>
        <div class="card-date">${formatDate(task.createdAt)}</div>
        <div class="card-actions">
          <button class="btn-outline btn-complete" data-id="${task.id}">${task.done ? '撤销' : '完成'}</button>
          <button class="btn-outline btn-delete" data-id="${task.id}">删除</button>
        </div>
      </div>`;
    cardTrack.appendChild(slide);
  });

  // 设置 track 宽度和偏移
  cardTrack.style.width = (slideW * tasks.length) + 'px';
  const offset = currentIndex * slideW - sideVisible + 14;
  cardTrack.style.transform = `translateX(${-offset}px)`;

  counter.textContent = `${currentIndex + 1} / ${tasks.length}`;
  updateDots();
  bindCardEvents();
}

function escHtml(str) {
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

// ===== Dots =====
function updateDots() {
  let dotsEl = document.getElementById('dotsEl');
  if (!dotsEl) {
    dotsEl = document.createElement('div');
    dotsEl.className = 'dots';
    dotsEl.id = 'dotsEl';
    document.querySelector('.main').appendChild(dotsEl);
  }
  dotsEl.innerHTML = '';
  tasks.forEach((_, i) => {
    const d = document.createElement('div');
    d.className = 'dot' + (i === currentIndex ? ' active' : '');
    dotsEl.appendChild(d);
  });
}

// ===== Card Events =====
function bindCardEvents() {
  document.querySelectorAll('.btn-complete').forEach(btn => {
    btn.addEventListener('click', e => {
      e.stopPropagation();
      const id = btn.dataset.id;
      tasks = tasks.map(t => t.id === id ? {...t, done: !t.done} : t);
      saveTasks(tasks);
      render();
    });
  });
  document.querySelectorAll('.btn-delete').forEach(btn => {
    btn.addEventListener('click', e => {
      e.stopPropagation();
      const id = btn.dataset.id;
      tasks = tasks.filter(t => t.id !== id);
      if (currentIndex >= tasks.length && currentIndex > 0) currentIndex--;
      saveTasks(tasks);
      render();
    });
  });
  document.querySelectorAll('.status-icon').forEach(icon => {
    icon.addEventListener('click', e => {
      e.stopPropagation();
      const id = icon.dataset.id;
      tasks = tasks.map(t => t.id === id ? {...t, done: !t.done} : t);
      saveTasks(tasks);
      render();
    });
  });
}

// ===== Swipe / Drag =====
let startX = 0, startY = 0, isDragging = false, hasMoved = false;

function onPointerDown(e) {
  startX = e.touches ? e.touches[0].clientX : e.clientX;
  startY = e.touches ? e.touches[0].clientY : e.clientY;
  isDragging = true; hasMoved = false;
}
function onPointerMove(e) {
  if (!isDragging) return;
  const dx = (e.touches ? e.touches[0].clientX : e.clientX) - startX;
  const dy = (e.touches ? e.touches[0].clientY : e.clientY) - startY;
  if (!hasMoved && Math.abs(dy) > Math.abs(dx)) { isDragging = false; return; }
  hasMoved = true;
  if (e.cancelable) e.preventDefault();
}
function onPointerUp(e) {
  if (!isDragging) return;
  isDragging = false;
  const endX = e.changedTouches ? e.changedTouches[0].clientX : e.clientX;
  const dx = endX - startX;
  if (Math.abs(dx) > 50) {
    if (dx < 0 && currentIndex < tasks.length - 1) currentIndex++;
    else if (dx > 0 && currentIndex > 0) currentIndex--;
    render();
  }
}

cardViewport.addEventListener('touchstart', onPointerDown, { passive: true });
cardViewport.addEventListener('touchmove', onPointerMove, { passive: false });
cardViewport.addEventListener('touchend', onPointerUp);
cardViewport.addEventListener('mousedown', onPointerDown);
cardViewport.addEventListener('mousemove', onPointerMove);
cardViewport.addEventListener('mouseup', onPointerUp);

// 键盘左右
document.addEventListener('keydown', e => {
  if (e.key === 'ArrowRight' && currentIndex < tasks.length - 1) { currentIndex++; render(); }
  if (e.key === 'ArrowLeft'  && currentIndex > 0)                { currentIndex--; render(); }
});

// ===== Modal =====
fabBtn.addEventListener('click', () => {
  inputTitle.value = '';
  inputDesc.value = '';
  selectedPriority = 'MEDIUM';
  document.querySelectorAll('.priority-btn').forEach(b => b.classList.toggle('active', b.dataset.p === 'MEDIUM'));
  modalOverlay.classList.add('open');
  setTimeout(() => inputTitle.focus(), 100);
});

cancelBtn.addEventListener('click', () => modalOverlay.classList.remove('open'));
modalOverlay.addEventListener('click', e => { if (e.target === modalOverlay) modalOverlay.classList.remove('open'); });

addBtn.addEventListener('click', () => {
  const title = inputTitle.value.trim();
  if (!title) { inputTitle.style.borderColor = '#E53935'; inputTitle.focus(); return; }
  inputTitle.style.borderColor = '';
  const task = {
    id: genId(),
    title,
    description: inputDesc.value.trim(),
    priority: selectedPriority,
    done: false,
    createdAt: Date.now()
  };
  tasks.push(task);
  saveTasks(tasks);
  tasks = sortTasks(tasks);
  currentIndex = tasks.findIndex(t => t.id === task.id);
  modalOverlay.classList.remove('open');
  render();
});

inputTitle.addEventListener('keydown', e => { if (e.key === 'Enter') addBtn.click(); });

// ===== Settings =====
settingsBtn.addEventListener('click', () => settingsOverlay.classList.add('open'));
settingsCloseBtn.addEventListener('click', () => settingsOverlay.classList.remove('open'));
settingsOverlay.addEventListener('click', e => { if (e.target === settingsOverlay) settingsOverlay.classList.remove('open'); });

// ===== Init =====
render();
