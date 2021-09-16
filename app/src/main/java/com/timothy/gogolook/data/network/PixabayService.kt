package com.timothy.gogolook.data.network

import com.timothy.gogolook.data.model.PixabaySearchResponse
import com.timothy.gogolook.util.KEY
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


const val IMAGE_SEARCH_INITIAL_KEY = 0L
const val IMAGE_SEARCH_PAGE_SIZE = 30

interface PixabayService {
    @GET("/api?")
    fun getImages(
        @Query("q") query:String,
//        @Query("page") page:Int,
        @Query("key") key: String= KEY
    ): Single<Response<PixabaySearchResponse>>

}