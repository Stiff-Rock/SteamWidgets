package com.stiffrock.steamwidgets.ui

import Game
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.stiffrock.steamwidgets.R
import com.stiffrock.steamwidgets.data.SteamApi
import com.stiffrock.steamwidgets.ui.MainActivity.Companion.PREFS_FILENAME
import com.stiffrock.steamwidgets.ui.MainActivity.Companion.STEAM_USER_ID_PREF
import com.stiffrock.steamwidgets.utils.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// IDs for language labels
private val langTextViews = arrayOf(
    R.id.game_label_1,
    R.id.game_label_2,
    R.id.game_label_3,
)

class SteamGamesStats : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val sharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
    val savedSteamUserId = sharedPreferences.getString(STEAM_USER_ID_PREF, "") ?: ""

    // Create initial views with loading state
    val views = RemoteViews(context.packageName, R.layout.steam_games_stats)

    // Set up click intent
    setupClickIntent(context, views)

    // Update widget initially (can show loading state)
    appWidgetManager.updateAppWidget(appWidgetId, views)

    // Launch a coroutine to fetch data
    CoroutineScope(Dispatchers.IO).launch {
        val langStats = getRecentlyPlayedGames(savedSteamUserId)
        val username = getUsername(savedSteamUserId)

        // Update UI on the Main dispatcher
        withContext(Dispatchers.Main) {
            uiWidgetUpdate(appWidgetManager, appWidgetId, views, username, langStats)
        }
    }
}

// Separated UI update logic
private fun uiWidgetUpdate(
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    views: RemoteViews,
    username: String,
    games: List<Game>
) {
    val title = "Language Stats - $username"
    views.setTextViewText(R.id.widget_title, title)

    for (i in games.indices) {
        if (i >= langTextViews.size) break

        val tvId = langTextViews[i]
        val game = games[i]
        val langName = game.name
        val timeText = game.prettyPlaytime()
        val text = "$langName - $timeText"

        views.setTextViewText(tvId, text)
    }

    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun setupClickIntent(context: Context, views: RemoteViews) {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.steamWidgetRoot, pendingIntent)
}

private suspend fun getUsername(steamUserId: String): String {
    return try {
        val response = SteamApi.service.getPlayerSummaries(steamIds = steamUserId)

        if (!response.isSuccessful) {
            Log.w(TAG, "API Error: ${response.code()} - ${response.errorBody()}")
            return "Unknown"
        }

        val username = response.body()?.response?.players?.first()?.personaname
        if (username.isNullOrEmpty()) {
            Log.w(TAG, "Failed to get username")
            "Unknown"
        } else {
            username
        }
    } catch (e: Exception) {
        Log.w(TAG, "Error obtaining username: ${e.message}")
        "Unknown"
    }
}

private suspend fun getRecentlyPlayedGames(steamUserId: String): List<Game> {
    Log.d(TAG, "Obtaining recently played games")

    val fallbackGames = arrayOf(
        Game(0, 0, "", 0, 0, 0, 0, 0, "NO DATA")
    ).toList()

    if (steamUserId.isEmpty()) {
        Log.w(
            TAG,
            "No API key provided"
        )
        return fallbackGames
    }

    return try {
        val response = SteamApi.service.getRecentlyPlayedGames(steamId = steamUserId)
        if (!response.isSuccessful) {
            Log.e(
                TAG,
                "API response error with code '${response.code()}': ${response.errorBody()}"
            )
        }
        val body = response.body()

        val games = body?.response?.games

        games ?: fallbackGames
    } catch (e: Exception) {
        Log.e(TAG, "Error obtaining last 7 days lang stats: ${e.message}")
        fallbackGames
    }
}