package com.timothy.gogolook.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.timothy.gogolook.data.Repository
import com.timothy.gogolook.data.model.*
import com.timothy.gogolook.ui.adapters.ImageSearchResultPagedDataSource
import com.timothy.gogolook.util.HISTORY_MAX_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    //for loading progressbar display
    val loadStatus : LiveData<LoadingStatus>
        get() = _loadStatus
    private val _loadStatus = LoadingStatusMutableLiveData().apply {
        setSuccess()
    }

    //recording current search terms
    val searchTerms : LiveData<String>
        get() = _searchTerms
    private val _searchTerms = MutableLiveData<String>().apply {
        value = "flower yellow"
    }

    //get init data from repository(from sharedPreference)
    //save to repository when viewmodel onClear
    val searchTermsHistory:MediatorLiveData<Queue<String>> = MediatorLiveData()

    private val flowPagingSource = MutableStateFlow(ImageSearchResultPagedDataSource(repository, _searchTerms.value!!))
    val pagingFlow = flowPagingSource.flatMapLatest {
            Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = {it}
            ).flow.cachedIn(viewModelScope)
    }

    init {
        initSearchTermsHistory()

        //searchTermsHistory listen for update of searchTerms
        searchTermsHistory.addSource(_searchTerms){
            val tmpTermsList = searchTermsHistory.value ?: LinkedList()
            val index = tmpTermsList.indexOf(it)

            //not found, push into queue
            if(index == -1){
                tmpTermsList.offer(it)
            }else{
                tmpTermsList.remove(tmpTermsList.elementAt(index))
                tmpTermsList.offer(it)
            }
            //pop those element out of range
            while(tmpTermsList.size > HISTORY_MAX_SIZE)
                tmpTermsList.poll()

            searchTermsHistory.value = tmpTermsList

            //save the history
            saveSearchTermsHistory()
        }
    }

    private fun initSearchTermsHistory(){
        searchTermsHistory.value = repository.getHistoryTerms()
    }

    private fun saveSearchTermsHistory(){
        if(!searchTermsHistory.value.isNullOrEmpty()) {
            repository.saveHistoryTerms(searchTermsHistory.value!!)
        }
    }

    fun updateSearchTerms(terms:String){
        _searchTerms.value = terms
        viewModelScope.launch {
            flowPagingSource.emit(ImageSearchResultPagedDataSource(repository, terms))
        }
    }

    fun retry(){
//        dataSourceFactory.dataSource.retry()
    }

    override fun onCleared() {
        super.onCleared()
//        compositeDisposable.clear()
    }
}