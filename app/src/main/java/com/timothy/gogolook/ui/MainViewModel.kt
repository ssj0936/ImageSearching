package com.timothy.gogolook.ui

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.timothy.gogolook.data.Repository
import com.timothy.gogolook.ui.adapters.ImageSearchResultPagedDataSource
import com.timothy.gogolook.ui.adapters.LayoutType
import com.timothy.gogolook.util.DEFAULT_LAYOUT_TYPE
import com.timothy.gogolook.util.IMAGE_SEARCH_PAGE_SIZE
import com.timothy.gogolook.util.LRUCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

data class UIState(
    val searchTerms: String,
    val isGrid: Boolean
) : ViewModelState

sealed class UIEvent:ViewModelEvent{
    data class OnSearch(val searchTerm:String):UIEvent()
    data class OnLayoutToggle(val isGrid: Boolean):UIEvent()
}

sealed class UIEffect:ViewModelEffect{
    data class OnSnackBarShow(val msg:String):UIEffect()
    object OnLoadingSuccess:UIEffect()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : BaseViewModel<UIState, UIEvent, UIEffect>() {
    override fun initState(): UIState =
        UIState(
            searchTerms = "flower yellow",
            isGrid = DEFAULT_LAYOUT_TYPE is LayoutType.Grid
        )

    override fun handleEvent(event: UIEvent) {
        when(event){
            is UIEvent.OnSearch->{}
            is UIEvent.OnLayoutToggle->{
                toggleRecyclerViewLayout(event.isGrid)
                Timber.d("toggleRecyclerViewLayout $event")
            }
            else->{}
        }
    }

    val searchTermsHistory: StateFlow<List<String>> =
        uiState.map { it.searchTerms }.mapLatest { searchTerms ->
            viewModelScope.async {
                val tmpTermsList = getSearchTermsHistory()
                tmpTermsList.add(searchTerms)
                saveSearchTermsHistory(tmpTermsList)

                tmpTermsList.toList()
            }.await()
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(3000),
            initialValue = emptyList()
        )

    val pagingFlow = uiState.map { it.searchTerms }.distinctUntilChanged().flatMapLatest{
        Pager(
            config = PagingConfig(
                pageSize = IMAGE_SEARCH_PAGE_SIZE
            ),
            pagingSourceFactory = { getImageSearchResultPagedDataSourceInstance(searchTerms = it) }
        ).flow
    }.cachedIn(viewModelScope)

    private suspend fun saveSearchTermsHistory(value: LRUCache<String>) =
        withContext(Dispatchers.IO) {
            if (!value.isEmpty()) {
                repository.saveHistoryTerms(value)
            }
        }

    private suspend fun getSearchTermsHistory(): LRUCache<String> =
        withContext(Dispatchers.IO) { repository.getHistoryTerms() }

    fun updateSearchTerms(terms: String) {
        setState { copy(searchTerms = terms) }
    }

    fun toggleRecyclerViewLayout(isGrid:Boolean){
        setState { copy(isGrid = isGrid) }
    }

    private fun getImageSearchResultPagedDataSourceInstance(
        repository: Repository = this.repository,
        searchTerms: String = currentState.searchTerms
    ) = ImageSearchResultPagedDataSource(repository, searchTerms)
}