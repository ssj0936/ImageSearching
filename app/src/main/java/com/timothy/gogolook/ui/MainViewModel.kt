package com.timothy.gogolook.ui

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.timothy.gogolook.data.Repository
import com.timothy.gogolook.data.model.LoadingStatus
import com.timothy.gogolook.ui.adapters.ImageSearchResultPagedDataSource
import com.timothy.gogolook.util.HISTORY_MAX_SIZE
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
import java.util.LinkedList
import javax.inject.Inject

data class UIState(
    val loadState: LoadingStatus,
    val searchTerms: String
) : ViewModelState

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : BaseViewModel<UIState>() {
    override fun initState(): UIState =
        UIState(loadState = LoadingStatus.Loading, searchTerms = "flower yellow")

    val searchTermsHistory: StateFlow<LinkedList<String>> = uiState.map { it.searchTerms }.mapLatest {searchTerms->
        viewModelScope.async {
            val tmpTermsList = getSearchTermsHistory()
            val index = tmpTermsList.indexOf(searchTerms)

            //not found, push into queue
            if (index == -1) {
                tmpTermsList.offer(searchTerms)
            } else {
                tmpTermsList.remove(tmpTermsList.elementAt(index))
                tmpTermsList.offer(searchTerms)
            }
            //pop those element out of range
            while (tmpTermsList.size > HISTORY_MAX_SIZE)
                tmpTermsList.poll()

            saveSearchTermsHistory(tmpTermsList)

            tmpTermsList
        }.await()
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(3000),
        initialValue = LinkedList()
    )

    private val flowPagingSource = MutableStateFlow(ImageSearchResultPagedDataSource(
        repository = repository,
        searchTerms = currentState.searchTerms,
        onLoading = { setState { copy(loadState = LoadingStatus.Loading) } },
        onLoadingFinish = { setState { copy(loadState = LoadingStatus.Success) } },
        onLoadingFail = { setState { copy(loadState = LoadingStatus.Error(it)) } }
    ))

    val pagingFlow = flowPagingSource.flatMapLatest {
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { it }
        ).flow.cachedIn(viewModelScope)
    }

    private suspend fun saveSearchTermsHistory(value: LinkedList<String>) =
        withContext(Dispatchers.IO) {
            if (!value.isEmpty()) {
                repository.saveHistoryTerms(value)
            }
        }

    private suspend fun getSearchTermsHistory(): LinkedList<String> =
        withContext(Dispatchers.IO) { repository.getHistoryTerms() }

    fun updateSearchTerms(terms: String) {
        setState { copy(searchTerms = terms) }

        viewModelScope.launch {
            flowPagingSource.emit(ImageSearchResultPagedDataSource(repository, terms))
        }
    }

    fun retry() {
//        dataSourceFactory.dataSource.retry()
    }
}