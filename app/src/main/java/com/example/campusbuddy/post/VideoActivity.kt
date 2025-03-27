package com.example.campusbuddy.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.example.campusbuddy.HomeActivity
import com.example.campusbuddy.Models.VideoPost
import com.example.campusbuddy.R
import com.example.campusbuddy.databinding.ActivityVideoBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VideoActivity : AppCompatActivity() {

    private val binding by lazy { ActivityVideoBinding.inflate(layoutInflater) }
    private lateinit var videoView: VideoView
    private lateinit var videoTitleEditText: TextInputEditText
    private lateinit var videoDescriptionEditText: TextInputEditText
    private lateinit var btnSelectVideo: Button
    private lateinit var btnUpload: Button
    private lateinit var btnCancel: Button
    private lateinit var uploadProgressBar: ProgressBar
    private var videoPath: Uri? = null
    private var currentUserRole: String = "Buyer" // Default role

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                videoPath = uri
                videoView.setVideoURI(uri)
                videoView.requestFocus()
                videoView.start()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initializeViews()
        fetchUserRole()
        setupClickListeners()
        initCloudinary()
    }

    private fun initializeViews() {
        videoView = binding.videoView
        videoTitleEditText = binding.videoTitleEditText
        videoDescriptionEditText = binding.videoDescriptionEditText
        btnSelectVideo = binding.btnSelectVideo
        btnUpload = binding.btnUpload
        btnCancel = binding.btnCancel
        uploadProgressBar = binding.uploadProgressBar
    }

    private fun fetchUserRole() {
        Firebase.auth.currentUser?.uid?.let { userId ->
            Firebase.firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    document.getString("role")?.let { role ->
                        currentUserRole = role
                    }
                }
        }
    }

    private fun setupClickListeners() {
        btnSelectVideo.setOnClickListener { openVideoPicker() }

        btnUpload.setOnClickListener {
            if (validateInputs()) {
                uploadProgressBar.visibility = ProgressBar.VISIBLE
                uploadVideoToCloudinary { videoUrl, thumbnailUrl ->
                    saveVideoToFirestore(
                        videoTitleEditText.text.toString().trim(),
                        videoDescriptionEditText.text.toString().trim(),
                        videoUrl,
                        thumbnailUrl
                    )
                }
            }
        }

        btnCancel.setOnClickListener { finishWithRole() }
    }

    private fun validateInputs(): Boolean {
        return when {
            videoPath == null -> {
                Toast.makeText(this, "Please select a video first", Toast.LENGTH_SHORT).show()
                false
            }
            videoTitleEditText.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Please enter video title", Toast.LENGTH_SHORT).show()
                false
            }
            videoDescriptionEditText.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Please enter video description", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun initCloudinary() {
        val config = hashMapOf(
            "cloud_name" to "deo53rj5g",
            "api_key" to "431559213483915",
            "api_secret" to "arDGi18_ajFU6GHmGSS-o6JvVQw"
        )
        MediaManager.init(this, config)
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        videoPickerLauncher.launch(intent)
    }

    private fun uploadVideoToCloudinary(onSuccess: (String, String) -> Unit) {
        videoPath?.let { uri ->
            MediaManager.get().upload(uri)
                .option("resource_type", "video")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Video upload started")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        runOnUiThread {
                            uploadProgressBar.progress = (bytes * 100 / totalBytes).toInt()
                        }
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        try {
                            val videoUrl = resultData["secure_url"]?.toString()
                                ?: resultData["url"]?.toString()?.replace("http://", "https://")
                                ?: throw Exception("Could not extract video URL")

                            val thumbnailUrl = generateThumbnailUrl(videoUrl)
                            onSuccess(videoUrl, thumbnailUrl)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing upload", e)
                            runOnUiThread {
                                uploadProgressBar.visibility = ProgressBar.GONE
                                Toast.makeText(
                                    this@VideoActivity,
                                    "Upload completed but thumbnail processing failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    override fun onError(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {
                        Log.e(TAG, "Upload failed: ${error.description}")
                        runOnUiThread {
                            uploadProgressBar.visibility = ProgressBar.GONE
                            Toast.makeText(
                                this@VideoActivity,
                                "Upload failed: ${error.description}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onReschedule(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {
                        Log.d(TAG, "Upload rescheduled: ${error.description}")
                    }
                }).dispatch(this)
        }
    }

    private fun generateThumbnailUrl(videoUrl: String): String {
        return try {
            videoUrl.replace("/upload/", "/upload/w_400,h_300,c_fill/")
                .substringBeforeLast(".") + ".jpg"
        } catch (e: Exception) {
            Log.e(TAG, "Error generating thumbnail", e)
            "https://res.cloudinary.com/deo53rj5g/image/upload/w_400,h_300,c_fill/video_placeholder.jpg"
        }
    }

    private fun saveVideoToFirestore(title: String, description: String, videoUrl: String, thumbnailUrl: String) {
        Firebase.auth.currentUser?.let { user ->
            val videoPost = VideoPost(
                videoId = null,
                userId = user.uid,
                username = user.displayName ?: "Anonymous",
                title = title,
                description = description,
                videoUrl = videoUrl,
                thumbnailUrl = thumbnailUrl,
                timestamp = System.currentTimeMillis(),
                views = 0,
                likes = 0
            )

            Firebase.firestore.collection("videos")
                .add(videoPost)
                .addOnSuccessListener { docRef ->
                    docRef.update("videoId", docRef.id)
                        .addOnSuccessListener {
                            Log.d(TAG, "Video saved successfully")
                            runOnUiThread {
                                uploadProgressBar.visibility = ProgressBar.GONE
                                Toast.makeText(this, "Video uploaded!", Toast.LENGTH_SHORT).show()
                                finishWithRole()
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error saving video", e)
                    runOnUiThread {
                        uploadProgressBar.visibility = ProgressBar.GONE
                        Toast.makeText(this, "Failed to save video", Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            uploadProgressBar.visibility = ProgressBar.GONE
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finishWithRole() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("ROLE", currentUserRole)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "VideoActivity"
    }
}