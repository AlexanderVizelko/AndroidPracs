package com.example.hw_3


sealed class Routes(val route: String) {
    object Welcome : Routes("welcome")
    object Screen1 : Routes("screen1")
    object Screen2 : Routes("screen2")
    object Screen3 : Routes("screen3")
    object FilterScreen : Routes("filter_screen")
    object Favorites : Routes("favorites")
    object FavoriteDetail : Routes("favorite_detail/{favoriteIndex}") {
        fun createRoute(favoriteIndex: Int) = "favorite_detail/$favoriteIndex"
    }
    object QuoteDetail : Routes("quote_detail/{quoteIndex}") {
        fun createRoute(quoteIndex: Int) = "quote_detail/$quoteIndex"
    }
    object EditProfile : Routes("edit_profile")
}