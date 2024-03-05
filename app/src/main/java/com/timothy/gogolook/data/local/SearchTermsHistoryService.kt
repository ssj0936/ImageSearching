package com.timothy.gogolook.data.local

import com.timothy.gogolook.util.HISTORY_MAX_SIZE
import com.timothy.gogolook.util.LRUCache

interface SearchTermsHistoryService {
    //save latest [size] history terms
    fun saveHistoryTerms(termsList: LRUCache<String>, size:Int = HISTORY_MAX_SIZE)

    //get all history terms
    fun getHistoryTerms(): LRUCache<String>
}