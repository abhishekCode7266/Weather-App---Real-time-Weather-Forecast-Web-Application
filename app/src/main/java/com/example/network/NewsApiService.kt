package com.example.network

import com.example.model.ArticleItem
import com.example.model.PostDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface JsonPlaceholderApi {
    @GET("posts")
    suspend fun getPosts(): List<PostDto>
}

class NewsRepository {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api = retrofit.create(JsonPlaceholderApi::class.java)

    private val sampleCategories = listOf("Technology", "Mobile Dev", "Cloud Computing", "AI & ML", "Architecture", "Flutter & Dart")
    private val sampleAuthors = listOf("Alex Rivera", "Devon Lane", "Courtney Henry", "Elena Rostova", "Liam Patel", "Sophia Wu")

    suspend fun fetchFeed(): Result<List<ArticleItem>> = withContext(Dispatchers.IO) {
        try {
            val posts = api.getPosts()
            if (posts.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            val articles = posts.take(25).mapIndexed { index, post ->
                val category = sampleCategories[index % sampleCategories.size]
                val author = sampleAuthors[index % sampleAuthors.size]
                val readTime = (post.body.length / 50).coerceIn(2, 8)
                val dayOffset = (index % 14) + 1

                ArticleItem(
                    id = post.id,
                    title = post.title.replaceFirstChar { it.uppercase() },
                    summary = post.body.replace("\n", " ").take(140) + "...",
                    author = author,
                    category = category,
                    readTimeMinutes = readTime,
                    formattedDate = "$dayOffset days ago",
                    views = 120 + (post.id * 37) % 890
                )
            }
            Result.success(articles)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("Unable to resolve host") == true ->
                    "Cannot reach news server. Please check internet connection."
                else -> "Feed error: ${e.localizedMessage ?: "Failed to load articles"}"
            }
            Result.failure(Exception(message, e))
        }
    }
}
