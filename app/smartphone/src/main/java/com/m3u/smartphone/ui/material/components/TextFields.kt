@file:Suppress("unused")

package com.m3u.smartphone.ui.material.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m3u.smartphone.ui.material.ktx.InteractionType
import com.m3u.smartphone.ui.material.ktx.interactionBorder

@Composable
fun TextField(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = TextFieldDefaults.containerColor(),
    contentColor: Color = TextFieldDefaults.contentColor(),
    shape: Shape = TextFieldDefaults.shape(),
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    imeAction: ImeAction? = null,
    enabled: Boolean = true,
    keyboardActions: KeyboardActions? = null,
    fontSize: TextUnit = TextFieldDefaults.TextFontSize,
    fontWeight: FontWeight? = null,
    isError: Boolean = false,
    onValueChange: (String) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val focus by interactionSource.collectIsFocusedAsState()

    BackHandler(focus) {
        focusManager.clearFocus()
    }

    val theme = MaterialTheme.colorScheme
    CompositionLocalProvider(
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor = theme.primary,
            backgroundColor = theme.primary.copy(alpha = 0.45f)
        )
    ) {
        BasicTextField(
            value = text,
            singleLine = singleLine,
            enabled = enabled,
            textStyle = TextStyle(
                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                fontSize = fontSize,
                color = contentColor,
                fontWeight = fontWeight
            ),
            onValueChange = {
                onValueChange(it)
            },
            keyboardActions = keyboardActions ?: KeyboardActions(
                onDone = { focusManager.clearFocus() },
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onSearch = { focusManager.clearFocus() }
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                autoCorrectEnabled = false,
                imeAction = imeAction ?: if (singleLine) ImeAction.Done else ImeAction.Default
            ),
            interactionSource = interactionSource,
            modifier = modifier.fillMaxWidth(),
            readOnly = readOnly,
            cursorBrush = SolidColor(contentColor),
            decorationBox = { innerTextField ->
                Box(
                    Modifier
                        .clip(shape)
                        .background(
                            if (isError) MaterialTheme.colorScheme.error
                            else backgroundColor
                        )
                        .interactionBorder(
                            type = InteractionType.PRESS,
                            source = interactionSource,
                            shape = shape
                        )
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                        .padding(
                            top = if (singleLine) 0.dp else 12.5.dp,
                            bottom = if (singleLine) 2.5.dp else 12.5.dp,
                            start = 12.dp,
                            end = 12.dp
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    innerTextField()

                    if (text.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = contentColor.copy(.35f),
                            fontSize = fontSize,
                            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun PlaceholderField(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = TextFieldDefaults.containerColor(),
    contentColor: Color = TextFieldDefaults.contentColor(),
    shape: Shape = TextFieldDefaults.shape(),
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    fontWeight: FontWeight? = null,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions? = null,
    icon: ImageVector? = null,
    onValueChange: (String) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val focus by interactionSource.collectIsFocusedAsState()

    BackHandler(focus) {
        focusManager.clearFocus()
    }

    val fontSize = TextFieldDefaults.MinimizeLabelFontSize

    val theme = MaterialTheme.colorScheme
    CompositionLocalProvider(
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor = theme.primary,
            backgroundColor = theme.primary.copy(alpha = 0.45f)
        )
    ) {
        BasicTextField(
            value = text,
            singleLine = singleLine,
            enabled = enabled,
            textStyle = TextStyle(
                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                fontSize = fontSize,
                color = contentColor,
                fontWeight = fontWeight
            ),
            onValueChange = {
                onValueChange(it)
            },
            keyboardActions = keyboardActions ?: KeyboardActions(
                onDone = { focusManager.clearFocus() },
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onSearch = { focusManager.clearFocus() }
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                autoCorrectEnabled = false,
                imeAction = imeAction
            ),
            interactionSource = interactionSource,
            modifier = modifier.fillMaxWidth(),
            readOnly = readOnly,
            cursorBrush = SolidColor(contentColor.copy(.35f)),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .clip(shape)
                        .background(backgroundColor)
                        .interactionBorder(
                            type = InteractionType.PRESS,
                            source = interactionSource,
                            shape = shape
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    icon?.let { icon ->
                        Icon(
                            modifier = Modifier
                                .size(56.dp)
                                .padding(15.dp),
                            imageVector = icon,
                            contentDescription = null,
                            tint = contentColor
                        )
                    }

                    Box(
                        Modifier
                            .interactionBorder(
                                type = InteractionType.PRESS,
                                source = interactionSource,
                                shape = shape
                            )
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp)
                            .padding(
                                start = if (icon == null) 15.dp else 0.dp,
                                end = 15.dp
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val hasText = text.isNotEmpty()

                        val animPlaceholder: Dp by animateDpAsState(
                            if (focus || hasText) (-10).dp else 0.dp,
                            label = "placeholder-translation-y"
                        )
                        val animPlaceHolderFontSize: Float by animateFloatAsState(
                            targetValue = if (focus || hasText) 12f else 14f,
                            label = "placeholder-font-size"
                        )

                        Text(
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = animPlaceholder.toPx()
                                },
                            text = placeholder,
                            color = contentColor.copy(alpha = .35f),
                            fontSize = animPlaceHolderFontSize.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold
                        )

                        Box(
                            Modifier
                                .padding(top = -animPlaceholder)
                                .fillMaxWidth()
                                .heightIn(18.dp),
                        ) {
                            innerTextField()
                        }
                    }
                }
            }
        )
    }
}

private object TextFieldDefaults {
    val TextFontSize = 16.sp
    val LabelFontSize = 18.sp
    val MinimizeLabelFontSize = 14.sp

    @Composable
    fun containerColor() = MaterialTheme.colorScheme.surfaceVariant

    @Composable
    fun contentColor() = MaterialTheme.colorScheme.onSurface

    @Composable
    fun shape() = AbsoluteRoundedCornerShape(16.dp)
}
