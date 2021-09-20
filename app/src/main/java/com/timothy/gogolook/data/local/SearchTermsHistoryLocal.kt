package com.timothy.gogolook.data.local

import android.content.Context.MODE_PRIVATE
import android.content.Context
import com.timothy.gogolook.data.SearchTermsHistoryService
import com.timothy.gogolook.util.HISTORY_PREF_KEY
import com.timothy.gogolook.util.HISTORY_PREF_VALUE
import timber.log.Timber
import java.util.*

class SearchTermsHistoryLocal(
    private val context: Context
):SearchTermsHistoryService {

    override fun saveHistoryTerms(termsList:Queue<String>,size:Int){
        while (termsList.size>size) {
            val pop = termsList.poll()
            Timber.d("pop:$pop")
        }

        context.getSharedPreferences(HISTORY_PREF_KEY,MODE_PRIVATE)
            .edit()
            .putString(HISTORY_PREF_VALUE, termsList.joinToString(separator = ",,"))
            .commit()
    }

    override fun getHistoryTerms():LinkedList<String>{
        val historyTermsString = context.getSharedPreferences(HISTORY_PREF_KEY,MODE_PRIVATE)
            .getString(HISTORY_PREF_VALUE, null)

        val queue = LinkedList<String>()

        return if(historyTermsString.isNullOrEmpty()) queue
        else{
            historyTermsString.split(",,").forEach {
                queue.offer(it)
            }
            queue
        }
    }
}