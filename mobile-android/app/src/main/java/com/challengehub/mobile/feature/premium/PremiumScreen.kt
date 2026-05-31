package com.challengehub.mobile.feature.premium

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.Gold
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.Pink
import com.challengehub.mobile.core.design.Purple
import com.challengehub.mobile.core.design.Surface
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun PremiumScreen(contentPadding: PaddingValues, token: String?, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }

    fun openCheckout() {
        val currentToken = token ?: return
        scope.launch {
            runCatching { NetworkModule.api.premiumCheckout("Bearer $currentToken") }
                .onSuccess { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.checkoutUrl))) }
                .onFailure { message = "Stripe checkout тимчасово недоступний." }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Закрити", tint = Color.White, modifier = Modifier.size(28.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFFD400), Color(0xFFFF8A00)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.Black, modifier = Modifier.size(42.dp))
            }
            Text("Premium підписка", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
            Text("Відкрий повний досвід ChallengeHub", color = TextMuted, fontSize = 14.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(ChallengeGradient)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("Premium місячна", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                    Text("Щомісячна оплата", color = Color.White.copy(alpha = 0.82f), fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₴299", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text("/місяць", color = Color.White, fontSize = 12.sp)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
                    .clickable(onClick = ::openCheckout),
                contentAlignment = Alignment.Center,
            ) {
                Text("Оформити Premium", color = Purple, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }

        Text("Premium можливості", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PremiumFeature(Icons.Default.AutoGraph, "Необмежений\nAI", Gold, Modifier.weight(1f))
            PremiumFeature(Icons.Default.FlashOn, "2x XP\nбуст", Color(0xFFFF8A00), Modifier.weight(1f))
            PremiumFeature(Icons.Default.NotInterested, "Без\nреклами", Color(0xFF15D48A), Modifier.weight(1f))
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CompareRow("Без реклами", free = false, premium = true)
            CompareRow("Необмежений AI помічник", free = false, premium = true)
            CompareRow("2x буст XP", free = false, premium = true)
            CompareRow("Зміна фону профілю", free = false, premium = true)
            CompareRow("Базові челенджі", free = true, premium = true)
            CompareRow("Доступ до спільноти", free = true, premium = true)
        }

        if (message != null) Text(message.orEmpty(), color = TextMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text("Платіж обробляється безпечно через Stripe", color = TextMuted.copy(alpha = 0.68f), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(
            "Оформлюючи підписку, ти погоджуєшся з Умовами використання та Політикою конфіденційності",
            color = TextMuted.copy(alpha = 0.68f),
            fontSize = 11.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(88.dp))
    }
}

@Composable
private fun PremiumFeature(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(86.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color.White.copy(alpha = 0.88f), fontSize = 11.sp, lineHeight = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CompareRow(title: String, free: Boolean, premium: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, modifier = Modifier.weight(1f))
        CompareMark(free)
        Spacer(Modifier.size(24.dp))
        CompareMark(premium)
    }
}

@Composable
private fun CompareMark(available: Boolean) {
    Icon(
        imageVector = if (available) Icons.Default.Check else Icons.Default.Close,
        contentDescription = null,
        tint = if (available) Color(0xFF15D48A) else TextMuted.copy(alpha = 0.55f),
        modifier = Modifier.size(18.dp),
    )
}
