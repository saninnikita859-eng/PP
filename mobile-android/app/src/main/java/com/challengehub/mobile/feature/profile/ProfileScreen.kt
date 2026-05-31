package com.challengehub.mobile.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.challengehub.mobile.core.badges.catalogBadge
import com.challengehub.mobile.core.badges.earnedBadgeItems
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.GlassCard
import com.challengehub.mobile.core.design.Gold
import com.challengehub.mobile.core.design.GradientButton
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.Pink
import com.challengehub.mobile.core.design.Purple
import com.challengehub.mobile.core.design.Surface
import com.challengehub.mobile.core.design.SurfaceSoft
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.BadgeDto
import com.challengehub.mobile.core.network.ChallengeDto
import com.challengehub.mobile.core.network.MobileUserDto
import com.challengehub.mobile.core.network.NetworkModule
import com.challengehub.mobile.core.network.VideoDto
import com.challengehub.mobile.core.network.resolveMediaUrl
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    token: String?,
    onLogout: () -> Unit,
    viewedUserId: String? = null,
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onPremium: () -> Unit = {},
    onAssistant: () -> Unit = {},
    onEdit: () -> Unit = {},
    onBadges: () -> Unit = {},
) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<MobileUserDto?>(null) }
    var badges by remember { mutableStateOf<List<BadgeDto>>(emptyList()) }
    var videos by remember { mutableStateOf<List<VideoDto>>(emptyList()) }
    var challenges by remember { mutableStateOf<List<ChallengeDto>>(emptyList()) }
    var followersCount by remember { mutableStateOf(0) }
    var isFollowing by remember { mutableStateOf(false) }
    var profileTab by remember { mutableStateOf("VIDEOS") }
    var unreadNotifications by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    val isPublicProfile = viewedUserId != null
    val scope = rememberCoroutineScope()

    LaunchedEffect(token, viewedUserId) {
        val currentToken = token ?: return@LaunchedEffect
        val publicId = viewedUserId
        if (publicId != null) {
            runCatching { NetworkModule.api.publicProfile(publicId, "Bearer $currentToken") }
                .onSuccess {
                    user = it.user
                    badges = it.badges
                    videos = it.videos
                    challenges = it.challenges
                    followersCount = it.followersCount
                    isFollowing = it.isFollowing
                    error = null
                }
                .onFailure { error = "Не вдалося завантажити профіль" }
        } else {
            runCatching { NetworkModule.api.me("Bearer $currentToken") }
                .onSuccess {
                    runCatching { NetworkModule.api.publicProfile(it.id, "Bearer $currentToken") }
                        .onSuccess { profile ->
                            user = profile.user
                            badges = profile.badges
                            videos = profile.videos
                            challenges = profile.challenges
                            followersCount = profile.followersCount
                            isFollowing = profile.isFollowing
                        }
                }
                .onFailure { error = "Не вдалося завантажити профіль" }
            runCatching { NetworkModule.api.notifications("Bearer $currentToken") }
                .onSuccess { unreadNotifications = it.count { n -> !n.isRead } }
        }
    }

    val backgroundRes = remember(user?.profileBackground) {
        val key = user?.profileBackground ?: return@remember 0
        context.resources.getIdentifier(key, "drawable", context.packageName)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding),
    ) {
        if (backgroundRes != 0) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = backgroundRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.28f,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isPublicProfile) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }
                Text("Профіль", fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
            } else {
                Text("Профіль", fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.CenterStart))
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onNotifications) {
                        Box {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "Сповіщення", tint = Color.White)
                            if (unreadNotifications > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-4).dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE53935)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = if (unreadNotifications > 99) "99+" else unreadNotifications.toString(),
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        lineHeight = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.offset(y = (-0.5).dp),
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Налаштування", tint = Color.White)
                    }
                }
            }
        }
        if (error != null) Text(error.orEmpty(), color = TextMuted)
        AvatarBlock(user)
        XpProgress(user?.xp ?: 0)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileStat(challenges.size.toString(), "Челенджі", Modifier.weight(1f))
            ProfileStat(followersCount.toString(), "Підписники", Modifier.weight(1f))
            ProfileStat(videos.sumOf { it.likesCount }.toString(), "Лайки", Modifier.weight(1f))
        }
        if (isPublicProfile && user != null) {
            FollowButton(
                isFollowing = isFollowing,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val currentToken = token ?: return@FollowButton
                    val currentUser = user ?: return@FollowButton
                    val previousFollowing = isFollowing
                    val previousFollowers = followersCount
                    isFollowing = !previousFollowing
                    followersCount = (followersCount + if (previousFollowing) -1 else 1).coerceAtLeast(0)
                    scope.launch {
                        runCatching {
                            if (previousFollowing) NetworkModule.api.unfollowUser("Bearer $currentToken", currentUser.id)
                            else NetworkModule.api.followUser("Bearer $currentToken", currentUser.id)
                        }.onSuccess {
                            followersCount = it.followersCount
                            isFollowing = it.isFollowing
                        }.onFailure {
                            followersCount = previousFollowers
                            isFollowing = previousFollowing
                        }
                    }
                },
            )
        }
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆 Значки", fontWeight = FontWeight.Bold)
                    androidx.compose.material3.TextButton(onClick = onBadges) {
                        Text("Переглянути всі", color = HotPink, fontSize = 12.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    val shownBadges = if (isPublicProfile) {
                        badges.filter { it.isPinned }.take(3)
                    } else {
                        badges.filter { it.isPinned }.take(3)
                    }
                    val resolvedBadges = if (shownBadges.isNotEmpty()) shownBadges else badges.take(3)
                    resolvedBadges.forEach { badge ->
                        val item = catalogBadge(badge.badgeCode)
                        BadgeCard(item.title, item.emoji, modifier = Modifier.weight(1f))
                    }
                    if (resolvedBadges.isEmpty()) {
                        Text("Поки немає отриманих значків", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        }
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.TextButton(onClick = { profileTab = "VIDEOS" }) {
                        Text("Відео", color = if (profileTab == "VIDEOS") HotPink else TextMuted)
                    }
                    androidx.compose.material3.TextButton(onClick = { profileTab = "CHALLENGES" }) {
                        Text("Челенджі", color = if (profileTab == "CHALLENGES") HotPink else TextMuted)
                    }
                }
                if (profileTab == "VIDEOS") {
                    if (videos.isEmpty()) {
                        Text("У тебе ще немає відео", color = TextMuted)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            videos.chunked(3).forEachIndexed { rowIndex, chunk ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    chunk.forEachIndexed { index, video ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(118.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Brush.linearGradient(listOf(Color(0xFF0C4949), Purple.copy(alpha = 0.55f), Pink.copy(alpha = 0.45f))))
                                        ) {
                                            Text(video.description ?: "#${rowIndex * 3 + index + 1}", modifier = Modifier.align(Alignment.BottomStart).padding(8.dp), color = Color.White)
                                        }
                                    }
                                    repeat(3 - chunk.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (challenges.isEmpty()) {
                        Text("Активні та завершені челенджі з'являться тут.", color = TextMuted)
                    } else {
                        val completed = challenges.filter { it.participationStatus == "COMPLETED" }
                        val active = challenges.filter { it.participationStatus != "COMPLETED" }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (active.isNotEmpty()) {
                                Text("Активні", color = TextMuted, fontSize = 13.sp)
                                active.take(5).forEach { challenge -> ActiveChallengeRow(challenge) }
                            }
                            if (completed.isNotEmpty()) {
                                Text("Завершені", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                                completed.take(5).forEach { challenge -> CompletedChallengeRow(challenge) }
                            }
                        }
                    }
                }
            }
        }
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun AvatarBlock(user: MobileUserDto?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(112.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(ChallengeGradient),
                contentAlignment = Alignment.Center,
            ) {
                val avatarUrl = user?.avatarUrl?.let { resolveMediaUrl(it) }
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Аватар",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    )
                } else {
                    Text(user?.fullName?.firstOrNull()?.uppercase() ?: "Г", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                }
            }
            if (user?.isPremium == true) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Gold),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }
        }
        Text(user?.fullName ?: "Користувач", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("@${user?.username ?: "guest"}", color = TextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun FollowButton(isFollowing: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (isFollowing) {
        Box(
            modifier = modifier
                .height(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceSoft)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text("Відписатися", color = TextMuted, fontWeight = FontWeight.Bold)
        }
    } else {
        GradientButton(
            text = "Підписатися",
            modifier = modifier,
            onClick = onClick,
        )
    }
}

@Composable
private fun XpProgress(xp: Int) {
    val level = (xp / 1000) + 1
    val current = xp % 1000
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("⚡ Рівень $level", fontWeight = FontWeight.Bold)
            Text("$current / 1000 XP", color = TextMuted, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Surface),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((current.coerceIn(0, 1000) / 1000f))
                    .height(12.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ChallengeGradient),
            )
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String, modifier: Modifier) {
    GlassCard(modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, textAlign = TextAlign.Center)
            Text(label, color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun BadgeCard(title: String, icon: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2B102E)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 22.sp)
            Text(title, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 13.sp)
        }
    }
}

@Composable
private fun ActiveChallengeRow(challenge: ChallengeDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceSoft)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.RadioButtonChecked, contentDescription = null, tint = Purple, modifier = Modifier.size(17.dp))
            Text(challenge.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        val progress = challengeProgress(challenge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Прогрес", color = TextMuted, fontSize = 12.sp)
            Text("$progress / ${challenge.durationDays.coerceAtLeast(1)} днів", color = Purple, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.toFloat() / challenge.durationDays.coerceAtLeast(1))
                    .height(7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ChallengeGradient),
            )
        }
    }
}

@Composable
private fun CompletedChallengeRow(challenge: ChallengeDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceSoft)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15D48A), modifier = Modifier.size(18.dp))
            Text(challenge.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
            Text("+${challenge.xpReward}", color = Gold, fontWeight = FontWeight.Bold)
        }
    }
}

private fun challengeProgress(challenge: ChallengeDto): Int {
    val duration = challenge.durationDays.coerceAtLeast(1)
    if (challenge.participationStatus == "COMPLETED") return duration
    return challenge.progressVideos.coerceIn(0, duration)
}
