package com.smashsonic

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.smashsonic.service.PlaybackService
import com.smashsonic.ui.components.LaunchScreen
import com.smashsonic.ui.navigation.SmashSonicNavHost
import com.smashsonic.ui.theme.SmashSonicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Connecting a MediaController to the PlaybackService (a MediaSessionService) is what
    // actually starts it and lets Media3 promote it to a foreground service while playback
    // is ongoing — which keeps audio playing when the screen turns off or the app is
    // backgrounded, and provides the media notification / lock-screen controls.
    private var controllerFuture: ListenableFuture<MediaController>? = null

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

    override fun onStart() {
        super.onStart()
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
    }

    override fun onStop() {
        super.onStop()
        // Releasing the controller does NOT stop playback: while audio is ongoing the
        // MediaSessionService stays in the foreground on its own.
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }
}
