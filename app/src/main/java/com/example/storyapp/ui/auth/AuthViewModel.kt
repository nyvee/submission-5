package com.example.storyapp.ui.auth

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(context: Context) : ViewModel() {
    private val repository = UserRepository(context)
    val loginState = MutableLiveData<Boolean>()
    val wrongCredentials = MutableLiveData<String>()
    val registrationState = MutableLiveData<Boolean>()

    fun register(name: String, email: String, password: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.register(name, email, password)
                callback(true, response.message)
            } catch (e: Exception) {
                callback(false, e.message)
            }
        }
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                callback(true, response.message)
            } catch (e: Exception) {
                callback(false, e.message)
            }
        }
    }

    fun logout() {
        repository.clearSession()
    }

    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }
}