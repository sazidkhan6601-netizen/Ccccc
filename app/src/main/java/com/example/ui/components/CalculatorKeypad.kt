package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorKeypad(
    hasInput: Boolean,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onNegate: () -> Unit,
    onCalculate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: AC/C, ⌫ (Backspace), ± (Negate), ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton(
                text = if (hasInput) "C" else "AC",
                type = CalcButtonType.ALERT,
                onClick = onClear,
                modifier = Modifier.weight(1f)
            )
            CalcIconButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                type = CalcButtonType.FUNCTION,
                onClick = onBackspace,
                modifier = Modifier.weight(1f)
            )
            CalcButton(
                text = "±",
                type = CalcButtonType.FUNCTION,
                onClick = onNegate,
                modifier = Modifier.weight(1f)
            )
            CalcButton(
                text = "÷",
                type = CalcButtonType.OPERATOR,
                onClick = { onOperator("÷") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton(text = "7", type = CalcButtonType.NUMBER, onClick = { onDigit("7") }, modifier = Modifier.weight(1f))
            CalcButton(text = "8", type = CalcButtonType.NUMBER, onClick = { onDigit("8") }, modifier = Modifier.weight(1f))
            CalcButton(text = "9", type = CalcButtonType.NUMBER, onClick = { onDigit("9") }, modifier = Modifier.weight(1f))
            CalcButton(text = "×", type = CalcButtonType.OPERATOR, onClick = { onOperator("×") }, modifier = Modifier.weight(1f))
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton(text = "4", type = CalcButtonType.NUMBER, onClick = { onDigit("4") }, modifier = Modifier.weight(1f))
            CalcButton(text = "5", type = CalcButtonType.NUMBER, onClick = { onDigit("5") }, modifier = Modifier.weight(1f))
            CalcButton(text = "6", type = CalcButtonType.NUMBER, onClick = { onDigit("6") }, modifier = Modifier.weight(1f))
            CalcButton(text = "−", type = CalcButtonType.OPERATOR, onClick = { onOperator("−") }, modifier = Modifier.weight(1f))
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton(text = "1", type = CalcButtonType.NUMBER, onClick = { onDigit("1") }, modifier = Modifier.weight(1f))
            CalcButton(text = "2", type = CalcButtonType.NUMBER, onClick = { onDigit("2") }, modifier = Modifier.weight(1f))
            CalcButton(text = "3", type = CalcButtonType.NUMBER, onClick = { onDigit("3") }, modifier = Modifier.weight(1f))
            CalcButton(text = "+", type = CalcButtonType.OPERATOR, onClick = { onOperator("+") }, modifier = Modifier.weight(1f))
        }

        // Row 5: 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton(
                text = "0",
                type = CalcButtonType.NUMBER,
                onClick = { onDigit("0") },
                modifier = Modifier.weight(2f)
            )
            CalcButton(
                text = ".",
                type = CalcButtonType.NUMBER,
                onClick = onDecimal,
                modifier = Modifier.weight(1f)
            )
            CalcButton(
                text = "=",
                type = CalcButtonType.EQUALS,
                onClick = onCalculate,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

enum class CalcButtonType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    ALERT,
    EQUALS
}

@Composable
fun CalcButton(
    text: String,
    type: CalcButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (type) {
        CalcButtonType.NUMBER -> Pair(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurface
        )
        CalcButtonType.OPERATOR -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        CalcButtonType.FUNCTION -> Pair(
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        CalcButtonType.ALERT -> Pair(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.onErrorContainer
        )
        CalcButtonType.EQUALS -> Pair(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp)
            .testTag("btn_$text"),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (type == CalcButtonType.EQUALS) 4.dp else 0.dp,
            pressedElevation = 1.dp
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = if (type == CalcButtonType.OPERATOR || type == CalcButtonType.EQUALS) 28.sp else 24.sp,
            fontWeight = if (type == CalcButtonType.NUMBER) FontWeight.Normal else FontWeight.SemiBold
        )
    }
}

@Composable
fun CalcIconButton(
    icon: ImageVector,
    contentDescription: String,
    type: CalcButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (type) {
        CalcButtonType.FUNCTION -> Pair(
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurface
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp)
            .testTag("btn_icon_${contentDescription.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}
