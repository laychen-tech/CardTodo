-- CardTodo D1 Schema
CREATE TABLE IF NOT EXISTS tasks (
  id          TEXT PRIMARY KEY,
  title       TEXT NOT NULL,
  description TEXT DEFAULT '',
  priority    TEXT DEFAULT 'MEDIUM' CHECK(priority IN ('HIGH', 'MEDIUM', 'LOW')),
  done        INTEGER DEFAULT 0,
  created_at  INTEGER NOT NULL,
  updated_at  INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tasks_done ON tasks(done);
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);
