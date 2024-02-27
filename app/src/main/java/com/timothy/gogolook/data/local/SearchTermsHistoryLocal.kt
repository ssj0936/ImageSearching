package com.timothy.gogolook.data.local

import android.content.Context.MODE_PRIVATE
import android.content.Context
import com.timothy.gogolook.data.SearchTermsHistoryService
import com.timothy.gogolook.util.HISTORY_PREF_KEY
import com.timothy.gogolook.util.HISTORY_PREF_VALUE
import com.timothy.gogolook.util.LRUCache
import com.timothy.gogolook.util.LRUCacheImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject

class SearchTermsHistoryLocal @Inject constructor(
    @ApplicationContext private val context: Context
):SearchTermsHistoryService {
    private val separator = ",,"

    override fun saveHistoryTerms(termsList: LRUCache<String>, size:Int){
        context.getSharedPreferences(HISTORY_PREF_KEY,MODE_PRIVATE)
            .edit()
            .putString(HISTORY_PREF_VALUE, termsList.toList().joinToString(separator = separator))
            .apply()
    }

    override fun getHistoryTerms():LRUCache<String>{
        val historyTermsString = context.getSharedPreferences(HISTORY_PREF_KEY,MODE_PRIVATE)
            .getString(HISTORY_PREF_VALUE, null)

        return if(historyTermsString.isNullOrEmpty())
            LRUCacheImpl()
        else
            LRUCacheImpl(LinkedList(historyTermsString.split(separator)))
    }
}