package com.example.storyapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.ui.auth.AuthViewModel
import com.example.storyapp.ui.auth.AuthViewModelFactory
import com.example.storyapp.ui.auth.login.LoginFragment
import com.example.storyapp.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val factory = AuthViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        if (viewModel.isLoggedIn()) {
            navigateToFragment(HomeFragment())
        } else {
            navigateToFragment(LoginFragment())
        }
    }

    fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
        updateToolbarVisibility(fragment)
    }

    private fun updateToolbarVisibility(fragment: Fragment) {
        toolbar.visibility = if (fragment is HomeFragment) View.VISIBLE else View.GONE
    }

    fun logout() {
        viewModel.logout()
        navigateToFragment(LoginFragment())
    }
}