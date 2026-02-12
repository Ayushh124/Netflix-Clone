package com.netflixclone.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netflixclone.data_models.Media
import com.netflixclone.data_models.Movie
import com.netflixclone.data_models.MovieTag
import com.netflixclone.network.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchResultsViewModel @Inject constructor(
    private val apiService: com.netflixclone.network.services.ApiService
) : ViewModel() {

    val popularMoviesLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val popularMovies: MutableLiveData<List<Movie>> = MutableLiveData()

    val searchResultsLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val searchResults: MutableLiveData<List<Movie>> = MutableLiveData()
    val tags: MutableLiveData<List<MovieTag>> = MutableLiveData()

    fun fetchPopularMovies() {
        viewModelScope.launch(Dispatchers.IO) {
            popularMoviesLoading.postValue(true)
            try {
                // Use local API instead of TMDB
                val response = apiService.getMoviesFiltered()
                popularMovies.postValue(response)
                popularMoviesLoading.postValue(false)
            } catch (e: Exception) {
                popularMoviesLoading.postValue(false)
            }
        }
    }
    
    fun fetchTags() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Use local API
                val response = apiService.getTags()
                tags.postValue(response)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun fetchSearchResults(query: String, tag: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            searchResultsLoading.postValue(true)
            try {
                // Use local API
                val movies = apiService.getMoviesFiltered(tag = tag, search = query)
                searchResults.postValue(movies)
                searchResultsLoading.postValue(false)
            } catch (e: Exception) {
                searchResultsLoading.postValue(false)
                searchResults.postValue(emptyList()) 
            }
        }
    }
}