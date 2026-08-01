package com.example.cardtodo

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority(val label: String, val order: Int) {
    HIGH("高优先级", 0),
    MEDIUM("中优先级", 1),
    LOW("低优先级", 2)
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: String = "",   // Cloudflare Worker / D1 的唯一 ID
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: String = Priority.MEDIUM.name,
    val createdAt: Long = System.currentTimeMillis()
)
