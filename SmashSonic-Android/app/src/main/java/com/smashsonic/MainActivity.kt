package com.smashsonic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.smashsonic.ui.components.LaunchScreen
import com.smashsonic.ui.navigation.SmashSonicNavHost
import com.smashsonic.ui.theme.SmashSonicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmashSonicTheme {
                var showLaunchScreen by remember { mutableStateOf(true) }

                if (showLaunchScreen) {
                    LaunchScreen(onFinished = { showLaunchScreen = false })
                } else {
                    SmashSonicNavHost()
                }
            }
        }
    }
}
