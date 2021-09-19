package com.timothy.gogolook.ui.adapters

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.timothy.gogolook.data.Repository
import com.timothy.gogolook.data.model.HitsItem
import com.timothy.gogolook.data.model.LoadingStatusMutableLiveData
import io.reactivex.disposables.CompositeDisposable

class ImageSearchResultDataSourceFactory(
    private val repository: Repository,
    private val compositeDisposable: CompositeDisposable,
    private val searchTerms: LiveData<String>,
    private val loadStatus: LoadingStatusMutableLiveData
) : DataSource.Factory<Int, HitsItem>()  {

    lateinit var dataSource: ImageSearchResultPagedDataSource
        private set

    override fun create(): DataSource<Int, HitsItem> {
        dataSource = ImageSearchResultPagedDataSource(repository,compositeDisposable,searchTerms,loadStatus)
        return dataSource
    }
}