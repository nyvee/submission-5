package com.example.storyapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.storyapp.ui.auth.AuthViewModel
import com.example.storyapp.ui.auth.AuthViewModelFactory
import com.google.android.material.appbar.AppBarLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.NavOptions
import android.util.Log

class MainActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var viewModel: AuthViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var appBarLayout: AppBarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        appBarLayout = findViewById(R.id.appBarLayout)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        setSupportActionBar(toolbar)
        toolbar.overflowIcon?.let {
            val wrappedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, R.color.white))
            toolbar.overflowIcon = wrappedIcon
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Centralized toolbar updates for each destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> updateToolbar(getString(R.string.app_name), false)
                R.id.storyDetailFragment -> updateToolbar(getString(R.string.story_detail_title), true)
                R.id.addStoryFragment -> updateToolbar(getString(R.string.add_story_title), true)
                else -> updateToolbarVisibility(false) // Hide for unexpected destinations
            }
        }

        val factory = AuthViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        if (viewModel.isLoggedIn()) {
            navController.navigate(R.id.homeFragment)
        } else {
            navController.navigate(R.id.loginFragment)
        }
    }

    // Reusable method to update toolbar properties
    fun updateToolbar(title: String, showBackButton: Boolean) {
        supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(showBackButton)
        }
        updateToolbarVisibility(true)
    }

    // Hide or show toolbar and AppBarLayout
    private fun updateToolbarVisibility(isVisible: Boolean) {
        Log.d("MainActivity", "Setting toolbar visibility to: $isVisible")
        if (isVisible) {
            supportActionBar?.show()
            appBarLayout.visibility = View.VISIBLE
        } else {
            supportActionBar?.hide()
            appBarLayout.visibility = View.GONE
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun logout() {
        viewModel.logout()
        finishAffinity()
    }

    fun navigateWithAnimation(destinationId: Int, args: Bundle? = null) {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        navController.navigate(destinationId, args, navOptions)
    }
}
