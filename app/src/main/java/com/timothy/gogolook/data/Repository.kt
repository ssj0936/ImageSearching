package com.timothy.gogolook.data

import com.timothy.gogolook.data.local.SearchTermsHistoryLocal
import com.timothy.gogolook.data.model.PixabaySearchResponse
import com.timothy.gogolook.data.network.PixabayService
import io.reactivex.Single
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class Repository @Inject constructor(
    private val pixabayService: PixabayService,
    private val SearchTermsHistoryService: SearchTermsHistoryService
){
    fun getSearchImages(searchTerms:String, page:Int = 1): Single<Response<PixabaySearchResponse>>
        = pixabayService.getImages(searchTerms,page)

    fun getHistoryTerms(): Queue<String> = SearchTermsHistoryService.getHistoryTerms()

    fun saveHistoryTerms(termsList:Queue<String>) = SearchTermsHistoryService.saveHistoryTerms(termsList)
}