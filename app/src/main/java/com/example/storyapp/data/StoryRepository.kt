package com.example.storyapp.data

import android.content.Context
import com.example.storyapp.data.remote.response.StoryResponse
import com.example.storyapp.data.remote.retrofit.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoryRepository(private val context: Context) {
    private val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    suspend fun getStories(page: Int? = null, size: Int? = null, location: Int = 0): StoryResponse {
        return withContext(Dispatchers.IO) {
            val token = sharedPref.getString("token", null)
            if (token == null) {
                throw Exception("Token not found")
            } else {
            }
            RetrofitInstance.api.getStories("Bearer $token", page, size, location)
        }
    }
}