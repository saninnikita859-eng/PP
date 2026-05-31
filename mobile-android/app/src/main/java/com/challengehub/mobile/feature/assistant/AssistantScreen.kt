package com.challengehub.mobile.feature.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.Surface
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.AssistantRequestDto
import com.challengehub.mobile.core.network.NetworkModule
import kotlinx.coroutines.launch
import retrofit2.HttpException

private data class ChatMessage(val text: String, val mine: Boolean)

@Composable
fun AssistantScreen(contentPadding: PaddingValues, token: String?, onClose: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("Я AI-помічник ChallengeHub. Допоможу підібрати челендж, поясню XP і підкажу ідею для відео.", false)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(ChallengeGradient).padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                }
                Column {
                    Text("AI-помічник", fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text("Завжди готовий допомогти", color = TextMuted, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Закрити", tint = Color.White)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages) { message ->
                ChatBubble(message)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Запитай щось...") },
                modifier = Modifier.weight(1f),
            )
            Button(
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Surface),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(ChallengeGradient),
                onClick = {
                    val currentToken = token ?: return@Button
                    if (input.isBlank()) return@Button
                    val question = input.trim()
                    messages = messages + ChatMessage(question, true)
                    input = ""
                    loading = true
                    scope.launch {
                        val auth = "Bearer $currentToken"
                        val me = runCatching { NetworkModule.api.me(auth) }.getOrElse {
                            messages = messages + ChatMessage("Не вдалося перевірити Premium-статус. Перевірте інтернет або сервер URL.", false)
                            loading = false
                            return@launch
                        }
                        if (!me.isPremium) {
                            messages = messages + ChatMessage("AI-помічник доступний тільки Premium-користувачам. Premium ще не активований для цього акаунта.", false)
                            loading = false
                            return@launch
                        }
                        runCatching { NetworkModule.api.assistant(auth, AssistantRequestDto(question)) }
                            .onSuccess { messages = messages + ChatMessage(it.answer, false) }
                            .onFailure {
                                val text = when ((it as? HttpException)?.code()) {
                                    401 -> "Сесію втрачено. Увійдіть в акаунт повторно."
                                    403 -> "Premium є в застосунку, але сервер відхилив AI-запит. Перезапустіть застосунок або перевірте акаунт у профілі."
                                    502, 503 -> "AI тимчасово недоступний на сервері. Спробуйте ще раз пізніше."
                                    else -> "Не вдалося отримати відповідь AI. Перевірте інтернет або сервер."
                                }
                                messages = messages + ChatMessage(text, false)
                            }
                        loading = false
                    }
                },
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Надіслати", tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.mine) Arrangement.End else Arrangement.Start,
    ) {
        Text(
            text = message.text,
            color = Color.White,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (message.mine) ChallengeGradient else Brush.linearGradient(listOf(Surface, Surface)))
                .padding(14.dp),
        )
    }
}
