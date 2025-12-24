package com.example.hw_3.data

import com.google.gson.annotations.SerializedName

///data class - специальный тип класса в Kotlin для хранения данных
data class MatchesResponse(
    //@SerializedName("matches") - указывает, что JSON поле "matches" маппится на это свойство
    //val matches: List<Match> - список объектов матчей
    @SerializedName("matches") val matches: List<Match>
)

data class Match(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String,
    @SerializedName("homeTeam") val homeTeam: Team,
    @SerializedName("awayTeam") val awayTeam: Team,
    @SerializedName("score") val score: Score?,
    @SerializedName("utcDate") val date: String,


)

data class Team(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("shortName") val shortName: String,
    @SerializedName("crest") val crest: String?
)

data class Score(
    @SerializedName("fullTime") val fullTime: FullTimeScore?
)

data class FullTimeScore(
    @SerializedName("home") val home: Int?,
    @SerializedName("away") val away: Int?
)
