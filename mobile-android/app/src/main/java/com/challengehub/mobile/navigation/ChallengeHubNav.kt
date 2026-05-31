package com.challengehub.mobile.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.SurfaceDeep
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.ChallengeDto
import com.challengehub.mobile.core.network.NetworkModule
import com.challengehub.mobile.core.network.VideoDto
import com.challengehub.mobile.feature.auth.AuthScreen
import com.challengehub.mobile.feature.assistant.AssistantScreen
import com.challengehub.mobile.feature.badges.BadgesScreen
import com.challengehub.mobile.feature.camera.CameraScreen
import com.challengehub.mobile.feature.challenge.ChallengeDetailsScreen
import com.challengehub.mobile.feature.challenge.CreateChallengeScreen
import com.challengehub.mobile.feature.challenges.ChallengesScreen
import com.challengehub.mobile.feature.feed.FeedScreen
import com.challengehub.mobile.feature.leaderboard.LeaderboardScreen
import com.challengehub.mobile.feature.notifications.NotificationsScreen
import com.challengehub.mobile.feature.premium.PremiumScreen
import com.challengehub.mobile.feature.profile.EditProfileScreen
import com.challengehub.mobile.feature.profile.ProfileScreen
import com.challengehub.mobile.feature.settings.SettingsScreen
import com.challengehub.mobile.feature.upload.UploadScreen
import kotlinx.coroutines.launch

