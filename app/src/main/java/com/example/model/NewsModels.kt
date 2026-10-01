package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostDto(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)

data class ArticleItem(
    val id: Int,
    val title: String,
    val summary: String,
    val author: String,
    val category: String,
    val readTimeMinutes: Int,
    val formattedDate: String,
    val views: Int
)
