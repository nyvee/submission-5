package com.example.storyapp.data

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
                pageSize = 10, // Number of items per page
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(apiService, token) }
        ).flow
    }
}
