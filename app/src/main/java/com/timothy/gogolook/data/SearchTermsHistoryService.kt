package com.timothy.gogolook.data

import java.util.*

const val HISTORY_MAX_SIZE = 8

interface SearchTermsHistoryService {
    //save latest [size] history terms
    fun saveHistoryTerms(termsList:Queue<String>, size:Int = HISTORY_MAX_SIZE)

    //get all history terms
    fun getHistoryTerms(): Queue<String>
}