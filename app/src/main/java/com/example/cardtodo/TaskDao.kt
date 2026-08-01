package com.example.cardtodo

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
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

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(tasks: List<Task>) {
        deleteAll()
        tasks.forEach { insert(it) }
    }
}
