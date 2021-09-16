package com.timothy.gogolook.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.timothy.gogolook.data.Repository
import com.timothy.gogolook.data.model.HitsItem
import com.timothy.gogolook.data.model.PixabaySearchResponse
import com.timothy.gogolook.data.model.ResultOf
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    val imagesSearchResult:LiveData<ResultOf<List<HitsItem>>>
        get() = _imagesSearchResult
    private val _imagesSearchResult = MutableLiveData<ResultOf<List<HitsItem>>>()

    fun searchImages(searchTerms:String){
        repository.getSearchImages(searchTerms)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy (
                onSuccess = { response ->
                    if(response.isSuccessful){
                        val result = response.body() as PixabaySearchResponse
                        if(result.hits!=null){
                            _imagesSearchResult.value = ResultOf.Success(result.hits)
                        }
                    }else{
                        _imagesSearchResult.value = ResultOf.Failure("error please retry")
                    }
                },
                onError = { error ->
                    Timber.d("ERROR loadInitial: $error")
                }
            )
            .addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}