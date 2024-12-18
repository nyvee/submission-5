package com.example.storyapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.remote.response.Story
import kotlinx.coroutines.flow.Flow

class HomeViewModel(private val repository: StoryRepository) : ViewModel() {

    fun getStoriesPaging(): Flow<PagingData<Story>> {
        return repository.getStoriesPaging().cachedIn(viewModelScope)
    }
}
