package com.stiffrock.steamwidgets.data

import RecentlyPlayedGamesResponse
import androidx.core.os.BuildCompat
import com.stiffrock.steamwidgets.BuildConfig
import com.stiffrock.steamwidgets.data.model.PlayerSummariesResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// API interface
interface WakaTimeApiService {
    @GET("IPlayerService/GetRecentlyPlayedGames/v0001/")
    suspend fun getRecentlyPlayedGames(
        @Query("key") apiKey: String = BuildConfig.steamApi,
        @Query("steamid") steamId: String,
        @Query("format") format: String = "json",
    ): Response<RecentlyPlayedGamesResponse>

    @GET("ISteamUser/GetPlayerSummaries/v0002/")
    suspend fun getPlayerSummaries(
        @Query("key") apiKey: String = BuildConfig.steamApi,
        @Query("steamids") steamIds: String,
        @Query("format") format: String = "json",
    ): Response<PlayerSummariesResponse>
}

// API client singleton
object SteamApi {
    private const val BASE_URL = "https://api.steampowered.com/"

    private val retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: WakaTimeApiService by lazy {
        retrofit.create(WakaTimeApiService::class.java)
    }
}