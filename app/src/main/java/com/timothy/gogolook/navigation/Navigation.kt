package com.timothy.gogolook.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.timothy.gogolook.ui.SearchPage

@Composable
fun Navigation(
    paddingValues: PaddingValues,
    navHostController: NavHostController
) {
    NavHost(navController = navHostController, startDestination = Destinations.MainPage.route){
        composable(route = Destinations.MainPage.route){
            Box(modifier = Modifier.padding(paddingValues)) {
                SearchPage()
            }
        }
    }
}

sealed class Destinations(
    val route:String,
    val label:String,
    val iconResourceId:Int? = null
){
    object MainPage:Destinations(
        route = "main_page",
        label = "MainPage"
    )
}