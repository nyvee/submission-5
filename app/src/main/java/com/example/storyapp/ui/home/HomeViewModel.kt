package com.example.storyapp.ui.home

import android.content.Context
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
            try {
                val response = repository.getStories(page, size, location)
                stories.postValue(response.listStory)
            } catch (e: Exception) {
                if (e.message?.contains("401") == true) {
                    Toast.makeText(context, "Unauthorized: Please check your credentials", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Story fetch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading.postValue(false)
            }
        }
    }
}