package com.example.campusbuddy.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.campusbuddy.Models.Post
import com.example.campusbuddy.adapters.MyPostRvAdapter
import com.example.campusbuddy.databinding.FragmentMyPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class MyPostFragment : Fragment() {

    private lateinit var binding: FragmentMyPostBinding
    private lateinit var postList: ArrayList<Post>
    private lateinit var adapter: MyPostRvAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyPostBinding.inflate(inflater, container, false)

        postList = ArrayList()
        adapter = MyPostRvAdapter(
            requireContext(),
            postList,
            onPostDeleted = {
                loadPosts()
            }
        )

        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.adapter = adapter

        // Set up swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadPosts()
        }

        loadPosts()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    private fun loadPosts() {
        binding.progressBar.visibility = View.VISIBLE
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            Firebase.firestore.collection("posts")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener { result ->
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false

                    val tempList = arrayListOf<Post>()
                    for (document in result.documents) {
                        val post: Post? = document.toObject(Post::class.java)
                        post?.let { tempList.add(it) }
                    }

                    postList.clear()
                    postList.addAll(tempList)
                    adapter.notifyDataSetChanged()

                    // Show/hide the empty state message
                    if (postList.isEmpty()) {
                        binding.textNoPosts.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.textNoPosts.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { exception ->
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    Log.e("MyPostFragment", "Error loading posts: ${exception.message}")
                }
        } else {
            binding.progressBar.visibility = View.GONE
            Log.e("MyPostFragment", "User not logged in")
        }
    }
}