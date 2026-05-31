package com.challengehub.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeHubTheme
import com.challengehub.mobile.core.session.SessionStore
import com.challengehub.mobile.navigation.ChallengeHubNav

class MainActivity : ComponentActivity() {
    private val deepLink = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLink.value = intent?.dataString
        val sessionStore = SessionStore(applicationContext)
        setContent {
            ChallengeHubTheme {
                AppRoot(sessionStore, deepLink.value)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLink.value = intent.dataString
    }
}

@Composable
private fun AppRoot(sessionStore: SessionStore, deepLink: String?) {
    val token by sessionStore.token.collectAsState(initial = null)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center,
    ) {
        ChallengeHubNav(sessionStore = sessionStore, initialToken = token, deepLink = deepLink)
    }
}
