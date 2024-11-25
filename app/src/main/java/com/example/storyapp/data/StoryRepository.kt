package com.example.storyapp.data

import android.content.Context
import android.util.Log
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
                Log.e("StoryRepository", "Token not found")
                throw Exception("Token not found")
            } else {
                Log.d("StoryRepository", "Using token: $token")
            }
            RetrofitInstance.api.getStories("Bearer $token", page, size, location)
        }
    }
}