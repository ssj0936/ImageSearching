package com.timothy.gogolook.data.model

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.MutableLiveData

sealed class LoadingStatus{
    object LOADING:LoadingStatus()
    object ERROR:LoadingStatus()
    object FINISH:LoadingStatus()
}

class LoadingStatusMutableLiveData:MutableLiveData<LoadingStatus>(){
    @SuppressLint("RestrictedApi")
    fun setLoading() {
        when (ArchTaskExecutor.getInstance().isMainThread) {
            true -> this.value = LoadingStatus.LOADING
            false -> this.postValue(LoadingStatus.LOADING)
        }
    }

    @SuppressLint("RestrictedApi")
    fun setError() {
        when (ArchTaskExecutor.getInstance().isMainThread) {
            true -> this.value = LoadingStatus.ERROR
            false -> this.postValue(LoadingStatus.ERROR)
        }
    }

    @SuppressLint("RestrictedApi")
    fun setLoadingFinish() {
        when (ArchTaskExecutor.getInstance().isMainThread) {
            true -> this.value = LoadingStatus.FINISH
            false -> this.postValue(LoadingStatus.FINISH)
        }
    }


}
