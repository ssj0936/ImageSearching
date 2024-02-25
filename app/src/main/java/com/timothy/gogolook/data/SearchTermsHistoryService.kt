package com.timothy.gogolook.data

import com.timothy.gogolook.util.HISTORY_MAX_SIZE
import java.util.*

interface SearchTermsHistoryService {
    //save latest [size] history terms
    fun saveHistoryTerms(termsList:Queue<String>, size:Int = HISTORY_MAX_SIZE)

    //get all history terms
    fun getHistoryTerms(): LinkedList<String>
}