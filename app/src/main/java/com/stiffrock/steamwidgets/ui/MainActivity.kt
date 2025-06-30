package com.stiffrock.steamwidgets.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.stiffrock.steamwidgets.R
import com.stiffrock.steamwidgets.data.SteamApi
import com.stiffrock.steamwidgets.utils.TAG
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        const val PREFS_FILENAME = "user_prefs"
        const val STEAM_USER_ID_PREF = "steam_id"
    }

    private lateinit var currentUserTextView: TextView
    private lateinit var steamUserIdInput: EditText
    private lateinit var saveBtn: Button

    //TODO: MAYBE DO OAuth 2.0
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        currentUserTextView = findViewById(R.id.currentUser)
        steamUserIdInput = findViewById(R.id.steamUserIdInput)
        saveBtn = findViewById(R.id.saveBtn)

        sharedPreferences =
            this.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

        val steamUserId = sharedPreferences.getString(STEAM_USER_ID_PREF, null)

        if (steamUserId != null) {
            fetchUsername(steamUserId, false)
            steamUserIdInput.setText(steamUserId)
        }

        saveBtn.setOnClickListener {
            val apiKey = steamUserIdInput.text.toString().trim()

            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Please provide your Steam id", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            fetchUsername(apiKey, true)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchUsername(steamUserId: String, save: Boolean) {
        lifecycleScope.launch {
            try {
                val response = SteamApi.service.getPlayerSummaries(steamIds = steamUserId)

                if (!response.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "API Error: ${response.code()}")
                    return@launch
                }

                val body = response.body()
                val username = body?.response?.players?.first()?.personaname
                if (save) {
                    sharedPreferences.edit().apply {
                        putString(STEAM_USER_ID_PREF, steamUserId)
                        apply()
                    }
                }

                if (username == null) {
                    Toast.makeText(
                        this@MainActivity, "Failed to get username", Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Failed to get username")
                    return@launch
                }

                currentUserTextView.text = username
                Toast.makeText(
                    this@MainActivity,
                    "Successfully saved steam id",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error: ${e.message}")
            }
        }
    }

}