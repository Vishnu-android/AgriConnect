package com.example.campusbuddy.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.campusbuddy.R
import com.example.campusbuddy.databinding.FragmentAddBinding
import com.example.campusbuddy.post.PostActivity
import com.example.campusbuddy.post.VideoActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAddBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddBinding.inflate(inflater, container, false)

        // Set up the click listener for the "Post" button
        binding.post.setOnClickListener {
            activity?.startActivity(Intent(requireContext(), PostActivity::class.java))
        }
        binding.video.setOnClickListener {
activity?.startActivity(Intent(requireContext(), VideoActivity::class.java))
        }

        return binding.root
    }
}
