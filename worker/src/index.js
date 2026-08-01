/**
 * CardTodo Cloudflare Worker API
 * Routes:
 *   GET    /tasks          - list all tasks
 *   POST   /tasks          - create task
 *   PUT    /tasks/:id      - update task (done, title, description, priority)
 *   DELETE /tasks/:id      - delete task
 *   DELETE /tasks          - delete all completed tasks (bulk cleanup)
 */

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type',
};

function json(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { 'Content-Type': 'application/json', ...CORS_HEADERS },
  });
}

function err(msg, status = 400) {
  return json({ error: msg }, status);
}

function rowToTask(row) {
  return {
    id:          row.id,
    title:       row.title,
    description: row.description || '',
    priority:    row.priority,
    done:        row.done === 1,
    createdAt:   row.created_at,
    updatedAt:   row.updated_at,
  };
}

export default {
  async fetch(request, env) {
    const url    = new URL(request.url);
    const method = request.method.toUpperCase();
    const path   = url.pathname;

    // Preflight
    if (method === 'OPTIONS') {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }

    // ── GET /tasks ──────────────────────────────────────────────────────────
    if (method === 'GET' && path === '/tasks') {
      const { results } = await env.DB.prepare(
        'SELECT * FROM tasks ORDER BY done ASC, CASE priority WHEN "HIGH" THEN 0 WHEN "MEDIUM" THEN 1 ELSE 2 END ASC, created_at DESC'
      ).all();
      return json(results.map(rowToTask));
    }

    // ── POST /tasks ─────────────────────────────────────────────────────────
    if (method === 'POST' && path === '/tasks') {
      let body;
      try { body = await request.json(); } catch { return err('Invalid JSON'); }

      const { id, title, description = '', priority = 'MEDIUM', done = false, createdAt } = body;
      if (!id)    return err('id is required');
      if (!title) return err('title is required');

      const VALID_PRIORITY = ['HIGH', 'MEDIUM', 'LOW'];
      if (!VALID_PRIORITY.includes(priority)) return err('Invalid priority');

      const now = Date.now();
      await env.DB.prepare(
        'INSERT INTO tasks (id, title, description, priority, done, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)'
      ).bind(id, title, description, priority, done ? 1 : 0, createdAt || now, now).run();

      return json({ ok: true, id }, 201);
    }

    // ── PUT /tasks/:id ───────────────────────────────────────────────────────
    const putMatch = path.match(/^\/tasks\/([^/]+)$/);
    if (method === 'PUT' && putMatch) {
      const id = decodeURIComponent(putMatch[1]);
      let body;
      try { body = await request.json(); } catch { return err('Invalid JSON'); }

      // Build dynamic SET clause from provided fields
      const fields = [];
      const vals   = [];

      if (body.title       !== undefined) { fields.push('title = ?');       vals.push(body.title); }
      if (body.description !== undefined) { fields.push('description = ?'); vals.push(body.description); }
      if (body.priority    !== undefined) {
        const VALID_PRIORITY = ['HIGH', 'MEDIUM', 'LOW'];
        if (!VALID_PRIORITY.includes(body.priority)) return err('Invalid priority');
        fields.push('priority = ?'); vals.push(body.priority);
      }
      if (body.done !== undefined) { fields.push('done = ?'); vals.push(body.done ? 1 : 0); }

      if (fields.length === 0) return err('No fields to update');

      fields.push('updated_at = ?');
      vals.push(Date.now());
      vals.push(id);

      const result = await env.DB.prepare(
        `UPDATE tasks SET ${fields.join(', ')} WHERE id = ?`
      ).bind(...vals).run();

      if (result.meta.changes === 0) return err('Task not found', 404);
      return json({ ok: true });
    }

    // ── DELETE /tasks/:id ───────────────────────────────────────────────────
    if (method === 'DELETE' && putMatch) {
      const id = decodeURIComponent(putMatch[1]);
      const result = await env.DB.prepare('DELETE FROM tasks WHERE id = ?').bind(id).run();
      if (result.meta.changes === 0) return err('Task not found', 404);
      return json({ ok: true });
    }

    // ── DELETE /tasks (bulk: remove all completed) ──────────────────────────
    if (method === 'DELETE' && path === '/tasks') {
      const result = await env.DB.prepare('DELETE FROM tasks WHERE done = 1').run();
      return json({ ok: true, deleted: result.meta.changes });
    }

    return err('Not found', 404);
  },
};
