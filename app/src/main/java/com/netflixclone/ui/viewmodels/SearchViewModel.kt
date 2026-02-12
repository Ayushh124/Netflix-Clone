package com.netflixclone.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netflixclone.data.repository.SearchRepository
import com.netflixclone.data_models.Movie
import com.netflixclone.data_models.MovieTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState

    private val _tags = MutableStateFlow<List<MovieTag>>(emptyList())
    val tags: StateFlow<List<MovieTag>> = _tags

    // Track multiple selected tag IDs
    private val _selectedTagIds = mutableStateListOf<Int>()
    val selectedTagIds: List<Int> = _selectedTagIds

    private var _currentQuery: String = ""

    private var searchJob: Job? = null

    init {
        fetchTags()
    }

    fun fetchTags() {
        viewModelScope.launch {
            val result = searchRepository.getTags()
            if (result.isSuccess) {
                _tags.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _currentQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            performSearch()
        }
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
        
        // Perform search with updated tags
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            performSearch()
        }
    }

    /**
     * Check if a tag is currently selected
     */
    fun isTagSelected(tagId: Int): Boolean {
        return _selectedTagIds.contains(tagId)
    }

    /**
     * Reset all filters (clear tags and search query)
     */
    fun resetFilters() {
        _selectedTagIds.clear()
        _currentQuery = ""
        _searchState.value = SearchState.Idle
    }

    /**
     * Perform search with current query and selected tags
     */
    private suspend fun performSearch() {
        _searchState.value = SearchState.Loading
        
        val query = if (_currentQuery.isBlank()) null else _currentQuery
        val tagIds = if (_selectedTagIds.isEmpty()) null else _selectedTagIds.joinToString(",")
        
        val result = searchRepository.searchVideosByTags(query, tagIds)
        if (result.isSuccess) {
            _searchState.value = SearchState.Success(result.getOrNull() ?: emptyList())
        } else {
            _searchState.value = SearchState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
        }
    }
}

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val movies: List<Movie>) : SearchState()
    data class Error(val message: String) : SearchState()
}
