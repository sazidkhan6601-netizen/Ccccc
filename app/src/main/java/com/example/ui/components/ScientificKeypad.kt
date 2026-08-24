package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScientificKeypad(
    isDegreeMode: Boolean,
    isSecondFunction: Boolean,
    onSecondToggle: () -> Unit,
    onAngleToggle: () -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onParenthesis: (String) -> Unit,
    onSquare: () -> Unit,
    onCube: () -> Unit,
    onPower: () -> Unit,
    onSquareRoot: () -> Unit,
    onCubeRoot: () -> Unit,
    onFactorial: () -> Unit,
    onInverse: () -> Unit,
    onPercent: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Memory Quick Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SciSmallButton("MC", onClick = onMemoryClear, modifier = Modifier.weight(1f))
            SciSmallButton("MR", onClick = onMemoryRecall, modifier = Modifier.weight(1f))
            SciSmallButton("M+", onClick = onMemoryAdd, modifier = Modifier.weight(1f))
            SciSmallButton("M-", onClick = onMemorySubtract, modifier = Modifier.weight(1f))
        }

        // Scientific Row 1: 2nd, RAD/DEG, sin/sin⁻¹, cos/cos⁻¹, tan/tan⁻¹
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SciKeyButton(
                text = "2nd",
                isActive = isSecondFunction,
                onClick = onSecondToggle,
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = if (isDegreeMode) "deg" else "rad",
                onClick = onAngleToggle,
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = if (isSecondFunction) "sin⁻¹" else "sin",
                onClick = { onFunction(if (isSecondFunction) "sin⁻¹" else "sin") },
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = if (isSecondFunction) "cos⁻¹" else "cos",
                onClick = { onFunction(if (isSecondFunction) "cos⁻¹" else "cos") },
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = if (isSecondFunction) "tan⁻¹" else "tan",
                onClick = { onFunction(if (isSecondFunction) "tan⁻¹" else "tan") },
                modifier = Modifier.weight(1f)
            )
        }

        // Scientific Row 2: ln/eˣ, log/10ˣ, 1/x, (, )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SciKeyButton(
                text = if (isSecondFunction) "eˣ" else "ln",
                onClick = { if (isSecondFunction) onFunction("exp") else onFunction("ln") },
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = if (isSecondFunction) "log₂" else "log",
                onClick = { if (isSecondFunction) onFunction("log2") else onFunction("log") },
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = "1/x",
                onClick = onInverse,
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = "(",
                onClick = { onParenthesis("(") },
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = ")",
                onClick = { onParenthesis(")") },
                modifier = Modifier.weight(1f)
            )
        }

        // Scientific Row 3: √ / ∛, x² / x³, xʸ, π, e
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SciKeyButton(
                text = if (isSecondFunction) "∛x" else "√x",
                onClick = { if (isSecondFunction) onCubeRoot() else onSquareRoot() },
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = if (isSecondFunction) "x³" else "x²",
                onClick = { if (isSecondFunction) onCube() else onSquare() },
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = "xʸ",
                onClick = onPower,
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = "π",
                onClick = { onConstant("π") },
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = "e",
                onClick = { onConstant("e") },
                modifier = Modifier.weight(1f)
            )
        }

        // Scientific Row 4: n!, %, |x|, sinh, cosh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SciKeyButton(
                text = "n!",
                onClick = onFactorial,
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = "%",
                onClick = onPercent,
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = "|x|",
                onClick = { onFunction("abs") },
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = "sinh",
                onClick = { onFunction("sinh") },
                modifier = Modifier.weight(1f)
            )
            SciKeyButton(
                text = "cosh",
                onClick = { onFunction("cosh") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SciKeyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .testTag("btn_sci_$text"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SciSmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .height(34.dp)
            .testTag("btn_mem_$text"),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
