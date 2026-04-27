package com.silas.omaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import com.silas.omaster.ui.theme.AppDesign
import com.silas.omaster.ui.theme.themedBackground
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.util.perform

@Composable
fun HomeTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(themedBackground())
            .padding(horizontal = AppDesign.TabBarPadding, vertical = AppDesign.ItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(AppDesign.ItemSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, (title, count) ->
            val isSelected = selectedTab == index
            val displayText = if (count > 0) "$title $count" else title

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else themedCardBackground(),
                label = "tabBackground"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else themedTextPrimary().copy(alpha = AppDesign.TertiaryAlpha),
                label = "tabText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(AppDesign.TabBarHeight)
                    .clip(AppDesign.PillShape)
                    .background(backgroundColor)
                    .clickable {
                        haptic.perform(HapticFeedbackType.ToggleOn)
                        onTabSelected(index)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayText,
                    color = textColor,
                    fontSize = AppDesign.TabTextSize,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
