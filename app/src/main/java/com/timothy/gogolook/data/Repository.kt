package com.timothy.gogolook.data

import com.timothy.gogolook.data.model.PixabaySearchResponse
import com.timothy.gogolook.data.network.PixabayService
import com.timothy.gogolook.util.LRUCache
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor(
    private val pixabayService: PixabayService,
    private val searchTermsHistoryService: SearchTermsHistoryService
){
    suspend fun getSearchImages(searchTerms:String, page:Int = 1): Response<PixabaySearchResponse>
        = pixabayService.getImages(searchTerms,page)

    fun getHistoryTerms(): LRUCache<String> = searchTermsHistoryService.getHistoryTerms()

    fun saveHistoryTerms(termsList:LRUCache<String>) = searchTermsHistoryService.saveHistoryTerms(termsList)
}