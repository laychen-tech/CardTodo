package com.example.cardtodo

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    val allTasks: LiveData<List<Task>>

    private val _syncState = MutableLiveData<SyncState>(SyncState.Idle)
    val syncState: LiveData<SyncState> = _syncState

    init {
        val db = TaskDatabase.getDatabase(application)
        repository = TaskRepository(db.taskDao())
        allTasks = repository.allTasks.asLiveData()
        // 启动时从 API 同步一次
        syncFromApi()
    }

    fun syncFromApi() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            try {
                repository.syncFromApi()
                _syncState.value = SyncState.Success
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message ?: "同步失败")
            }
        }
    }

    fun addTask(title: String, description: String, priority: Priority) {
        val task = Task(title = title, description = description, priority = priority.name)
        viewModelScope.launch { repository.insert(task) }
    }

    fun toggleComplete(task: Task) {
        viewModelScope.launch { repository.update(task.copy(isCompleted = !task.isCompleted)) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }
}

sealed class SyncState {
    object Idle    : SyncState()
    object Loading : SyncState()
    object Success : SyncState()
    data class Error(val msg: String) : SyncState()
}
