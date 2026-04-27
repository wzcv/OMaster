package com.silas.omaster.ui.discover

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silas.omaster.R
import com.silas.omaster.data.ColorCardLibrary
import com.silas.omaster.data.local.ColorCardHistory
import com.silas.omaster.data.local.CheckInManager
import com.silas.omaster.data.local.ColorWalkManager
import com.silas.omaster.model.ColorCard
import com.silas.omaster.model.ColorInfo
import com.silas.omaster.model.ColorRole
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.AppDesign
import com.silas.omaster.ui.theme.themedBackground
import com.silas.omaster.ui.theme.themedBorderLight
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val PREFS_NAME = "color_walk_prefs"
private const val KEY_HAS_SEEN_GUIDE = "has_seen_guide"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorWalkScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val colorWalkManager = remember { ColorWalkManager(context) }
    val checkInManager = remember { CheckInManager(context) }

    val todayCard by colorWalkManager.todayCardFlow.collectAsState()
    val history by colorWalkManager.historyFlow.collectAsState()
    val drawCount by colorWalkManager.drawCountFlow.collectAsState()

    var isAnimating by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(false) }
    var selectedHistoryCard by remember { mutableStateOf<ColorCard?>(null) }
    var selectedHistoryDate by remember { mutableStateOf<String?>(null) }
    var isChallengeCompleted by remember(todayCard?.id, drawCount) {
        mutableStateOf(checkInManager.getTodayRecord()?.hasCompletedChallenge == true)
    }

    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        showGuide = !prefs.getBoolean(KEY_HAS_SEEN_GUIDE, false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themedBackground())
    ) {
        OMasterTopAppBar(
            title = stringResource(R.string.colorwalk_title),
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            onBack = onBack,
            actions = {
                IconButton(onClick = { showGuide = true }) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "玩法说明",
                        tint = themedTextPrimary()
                    )
                }
                if (history.isNotEmpty()) {
                    IconButton(onClick = { showHistorySheet = true }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.colorwalk_history),
                            tint = themedTextPrimary()
                        )
                    }
                }
            }
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(AppDesign.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (todayCard != null) {
                ColorWalkCard(
                    card = todayCard!!,
                    isAnimating = isAnimating,
                    drawCount = drawCount,
                    dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("MM.dd")),
                    onRedraw = {
                        if (colorWalkManager.canDrawMore()) {
                            isAnimating = true
                            val nextCard = colorWalkManager.drawTodayCard()
                            if (nextCard != null) {
                                checkInManager.checkIn(
                                    cardId = nextCard.id,
                                    cardTheme = context.getString(nextCard.themeResId)
                                )
                                isChallengeCompleted = false
                            }
                        }
                    },
                    onClear = {
                        colorWalkManager.clearTodayCard()
                        isChallengeCompleted = false
                    },
                    isChallengeCompleted = isChallengeCompleted,
                    onCompleteChallenge = {
                        checkInManager.markChallengeCompleted()
                        isChallengeCompleted = true
                    },
                    canRedraw = colorWalkManager.canDrawMore(),
                    remainingDraws = colorWalkManager.getRemainingDraws(),
                    showActions = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                ColorWalkEmptyCard(
                    onDrawClick = {
                        isAnimating = true
                        val card = colorWalkManager.drawTodayCard()
                        if (card != null) {
                            checkInManager.checkIn(
                                cardId = card.id,
                                cardTheme = context.getString(card.themeResId)
                            )
                            isChallengeCompleted = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showGuide) {
        ColorWalkGuide(
            onDismiss = {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(KEY_HAS_SEEN_GUIDE, true).apply()
                showGuide = false
            }
        )
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = sheetState
        ) {
            HistorySheetContent(
                history = history,
                onClearHistory = {
                    showHistorySheet = false
                    showClearHistoryDialog = true
                },
                onCardClick = { card, date ->
                    selectedHistoryCard = card
                    selectedHistoryDate = date
                    showHistorySheet = false
                }
            )
        }
    }

    if (selectedHistoryCard != null) {
        AlertDialog(
            onDismissRequest = {
                selectedHistoryCard = null
                selectedHistoryDate = null
            },
            title = {
                Text(
                    text = stringResource(R.string.colorwalk_history_preview_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 560.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ColorWalkCard(
                        card = selectedHistoryCard!!,
                        isAnimating = false,
                        drawCount = 0,
                        dateLabel = selectedHistoryDate,
                        onRedraw = {},
                        onClear = {},
                        isChallengeCompleted = false,
                        onCompleteChallenge = {},
                        canRedraw = false,
                        remainingDraws = 0,
                        showActions = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedHistoryCard = null
                        selectedHistoryDate = null
                    }
                ) {
                    Text(stringResource(R.string.colorwalk_close_preview))
                }
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.colorwalk_clear_history)) },
            text = { Text(stringResource(R.string.colorwalk_clear_history_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        colorWalkManager.clearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(stringResource(R.string.colorwalk_confirm_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(stringResource(R.string.colorwalk_cancel))
                }
            }
        )
    }

    LaunchedEffect(todayCard) {
        if (todayCard != null && isAnimating) {
            kotlinx.coroutines.delay(500)
            isAnimating = false
        }
    }
}

@Composable
private fun ColorWalkGuide(
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.colorwalk_guide_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.colorwalk_guide_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = themedTextSecondary(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    GuideStep(page = page)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        themedTextSecondary().copy(alpha = 0.3f)
                                )
                        )
                        if (index < 2) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(
                    text = if (pagerState.currentPage < 2)
                        stringResource(R.string.colorwalk_guide_next)
                    else
                        stringResource(R.string.colorwalk_guide_start)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.colorwalk_cancel))
            }
        }
    )
}

