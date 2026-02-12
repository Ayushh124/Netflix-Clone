package com.netflixclone.network.models

data class User(
    val email: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val token: String,  // Capture JWT token
    val subscribed: Boolean // Capture subscription status

)
