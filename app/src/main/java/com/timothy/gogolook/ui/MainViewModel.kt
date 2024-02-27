package com.timothy.gogolook.ui

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.timothy.gogolook.data.Repository
import com.timothy.gogolook.data.model.LoadingStatus
import com.timothy.gogolook.ui.adapters.ImageSearchResultPagedDataSource
import com.timothy.gogolook.ui.adapters.LayoutType
import com.timothy.gogolook.util.DEFAULT_LAYOUT_TYPE
import com.timothy.gogolook.util.LRUCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

data class UIState(
    val loadState: LoadingStatus,
    val searchTerms: String,
    val isGrid: Boolean
) : ViewModelState

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : BaseViewModel<UIState>() {
    override fun initState(): UIState =
        UIState(
            loadState = LoadingStatus.Loading,
            searchTerms = "flower yellow",
            isGrid = DEFAULT_LAYOUT_TYPE is LayoutType.Grid
        )

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

    private val flowPagingSource = MutableStateFlow(getImageSearchResultPagedDataSourceInstance())

    val pagingFlow = flowPagingSource.flatMapLatest {
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { it }
        ).flow.cachedIn(viewModelScope)
    }

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

        viewModelScope.launch {
            flowPagingSource.emit(getImageSearchResultPagedDataSourceInstance())
        }
    }

    fun toggleRecyclerViewLayout(isGrid:Boolean){
        setState { copy(isGrid = isGrid) }
    }

    fun retry() {
//        dataSourceFactory.dataSource.retry()
    }

    private fun getImageSearchResultPagedDataSourceInstance(
        repository: Repository = this.repository,
        searchTerms: String = currentState.searchTerms,
        onLoading:()->Unit = { setState { copy(loadState = LoadingStatus.Loading) }.also { Timber.d("Loading") } },
        onLoadingFinish:()->Unit = { setState { copy(loadState = LoadingStatus.Success) }.also { Timber.d("Success")  }},
        onLoadingFail:(String?)->Unit = { setState { copy(loadState = LoadingStatus.Error(it)) }.also { Timber.d("Error!!")  }},
    ) = ImageSearchResultPagedDataSource(repository, searchTerms, onLoading, onLoadingFinish, onLoadingFail)
}