private const val AUTH = "auth"
private const val FEED = "feed"
private const val CHALLENGES = "challenges"
private const val LEADERS = "leaders"
private const val PROFILE = "profile"
private const val UPLOAD = "upload"
private const val CREATE_CHALLENGE = "create_challenge"
private const val CHALLENGE_DETAILS = "challenge_details"
private const val CAMERA = "camera"
private const val PREMIUM = "premium"
private const val ASSISTANT = "assistant"
private const val SETTINGS = "settings"
private const val EDIT_PROFILE = "edit_profile"
private const val BADGES = "badges"
private const val PUBLIC_PROFILE = "public_profile"
private const val PUBLIC_BADGES = "public_badges"
private const val NOTIFICATIONS = "notifications"

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun ChallengeHubNav(sessionStore: com.challengehub.mobile.core.session.SessionStore, initialToken: String?, deepLink: String? = null) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var token by remember(initialToken) { mutableStateOf(initialToken) }
    var hasPremium by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf<String?>(null) }
    var recordedVideoUri by remember { mutableStateOf<String?>(null) }
    var uploadChallenge by remember { mutableStateOf<ChallengeDto?>(null) }
    var feedCache by remember { mutableStateOf<List<VideoDto>?>(null) }
    var selectedChallenge by remember { mutableStateOf<ChallengeDto?>(null) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val start = if (token.isNullOrBlank()) AUTH else FEED
    val tabs = listOf(
        TabItem(FEED, "Головна", Icons.Default.Home),
        TabItem(CHALLENGES, "Челенджі", Icons.Default.Bolt),
        TabItem(CAMERA, "Створити", Icons.Default.AddCircle),
        TabItem(LEADERS, "Лідери", Icons.Default.EmojiEvents),
        TabItem(PROFILE, "Профіль", Icons.Default.Person),
    )

    LaunchedEffect(initialToken) {
        if (!initialToken.isNullOrBlank()) {
            token = initialToken
            if (navController.currentDestination?.route != FEED) {
                navController.navigate(FEED) {
                    popUpTo(AUTH) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(token, currentRoute) {
        val currentToken = token
        if (currentToken.isNullOrBlank()) {
            hasPremium = false
            return@LaunchedEffect
        }
        runCatching { NetworkModule.api.me("Bearer $currentToken") }
            .onSuccess { hasPremium = it.isPremium; currentUserId = it.id }
            .onFailure { hasPremium = false; currentUserId = null }
    }

    LaunchedEffect(deepLink, token) {
        val currentToken = token
        if (deepLink?.startsWith("challengehub://premium/success") == true && !currentToken.isNullOrBlank()) {
            runCatching { NetworkModule.api.me("Bearer $currentToken") }
                .onSuccess { hasPremium = it.isPremium; currentUserId = it.id }
            navController.navigate(PROFILE) {
                popUpTo(FEED) { inclusive = false }
            }
        }
    }

    Scaffold(
        containerColor = Background,
        floatingActionButton = {
            if (!token.isNullOrBlank() && hasPremium && currentRoute != ASSISTANT) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ChallengeGradient)
                        .clickable { navController.navigate(ASSISTANT) },
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    Icon(Icons.Default.Star, contentDescription = "AI-помічник", tint = Color.White)
                }
            }
        },
        bottomBar = {
            if (!token.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(SurfaceDeep.copy(alpha = 0.96f))
                        .border(1.dp, Color.White.copy(alpha = 0.07f))
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(74.dp),
                    ) {
                        tabs.forEach { tab ->
                            val selected = backStackEntry?.destination?.route == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navController.navigate(tab.route) },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = HotPink,
                                    selectedTextColor = HotPink,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = Color.Transparent,
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = navController, startDestination = start) {
            composable(AUTH) {
                AuthScreen(
                    onAuthenticated = { newToken ->
                        token = newToken
                        scope.launch { sessionStore.saveToken(newToken) }
                        navController.navigate(FEED) {
                            popUpTo(AUTH) { inclusive = true }
                        }
                    },
                )
            }
            composable(FEED) {
                FeedScreen(
                    contentPadding = padding,
                    token = token,
                    cachedVideos = feedCache,
                    onCacheUpdate = { feedCache = it },
                    onOpenChallenge = { challenge ->
                        selectedChallenge = challenge
                        navController.navigate(CHALLENGE_DETAILS)
                    },
                    onOpenUser = { userId ->
                        if (userId == currentUserId) {
                            navController.navigate(PROFILE)
                        } else {
                            selectedUserId = userId
                            navController.navigate(PUBLIC_PROFILE)
                        }
                    },
                )
            }
            composable(CHALLENGES) {
                ChallengesScreen(
                    contentPadding = padding,
                    token = token,
                    onCreateChallenge = { navController.navigate(CREATE_CHALLENGE) },
                    onOpenDetails = { challenge ->
                        selectedChallenge = challenge
                        navController.navigate(CHALLENGE_DETAILS)
                    },
                )
            }
            composable(LEADERS) { LeaderboardScreen(contentPadding = padding) }
            composable(UPLOAD) {
                UploadScreen(
                    contentPadding = padding,
                    token = token,
                    initialVideoUri = recordedVideoUri,
                    initialChallenge = uploadChallenge,
                    onRecordVideo = { navController.navigate(CAMERA) },
                    onUploaded = {
                        recordedVideoUri = null
                        uploadChallenge = null
                        feedCache = null
                        navController.navigate(FEED)
                    },
                )
            }
            composable(CREATE_CHALLENGE) { CreateChallengeScreen(contentPadding = padding, token = token, onCreated = { navController.navigate(CHALLENGES) }, onBack = { navController.popBackStack() }) }
            composable(CHALLENGE_DETAILS) {
                ChallengeDetailsScreen(
                    contentPadding = padding,
                    challenge = selectedChallenge,
                    token = token,
                    onUpload = { challenge -> uploadChallenge = challenge; navController.navigate(UPLOAD) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(CAMERA) {
                CameraScreen(
                    contentPadding = padding,
                    onOpenUpload = { navController.navigate(UPLOAD) },
                    onRecorded = { uri ->
                        recordedVideoUri = uri.toString()
                        navController.navigate(UPLOAD)
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(PREMIUM) { PremiumScreen(contentPadding = padding, token = token, onBack = { navController.popBackStack() }) }
            composable(ASSISTANT) { AssistantScreen(contentPadding = padding, token = token, onClose = { navController.popBackStack() }) }
            composable(SETTINGS) {
                SettingsScreen(
                    contentPadding = padding,
                    token = token,
                    onPremium = { navController.navigate(PREMIUM) },
                    onEditProfile = { navController.navigate(EDIT_PROFILE) },
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        scope.launch { sessionStore.clear() }
                        token = null
                        navController.navigate(AUTH) { popUpTo(0) }
                    },
                )
            }
            composable(EDIT_PROFILE) {
                EditProfileScreen(
                    contentPadding = padding,
                    token = token,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.navigate(PROFILE) { popUpTo(EDIT_PROFILE) { inclusive = true } } },
                )
            }
            composable(NOTIFICATIONS) {
                NotificationsScreen(
                    contentPadding = padding,
                    token = token,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(BADGES) { BadgesScreen(contentPadding = padding, token = token, onBack = { navController.popBackStack() }) }
            composable(PUBLIC_BADGES) { BadgesScreen(contentPadding = padding, token = token, userId = selectedUserId, onBack = { navController.popBackStack() }) }
            composable(PUBLIC_PROFILE) {
                ProfileScreen(
                    contentPadding = padding,
                    token = token,
                    viewedUserId = selectedUserId,
                    onBack = { navController.popBackStack() },
                    onBadges = { navController.navigate(PUBLIC_BADGES) },
                    onLogout = {},
                )
            }
            composable(PROFILE) {
                ProfileScreen(
                    contentPadding = padding,
                    token = token,
                    onNotifications = { navController.navigate(NOTIFICATIONS) },
                    onSettings = { navController.navigate(SETTINGS) },
                    onPremium = { navController.navigate(PREMIUM) },
                    onAssistant = { navController.navigate(ASSISTANT) },
                    onEdit = { navController.navigate(EDIT_PROFILE) },
                    onBadges = { navController.navigate(BADGES) },
                    onLogout = {
                        scope.launch { sessionStore.clear() }
                        token = null
                        navController.navigate(AUTH) { popUpTo(0) }
                    },
                )
            }
        }
    }
}



