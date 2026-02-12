package com.netflixclone.data.repository

import com.netflixclone.network.services.ApiService
import com.netflixclone.network.services.GoogleLoginRequest
import com.netflixclone.network.services.LoginRequest
import com.netflixclone.network.services.TokenResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun login(email: String, password: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(
                    LoginRequest(email, password)
                )
                Result.success(response)

            } catch (e: Exception) {
                Result.failure(Exception(parseError(e)))
            }
        }
    }

    suspend fun signup(email: String, password: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.signup(
                    LoginRequest(email = email, password = password)
                )
                Result.success(response)

            } catch (e: Exception) {
                Result.failure(Exception(parseError(e)))
            }
        }
    }

    suspend fun googleLogin(idToken: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.googleLogin(
                    GoogleLoginRequest(idToken)
                )
                Result.success(response)

            } catch (e: Exception) {
                Result.failure(Exception(parseError(e)))
            }
        }
    }

    /**
     * Verifies that the session is active by making a request to a protected endpoint
     * Used after GitHub OAuth callback to confirm authentication
     */
    suspend fun verifySession(): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Make a test request to a protected endpoint
                // If it succeeds, the session cookie is valid
                apiService.getTags()
                
                // Return success with dummy token response
                Result.success(
                    TokenResponse(
                        token = "session_active",
                        subscribed = true,
                        message = "Session verified"
                    )
                )
            } catch (e: Exception) {
                Result.failure(Exception(parseError(e)))
            }
        }
    }

    private fun parseError(e: Exception): String {
        if (e is retrofit2.HttpException) {
            return try {
                val errorBody = e.response()?.errorBody()?.string()
                if (errorBody != null) {
                    val json = org.json.JSONObject(errorBody)
                    json.optString("error", json.optString("message", "Unknown Error"))
                } else {
                    e.message ?: "Unknown Error"
                }
            } catch (e2: Exception) {
                e.message ?: "Unknown Error"
            }
        }
        return e.message ?: "Unknown Error"
    }
}
