package com.example.storyapp.data

import android.content.Context
import com.example.storyapp.data.remote.response.ErrorResponse
import com.example.storyapp.data.remote.response.LoginResponse
import com.example.storyapp.data.remote.response.RegisterResponse
import com.example.storyapp.data.remote.retrofit.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class UserRepository(private val context: Context) {
    private val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitInstance.api.register(name, email, password)
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                throw Exception(errorBody.message)
            }
        }
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.login(email, password)
                saveToken(response.loginResult.token)
                response
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                throw Exception(errorBody.message)
            }
        }
    }

    private fun saveToken(token: String?) {
        sharedPref.edit().putString("token", token).apply()
    }

    fun clearSession() {
        sharedPref.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPref.getString("token", null) != null
    }
}