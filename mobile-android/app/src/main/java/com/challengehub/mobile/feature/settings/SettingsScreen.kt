package com.challengehub.mobile.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.challengehub.mobile.BuildConfig
import com.challengehub.mobile.R
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    token: String?,
    darkThemeEnabled: Boolean = true,
    onDarkThemeChanged: (Boolean) -> Unit = {},
    onPremium: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notifications by remember { mutableStateOf(true) }
    var darkTheme by remember(darkThemeEnabled) { mutableStateOf(darkThemeEnabled) }
    var youtubeConnected by remember { mutableStateOf(false) }
    var tiktokConnected by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun connect(platform: String) {
        val currentToken = token
        if (currentToken == null) {
            message = "Спочатку увійдіть в акаунт"
            return
        }
        scope.launch {
            val result = if (platform == "youtube") {
                runCatching { NetworkModule.api.youtubeConnectUrl("Bearer $currentToken") }
            } else {
                runCatching { NetworkModule.api.tiktokConnectUrl("Bearer $currentToken") }
            }
            result
                .onSuccess { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.connectUrl))) }
                .onFailure { message = "Не вдалося відкрити OAuth для ${if (platform == "youtube") "YouTube" else "TikTok"}" }
        }
    }

    fun disconnect(platform: String) {
        val currentToken = token
        if (currentToken == null) {
            message = "Спочатку увійдіть в акаунт"
            return
        }
        scope.launch {
            val result = if (platform == "youtube") {
                runCatching { NetworkModule.api.disconnectYoutube("Bearer $currentToken") }
            } else {
                runCatching { NetworkModule.api.disconnectTiktok("Bearer $currentToken") }
            }
            result
                .onSuccess {
                    if (platform == "youtube") youtubeConnected = false else tiktokConnected = false
                    message = "Акаунт ${if (platform == "youtube") "YouTube" else "TikTok"} відключено"
                }
                .onFailure { message = "Не вдалося відключити ${if (platform == "youtube") "YouTube" else "TikTok"}" }
        }
    }

    LaunchedEffect(token) {
        val currentToken = token ?: return@LaunchedEffect
        runCatching { NetworkModule.api.youtubeStatus("Bearer $currentToken") }
            .onSuccess { youtubeConnected = it.connected }
        runCatching { NetworkModule.api.tiktokStatus("Bearer $currentToken") }
            .onSuccess { tiktokConnected = it.connected }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.onBackground)
        }
            Text("Налаштування", color = MaterialTheme.colorScheme.onBackground, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }

        SettingsSection("Акаунт") {
            ActionRow(Icons.Default.AccountCircle, "Редагувати профіль", onClick = onEditProfile)
            DividerLine()
            ActionRow(Icons.Default.WorkspacePremium, "Купити Premium", onClick = onPremium)
        }

        SettingsSection("Підключення") {
            PlatformActionRow(
                platform = PlatformType.TikTok,
                title = if (tiktokConnected) "Відключити TikTok" else "Підключити TikTok",
                danger = tiktokConnected,
            ) { if (tiktokConnected) disconnect("tiktok") else connect("tiktok") }
            DividerLine()
            PlatformActionRow(
                platform = PlatformType.YouTube,
                title = if (youtubeConnected) "Відключити YouTube" else "Підключити YouTube",
                danger = youtubeConnected,
            ) { if (youtubeConnected) disconnect("youtube") else connect("youtube") }
        }

        if (message != null) {
            Text(message.orEmpty(), color = TextMuted, fontSize = 12.sp)
        }

        SettingsSection("Налаштування") {
            ToggleRow(Icons.Default.NotificationsNone, "Сповіщення", notifications) { notifications = it }
            DividerLine()
            ToggleRow(Icons.Default.DarkMode, "Темна тема", darkTheme) {
                darkTheme = it
                onDarkThemeChanged(it)
            }
        }

        LogoutCard(onLogout)

        Spacer(Modifier.height(2.dp))
        Text(
            "ChallengeHub v${BuildConfig.VERSION_NAME}",
            color = TextMuted.copy(alpha = 0.62f),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(92.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = TextMuted.copy(alpha = 0.74f), fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface),
            content = content,
        )
    }
}

@Composable
private fun ActionRow(icon: ImageVector, title: String, danger: Boolean = false, onClick: () -> Unit) {
    val color = if (danger) HotPink else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = if (danger) HotPink else TextMuted, modifier = Modifier.size(18.dp))
        Text(title, color = color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

private enum class PlatformType { TikTok, YouTube }

@Composable
private fun PlatformActionRow(platform: PlatformType, title: String, danger: Boolean = false, onClick: () -> Unit) {
    val color = if (danger) HotPink else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when (platform) {
            PlatformType.TikTok -> Image(painter = painterResource(id = R.drawable.tiktok_logo), contentDescription = null, modifier = Modifier.size(18.dp))
            PlatformType.YouTube -> Image(painter = painterResource(id = R.drawable.youtube_logo), contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Text(title, color = color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ToggleRow(icon: ImageVector, title: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Switch(
            checked = value,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4A4A4A),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF4A4A4A),
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.06f)),
    )
}

@Composable
private fun LogoutCard(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onLogout)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(22.dp).clip(CircleShape).background(Color.Transparent), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = HotPink, modifier = Modifier.size(19.dp))
        }
        Text("Вийти", color = HotPink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
