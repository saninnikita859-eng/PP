package com.challengehub.mobile.feature.badges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.Pink
import com.challengehub.mobile.core.design.Purple
import com.challengehub.mobile.core.design.Surface
import com.challengehub.mobile.core.design.SurfaceSoft
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.badges.BadgeCatalog
import com.challengehub.mobile.core.badges.BadgeCatalogItem
import com.challengehub.mobile.core.badges.earnedBadgeItems
import com.challengehub.mobile.core.network.BadgeDto
import com.challengehub.mobile.core.network.NetworkModule

@Composable
fun BadgesScreen(contentPadding: PaddingValues, token: String?, userId: String? = null, onBack: () -> Unit = {}) {
    var badges by remember { mutableStateOf<List<BadgeDto>>(emptyList()) }
    LaunchedEffect(token) {
        val currentToken = token ?: return@LaunchedEffect
        runCatching {
            if (userId == null) NetworkModule.api.myBadges("Bearer $currentToken")
            else NetworkModule.api.publicProfile(userId, "Bearer $currentToken").badges
        }
            .onSuccess { badges = it }
    }

    val earned = remember(badges) { earnedBadgeItems(badges) }
    val earnedCodes = earned.map { it.code }.toSet()
    val locked = BadgeCatalog.filterNot { it.code in earnedCodes }
    val progress = earned.size.toFloat() / BadgeCatalog.size.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }
                Column {
                    Text("Значки", fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text("${earned.size} з ${BadgeCatalog.size} отримано", color = TextMuted, fontSize = 13.sp)
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(SurfaceSoft)
                    .padding(6.dp),
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = Pink,
                    trackColor = Color.White.copy(alpha = 0.08f),
                )
            }
        }
        item { SectionTitle("Отримані") }
        item {
            if (earned.isEmpty()) {
                Text("Поки немає отриманих значків", color = TextMuted, modifier = Modifier.padding(horizontal = 6.dp))
            } else {
                EarnedGrid(earned)
            }
        }
        item { SectionTitle("Заблоковані") }
        locked.forEach { badge ->
            item { LockedBadgeRow(badge) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = TextMuted, fontSize = 15.sp, modifier = Modifier.padding(start = 6.dp))
}

@Composable
private fun EarnedGrid(items: List<BadgeCatalogItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { badge ->
                    EarnedBadgeCard(badge, Modifier.weight(1f))
                }
                repeat(3 - rowItems.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun EarnedBadgeCard(badge: BadgeCatalogItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(94.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Purple.copy(alpha = 0.42f), Pink.copy(alpha = 0.26f))))
            .border(1.dp, Pink.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(badge.emoji, fontSize = 24.sp)
        Text(badge.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 14.sp, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun LockedBadgeRow(badge: BadgeCatalogItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface.copy(alpha = 0.82f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
            Text(badge.emoji, fontSize = 26.sp, color = Color.White.copy(alpha = 0.35f))
            Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted.copy(alpha = 0.7f), modifier = Modifier.size(15.dp).align(Alignment.BottomEnd))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(badge.title, color = Color.White.copy(alpha = 0.58f), fontWeight = FontWeight.Bold)
            Text(badge.description, color = TextMuted.copy(alpha = 0.72f), fontSize = 12.sp)
        }
    }
}

