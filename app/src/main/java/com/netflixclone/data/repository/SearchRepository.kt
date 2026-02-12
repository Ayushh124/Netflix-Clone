package com.netflixclone.data.repository

import com.netflixclone.data_models.Movie
import com.netflixclone.network.services.ApiClient
import com.netflixclone.network.services.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.netflixclone.data_models.MovieTag

class SearchRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getTags(): Result<List<MovieTag>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTags()
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchVideos(query: String?, tagIds: String?): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                // Use the injected apiService with proper authentication
                val response = apiService.getMoviesFiltered(tag = null, search = query)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun searchVideosByTags(query: String?, tagIds: String?): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                // Call backend with tags parameter
                val response = apiService.getMoviesByTags(tags = tagIds, search = query)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun <T> Call<T>.await(): T = suspendCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    continuation.resume(response.body()!!)
                } else {
                     // Check if body is null but Success code? Usually unlikely for List<T>
                    continuation.resumeWithException(Exception("Error: ${response.code()} ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }
}