package com.example.campusbuddy.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusbuddy.Models.Post
import com.example.campusbuddy.R
import com.example.campusbuddy.adapters.MyPostRvAdapter
import com.example.campusbuddy.databinding.FragmentMyPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPostFragment : Fragment() {

    private lateinit var binding: FragmentMyPostBinding
    private lateinit var adapter: MyPostRvAdapter
    private val postList = mutableListOf<Post>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadPosts()
    }

    private fun setupRecyclerView() {
        adapter = MyPostRvAdapter(
            context = requireContext(),
            postList = postList,
            onPostUpdated = { loadPosts() },
            onPostEdit = { post ->

            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MyPostFragment.adapter
        }
    }

    private fun loadPosts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                postList.clear()
                querySnapshot.documents.forEach { document ->
                    try {
                        val post = document.toObject(Post::class.java)?.apply {
                            postId = document.id
                            // Handle field name mismatch
                            productAvailability = document.getString("productAvailableIlity") ?: Post.AVAIL_IN_STOCK
                        }
                        post?.let { postList.add(it) }
                    } catch (e: Exception) {
                        Log.e("MyPostFragment", "Error parsing document", e)
                    }
                }

                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
                updateEmptyView()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error loading posts: ${e.message}", Toast.LENGTH_SHORT).show()
                updateEmptyView()
            }
    }


    private fun updateEmptyView() {
        if (postList.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.findViewById<TextView>(R.id.emptyText).text =
                getString(R.string.no_posts_found)
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
}

