package com.example.storyapp.ui.home

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.remote.response.Story
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: StoryRepository, private val context: Context) : ViewModel() {
    val stories = MutableLiveData<List<Story>>()
    val isLoading = MutableLiveData<Boolean>()

    fun fetchStories(page: Int? = null, size: Int? = null, location: Int = 0) {
        viewModelScope.launch {
            isLoading.postValue(true)
            Log.d("HomeViewModel", "Fetching stories started")
            try {
                val response = repository.getStories(page, size, location)
                Log.d("HomeViewModel", "Stories fetched successfully: ${response.listStory}")
                stories.postValue(response.listStory)
            } catch (e: Exception) {
                if (e.message?.contains("401") == true) {
                    Log.e("HomeViewModel", "Unauthorized: ${e.message}", e)
                    Toast.makeText(context, "Unauthorized: Please check your credentials", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("HomeViewModel", "Error fetching stories", e)
                    Toast.makeText(context, "Story fetch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading.postValue(false)
                Log.d("HomeViewModel", "Fetching stories ended")
            }
        }
    }
}