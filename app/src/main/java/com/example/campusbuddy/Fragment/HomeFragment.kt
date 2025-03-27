package com.example.campusbuddy.Fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.campusbuddy.Models.Post
import com.example.campusbuddy.R
import com.example.campusbuddy.adapters.BannerAdapter
import com.example.campusbuddy.adapters.PostAdapter
import com.example.campusbuddy.databinding.FragmentHomeBinding
import com.example.campusbuddy.utils.FOLLOW
import com.example.campusbuddy.viewmodels.CartViewModel
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import me.relex.circleindicator.CircleIndicator3

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var postList = ArrayList<Post>()
    private var originalPostList = ArrayList<Post>() // Keep original list for filtering
    private lateinit var adapter: PostAdapter
    private var followedUsers = HashSet<String>()
    private lateinit var cartViewModel: CartViewModel
    private lateinit var bannerViewPager: ViewPager2
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var indicator: CircleIndicator3
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewPager2 and Indicator
        bannerViewPager = binding.bannerViewPager
        indicator = binding.indicator

        // Set up banners
        val banners = listOf(
            Banner(R.drawable.banner1, "Fresh from Farms", "Get 20% off on your first order"),
            Banner(R.drawable.banner_background, "Organic Products", "Healthy living starts here"),
            Banner(R.drawable.banner_background, "Seasonal Sale", "Up to 50% off on selected items")
        )

        bannerAdapter = BannerAdapter(banners)
        bannerViewPager.adapter = bannerAdapter
        indicator.setViewPager(bannerViewPager)

        // Initialize CartViewModel
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        // Pass CartViewModel to the PostAdapter
        adapter = PostAdapter(requireContext(), postList, followedUsers, cartViewModel)
        binding.recyclerViewHome.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHome.adapter = adapter

        // Fetch data and set up UI
        getFollowedUsers()
        setupCategories()
        setupSearchIcon()

        // Auto-swiping functionality for banners
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val currentItem = bannerViewPager.currentItem
                val nextItem = if (currentItem == banners.size - 1) 0 else currentItem + 1
                bannerViewPager.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 3000) // Change banner every 3 seconds
            }
        }
        handler.postDelayed(runnable, 3000)

        // Pause auto-swiping when the user interacts with the ViewPager
        bannerViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 3000)
            }
        })
    }

    private fun setupSearchIcon() {
        binding.searchIcon.setOnClickListener {
            toggleSearchView(true)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchProducts(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchProducts(newText)
                return true
            }
        })

        binding.closeSearchButton.setOnClickListener {
            toggleSearchView(false)
            adapter.updateList(originalPostList)
        }
    }

    private fun toggleSearchView(showSearch: Boolean) {
        if (showSearch) {
            binding.normalToolbarContent.visibility = View.GONE
            binding.searchToolbarContent.visibility = View.VISIBLE
            binding.searchView.requestFocus()
        } else {
            binding.normalToolbarContent.visibility = View.VISIBLE
            binding.searchToolbarContent.visibility = View.GONE
            binding.searchView.setQuery("", false)
        }
    }

    private fun searchProducts(query: String?) {
        if (query.isNullOrBlank()) {
            adapter.updateList(originalPostList)
            return
        }

        val filteredList = originalPostList.filter { post ->
            post.productName?.contains(query, true) == true ||
                    post.productDescription?.contains(query, true) == true ||
                    post.productCategory?.contains(query, true) == true
        }

        adapter.updateList(filteredList)
    }

    fun refreshPosts() {
        getFollowedUsers()
    }
    private fun getFollowedUsers() {
        followedUsers.clear()
        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
            .get()
            .addOnSuccessListener { querySnapshot ->
                followedUsers.clear()
                for (document in querySnapshot.documents) {
                    document.getString("userId")?.let {
                        followedUsers.add(it)
                    }
                }
                fetchPosts()
            }
    }

    private fun fetchPosts() {
        postList.clear()
        originalPostList.clear()

        Firebase.firestore.collection("posts")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val tempList = ArrayList<Post>()
                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        // Ensure the post has a valid category
                        if (post.productCategory.isNullOrEmpty()) {
                            post.productCategory = "Uncategorized" // Default category if missing
                        }

                        // Only add posts if the user is a Buyer
                        if (isBuyer()) {
                            tempList.add(post)
                        }
                    }
                }

                // Sort posts: followed users' posts first, then others
                val sortedList = tempList.sortedWith(compareByDescending<Post> { post ->
                    if (followedUsers.contains(post.userId)) 1 else 0
                }.thenByDescending { it.timestamp })

                postList.addAll(sortedList)
                originalPostList.addAll(sortedList) // Keep a copy of the original list
                adapter.updateList(postList) // Update the adapter with the fetched posts
                filterPostsByCategory("All")
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Toast.makeText(requireContext(), "Failed to fetch posts: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isBuyer(): Boolean {
        // Retrieve the role from the HomeActivity intent
        val role = activity?.intent?.getStringExtra("ROLE") ?: "Buyer"
        return role == "Buyer"
    }

    override fun onResume() {
        super.onResume()
        getFollowedUsers()
    }

    // Categories Section
    private fun setupCategories() {
        val categories = listOf(
            "All", "Vegetable", "Seed", "Grain", "Dairy", "Spice", "Organic Product", "Fruit",
            "Fertilizer & Pesticide", "Tools & Equipment"
        )

        categories.forEach { category ->
            val tab = binding.tabCategories.newTab()
            tab.text = category
            binding.tabCategories.addTab(tab)
        }

        val defaultTab = binding.tabCategories.getTabAt(0)
        defaultTab?.select()

        binding.tabCategories.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedCategory = tab?.text.toString()
                filterPostsByCategory(selectedCategory)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterPostsByCategory(category: String) {
        val filteredList = if (category == "All") {
            originalPostList // Show all posts
        } else {
            originalPostList.filter { post ->
                // Normalize both the post category and selected category
                val postCategory = post.productCategory?.trim()?.lowercase()
                val selectedCategory = category.trim().lowercase()
                postCategory == selectedCategory
            }
        }
        adapter.updateList(filteredList)
    }

    // Banner Data Class
    data class Banner(
        val imageRes: Int,
        val title: String,
        val description: String
    )


}