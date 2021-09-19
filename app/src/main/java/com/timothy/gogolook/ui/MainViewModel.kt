package com.timothy.gogolook.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.timothy.gogolook.data.Repository
import com.timothy.gogolook.data.model.*
import com.timothy.gogolook.ui.adapters.ImageSearchResultDataSourceFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    val loadStatus : LiveData<LoadingStatus>
        get() = _loadStatus
    private val _loadStatus = LoadingStatusMutableLiveData().apply {
        setLoadingFinish()
    }

    private val searchTerms = MutableLiveData<String>().apply {
        value = "flower yellow"
    }
    private val dataSourceFactory:ImageSearchResultDataSourceFactory =
        ImageSearchResultDataSourceFactory(repository, compositeDisposable, searchTerms, _loadStatus)
    val pagedList: LiveData<PagedList<HitsItem>>
    init {

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(5)
            .build()

        pagedList = LivePagedListBuilder(dataSourceFactory, config).build()
    }

    fun updateSearchTerms(terms:String){
        searchTerms.value = terms
        pagedList.value?.dataSource?.invalidate()
    }

    fun retry(){
        dataSourceFactory.dataSource.retry()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}