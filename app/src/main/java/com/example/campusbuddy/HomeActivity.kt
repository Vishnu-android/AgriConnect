package com.example.campusbuddy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.campusbuddy.Fragment.HomeFragment
import com.example.campusbuddy.databinding.ActivityHomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private var currentRole: String = "Buyer" // Default role

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get role from intent or fetch from Firestore
        currentRole = intent.getStringExtra("ROLE") ?: getDefaultRole()

        // Initialize navigation
        setupNavigation()

        // Setup button to navigate to DashboardActivity
        binding.btnGoToDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        binding.btnGoToDashboard.visibility = if (currentRole.lowercase() == "seller") View.VISIBLE else View.GONE
    }

    private fun getDefaultRole(): String {
        // Try to get role from Firestore if user is logged in
        Firebase.auth.currentUser?.uid?.let { userId ->
            Firebase.firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    document.getString("role")?.let { role ->
                        currentRole = role
                        setupNavigation() // Re-initialize with correct role
                    }
                }
        }
        return currentRole
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_home) as NavHostFragment
        navController = navHostFragment.navController

        // Setup bottom nav based on role
        binding.navView.menu.clear()
        when (currentRole.lowercase()) {
            "seller" -> {
                binding.navView.inflateMenu(R.menu.seller_bottom_nav_menu)
                navController.navigate(R.id.navMyPost)
            }
            else -> {
                binding.navView.inflateMenu(R.menu.buyer_bottom_nav_menu)
                navController.navigate(R.id.navHome)
            }
        }

        // Setup navigation listener
        binding.navView.setupWithNavController(navController)
        binding.navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navMyPost -> navController.navigate(R.id.navMyPost)
                R.id.navAdd -> navController.navigate(R.id.navAdd)
                R.id.navProfile -> navController.navigate(R.id.navProfile)
                R.id.navHome -> navController.navigate(R.id.navHome)
                R.id.navSearch -> navController.navigate(R.id.navSearch)
                R.id.navBuyerProfile -> navController.navigate(R.id.navBuyerProfile)
                R.id.navOrders -> navController.navigate(R.id.navOrders)
                R.id.navMyVideo -> navController.navigate(R.id.navMyVideo)
                R.id.navCart -> navController.navigate(R.id.navCart)
                else -> false
            }
            true
        }

    }

    fun refreshHomeFragment() {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_home) as? NavHostFragment)
            ?.childFragmentManager?.fragments?.forEach { fragment ->
                if (fragment is HomeFragment && fragment.isVisible) {
                    fragment.refreshPosts()
                }
            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getStringExtra("ROLE")?.let { role ->
            if (role != currentRole) {
                currentRole = role
                setupNavigation()
            }
        }
    }
}
