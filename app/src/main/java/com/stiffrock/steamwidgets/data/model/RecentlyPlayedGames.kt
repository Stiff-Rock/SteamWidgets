import com.google.gson.annotations.SerializedName

// Model for the entire response
data class RecentlyPlayedGamesResponse(
    val response: GamesResponse
)

// Container for the games array
data class GamesResponse(
    @SerializedName("total_count")
    val totalCount: Int,
    val games: List<Game>
)

data class Game(
    @SerializedName("playtime_forever")
    val playtimeForever: Int,

    @SerializedName("playtime_2weeks")
    val playtime2Weeks: Int,

    @SerializedName("img_icon_url")
    val imgIconUrl: String,

    @SerializedName("playtime_windows_forever")
    val playtimeWindowsForever: Int,

    @SerializedName("playtime_mac_forever")
    val playtimeMacForever: Int,

    @SerializedName("playtime_linux_forever")
    val playtimeLinuxForever: Int,

    @SerializedName("playtime_deck_forever")
    val playtimeDeckForever: Int,

    val appid: Int,
    val name: String
) {

    // Helper properties for improved usability

    // Convert total playtime to hours
    val playtimeForeverInHours: Float
        get() = playtimeForever / 60f

    // Convert 2-week playtime to hours
    val playtime2WeeksInHours: Float
        get() = playtime2Weeks / 60f

    // Format playtime2Weeks minutes as "Xh Ym"
    fun prettyPlaytime(): String {
        val minutes = playtime2Weeks;

        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }

    // Generate the full URL for the game's icon
    val fullIconUrl: String
        get() = "https://cdn.cloudflare.steamstatic.com/steamcommunity/public/images/apps/$appid/$imgIconUrl.jpg"

    // Check if the game has been played in the last 2 weeks
    val hasRecentlyPlayed: Boolean
        get() = playtime2Weeks > 0
}