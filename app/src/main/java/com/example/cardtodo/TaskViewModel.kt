package com.example.cardtodo

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    val allTasks: LiveData<List<Task>>

    init {
        val db = TaskDatabase.getDatabase(application)
        repository = TaskRepository(db.taskDao())
        allTasks = repository.allTasks.asLiveData()
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
