package com.example.campusbuddy.adapters

import User
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


import com.example.campusbuddy.databinding.SearchRvBinding
import com.example.campusbuddy.utils.FOLLOW
import com.example.campusbuddy.utils.USER_NODE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchAdapter(
    var context: Context,
    var userList: ArrayList<User>,
    private val onFollowStatusChanged: () -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    inner class ViewHolder(var binding: SearchRvBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SearchRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.username.text = user.name

        Firebase.firestore.collection(USER_NODE)
            .whereEqualTo("email", user.email)
            .get()
            .addOnSuccessListener { userSnapshot ->
                if (userSnapshot.documents.isNotEmpty()) {
                    val userId = userSnapshot.documents[0].id
                    checkFollowStatus(userId, holder)

                    holder.binding.btnFollow.setOnClickListener {
                        toggleFollowStatus(userId, user.name!!, holder)
                    }
                }
            }
    }

    private fun checkFollowStatus(userId: String, holder: ViewHolder) {
        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val isFollow = querySnapshot.documents.isNotEmpty()
                holder.binding.btnFollow.text = if (isFollow) "Unfollow" else "Follow"
                holder.binding.btnFollow.tag = isFollow
            }
    }

    private fun toggleFollowStatus(userId: String, userName: String, holder: ViewHolder) {
        val isCurrentlyFollowing = holder.binding.btnFollow.tag as? Boolean ?: false

        if (isCurrentlyFollowing) {
            Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        querySnapshot.documents[0].reference.delete()
                            .addOnSuccessListener {
                                holder.binding.btnFollow.text = "Follow"
                                holder.binding.btnFollow.tag = false
                                onFollowStatusChanged()
                            }
                    }
                }
        } else {
            val followData = hashMapOf(
                "userId" to userId,
                "name" to userName
            )

            Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
                .document()
                .set(followData)
                .addOnSuccessListener {
                    holder.binding.btnFollow.text = "Unfollow"
                    holder.binding.btnFollow.tag = true
                    onFollowStatusChanged()
                }
        }
    }
}