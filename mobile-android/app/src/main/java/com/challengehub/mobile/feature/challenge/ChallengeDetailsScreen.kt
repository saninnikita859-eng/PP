package com.challengehub.mobile.feature.challenge

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.GlassCard
import com.challengehub.mobile.core.design.Gold
import com.challengehub.mobile.core.design.GradientButton
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.Purple
import com.challengehub.mobile.core.design.SurfaceSoft
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.ChallengeDto
import com.challengehub.mobile.core.network.JoinChallengeRequestDto
import com.challengehub.mobile.core.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun ChallengeDetailsScreen(
    contentPadding: PaddingValues,
    challenge: ChallengeDto? = null,
    token: String? = null,
    onUpload: (ChallengeDto) -> Unit,
    onBack: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var resolvedChallenge by remember(challenge?.id) { mutableStateOf(challenge) }
    var joined by remember(challenge?.id) {
        mutableStateOf(challenge?.participationStatus in listOf("ACTIVE", "COMPLETED"))
    }

    val title = resolvedChallenge?.title ?: "Челендж"
    val description = resolvedChallenge?.description ?: "Опис челенджу недоступний."
    val duration = resolvedChallenge?.durationDays ?: 0
    val xp = resolvedChallenge?.xpReward ?: 0
    val participants = formatCount(resolvedChallenge?.participantsCount ?: 0)

    LaunchedEffect(challenge?.id) {
        val currentChallenge = challenge ?: return@LaunchedEffect
        runCatching { NetworkModule.api.challenges() }
            .onSuccess { all ->
                val full = all.firstOrNull {
                    it.id == currentChallenge.id && it.challengeType == currentChallenge.challengeType
                } ?: all.firstOrNull { it.id == currentChallenge.id }
                if (full != null) {
                    resolvedChallenge = full
                    joined = full.participationStatus in listOf("ACTIVE", "COMPLETED")
                }
            }
    }

    LaunchedEffect(token, resolvedChallenge?.id, resolvedChallenge?.challengeType) {
        val currentToken = token ?: return@LaunchedEffect
        val currentChallenge = resolvedChallenge ?: return@LaunchedEffect
        runCatching { NetworkModule.api.myChallenges("Bearer $currentToken") }
            .onSuccess { mine ->
                val mineItem = mine.firstOrNull {
                    it.id == currentChallenge.id && it.challengeType == currentChallenge.challengeType
                } ?: mine.firstOrNull { it.id == currentChallenge.id }
                if (mineItem != null) {
                    resolvedChallenge = mineItem
                    joined = mineItem.participationStatus in listOf("ACTIVE", "COMPLETED")
                } else {
                    joined = false
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp)
                .background(ChallengeGradient)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
                Text(description, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, maxLines = 2)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailStat(Icons.Default.Star, "$xp", "XP винагорода", Gold, Modifier.weight(1f))
                DetailStat(Icons.Default.Groups, participants, "Учасники", Purple, Modifier.weight(1f))
                DetailStat(Icons.Default.AccessTime, if (duration > 0) "$duration" else "-", "Днів", Color(0xFF55A9FF), Modifier.weight(1f))
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Про челендж", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(description, color = TextMuted, fontSize = 14.sp, lineHeight = 18.sp)
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Правила", fontSize = 22.sp, fontWeight = FontWeight.Black)
                RuleRow("Виконуй завдання відповідно до опису челенджу")
                RuleRow("Завантажуй власне відео-підтвердження")
                RuleRow("Не використовуй заборонений або чужий контент")
                RuleRow("AI автоматично перевірить відео перед публікацією")
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (joined) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(SurfaceSoft)
                            .clickable(enabled = resolvedChallenge != null) {
                                val currentToken = token
                                val currentChallenge = resolvedChallenge
                                if (currentToken != null && currentChallenge != null) {
                                    scope.launch {
                                        runCatching {
                                            NetworkModule.api.leaveChallenge(
                                                "Bearer $currentToken",
                                                currentChallenge.id,
                                                JoinChallengeRequestDto(currentChallenge.challengeType),
                                            )
                                        }.onSuccess { joined = false }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Вийти", color = TextMuted, fontWeight = FontWeight.Bold)
                    }
                } else {
                    GradientButton(
                        text = "Приєднатися",
                        modifier = Modifier.weight(1f),
                        enabled = resolvedChallenge != null,
                        onClick = {
                            val currentToken = token ?: return@GradientButton
                            val currentChallenge = resolvedChallenge ?: return@GradientButton
                            scope.launch {
                                runCatching {
                                    NetworkModule.api.joinChallenge(
                                        "Bearer $currentToken",
                                        currentChallenge.id,
                                        JoinChallengeRequestDto(currentChallenge.challengeType),
                                    )
                                }.onSuccess { joined = true }
                            }
                        },
                    )
                }
                GradientButton(
                    text = "Відео",
                    modifier = Modifier.weight(1f),
                    enabled = resolvedChallenge != null,
                    onClick = { resolvedChallenge?.let(onUpload) },
                )
            }
            Spacer(Modifier.height(84.dp))
        }
    }
}

@Composable
private fun DetailStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier,
) {
    GlassCard(modifier = modifier.height(88.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(23.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text(label, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun RuleRow(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HotPink, modifier = Modifier.size(19.dp))
        Text(text, color = TextMuted, fontSize = 13.sp, lineHeight = 17.sp)
    }
}

private fun formatCount(value: Int): String = value.toString().reversed().chunked(3).joinToString(" ").reversed()
