package com.example.storyapp.data

import android.content.Context

class UserPreferences(context: Context) {
    private val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun setUserId(userId: String) {
        sharedPref.edit().putString("USER_ID", userId).apply()
    }

    fun getUserId(): String? {
        return sharedPref.getString("USER_ID", null)
    }

    fun setToken(token: String) {
        sharedPref.edit().putString("TOKEN", token).apply()
    }

    fun getToken(): String? {
        return sharedPref.getString("TOKEN", null)
    }

    fun clearUserId() {
        sharedPref.edit().remove("USER_ID").apply()
    }

    fun clearToken() {
        sharedPref.edit().remove("TOKEN").apply()
    }

    fun clearSession() {
        sharedPref.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPref.getString("USER_ID", null) != null
    }
}