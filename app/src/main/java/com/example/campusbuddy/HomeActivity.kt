package com.example.campusbuddy
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.setupWithNavController
import com.example.campusbuddy.Fragment.HomeFragment
import com.example.campusbuddy.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Get the NavController safely
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_home)
                    as? androidx.navigation.fragment.NavHostFragment

        if (navHostFragment != null) {
            navController = navHostFragment.navController

            // Get the selected role from the intent
            val role = intent.getStringExtra("ROLE") ?: "Buyer"

            // Set up navigation based on the selected role
            setupNavigationBasedOnRole(role)

            // Set up bottom navigation based on user role
            navView.setupWithNavController(navController)
        }
    }

    private fun setupNavigationBasedOnRole(role: String) {
        // Navigate to the appropriate fragment based on the user's role
        when (role) {
            "Seller" -> {
                // Seller sees MyPostFragment by default
                navController.navigate(R.id.navMyPost)
            }
            else -> {
                // Buyer sees HomeFragment by default
                navController.navigate(R.id.navHome)
            }
        }

        // Set up bottom navigation based on user role
        binding.navView.menu.clear()
        when (role) {
            "Seller" -> binding.navView.inflateMenu(R.menu.seller_bottom_nav_menu)
            else -> binding.navView.inflateMenu(R.menu.buyer_bottom_nav_menu)
        }

        // Set up bottom navigation item selection listener
        binding.navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navMyPost -> {
                    navController.navigate(R.id.navMyPost)
                    true
                }
                R.id.navAdd -> {
                    navController.navigate(R.id.navAdd)
                    true
                }
                R.id.navProfile -> {
                    navController.navigate(R.id.navProfile)
                    true
                }
                R.id.navHome -> {
                    navController.navigate(R.id.navHome)
                    true
                }
                R.id.navSearch -> {
                    navController.navigate(R.id.navSearch)
                    true
                }
                R.id.navBuyerProfile -> {
                    navController.navigate(R.id.navBuyerProfile)
                    true
                }
                else -> false
            }
        }
    }

    fun refreshHomeFragment() {
        // Get the NavHostFragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_home)
                    as? androidx.navigation.fragment.NavHostFragment

        // Find HomeFragment in the NavHostFragment's child fragments
        navHostFragment?.childFragmentManager?.fragments?.forEach { fragment ->
            if (fragment is HomeFragment && fragment.isVisible) {
                fragment.refreshPosts()
            }
        }
    }
}