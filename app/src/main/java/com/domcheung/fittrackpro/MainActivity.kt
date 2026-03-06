package com.domcheung.fittrackpro

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.domcheung.fittrackpro.navigation.AppNavigation
import com.domcheung.fittrackpro.ui.theme.FitTrackProTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val runtimeMainTabRequest = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialMainTabRequest = extractMainTabRequest(intent)

        setContent {
            FitTrackProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        initialMainTabRequest = initialMainTabRequest,
                        runtimeMainTabRequest = runtimeMainTabRequest.value,
                        onRuntimeMainTabRequestConsumed = {
                            runtimeMainTabRequest.value = null
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        runtimeMainTabRequest.value = extractMainTabRequest(intent)
    }

    private fun extractMainTabRequest(intent: Intent?): String? {
        return intent?.getStringExtra(EXTRA_OPEN_MAIN_TAB)
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotEmpty() }
    }

    companion object {
        const val EXTRA_OPEN_MAIN_TAB = "open_main_tab"
        const val MAIN_TAB_WORKOUT = "workout"
    }
}
