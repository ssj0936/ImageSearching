package com.timothy.gogolook.ui.adapters

import androidx.lifecycle.LiveData
import androidx.paging.PageKeyedDataSource
import com.timothy.gogolook.data.Repository
import com.timothy.gogolook.data.model.HitsItem
import com.timothy.gogolook.data.model.LoadingStatusMutableLiveData
import com.timothy.gogolook.data.model.PixabaySearchResponse
import com.timothy.gogolook.data.network.IMAGE_SEARCH_INITIAL_KEY
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ImageSearchResultPagedDataSource (
    private val repository: Repository,
    private val compositeDisposable: CompositeDisposable,
    private val searchTerms: LiveData<String>,
    private val loadStatus: LoadingStatusMutableLiveData
): PageKeyedDataSource<Int, HitsItem>() {
    var initParams:LoadInitialParams<Int>? = null
    var lastParams:LoadParams<Int>? = null
    var initCallback:LoadInitialCallback<Int,HitsItem>? = null
    var lastCallback:LoadCallback<Int,HitsItem>? = null

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int,HitsItem>
    ) {
        initParams = params
        initCallback = callback

        searchTerms.value?.let{
            loadStatus.setLoading()
            repository.getSearchImages(it,IMAGE_SEARCH_INITIAL_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {response ->
                        if(response.isSuccessful){
                            val result = response.body() as PixabaySearchResponse
                            if(result.hits!=null){
                                callback.onResult(result.hits,null,IMAGE_SEARCH_INITIAL_KEY+1)
                            }else{
                                Timber.d("ERROR loadInitial: null list")
                            }
                        }else{
                            Timber.d("ERROR loadInitial: response fail")
                        }
                        loadStatus.setLoadingFinish()
                    },
                    onError = { error ->
                        Timber.d("ERROR loadInitial: $error")
                        loadStatus.setError()
                    }
                )
                .addTo(compositeDisposable)
        }
    }

    override fun loadAfter(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, HitsItem>
    ) {
        lastParams = params
        lastCallback = callback

        searchTerms.value?.let {
            loadStatus.setLoading()
            repository.getSearchImages(it, params.key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { response ->
                        if (response.isSuccessful) {
                            val result = response.body() as PixabaySearchResponse
                            if (result.hits != null) {
                                callback.onResult(result.hits, params.key + 1)
                            }else{
                                Timber.d("ERROR loadInitial: null list")
                            }
                        } else {
                            Timber.d("ERROR loadAfter: response fail")
                        }
                        loadStatus.setLoadingFinish()
                    },
                    onError = { error ->
                        Timber.d("ERROR loadAfter: $error")
                        loadStatus.setError()
                    }
                )
                .addTo(compositeDisposable)
        }
    }

    override fun loadBefore(
        params: LoadParams<Int>,
        callback: LoadCallback<Int,HitsItem>
    ) {}

    fun retry(){
        when{
            isLoadInitialSet() -> loadInitial(initParams ?: return, initCallback ?: return)
            isLoadAfterSet() -> loadAfter(lastParams ?: return, lastCallback ?: return)
        }
    }

    private fun isLoadInitialSet() : Boolean =
        initParams!=null && initCallback!=null && !isLoadAfterSet()

    private fun isLoadAfterSet() : Boolean =
        lastParams!=null && lastCallback!=null
}