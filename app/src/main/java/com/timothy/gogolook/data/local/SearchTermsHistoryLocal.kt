package com.timothy.gogolook.data.local

import android.content.Context.MODE_PRIVATE
import android.content.Context
import com.timothy.gogolook.data.SearchTermsHistoryService
import com.timothy.gogolook.util.HISTORY_PREF_KEY
import com.timothy.gogolook.util.HISTORY_PREF_VALUE
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject

class SearchTermsHistoryLocal @Inject constructor(
    @ApplicationContext private val context: Context
):SearchTermsHistoryService {
    private val separator = ",,"

    override fun saveHistoryTerms(termsList:Queue<String>,size:Int){
        while (termsList.size>size)
            termsList.poll()

        context.getSharedPreferences(HISTORY_PREF_KEY,MODE_PRIVATE)
            .edit()
            .putString(HISTORY_PREF_VALUE, termsList.joinToString(separator = separator))
            .commit()
    }

    override fun getHistoryTerms():LinkedList<String>{
        val historyTermsString = context.getSharedPreferences(HISTORY_PREF_KEY,MODE_PRIVATE)
            .getString(HISTORY_PREF_VALUE, null)

        val queue = LinkedList<String>()

        return if(historyTermsString.isNullOrEmpty()) queue
        else{
            historyTermsString.split(separator).forEach {
                queue.offer(it)
            }
            queue
        }
    }
}