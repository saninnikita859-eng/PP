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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.Gold
import com.challengehub.mobile.core.design.GradientButton
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.Surface
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.CreateChallengeRequestDto
import com.challengehub.mobile.core.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun CreateChallengeScreen(contentPadding: PaddingValues, token: String?, onCreated: () -> Unit, onBack: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("normal") }
    var difficultyOpen by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf("7") }
    var xp by remember { mutableStateOf("500") }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
            Text("Створити челендж", fontSize = 28.sp, fontWeight = FontWeight.Black)
        }

        LabeledInput(
            label = "Назва челенджу",
            value = title,
            onValueChange = { title = it },
            placeholder = "Наприклад: 30 днів медитації",
        )
        LabeledInput(
            label = "Опис",
            value = description,
            onValueChange = { description = it },
            placeholder = "Розкажіть детальніше про челендж...",
            minHeight = 126.dp,
            singleLine = false,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
            LabeledInput(
                label = "Винагорода XP",
                value = xp,
                onValueChange = { xp = it.filter(Char::isDigit).take(3) },
                placeholder = "500",
                keyboardType = KeyboardType.Number,
                trailing = { Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f),
            )
            LabeledInput(
                label = "Тривалість (днів)",
                value = duration,
                onValueChange = { duration = it.filter(Char::isDigit).take(2) },
                placeholder = "7",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Складність", color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface)
                    .clickable { difficultyOpen = true }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(difficultyLabel(difficulty), color = Color.White)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White)
                }
                DropdownMenu(expanded = difficultyOpen, onDismissRequest = { difficultyOpen = false }) {
                    listOf("easy" to "Легка", "normal" to "Середня", "hard" to "Складна").forEach { (value, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            difficulty = value
                            difficultyOpen = false
                        })
                    }
                }
            }
        }
        if (message != null) Text(message.orEmpty(), color = HotPink)
        Spacer(Modifier.height(26.dp))
        GradientButton("Опублікувати челендж", Modifier.fillMaxWidth().height(58.dp)) {
            val currentToken = token ?: return@GradientButton
            val durationValue = duration.toIntOrNull() ?: 0
            val xpValue = xp.toIntOrNull() ?: 0
            if (title.length < 3 || description.length < 10 || durationValue !in 1..60 || xpValue !in 1..500) {
                message = "Заповніть назву, опис, тривалість 1-60 днів і XP 1-500."
                return@GradientButton
            }
            scope.launch {
                runCatching {
                    NetworkModule.api.createChallenge(
                        "Bearer $currentToken",
                        CreateChallengeRequestDto(title, description, difficulty, durationValue, xpValue),
                    )
                }.onSuccess { onCreated() }
                    .onFailure { message = "Не вдалося створити челендж. Перевірте поля." }
            }
        }
    }
}

@Composable
private fun LabeledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted) },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 5,
            trailingIcon = trailing,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.75f),
                cursorColor = HotPink,
            ),
            modifier = Modifier.fillMaxWidth().height(minHeight),
        )
    }
}

private fun difficultyLabel(value: String): String = when (value) {
    "easy" -> "Легка"
    "hard" -> "Складна"
    else -> "Середня"
}
