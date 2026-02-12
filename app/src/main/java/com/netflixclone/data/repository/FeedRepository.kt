package com.netflixclone.data.repository

import com.netflixclone.data_models.Movie
import com.netflixclone.network.models.MovieDetailsResponse
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

class FeedRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getMovies(): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMovies()
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getFeaturedMovies(): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFeaturedMovies()
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getMoviesByCategory(category: String): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMoviesByCategory(category)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getTags(): Result<List<com.netflixclone.data_models.MovieTag>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTags()
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getMoviesByTag(tag: String): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMoviesFiltered(tag = tag)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getMovieDetails(id: String): Result<MovieDetailsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.TMDB.fetchMovieDetails(id.toInt())
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Helper removed
}