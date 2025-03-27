package com.example.campusbuddy.Fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusbuddy.HomeActivity
import com.example.campusbuddy.Models.User

import com.example.campusbuddy.adapters.SearchAdapter
import com.example.campusbuddy.databinding.FragmentSearchBinding
import com.example.campusbuddy.utils.USER_NODE
import com.example.campusbuddy.utils.FOLLOW
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    lateinit var adapter: SearchAdapter
    var userList = ArrayList<User>()
    var followedUsersList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.searchRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        adapter = SearchAdapter(
            requireContext(),
            userList,
            onFollowStatusChanged = {
                (activity as? HomeActivity)?.refreshHomeFragment()
            }
        )
        binding.searchRecyclerview.adapter = adapter

        fetchFollowedUsers()
        fetchAllUsers()

        binding.searchButton.setOnClickListener {
            val text = binding.searchView.text.toString()
            searchUser(text)
        }

        return binding.root
    }

    private fun fetchFollowedUsers() {
        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
            .get()
            .addOnSuccessListener { querySnapshot ->
                followedUsersList.clear()
                for (document in querySnapshot.documents) {
                    followedUsersList.add(document.getString("userId") ?: "")
                }
            }
    }

    private fun fetchAllUsers() {
        Firebase.firestore.collection(USER_NODE).get().addOnSuccessListener { querySnapshot ->
            val tempList = ArrayList<User>()
            userList.clear()
            for (document in querySnapshot.documents) {
                if (document.id != Firebase.auth.currentUser!!.uid) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        tempList.add(it)
                    }
                }
            }
            userList.addAll(tempList)
            adapter.notifyDataSetChanged()
        }
    }

    private fun searchUser(query: String) {
        Firebase.firestore.collection(USER_NODE).whereEqualTo("name", query).get()
            .addOnSuccessListener { querySnapshot ->
                val tempList = ArrayList<User>()
                userList.clear()
                if (querySnapshot.documents.isNotEmpty()) {
                    for (document in querySnapshot.documents) {
                        if (document.id != Firebase.auth.currentUser!!.uid) {
                            val user = document.toObject(User::class.java)
                            user?.let {
                                tempList.add(it)
                            }
                        }
                    }
                }
                userList.addAll(tempList)
                adapter.notifyDataSetChanged()
            }
    }
}