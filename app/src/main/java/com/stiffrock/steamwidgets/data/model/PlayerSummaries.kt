package com.stiffrock.steamwidgets.data.model

// Model for the entire response
data class PlayerSummariesResponse(
    val response: PlayersResponse
)

// Container for the players array
data class PlayersResponse(
    val players: List<Player>
)

// Individual player model with all fields from your JSON
data class Player(
    val steamid: String,
    val communityvisibilitystate: Int,
    val profilestate: Int,
    val personaname: String,
    val commentpermission: Int,
    val profileurl: String,
    val avatar: String,
    val avatarmedium: String,
    val avatarfull: String,
    val avatarhash: String,
    val lastlogoff: Long,
    val personastate: Int,
    val realname: String?,  // Nullable since it might not always be present
    val primaryclanid: String,
    val timecreated: Long,
    val personastateflags: Int,
    val loccountrycode: String?  // Nullable since it might not always be present
) {
    // Helper properties for improved usability

    // Format the creation date
    val accountCreatedDate: String
        get() = formatTimestamp(timecreated)

    // Format the last online time
    val lastOnlineTime: String
        get() = formatTimestamp(lastlogoff)

    // Convert persona state to a readable status
    val status: String
        get() = when(personastate) {
            0 -> "Offline"
            1 -> "Online"
            2 -> "Busy"
            3 -> "Away"
            4 -> "Snooze"
            5 -> "Looking to Trade"
            6 -> "Looking to Play"
            else -> "Unknown"
        }

    // Helper method to format Unix timestamps
    private fun formatTimestamp(timestamp: Long): String {
        val date = java.util.Date(timestamp * 1000)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}