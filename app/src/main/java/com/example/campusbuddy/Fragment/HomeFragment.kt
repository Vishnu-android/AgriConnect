package com.example.campusbuddy.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusbuddy.Models.Post
import com.example.campusbuddy.adapters.PostAdapter
import com.example.campusbuddy.databinding.FragmentHomeBinding
import com.example.campusbuddy.utils.FOLLOW
import com.example.campusbuddy.viewmodels.CartViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private var postList = ArrayList<Post>()
    private lateinit var adapter: PostAdapter
    private var followedUsers = HashSet<String>()
    private lateinit var cartViewModel: CartViewModel // Add CartViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize CartViewModel
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        // Pass CartViewModel to the PostAdapter
        adapter = PostAdapter(requireContext(), postList, followedUsers, cartViewModel)
        binding.recyclerViewHome.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHome.adapter = adapter

        getFollowedUsers()

        return binding.root
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
        Firebase.firestore.collection("posts")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val tempList = ArrayList<Post>()
                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
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
                adapter.notifyDataSetChanged()
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
}