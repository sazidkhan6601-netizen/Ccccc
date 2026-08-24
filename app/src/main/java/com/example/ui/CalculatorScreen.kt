package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.ScientificKeypad
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsStateWithLifecycle()
    val previewResult by viewModel.previewResult.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastResult.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isScientificMode by viewModel.isScientificMode.collectAsStateWithLifecycle()
    val isDegreeMode by viewModel.isDegreeMode.collectAsStateWithLifecycle()
    val isSecondFunction by viewModel.isSecondFunction.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isHistoryOpen by viewModel.isHistoryOpen.collectAsStateWithLifecycle()
    val memoryValue by viewModel.memoryValue.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val historySearchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()

    var showThemeMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Calculator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isScientificMode) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = if (isScientificMode) "Scientific" else "Standard",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isScientificMode) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Scientific Mode Toggle Button
                    FilledIconToggleButton(
                        checked = isScientificMode,
                        onCheckedChange = { viewModel.toggleScientificMode() },
                        modifier = Modifier.testTag("btn_toggle_scientific"),
                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Toggle Scientific Mode",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // History Button with Badge
                    IconButton(
                        onClick = { viewModel.setHistoryOpen(true) },
                        modifier = Modifier.testTag("btn_open_history")
                    ) {
                        BadgedBox(
                            badge = {
                                if (historyList.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text("${historyList.size.coerceAtMost(99)}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = "Calculation History"
                            )
                        }
                    }

                    // Theme Selector Dropdown
                    Box {
                        IconButton(
                            onClick = { showThemeMenu = true },
                            modifier = Modifier.testTag("btn_theme_menu")
                        ) {
                            Icon(
                                imageVector = when (themeMode) {
                                    ThemeMode.LIGHT -> Icons.Outlined.LightMode
                                    ThemeMode.DARK -> Icons.Outlined.DarkMode
                                    ThemeMode.OLED -> Icons.Default.Brightness2
                                    ThemeMode.SYSTEM -> Icons.Default.Palette
                                },
                                contentDescription = "Select Theme"
                            )
                        }

                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false },
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Dark Theme") },
                                leadingIcon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                                onClick = {
                                    viewModel.setThemeMode(ThemeMode.DARK)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Light Theme") },
                                leadingIcon = { Icon(Icons.Outlined.LightMode, contentDescription = null) },
                                onClick = {
                                    viewModel.setThemeMode(ThemeMode.LIGHT)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("OLED Pitch Black") },
                                leadingIcon = { Icon(Icons.Default.Brightness2, contentDescription = null) },
                                onClick = {
                                    viewModel.setThemeMode(ThemeMode.OLED)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("System Default") },
                                leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                                onClick = {
                                    viewModel.setThemeMode(ThemeMode.SYSTEM)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 540.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Display Area
                CalculatorDisplay(
                    expression = expression,
                    previewResult = previewResult,
                    lastResult = lastResult,
                    errorMessage = errorMessage,
                    isDegreeMode = isDegreeMode,
                    isSecondFunction = isSecondFunction,
                    memoryValue = memoryValue,
                    onToggleAngleMode = { viewModel.toggleAngleMode() },
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scientific Panel (Animated expansion)
                AnimatedVisibility(
                    visible = isScientificMode,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        ScientificKeypad(
                            isDegreeMode = isDegreeMode,
                            isSecondFunction = isSecondFunction,
                            onSecondToggle = { viewModel.toggleSecondFunction() },
                            onAngleToggle = { viewModel.toggleAngleMode() },
                            onFunction = { viewModel.onFunction(it) },
                            onConstant = { viewModel.onConstant(it) },
                            onParenthesis = { viewModel.onParenthesis(it) },
                            onSquare = { viewModel.onSquare() },
                            onCube = { viewModel.onCube() },
                            onPower = { viewModel.onPower() },
                            onSquareRoot = { viewModel.onSquareRoot() },
                            onCubeRoot = { viewModel.onCubeRoot() },
                            onFactorial = { viewModel.onFactorial() },
                            onInverse = { viewModel.onInverse() },
                            onPercent = { viewModel.onPercent() },
                            onMemoryAdd = { viewModel.onMemoryAdd() },
                            onMemorySubtract = { viewModel.onMemorySubtract() },
                            onMemoryRecall = { viewModel.onMemoryRecall() },
                            onMemoryClear = { viewModel.onMemoryClear() }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Standard Keypad
                CalculatorKeypad(
                    hasInput = expression.isNotEmpty(),
                    onDigit = { viewModel.onDigit(it) },
                    onDecimal = { viewModel.onDecimal() },
                    onOperator = { viewModel.onOperator(it) },
                    onClear = { viewModel.onClear() },
                    onBackspace = { viewModel.onBackspace() },
                    onNegate = { viewModel.onNegate() },
                    onCalculate = { viewModel.onCalculate() },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }

    // History Sheet
    if (isHistoryOpen) {
        HistoryBottomSheet(
            historyList = historyList,
            searchQuery = historySearchQuery,
            showFavoritesOnly = showFavoritesOnly,
            onSearchQueryChange = { viewModel.setHistorySearchQuery(it) },
            onToggleShowFavoritesOnly = { viewModel.toggleShowFavoritesOnly() },
            onUseExpression = { viewModel.onUseHistoryExpression(it) },
            onUseResult = { viewModel.onUseHistoryResult(it) },
            onDeleteHistory = { viewModel.onDeleteHistory(it) },
            onToggleFavorite = { id, current -> viewModel.onToggleFavorite(id, current) },
            onClearAll = { viewModel.onClearAllHistory() },
            onDismiss = { viewModel.setHistoryOpen(false) }
        )
    }
}
