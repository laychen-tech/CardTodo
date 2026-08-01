package com.example.cardtodo

import retrofit2.http.*

interface TaskApiService {
    @GET("tasks")
    suspend fun getTasks(): List<TaskDto>

    @POST("tasks")
    suspend fun createTask(@Body task: TaskDto): ApiResult

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body patch: TaskUpdateDto): ApiResult

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): ApiResult
}
