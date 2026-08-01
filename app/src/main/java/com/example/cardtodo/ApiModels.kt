package com.example.cardtodo

import com.google.gson.annotations.SerializedName

data class TaskDto(
    @SerializedName("id")          val id: String,
    @SerializedName("title")       val title: String,
    @SerializedName("description") val description: String = "",
    @SerializedName("priority")    val priority: String = "MEDIUM",
    @SerializedName("done")        val done: Boolean = false,
    @SerializedName("createdAt")   val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updatedAt")   val updatedAt: Long = System.currentTimeMillis()
)

data class TaskUpdateDto(
    @SerializedName("title")       val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("priority")    val priority: String? = null,
    @SerializedName("done")        val done: Boolean? = null
)

data class ApiResult(
    @SerializedName("ok")      val ok: Boolean,
    @SerializedName("id")      val id: String? = null,
    @SerializedName("deleted") val deleted: Int? = null,
    @SerializedName("error")   val error: String? = null
)
