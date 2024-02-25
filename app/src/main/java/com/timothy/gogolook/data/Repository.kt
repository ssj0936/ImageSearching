package com.timothy.gogolook.data

import com.timothy.gogolook.data.model.PixabaySearchResponse
import com.timothy.gogolook.data.network.PixabayService
import retrofit2.Response
import java.util.LinkedList
import javax.inject.Inject

class Repository @Inject constructor(
    private val pixabayService: PixabayService,
    private val searchTermsHistoryService: SearchTermsHistoryService
){
    suspend fun getSearchImages(searchTerms:String, page:Int = 1): Response<PixabaySearchResponse>
        = pixabayService.getImages(searchTerms,page)

    fun getHistoryTerms(): LinkedList<String> = searchTermsHistoryService.getHistoryTerms()

    fun saveHistoryTerms(termsList:LinkedList<String>) = searchTermsHistoryService.saveHistoryTerms(termsList)
}