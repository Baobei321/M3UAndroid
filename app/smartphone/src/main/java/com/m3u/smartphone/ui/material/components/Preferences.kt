package com.m3u.smartphone.ui.material.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.m3u.smartphone.ui.material.model.LocalSpacing
import com.m3u.core.foundation.components.AbsoluteSmoothCornerShape

@Composable
fun Preference(
    title: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: String? = null,
    elevation: Dp = 0.dp,
    onClick: () -> Unit = {},
    icon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val spacing = LocalSpacing.current
    val interactionSource = remember { MutableInteractionSource() }
    val focus by interactionSource.collectIsFocusedAsState()

    TooltipBox(
        state = rememberTooltipState(),
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            if (!content.isNullOrEmpty()) {
                PlainTooltip {
                    Text(
                        text = content.capitalize(Locale.current)
                    )
                }
            }
        }
    ) {
        val alpha = if (enabled) 1f else 0.38f
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(Color.Transparent),
            shape = AbsoluteSmoothCornerShape(spacing.medium, 65)
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                supportingContent = {
                    if (content != null) {
                        Text(
                            text = content.capitalize(Locale.current),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier then if (focus) Modifier.basicMarquee()
                            else Modifier
                        )
                    }
                },
                trailingContent = trailing,
                leadingContent = icon?.let {
                    @Composable {
                        Icon(imageVector = it, contentDescription = null)
                    }
                },
                tonalElevation = LocalAbsoluteTonalElevation.current,
                shadowElevation = elevation,
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    overlineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha),
                    supportingColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha),
                    headlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha)
                ),
                modifier = modifier
                    .semantics(mergeDescendants = true) {}
                    .clickable(
                        enabled = enabled,
                        onClick = onClick,
                        interactionSource = interactionSource,
                        indication = ripple()
                    )
                    .fillMaxWidth()
            )
        }
    }
}


@Composable
fun CheckBoxPreference(
    title: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: String? = null,
    elevation: Dp = 0.dp,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Preference(
        title = title,
        content = content,
        enabled = enabled,
        elevation = elevation,
        onClick = {
            if (enabled) {
                onChanged(!checked)
            }
        },
        modifier = modifier,
        trailing = {
            Checkbox(
                enabled = enabled,
                checked = checked,
                onCheckedChange = null
            )
        },
        icon = icon
    )
}

@Composable
fun SwitchPreference(
    title: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: String? = null,
    elevation: Dp = 0.dp,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Preference(
        title = title,
        content = content,
        elevation = elevation,
        enabled = enabled,
        onClick = {
            if (enabled) {
                onChanged(!checked)
            }
        },
        modifier = modifier,
        trailing = {
            Switch(
                enabled = enabled,
                checked = checked,
                onCheckedChange = null
            )
        },
        icon = icon
    )
}

@Composable
fun TrailingIconPreference(
    title: String,
    trailingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: String? = null,
    elevation: Dp = 0.dp,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Preference(
        title = title,
        content = content,
        enabled = enabled,
        onClick = onClick,
        elevation = elevation,
        modifier = modifier,
        trailing = {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.65f)
            )
        },
        icon = icon
    )
}

@Composable
fun TextPreference(
    title: String,
    trailing: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: String? = null,
    elevation: Dp = 0.dp,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Preference(
        title = title,
        content = content,
        enabled = enabled,
        elevation = elevation,
        onClick = {
            if (enabled) onClick()
        },
        modifier = modifier,
        trailing = {
            Text(
                text = trailing.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = icon
    )
}
