package com.example.campusbuddy.Fragment


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        inflater: LayoutInflater, container: ViewGroup?,
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

        loadPosts()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    private fun loadPosts() {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            Firebase.firestore.collection("posts")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener { result ->
                    val tempList = arrayListOf<Post>()
                    for (document in result.documents) {
                        val post: Post? = document.toObject(Post::class.java)
                        post?.let {
                            tempList.add(it)
                        }
                    }
                    postList.clear()
                    postList.addAll(tempList)
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e("MyPostFragment", "Error loading posts: ${exception.message}")
                }
        } else {
            Log.e("MyPostFragment", "User not logged in")
        }
    }
}

