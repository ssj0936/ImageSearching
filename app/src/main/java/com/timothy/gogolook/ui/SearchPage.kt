package com.timothy.gogolook.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timothy.gogolook.ui.widget.SearchBar
import com.timothy.gogolook.ui.widget.SearchResultList

@Composable
fun SearchPage(
) {
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar()
        Spacer(modifier = Modifier.height(4.dp))
        SearchResultList()
    }
}

