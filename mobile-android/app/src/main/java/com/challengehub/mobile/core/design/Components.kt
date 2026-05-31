package com.challengehub.mobile.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

val ChallengeGradient = Brush.linearGradient(
    colors = listOf(Purple, Pink),
    start = Offset.Zero,
    end = Offset.Infinite,
)

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = SurfaceSoft),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) ChallengeGradient else Brush.linearGradient(listOf(SurfaceSoft, SurfaceSoft))),
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DarkInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted) },
        leadingIcon = leadingIcon,
        visualTransformation = visualTransformation,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(color = TextPrimary),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedBorderColor = Purple,
            unfocusedBorderColor = Surface,
            cursorColor = Pink,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Surface)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)), RoundedCornerShape(22.dp)),
    ) {
        content()
    }
}

@Composable
fun CircleAction(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White)
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit = {}) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(SurfaceSoft)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
            .height(34.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        content = {
            Text(text, color = TextPrimary)
            content()
        },
    )
}