@Composable
private fun GuideStep(page: Int) {
    val (icon, title, desc) = when (page) {
        0 -> Triple(
            Icons.Default.AutoAwesome,
            stringResource(R.string.colorwalk_guide_step1_title),
            stringResource(R.string.colorwalk_guide_step1_desc)
        )
        1 -> Triple(
            Icons.Default.Palette,
            stringResource(R.string.colorwalk_guide_step2_title),
            stringResource(R.string.colorwalk_guide_step2_desc)
        )
        else -> Triple(
            Icons.Default.Refresh,
            stringResource(R.string.colorwalk_guide_step3_title),
            stringResource(R.string.colorwalk_guide_step3_desc)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = themedTextPrimary(),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = themedTextSecondary(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ColorWalkEmptyCard(
    onDrawClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = themedCardBackground()
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE57373))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFB74D))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF81C784))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF64B5F6))
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.colorwalk_empty_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = themedTextPrimary(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.colorwalk_empty_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = themedTextSecondary(),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .clickable { onDrawClick() }
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.colorwalk_draw_button),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.colorwalk_draw_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = themedTextSecondary(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ColorWalkCard(
    card: ColorCard,
    isAnimating: Boolean,
    drawCount: Int,
    dateLabel: String? = null,
    onRedraw: () -> Unit,
    onClear: () -> Unit,
    isChallengeCompleted: Boolean,
    onCompleteChallenge: () -> Unit,
    canRedraw: Boolean,
    remainingDraws: Int,
    showActions: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 0.95f else 1f,
        animationSpec = tween(durationMillis = 400),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = card.colors.map { it.toColor() }
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp, end = 120.dp)
                ) {
                    Text(
                        text = stringResource(card.themeResId),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(card.descriptionResId),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = dateLabel ?: LocalDate.now().format(DateTimeFormatter.ofPattern("MM.dd")),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    if (drawCount > 0) {
                        Text(
                            text = stringResource(R.string.colorwalk_draw_count, drawCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.colorwalk_history),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.colorwalk_today_challenge),
                    style = MaterialTheme.typography.labelMedium,
                    color = themedTextSecondary(),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ChallengeCard(
                    challenge = stringResource(card.challengeResId),
                    isCompleted = isChallengeCompleted,
                    onComplete = onCompleteChallenge,
                    showAction = showActions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = stringResource(R.string.colorwalk_best_scenes),
                    style = MaterialTheme.typography.labelMedium,
                    color = themedTextSecondary(),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    card.sceneTags.forEach { sceneTag ->
                        SceneTagChip(label = stringResource(sceneTag))
                    }
                }

                Text(
                    text = stringResource(R.string.colorwalk_theme_primary),
                    style = MaterialTheme.typography.labelMedium,
                    color = themedTextSecondary(),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    card.colors.filter { it.role == ColorRole.PRIMARY }.forEach { colorInfo ->
                        ColorBlock(
                            colorInfo = colorInfo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.colorwalk_theme_accent),
                    style = MaterialTheme.typography.labelMedium,
                    color = themedTextSecondary(),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    card.colors.filter { it.role == ColorRole.ACCENT }.forEach { colorInfo ->
                        ColorBlock(
                            colorInfo = colorInfo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            ShootingTipsCard(
                card = card,
                modifier = Modifier.fillMaxWidth()
            )

            if (showActions) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .clickable { onClear() }
                                .weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.colorwalk_hide_today_card),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .clickable { if (canRedraw) onRedraw() }
                                .weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (canRedraw)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else
                                    themedCardBackground()
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = if (canRedraw)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            themedTextSecondary(),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.colorwalk_redraw),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (canRedraw)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            themedTextSecondary(),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (canRedraw) {
                                    Text(
                                        text = "($remainingDraws)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.colorwalk_no_more_draws),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = themedTextSecondary()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorBlock(
    colorInfo: ColorInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorInfo.toColor())
                .border(
                    width = 1.dp,
                    color = themedBorderLight(),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(colorInfo.nameResId),
            style = MaterialTheme.typography.labelSmall,
            color = themedTextPrimary(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )

        Text(
            text = "#${colorInfo.hex}",
            style = MaterialTheme.typography.labelSmall,
            color = themedTextSecondary(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChallengeCard(
    challenge: String,
    isCompleted: Boolean,
    onComplete: () -> Unit,
    showAction: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                Color(0xFF4CAF50).copy(alpha = 0.10f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) {
                                Color(0xFF4CAF50).copy(alpha = 0.14f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = challenge,
                    style = MaterialTheme.typography.bodyMedium,
                    color = themedTextPrimary(),
                    modifier = Modifier.weight(1f)
                )
            }

            if (showAction) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onComplete,
                    enabled = !isCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) {
                            Color(0xFF4CAF50).copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        contentColor = if (isCompleted) Color(0xFF2E7D32) else Color.White,
                        disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.16f),
                        disabledContentColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isCompleted) {
                            stringResource(R.string.checkin_completed)
                        } else {
                            stringResource(R.string.checkin_mark_complete)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SceneTagChip(
    label: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(themedCardBackground())
            .border(
                width = 1.dp,
                color = themedBorderLight(),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = themedTextPrimary()
        )
    }
}

@Composable
private fun HistorySheetContent(
    history: List<com.silas.omaster.data.local.ColorCardHistory>,
    onClearHistory: () -> Unit,
    onCardClick: (ColorCard, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.colorwalk_history),
                style = MaterialTheme.typography.titleLarge,
                color = themedTextPrimary(),
                fontWeight = FontWeight.Bold
            )

            if (history.isNotEmpty()) {
                TextButton(onClick = onClearHistory) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.colorwalk_clear_history),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.colorwalk_no_history),
                style = MaterialTheme.typography.bodyMedium,
                color = themedTextSecondary(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(history) { item: ColorCardHistory ->
                    val card = ColorCardLibrary.getCardById(item.cardId)
                    if (card != null) {
                        HistoryCardItem(
                            card = card,
                            date = item.date,
                            onClick = { onCardClick(card, item.date) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ShootingTipsCard(
    card: ColorCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = themedBorderLight(),
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.colorwalk_shooting_tips),
                    style = MaterialTheme.typography.labelLarge,
                    color = themedTextPrimary(),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(card.tipsResId),
                style = MaterialTheme.typography.bodySmall,
                color = themedTextSecondary(),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun HistoryCardItem(
    card: ColorCard,
    date: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = themedBorderLight(),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                card.colors.take(4).forEach { colorInfo ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorInfo.toColor())
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = stringResource(card.themeResId),
                    style = MaterialTheme.typography.labelMedium,
                    color = themedTextPrimary(),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = themedTextSecondary()
                )
            }
        }
    }
}
