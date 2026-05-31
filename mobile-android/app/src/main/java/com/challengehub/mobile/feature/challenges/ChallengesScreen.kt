package com.challengehub.mobile.feature.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.Gold
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.Pink
import com.challengehub.mobile.core.design.Purple
import com.challengehub.mobile.core.design.Surface
import com.challengehub.mobile.core.design.SurfaceSoft
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.ChallengeDto
import com.challengehub.mobile.core.network.JoinChallengeRequestDto
import com.challengehub.mobile.core.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun ChallengesScreen(
    contentPadding: PaddingValues,
    token: String?,
    onCreateChallenge: () -> Unit = {},
    onOpenDetails: (ChallengeDto) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var challenges by remember { mutableStateOf<List<ChallengeDto>>(emptyList()) }
    var joinedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var message by remember { mutableStateOf<String?>(null) }
    var activeFilter by remember { mutableStateOf("ALL") }

    fun refreshJoined(currentToken: String?) {
        if (currentToken.isNullOrBlank()) return
        scope.launch {
            runCatching { NetworkModule.api.myChallenges("Bearer $currentToken") }
                .onSuccess { joinedIds = it.map { item -> item.id }.toSet() }
        }
    }

    LaunchedEffect(token) {
        runCatching { NetworkModule.api.challenges() }
            .onSuccess { challenges = it }
            .onFailure { message = "Не вдалося завантажити челенджі" }
        refreshJoined(token)
    }

    val visible = when (activeFilter) {
        "POPULAR" -> challenges.sortedByDescending { it.xpReward }
        "NEW" -> challenges.sortedByDescending { it.createdAt }
        else -> challenges
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 110.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Челенджі", fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text("Обери свою наступну пригоду", color = TextMuted, fontSize = 13.sp)
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ChallengeGradient)
                        .clickable(onClick = onCreateChallenge),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Створити", tint = Color.White, modifier = Modifier.size(34.dp))
                }
            }
            Spacer(Modifier.height(22.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip("Усі", Icons.Default.Public, activeFilter == "ALL", Modifier.weight(1f)) { activeFilter = "ALL" }
                FilterChip("Популярні", Icons.Default.AutoGraph, activeFilter == "POPULAR", Modifier.weight(1f)) { activeFilter = "POPULAR" }
                FilterChip("Нові", Icons.Default.AccessTime, activeFilter == "NEW", Modifier.weight(1f)) { activeFilter = "NEW" }
            }
            if (message != null) {
                Spacer(Modifier.height(12.dp))
                Text(message.orEmpty(), color = TextMuted)
            }
        }
        items(visible) { challenge ->
            val joined = challenge.id in joinedIds
            ChallengeCard(
                challenge = challenge,
                joined = joined,
                onJoinToggle = {
                    val currentToken = token ?: return@ChallengeCard
                    scope.launch {
                        runCatching {
                            if (joined) {
                                NetworkModule.api.leaveChallenge("Bearer $currentToken", challenge.id, JoinChallengeRequestDto(challenge.challengeType))
                            } else {
                                NetworkModule.api.joinChallenge("Bearer $currentToken", challenge.id, JoinChallengeRequestDto(challenge.challengeType))
                            }
                        }
                        .onSuccess {
                            joinedIds = if (joined) joinedIds - challenge.id else joinedIds + challenge.id
                            message = if (joined) "Челендж прибрано з активних" else "Челендж додано до активних"
                        }
                        .onFailure {
                            message = "Не вдалося оновити участь. Перевір ліміт 5 активних челенджів."
                        }
                    }
                },
                onDetails = { onOpenDetails(challenge) },
            )
        }
    }
}

@Composable
private fun FilterChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) ChallengeGradient else Brush.linearGradient(listOf(SurfaceSoft, SurfaceSoft)))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = if (active) Color.White else TextMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(6.dp))
        Text(text, color = if (active) Color.White else TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ChallengeCard(challenge: ChallengeDto, joined: Boolean, onJoinToggle: () -> Unit, onDetails: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Surface)
            .clickable(onClick = onDetails)
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(challenge.title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                        StatusPill(if (joined) "Активний" else if (challenge.status == "PUBLISHED") "Новий" else challenge.status, joined)
                    }
                    Text(challenge.description, color = TextMuted, fontSize = 12.sp, maxLines = 2)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Stat(Icons.Default.Star, "${challenge.xpReward} XP", Gold)
                Stat(Icons.Default.Groups, formatCount(challenge.participantsCount), Purple)
                DifficultyPill(challenge.difficulty)
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (joined) Brush.linearGradient(listOf(SurfaceSoft, SurfaceSoft)) else Brush.linearGradient(listOf(Purple, Pink)))
                        .clickable(onClick = onJoinToggle)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                ) {
                    Text(if (joined) "Вийти" else "Приєднатись", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) Color(0xFF145F38) else SurfaceSoft)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text, color = if (active) Color(0xFF59E391) else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Stat(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text, color = Color.White.copy(alpha = 0.88f), fontSize = 12.sp)
    }
}

@Composable
private fun DifficultyPill(value: String) {
    val color = when (value) {
        "easy" -> Color(0xFF22C55E)
        "hard" -> HotPink
        else -> Gold
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(translateDifficulty(value), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private fun translateDifficulty(value: String): String = when (value) {
    "easy" -> "Легкий"
    "hard" -> "Складний"
    else -> "Середній"
}

private fun formatCount(value: Int): String {
    return value.toString().reversed().chunked(3).joinToString(" ").reversed()
}
