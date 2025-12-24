package com.example.hw_3.api

import com.example.hw_3.data.MatchesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface FootballApiService {
    @GET("matches")
    @Headers("X-Auth-Token: 00c0f658b6094ce3aaf2dec9c8a89c64")
    suspend fun getMatches(): Response<MatchesResponse>
}