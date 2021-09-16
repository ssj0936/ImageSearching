package com.timothy.gogolook.data

import com.timothy.gogolook.data.model.PixabaySearchResponse
import com.timothy.gogolook.data.network.PixabayService
import io.reactivex.Single
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor(
    private val pixabayService: PixabayService
){
    fun getSearchImages(searchTerms:String): Single<Response<PixabaySearchResponse>>
        = pixabayService.getImages(searchTerms)
}