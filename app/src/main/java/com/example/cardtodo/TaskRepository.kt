package com.example.cardtodo

import android.util.Log
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskRepository(private val taskDao: TaskDao) {

    // Room 作为本地缓存，UI 直接观察
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    // ── 从 API 拉取并写入本地缓存 ──────────────────────────────────────────
    suspend fun syncFromApi() {
        try {
            val dtos = ApiClient.api.getTasks()
            // 用服务器数据完整替换本地缓存
            taskDao.replaceAll(dtos.map { it.toTask() })
        } catch (e: Exception) {
            Log.w("TaskRepository", "syncFromApi failed: ${e.message}")
            // 失败则继续使用本地缓存，不抛出
        }
    }

    // ── 新建 ────────────────────────────────────────────────────────────────
    suspend fun insert(task: Task): Task {
        // 生成唯一 string id
        val withId = if (task.remoteId.isBlank())
            task.copy(remoteId = UUID.randomUUID().toString().replace("-", "").take(20))
        else task

        taskDao.insert(withId)

        try {
            ApiClient.api.createTask(withId.toDto())
        } catch (e: Exception) {
            Log.w("TaskRepository", "createTask API failed: ${e.message}")
        }
        return withId
    }

    // ── 更新 ────────────────────────────────────────────────────────────────
    suspend fun update(task: Task) {
        taskDao.update(task)
        try {
            ApiClient.api.updateTask(
                task.remoteId,
                TaskUpdateDto(
                    title       = task.title,
                    description = task.description,
                    priority    = task.priority,
                    done        = task.isCompleted
                )
            )
        } catch (e: Exception) {
            Log.w("TaskRepository", "updateTask API failed: ${e.message}")
        }
    }

    // ── 删除 ────────────────────────────────────────────────────────────────
    suspend fun delete(task: Task) {
        taskDao.delete(task)
        try {
            ApiClient.api.deleteTask(task.remoteId)
        } catch (e: Exception) {
            Log.w("TaskRepository", "deleteTask API failed: ${e.message}")
        }
    }
}

// ── 转换扩展 ─────────────────────────────────────────────────────────────────
fun TaskDto.toTask() = Task(
    remoteId    = id,
    title       = title,
    description = description,
    isCompleted = done,
    priority    = priority,
    createdAt   = createdAt
)

fun Task.toDto() = TaskDto(
    id          = remoteId,
    title       = title,
    description = description,
    priority    = priority,
    done        = isCompleted,
    createdAt   = createdAt
)
