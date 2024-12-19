package com.example.storyapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.storyapp.ui.home.HomeFragment
import com.example.storyapp.ui.auth.AuthViewModel
import com.example.storyapp.ui.auth.AuthViewModelFactory
import com.google.android.material.appbar.AppBarLayout
import androidx.navigation.NavOptions
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var viewModel: AuthViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var appBarLayout: AppBarLayout

    private var backPressedTime: Long = 0
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        appBarLayout = findViewById(R.id.appBarLayout)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        setSupportActionBar(toolbar)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    updateToolbar(getString(R.string.app_name), false)
                    invalidateOptionsMenu()
                }
                R.id.storyDetailFragment -> updateToolbar(getString(R.string.story_detail_title), true)
                R.id.addStoryFragment -> updateToolbar(getString(R.string.add_story_title), true)
                R.id.mapsFragment -> updateToolbar("Maps", true)
                else -> updateToolbarVisibility(false)
            }
        }

        val factory = AuthViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        if (viewModel.isLoggedIn()) {
            navController.navigate(R.id.homeFragment)
        } else {
            navController.navigate(R.id.loginFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val logoutItem = menu.findItem(R.id.action_logout)
        val localizationItem = menu.findItem(R.id.action_localization)

        val isHomeFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager?.fragments?.get(0) is HomeFragment
        logoutItem.isVisible = isHomeFragment
        localizationItem.isVisible = isHomeFragment

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_localization -> {
                switchLocalization()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateToolbar(title: String, showBackButton: Boolean) {
        supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(showBackButton)
            if (showBackButton) {
                setHomeAsUpIndicator(R.drawable.ic_back)
            }
        }
        updateToolbarVisibility(true)
    }

    private fun updateToolbarVisibility(isVisible: Boolean) {
        if (isVisible) {
            supportActionBar?.show()
            appBarLayout.visibility = View.VISIBLE
        } else {
            supportActionBar?.hide()
            appBarLayout.visibility = View.GONE
        }
    }

    private fun logout() {
        viewModel.logout()
        finishAffinity()
    }

    private fun switchLocalization() {
        val currentLocale = resources.configuration.locales[0].language
        val newLocale = if (currentLocale == "en") "id" else "en"
        setLocale(newLocale)
    }

    private fun setLocale(localeName: String) {
        val locale = Locale(localeName)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
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

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (navController.currentDestination?.id == R.id.homeFragment) {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                toast.cancel()
                finishAffinity()
            } else {
                toast = Toast.makeText(this, getString(R.string.exit_app), Toast.LENGTH_SHORT)
                toast.show()
            }
            backPressedTime = System.currentTimeMillis()
        } else {
            super.onBackPressed()
        }
    }
}