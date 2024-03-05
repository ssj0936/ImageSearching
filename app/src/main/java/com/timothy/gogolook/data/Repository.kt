package com.timothy.gogolook.data

import com.timothy.gogolook.data.local.SearchTermsHistoryService
import com.timothy.gogolook.data.model.PixabaySearchResponse
import com.timothy.gogolook.data.network.PixabayService
import com.timothy.gogolook.util.LRUCache
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor(
    private val pixabayService: PixabayService,
    private val searchTermsHistoryService: SearchTermsHistoryService
){
    suspend fun getSearchImages(searchTerms:String, page:Int = 1, pageSize:Int): Response<PixabaySearchResponse>
        = pixabayService.getImages(query = searchTerms, page = page, perPage = pageSize)

    fun getHistoryTerms(): LRUCache<String> = searchTermsHistoryService.getHistoryTerms()

    fun saveHistoryTerms(termsList:LRUCache<String>) = searchTermsHistoryService.saveHistoryTerms(termsList)
}