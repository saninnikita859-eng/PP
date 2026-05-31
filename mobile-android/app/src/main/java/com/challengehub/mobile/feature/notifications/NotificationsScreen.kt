package com.challengehub.mobile.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.Surface
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.NetworkModule
import com.challengehub.mobile.core.network.NotificationDto
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Composable
fun NotificationsScreen(
    contentPadding: PaddingValues,
    token: String?,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        val currentToken = token ?: return@LaunchedEffect
        runCatching { NetworkModule.api.notifications("Bearer $currentToken") }
            .onSuccess { items = it; error = null }
            .onFailure { error = "Не вдалося завантажити сповіщення" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text("Сповіщення", fontSize = 24.sp, fontWeight = FontWeight.Black)
        }

        if (error != null) {
            Text(error.orEmpty(), color = TextMuted)
        } else if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Поки що сповіщень немає", color = TextMuted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items, key = { it.id }) { notification ->
                    val isUnread = !notification.isRead
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Surface.copy(alpha = 0.86f))
                            .border(
                                width = if (isUnread) 1.dp else 0.dp,
                                color = if (isUnread) HotPink.copy(alpha = 0.7f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable {
                                if (isUnread && token != null) {
                                    val previous = items
                                    items = items.map { if (it.id == notification.id) it.copy(isRead = true) else it }
                                    scope.launch {
                                        runCatching { NetworkModule.api.markNotificationRead("Bearer $token", notification.id) }
                                            .onFailure { items = previous }
                                    }
                                }
                            }
                    ) {
                        if (isUnread) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                                    .background(HotPink)
                            )
                        } else {
                            Spacer(modifier = Modifier.padding(start = 6.dp))
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isUnread) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(HotPink),
                                    )
                                }
                                Text(notification.title, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                notification.body,
                                color = TextMuted,
                                fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                            )
                            Text(relativeTime(notification.createdAt), color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun relativeTime(iso: String): String {
    val now = OffsetDateTime.now(ZoneOffset.UTC)
    val then = runCatching { OffsetDateTime.parse(iso) }
        .recoverCatching { LocalDateTime.parse(iso).atOffset(ZoneOffset.UTC) }
        .getOrElse { return "щойно" }
    val d = Duration.between(then, now)
    val minutes = d.toMinutes().coerceAtLeast(0)
    return when {
        minutes < 1 -> "щойно"
        minutes < 60 -> "$minutes хв тому"
        minutes < 60 * 24 -> "${minutes / 60} год тому"
        minutes < 60 * 24 * 30 -> "${minutes / (60 * 24)} дн тому"
        else -> "${minutes / (60 * 24 * 30)} міс тому"
    }
}
