package com.example.campusbuddy.Models

import java.io.Serializable

data class VideoPost(
    var videoId: String? = null,
    var userId: String? = null,
    var username: String? = null,
    var title: String? = null,
    var description: String? = null,
    var videoUrl: String? = null,
    var thumbnailUrl: String? = null,
    var timestamp: Long? = null,
    var views: Int = 0,
    var likes: Int = 0
) : Serializable {
    // Default constructor (required for Firestore)
    constructor() : this(null, null, null, null, null, null, null, null, 0, 0)
}
