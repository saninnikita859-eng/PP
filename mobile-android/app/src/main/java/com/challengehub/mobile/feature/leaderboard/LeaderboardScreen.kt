package com.challengehub.mobile.feature.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.GlassCard
import com.challengehub.mobile.core.design.Gold
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.SurfaceSoft
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.LeaderboardEntryDto
import com.challengehub.mobile.core.network.NetworkModule

@Composable
fun LeaderboardScreen(contentPadding: PaddingValues) {
    var leaders by remember { mutableStateOf<List<LeaderboardEntryDto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var period by remember { mutableStateOf("weekly") }

    LaunchedEffect(period) {
        runCatching { NetworkModule.api.leaderboard(period) }
            .onSuccess { leaders = it; error = null }
            .onFailure { error = "Не вдалося завантажити лідерів" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 42.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(78.dp))
                Text("Таблиця лідерів", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Найкращі користувачі ChallengeHub", color = TextMuted, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 22.dp)) {
                    PeriodPill("Цей тиждень", period == "weekly") { period = "weekly" }
                    PeriodPill("Весь час", period == "all_time") { period = "all_time" }
                }
                if (error != null) Text(error.orEmpty(), color = TextMuted, modifier = Modifier.padding(top = 8.dp))
            }
        }
        items(leaders) { leader ->
            LeaderRow(leader)
        }
    }
}

@Composable
private fun PeriodPill(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) ChallengeGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(SurfaceSoft, SurfaceSoft)))
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 11.dp),
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun LeaderRow(leader: LeaderboardEntryDto) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("#${leader.rank}", color = if (leader.rank <= 3) Gold else TextMuted, fontWeight = FontWeight.Black)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ChallengeGradient),
                contentAlignment = Alignment.Center,
            ) {
                Text(leader.fullName.firstOrNull()?.uppercase() ?: "U", color = Color.White, fontWeight = FontWeight.Black)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(leader.fullName, fontWeight = FontWeight.Bold)
                Text("@${leader.username}", color = TextMuted, fontSize = 12.sp)
            }
            Text("⚡ ${leader.xp}", color = Gold, fontWeight = FontWeight.Bold)
        }
    }
}
