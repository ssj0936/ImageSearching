package com.timothy.gogolook.ui

import androidx.lifecycle.viewModelScope
import com.timothy.gogolook.data.Repository
import com.timothy.gogolook.data.model.HitsItem
import com.timothy.gogolook.util.DEFAULT_LAYOUT_TYPE
import com.timothy.gogolook.util.IMAGE_SEARCH_PAGE_SIZE
import com.timothy.gogolook.util.LAYOUT_TYPE_GRID
import com.timothy.gogolook.util.LRUCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

data class DataWrapper(
    val dataList: List<HitsItem> = emptyList(),
    val page: Int = 1
)

data class UIState(
    val searchTerms: String,
    val isGrid: Boolean,
    val dataWrapper: DataWrapper
) : ViewModelState

sealed class UIEvent : ViewModelEvent {
    data class OnSearch(val searchTerm: String) : UIEvent()
    object OnLoadNewPage : UIEvent()
    data class OnLayoutToggle(val isGrid: Boolean) : UIEvent()
}

sealed class UIEffect : ViewModelEffect {
    data class OnSnackBarShow(val msg: String) : UIEffect()
    object OnLoadingSuccess : UIEffect()
}

data class PagingState(
    var currPage: Int? = 1,
    var prevPage: Int? = null,
    var nextPage: Int? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : BaseViewModel<UIState, UIEvent, UIEffect>() {

    private var isLoading = MutableStateFlow(false)
    private var pagingState = PagingState()

    override fun initState(): UIState =
        UIState(
            searchTerms = "flower yellow",
            isGrid = DEFAULT_LAYOUT_TYPE == LAYOUT_TYPE_GRID,
            dataWrapper = DataWrapper()
        )

    override fun handleEvent(event: UIEvent) {
        when (event) {
            is UIEvent.OnSearch -> {
                onSearch(event.searchTerm)
            }

            is UIEvent.OnLayoutToggle -> {
                toggleRecyclerViewLayout(event.isGrid)
            }

            is UIEvent.OnLoadNewPage -> {
                onLoadNewPage()
            }
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

    private suspend fun saveSearchTermsHistory(value: LRUCache<String>) =
        withContext(Dispatchers.IO) {
            if (!value.isEmpty()) {
                repository.saveHistoryTerms(value)
            }
        }

    private suspend fun getSearchTermsHistory(): LRUCache<String> =
        withContext(Dispatchers.IO) { repository.getHistoryTerms() }

    private fun onSearch(terms: String) = viewModelScope.launch {
        setState { copy(searchTerms = terms) }
        val initSize = IMAGE_SEARCH_PAGE_SIZE * 3
        val resp =
            repository.getSearchImages(searchTerms = terms, pageSize = initSize)

        if (!resp.isSuccessful || resp.body()?.hits == null) {
            setState { copy(dataWrapper = DataWrapper()) }
            return@launch
        }
        pagingState.apply {
            currPage = 1
            prevPage = null
            nextPage =
                if (resp.body()!!.hits!!.size < initSize) null else (resp.body()!!.hits!!.size / IMAGE_SEARCH_PAGE_SIZE + 1)
        }

        setState { copy(dataWrapper = DataWrapper(dataList = resp.body()!!.hits!!)) }
    }

    private fun onLoadNewPage() {
        if (isLoading.value || pagingState.nextPage == null) return

        viewModelScope.launch {
            isLoading.value = true

            val newPage = pagingState.nextPage!!
            val resp = repository.getSearchImages(
                searchTerms = currentState.searchTerms,
                page = newPage,
                pageSize = IMAGE_SEARCH_PAGE_SIZE
            )

            if (!resp.isSuccessful || resp.body()?.hits == null) {
                Timber.e(resp.message())
            } else {
                pagingState.apply {
                    prevPage = currPage
                    currPage = newPage
                    nextPage =
                        if (resp.body()!!.hits!!.size < IMAGE_SEARCH_PAGE_SIZE) null else newPage + 1
                }

                val dataList = currentState.dataWrapper.dataList.toMutableList()
                    .apply { addAll(resp.body()!!.hits!!) }
                setState { copy(dataWrapper = DataWrapper(dataList = dataList, page = newPage)) }
            }

            isLoading.value = false
        }
    }

    private fun toggleRecyclerViewLayout(isGrid: Boolean) {
        setState { copy(isGrid = isGrid) }
    }
}