package com.example.storyapp.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.auth0.jwt.JWT
import com.example.storyapp.data.remote.response.ErrorResponse
import com.example.storyapp.data.remote.response.LoginResponse
import com.example.storyapp.data.remote.response.RegisterResponse
import com.example.storyapp.data.remote.retrofit.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.*

class UserRepository(private val context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

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
        if (token != null) {
            sharedPref.edit().putString("token", token).apply()
            Log.d("UserRepository", "Token saved" + token)
        } else {
            throw Exception("Token is null")
        }
    }

    fun clearSession() {
        sharedPref.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        val token = sharedPref.getString("token", null)
        return token != null && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val decodedJWT = JWT.decode(token)
            val expiryDate = decodedJWT.expiresAt
            val currentDate = Calendar.getInstance().time

            if (expiryDate == null) {
                val issueDateClaim = decodedJWT.getClaim("iat").asDate()
                val issueDate = issueDateClaim ?: currentDate
                val validUntil = Date(issueDate.time + 24 * 60 * 60 * 1000)
                return currentDate.after(validUntil)
            }
            expiryDate != null && currentDate.after(expiryDate)
        } catch (e: Exception) {
            true
        }
    }
}
