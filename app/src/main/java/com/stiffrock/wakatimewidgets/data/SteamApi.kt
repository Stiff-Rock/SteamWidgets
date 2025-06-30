package com.stiffrock.wakatimewidgets.data

import com.stiffrock.wakatimewidgets.data.model.LangStatsResponse
import com.stiffrock.wakatimewidgets.data.model.UserResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// API interface
interface WakaTimeApiService {
    @GET("IPlayerService/GetRecentlyPlayedGames/v0001/")
    suspend fun getRecentlyPlayedGames(
        @Query("key") apiKey: String,
        @Query("steamid") steamId: String = "76561198148657625",
        @Query("format") format: String = "json",
    ): Response<*>

    @GET("ISteamUser/GetPlayerSummaries/v0002/")
    suspend fun getPlayerSummaries(
        @Query("key") apiKey: String,
        @Query("steamids") steamIds: String = "76561198148657625",
        @Query("format") format: String = "json",
    ): Response<*>
}

// API client singleton
object SteamApi {
    private const val BASE_URL = "http://api.steampowered.com/"

    private val retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: WakaTimeApiService by lazy {
        retrofit.create(WakaTimeApiService::class.java)
    }
}