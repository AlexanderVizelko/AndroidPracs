package com.example.hw_3.data

import com.google.gson.annotations.SerializedName

// Модель цитаты из Breaking Bad
data class Quote(
    @SerializedName("quote") val quote: String,
    @SerializedName("author") val author: String
)

