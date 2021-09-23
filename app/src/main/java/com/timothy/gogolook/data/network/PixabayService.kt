package com.timothy.gogolook.data.network

import com.timothy.gogolook.data.model.PixabaySearchResponse
import com.timothy.gogolook.util.IMAGE_SEARCH_PAGE_SIZE
import com.timothy.gogolook.util.KEY
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayService {
    @GET("/api?")
    fun getImages(
        @Query("q") query:String,
        @Query("page") page:Int,
        @Query("per_page") perPage:Int = IMAGE_SEARCH_PAGE_SIZE,
        @Query("key") key: String= KEY
    ): Single<Response<PixabaySearchResponse>>
}