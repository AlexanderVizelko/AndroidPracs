package com.example.hw_3


sealed class Routes(val route: String) {
    object Welcome : Routes("welcome")
    object Screen1 : Routes("screen1")
    object Screen2 : Routes("screen2")
    object Screen3 : Routes("screen3")
    object QuoteDetail : Routes("quote_detail/{quoteIndex}") {
        fun createRoute(quoteIndex: Int) = "quote_detail/$quoteIndex"
    }
}