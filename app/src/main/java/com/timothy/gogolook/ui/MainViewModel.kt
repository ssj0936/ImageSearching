package com.timothy.gogolook.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.timothy.gogolook.data.HISTORY_MAX_SIZE
import com.timothy.gogolook.data.Repository
import com.timothy.gogolook.data.model.*
import com.timothy.gogolook.ui.adapters.ImageSearchResultDataSourceFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    //for loading progressbar display
    val loadStatus : LiveData<LoadingStatus>
        get() = _loadStatus
    private val _loadStatus = LoadingStatusMutableLiveData().apply {
        setLoadingFinish()
    }

    //recording current search terms
    private val searchTerms = MutableLiveData<String>().apply {
        value = "flower yellow"
    }

    //get init data from repository(from sharedPreference)
    //save to repository when viewmodel onClear
    val searchTermsHistory:MediatorLiveData<Queue<String>> = MediatorLiveData()

    //paged recyclerview setup
    private val dataSourceFactory:ImageSearchResultDataSourceFactory =
        ImageSearchResultDataSourceFactory(repository, compositeDisposable, searchTerms, _loadStatus)
    val pagedList: LiveData<PagedList<HitsItem>>

    init {
        initSearchTermsHistory()

        //searchTermsHistory listen for update of searchTerms
        searchTermsHistory.addSource(searchTerms){
            val tmpTermsList = searchTermsHistory.value ?: LinkedList()
            val index = tmpTermsList.indexOf(it)

            //not found, push into queue
            if(index == -1){
                tmpTermsList.offer(it)
                //pop those element out of range
                while(tmpTermsList.size > HISTORY_MAX_SIZE)
                    tmpTermsList.poll()

                searchTermsHistory.value = tmpTermsList
            }else{
                tmpTermsList.remove(tmpTermsList.elementAt(index))
                tmpTermsList.offer(it)
                //pop those element out of range
                while(tmpTermsList.size > HISTORY_MAX_SIZE)
                    tmpTermsList.poll()

                searchTermsHistory.value = tmpTermsList
            }
            //save the history
            saveSearchTermsHistory()
        }

        //recyclerview datasource settings
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(5)
            .build()

        pagedList = LivePagedListBuilder(dataSourceFactory, config).build()
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