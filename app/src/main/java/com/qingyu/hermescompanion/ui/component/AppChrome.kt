package com.qingyu.hermescompanion.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qingyu.hermescompanion.R
import com.qingyu.hermescompanion.ui.AppRoute
import com.qingyu.hermescompanion.ui.theme.HermesSkin
import com.qingyu.hermescompanion.ui.theme.HermesSpacing
import com.qingyu.hermescompanion.ui.theme.HermesColors

val HermesGradient: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(MaterialTheme.colorScheme.primary, HermesColors.extended.success),
        start = Offset.Zero,
        end = Offset(420f, 420f),
    )

@Composable
fun AmbientBackground(content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val skin = HermesSkin.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (skin.glass) Brush.verticalGradient(
                    listOf(
                        colors.background,
                        colors.primaryContainer.copy(alpha = 0.30f),
                        colors.background,
                        colors.secondaryContainer.copy(alpha = 0.24f),
                    ),
                ) else SolidColor(colors.background),
            ),
    ) {
        if (skin.glass) {
            Box(
                Modifier
                    .size(300.dp)
                    .offset(x = (-130).dp, y = (-105).dp)
                    .background(
                        Brush.radialGradient(listOf(colors.primary.copy(alpha = 0.16f), Color.Transparent)),
                        CircleShape,
                    ),
            )
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(270.dp)
                    .offset(x = 135.dp, y = (-80).dp)
                    .background(
                        Brush.radialGradient(listOf(colors.secondary.copy(alpha = 0.14f), Color.Transparent)),
                        CircleShape,
                    ),
            )
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .size(250.dp)
                    .offset(x = (-120).dp, y = 120.dp)
                    .background(
                        Brush.radialGradient(listOf(colors.tertiary.copy(alpha = 0.11f), Color.Transparent)),
                        CircleShape,
                    ),
            )
        }
        content()
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(17.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val skin = HermesSkin.current
    val panelBrush = if (skin.glass) {
        Brush.linearGradient(
            listOf(
                colors.surfaceContainerLow,
                colors.surfaceContainer,
            ),
        )
    } else {
        Brush.linearGradient(listOf(colors.surfaceContainerLow, colors.surfaceContainerLow))
    }
    val panelModifier = modifier
        .then(
            if (skin.glass) {
                Modifier.shadow(
                    skin.shadowElevation.dp,
                    shape,
                    ambientColor = colors.primary.copy(alpha = 0.10f),
                    spotColor = Color.Black.copy(alpha = 0.18f),
                )
            } else {
                Modifier
            },
        )
        .clip(shape)
        .background(panelBrush)
        .then(
            if (skin.glass) {
                Modifier.border(
                    width = 0.9.dp,
                    color = colors.outlineVariant.copy(alpha = skin.borderAlpha),
                    shape = shape,
                )
            } else {
                Modifier
            },
        )
        .padding(contentPadding)
    Box(modifier = panelModifier, content = content)
}

@Composable
fun HermesSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    compact: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 11.dp else 16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            0.7.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 3.dp else 5.dp),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 4.dp),
        ) {
            items.forEachIndexed { index, label ->
                val selected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(if (compact) 8.dp else 12.dp))
                        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { onSelect(index) }
                        .padding(vertical = if (compact) 5.dp else 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
fun HermesMark(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    requestedSize: Dp? = null,
) {
    val skin = HermesSkin.current
    val size = requestedSize ?: if (compact) 32.dp else 48.dp
    val shape = RoundedCornerShape(size / 3.2f)
    Image(
        // Compose painterResource does not support LayerDrawable. Keep the in-app
        // Hermes mark on a raster resource so the setup screen can always compose.
        painter = painterResource(R.drawable.hermes_app_icon_art),
        contentDescription = "Hermes",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .shadow(if (skin.glass) (if (compact) 3.dp else 5.dp) else 0.dp, shape)
            .clip(shape)
            .border(0.8.dp, Color.White.copy(alpha = 0.75f), shape),
    )
}

@Composable
fun HermesBottomDock(
    selected: AppRoute,
    hasUnreadConversations: Boolean,
    onSelect: (AppRoute) -> Unit,
) {
    val skin = HermesSkin.current
    if (skin.glass) {
        GlassPanel(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                .padding(horizontal = HermesSpacing.sm, vertical = 7.dp),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 5.dp),
        ) {
            DockItems(selected, hasUnreadConversations, onSelect)
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            Column(Modifier.navigationBarsPadding()) {
                HorizontalDivider(
                    thickness = 0.6.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f),
                )
                Box(Modifier.padding(horizontal = HermesSpacing.xs, vertical = 4.dp)) {
                    DockItems(selected, hasUnreadConversations, onSelect)
                }
            }
        }
    }
}

@Composable
private fun DockItems(selected: AppRoute, hasUnreadConversations: Boolean, onSelect: (AppRoute) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(54.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DockItem("对话", HermesIconKind.CHAT, selected == AppRoute.SESSIONS, hasUnreadConversations, Modifier.weight(1f)) { onSelect(AppRoute.SESSIONS) }
        DockItem("空间", HermesIconKind.SPACE, selected == AppRoute.WORKSPACE, false, Modifier.weight(1f)) { onSelect(AppRoute.WORKSPACE) }
        DockItem("任务", HermesIconKind.TASK, selected == AppRoute.TASKS, false, Modifier.weight(1f)) { onSelect(AppRoute.TASKS) }
        DockItem("我的", HermesIconKind.PROFILE, selected == AppRoute.PROFILE, false, Modifier.weight(1f)) { onSelect(AppRoute.PROFILE) }
    }
}

@Composable
private fun DockItem(
    label: String,
    icon: HermesIconKind,
    selected: Boolean,
    showBadge: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .height(HermesSpacing.minTouchTarget)
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                else Color.Transparent,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Box(modifier = Modifier.size(width = 30.dp, height = 27.dp), contentAlignment = Alignment.Center) {
            HermesMulticolorIcon(
                kind = icon,
                contentDescription = label,
                iconSize = 25.dp,
                grayscale = !selected,
                modifier = Modifier.alpha(if (selected) 1f else 0.72f),
            )
            if (showBadge) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(7.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                )
            }
        }
        Text(
            label,
            color = tint,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal,
        )
    }
}
