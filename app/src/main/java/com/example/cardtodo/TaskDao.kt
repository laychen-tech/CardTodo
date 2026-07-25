package com.example.cardtodo

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // 按优先级排序：HIGH(0) > MEDIUM(1) > LOW(2)，完成的任务排后面
    @Query("""
        SELECT * FROM tasks 
        ORDER BY 
            isCompleted ASC,
            CASE priority 
                WHEN 'HIGH' THEN 0 
                WHEN 'MEDIUM' THEN 1 
                WHEN 'LOW' THEN 2 
                ELSE 1 
            END ASC,
            createdAt DESC
    """)
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}
