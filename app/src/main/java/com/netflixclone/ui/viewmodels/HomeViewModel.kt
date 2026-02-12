package com.netflixclone.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netflixclone.data.repository.FeedRepository
import com.netflixclone.data.repository.SearchRepository
import com.netflixclone.data_models.Movie
import com.netflixclone.data_models.MovieTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState: StateFlow<HomeState> = _homeState

    private val _featuredState = MutableStateFlow<HomeState>(HomeState.Loading)
    val featuredState: StateFlow<HomeState> = _featuredState

    private val _tags = MutableStateFlow<List<MovieTag>>(emptyList())
    val tags: StateFlow<List<MovieTag>> = _tags

    // Track multiple selected tag IDs
    private val _selectedTagIds = mutableStateListOf<Int>()
    val selectedTagIds: List<Int> = _selectedTagIds
    
    // Search query for title/description search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        fetchTags()
        fetchFeaturedMovies()
        fetchMovies()
    }

    fun fetchTags() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "🏷️ Fetching tags...")
            val result = searchRepository.getTags()
            if (result.isSuccess) {
                val tagsList = result.getOrNull() ?: emptyList()
                _tags.value = tagsList
                Log.d("HomeViewModel", "✅ Tags fetched successfully: ${tagsList.size} tags")
                tagsList.forEach { tag ->
                    Log.d("HomeViewModel", "  - Tag: ${tag.name} (ID: ${tag.id})")
                }
            } else {
                Log.e("HomeViewModel", "❌ Failed to fetch tags: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun fetchFeaturedMovies() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "🌟 Fetching featured movies...")
            _featuredState.value = HomeState.Loading
            
            val result = feedRepository.getFeaturedMovies()
            
            if (result.isSuccess) {
                val moviesList = result.getOrNull() ?: emptyList()
                _featuredState.value = HomeState.Success(moviesList)
                Log.d("HomeViewModel", "✅ Featured movies fetched: ${moviesList.size} movies")
            } else {
                Log.e("HomeViewModel", "❌ Failed to fetch featured movies: ${result.exceptionOrNull()?.message}")
                _featuredState.value = HomeState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }

    fun fetchMovies() {
        viewModelScope.launch {
            _homeState.value = HomeState.Loading
            
            val query = _searchQuery.value.ifEmpty { null }
            val tagIds = if (_selectedTagIds.isEmpty()) null else _selectedTagIds.joinToString(",")
            
            val result = if (query != null || tagIds != null) {
                // Use search endpoint when there's a search query or tag filters
                searchRepository.searchVideosByTags(query, tagIds)
            } else {
                // Use regular feed endpoint when no filters
                feedRepository.getMovies()
            }
            
            if (result.isSuccess) {
                _homeState.value = HomeState.Success(result.getOrNull() ?: emptyList())
            } else {
                _homeState.value = HomeState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }
    
    /**
     * Update search query and fetch filtered movies
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        fetchMovies()
    }
    
    /**
     * Clear search query
     */
    fun clearSearch() {
        _searchQuery.value = ""
        fetchMovies()
    }

    /**
     * Toggle tag selection (add if not selected, remove if already selected)
     */
    fun toggleTagSelection(tagId: Int) {
        if (_selectedTagIds.contains(tagId)) {
            _selectedTagIds.remove(tagId)
        } else {
            _selectedTagIds.add(tagId)
        }
        
        // Fetch movies with updated tags
        fetchMovies()
    }

    /**
     * Check if a tag is currently selected
     */
    fun isTagSelected(tagId: Int): Boolean {
        return _selectedTagIds.contains(tagId)
    }

    /**
     * Reset all filters and search
     */
    fun resetFilters() {
        _selectedTagIds.clear()
        _searchQuery.value = ""
        fetchMovies()
    }
}

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val movies: List<Movie>) : HomeState()
    data class Error(val message: String) : HomeState()
}
