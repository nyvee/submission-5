package com.example.storyapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.storyapp.data.remote.response.Story
import com.example.storyapp.data.remote.retrofit.ApiService
import com.example.storyapp.ui.home.StoryPagingSource
import kotlinx.coroutines.flow.Flow

class StoryRepository(private val apiService: ApiService, private val token: String) {

    fun getStoriesPaging(): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(apiService, token) }
        ).flow
    }

    fun getStoriesWithLocation(): LiveData<List<Story>> = liveData {
        try {
            val response = apiService.getStoriesWithLocation(token)
            emit(response.listStory.filter { it.lat != null && it.lon != null })
        } catch (e: Exception) {
        }
    }
}
