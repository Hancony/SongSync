package pl.lambada.songsync.ui.components.dropdown
/* Credits:
 *  Adapted from Metadator by @BobbyESP: https://github.com/BobbyESP/Metadator/
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.window.PopupPositionProvider
import pl.lambada.songsync.util.ui.EmphasizedAccelerate
import pl.lambada.songsync.util.ui.EmphasizedDecelerate
import pl.lambada.songsync.util.ui.MotionConstants.DURATION_ENTER
import pl.lambada.songsync.util.ui.MotionConstants.DURATION_ENTER_SHORT
import pl.lambada.songsync.util.ui.MotionConstants.DURATION_EXIT
import pl.lambada.songsync.util.ui.MotionConstants.DURATION_EXIT_SHORT

/**
 * Interfaces for positioning a menu within a window. This is the same purpose as the interface
 * [PopupPositionProvider], except [Vertical] and [Horizontal] separate out the positioning logic
 * for each direction individually.
 */
@Stable
internal object MenuPosition {
    /**
     * An interface to calculate the vertical position of a menu with respect to its anchor and
     * window. The returned y-coordinate is relative to the window.
     *
     * @see PopupPositionProvider
     */
    @Stable
    fun interface Vertical {
        fun position(
            anchorBounds: IntRect,
            windowSize: IntSize,
            menuHeight: Int,
        ): Int
    }

    /**
     * An interface to calculate the horizontal position of a menu with respect to its anchor,
     * window, and layout direction. The returned x-coordinate is relative to the window.
     *
     * @see PopupPositionProvider
     */
    @Stable
    fun interface Horizontal {
        fun position(
            anchorBounds: IntRect,
            windowSize: IntSize,
            menuWidth: Int,
            layoutDirection: LayoutDirection,
        ): Int
    }

    /**
     * Returns a [MenuPosition.Horizontal] which aligns the start of the menu to the start of the
     * anchor.
     *
     * The given [offset] is [LayoutDirection]-aware. It will be added to the resulting x position
     * for [LayoutDirection.Ltr] and subtracted for [LayoutDirection.Rtl].
     */
    fun startToAnchorStart(offset: Int = 0): Horizontal = AnchorAlignmentOffsetPosition.Horizontal(
        menuAlignment = Alignment.Start,
        anchorAlignment = Alignment.Start,
        offset = offset,
    )

    /**
     * Returns a [MenuPosition.Horizontal] which aligns the end of the menu to the end of the
     * anchor.
     *
     * The given [offset] is [LayoutDirection]-aware. It will be added to the resulting x position
     * for [LayoutDirection.Ltr] and subtracted for [LayoutDirection.Rtl].
     */
    fun endToAnchorEnd(offset: Int = 0): Horizontal = AnchorAlignmentOffsetPosition.Horizontal(
        menuAlignment = Alignment.End,
        anchorAlignment = Alignment.End,
        offset = offset,
    )

    /**
     * Returns a [MenuPosition.Horizontal] which aligns the left of the menu to the left of the
     * window.
     *
     * The resulting x position will be coerced so that the menu remains within the area inside the
     * given [margin] from the left and right edges of the window.
     */
    fun leftToWindowLeft(margin: Int = 0): Horizontal = WindowAlignmentMarginPosition.Horizontal(
        alignment = AbsoluteAlignment.Left,
        margin = margin,
    )

    /**
     * Returns a [MenuPosition.Horizontal] which aligns the right of the menu to the right of the
     * window.
     *
     * The resulting x position will be coerced so that the menu remains within the area inside the
     * given [margin] from the left and right edges of the window.
     */
    fun rightToWindowRight(margin: Int = 0): Horizontal = WindowAlignmentMarginPosition.Horizontal(
        alignment = AbsoluteAlignment.Right,
        margin = margin,
    )

    /**
     * Returns a [MenuPosition.Vertical] which aligns the top of the menu to the bottom of the
     * anchor.
     */
    fun topToAnchorBottom(offset: Int = 0): Vertical = AnchorAlignmentOffsetPosition.Vertical(
        menuAlignment = Alignment.Top,
        anchorAlignment = Alignment.Bottom,
        offset = offset,
    )

    /**
     * Returns a [MenuPosition.Vertical] which aligns the bottom of the menu to the top of the
     * anchor.
     */
    fun bottomToAnchorTop(offset: Int = 0): Vertical = AnchorAlignmentOffsetPosition.Vertical(
        menuAlignment = Alignment.Bottom,
        anchorAlignment = Alignment.Top,
        offset = offset,
    )

    /**
     * Returns a [MenuPosition.Vertical] which aligns the center of the menu to the top of the
     * anchor.
     */
    fun centerToAnchorTop(offset: Int = 0): Vertical = AnchorAlignmentOffsetPosition.Vertical(
        menuAlignment = Alignment.CenterVertically,
        anchorAlignment = Alignment.Top,
        offset = offset,
    )

    /**
     * Returns a [MenuPosition.Vertical] which aligns the top of the menu to the top of the
     * window.
     *
     * The resulting y position will be coerced so that the menu remains within the area inside the
     * given [margin] from the top and bottom edges of the window.
     */
    fun topToWindowTop(margin: Int = 0): Vertical = WindowAlignmentMarginPosition.Vertical(
        alignment = Alignment.Top,
        margin = margin,
    )

    /**
     * Returns a [MenuPosition.Vertical] which aligns the bottom of the menu to the bottom of the
     * window.
     *
     * The resulting y position will be coerced so that the menu remains within the area inside the
     * given [margin] from the top and bottom edges of the window.
     */
    fun bottomToWindowBottom(margin: Int = 0): Vertical = WindowAlignmentMarginPosition.Vertical(
        alignment = Alignment.Bottom,
        margin = margin,
    )
}

@Immutable
internal object AnchorAlignmentOffsetPosition {
    /**
     * A [MenuPosition.Horizontal] which horizontally aligns the given [menuAlignment] with the
     * given [anchorAlignment].
     *
     * The given [offset] is [LayoutDirection]-aware. It will be added to the resulting x position
     * for [LayoutDirection.Ltr] and subtracted for [LayoutDirection.Rtl].
     */
    @Immutable
    data class Horizontal(
        private val menuAlignment: Alignment.Horizontal,
        private val anchorAlignment: Alignment.Horizontal,
        private val offset: Int,
    ) : MenuPosition.Horizontal {
        override fun position(
            anchorBounds: IntRect,
            windowSize: IntSize,
            menuWidth: Int,
            layoutDirection: LayoutDirection,
        ): Int {
            val anchorAlignmentOffset = anchorAlignment.align(
                size = 0,
                space = anchorBounds.width,
                layoutDirection = layoutDirection,
            )
            val menuAlignmentOffset = -menuAlignment.align(
                size = 0,
                space = menuWidth,
                layoutDirection,
            )
            val resolvedOffset = if (layoutDirection == LayoutDirection.Ltr) offset else -offset
            return anchorBounds.left + anchorAlignmentOffset + menuAlignmentOffset + resolvedOffset
        }
    }

    /**
     * A [MenuPosition.Vertical] which vertically aligns the given [menuAlignment] with the given
     * [anchorAlignment].
     */
    @Immutable
    data class Vertical(
        private val menuAlignment: Alignment.Vertical,
        private val anchorAlignment: Alignment.Vertical,
        private val offset: Int,
    ) : MenuPosition.Vertical {
        override fun position(
            anchorBounds: IntRect,
            windowSize: IntSize,
            menuHeight: Int,
        ): Int {
            val anchorAlignmentOffset = anchorAlignment.align(
                size = 0,
                space = anchorBounds.height,
            )
            val menuAlignmentOffset = -menuAlignment.align(
                size = 0,
                space = menuHeight,
            )
            return anchorBounds.top + anchorAlignmentOffset + menuAlignmentOffset + offset
        }
    }
}

@Immutable
internal object WindowAlignmentMarginPosition {
    /**
     * A [MenuPosition.Horizontal] which horizontally aligns the menu within the window according
     * to the given [alignment].
     *
     * The resulting x position will be coerced so that the menu remains within the area inside the
     * given [margin] from the left and right edges of the window. If this is not possible, i.e.,
     * the menu is too wide, then it is centered horizontally instead.
     */
    @Immutable
    data class Horizontal(
        private val alignment: Alignment.Horizontal,
        private val margin: Int,
    ) : MenuPosition.Horizontal {
        override fun position(
            anchorBounds: IntRect,
            windowSize: IntSize,
            menuWidth: Int,
            layoutDirection: LayoutDirection,
        ): Int {
            if (menuWidth >= windowSize.width - 2 * margin) {
                return Alignment.CenterHorizontally.align(
                    size = menuWidth,
                    space = windowSize.width,
                    layoutDirection = layoutDirection,
                )
            }
            val x = alignment.align(
                size = menuWidth,
                space = windowSize.width,
                layoutDirection = layoutDirection,
            )
            return x.coerceIn(margin, windowSize.width - margin - menuWidth)
        }
    }

    /**
     * A [MenuPosition.Vertical] which vertically aligns the menu within the window according to
     * the given [alignment].
     *
     * The resulting y position will be coerced so that the menu remains within the area inside the
     * given [margin] from the top and bottom edges of the window. If this is not possible, i.e.,
     * the menu is too tall, then it is centered vertically instead.
     */
    @Immutable
    data class Vertical(
        private val alignment: Alignment.Vertical,
        private val margin: Int,
    ) : MenuPosition.Vertical {
        override fun position(
            anchorBounds: IntRect,
            windowSize: IntSize,
            menuHeight: Int,
        ): Int {
            if (menuHeight >= windowSize.height - 2 * margin) {
                return Alignment.CenterVertically.align(
                    size = menuHeight,
                    space = windowSize.height,
                )
            }
            val y = alignment.align(
                size = menuHeight,
                space = windowSize.height,
            )
            return y.coerceIn(margin, windowSize.height - margin - menuHeight)
        }
    }
}

/**
 * Calculates the position of a Material [DropdownMenu].
 */
@Immutable
internal data class DropdownMenuPositionProvider(
    val contentOffset: DpOffset,
    val density: Density,
    val verticalMargin: Int = with(density) { MenuVerticalMargin.roundToPx() },
    val onPositionCalculated: (anchorBounds: IntRect, menuBounds: IntRect) -> Unit = { _, _ -> }
) : PopupPositionProvider {
    // Horizontal position
    private val startToAnchorStart: MenuPosition.Horizontal
    private val endToAnchorEnd: MenuPosition.Horizontal
    private val leftToWindowLeft: MenuPosition.Horizontal
    private val rightToWindowRight: MenuPosition.Horizontal

    // Vertical position
    private val topToAnchorBottom: MenuPosition.Vertical
    private val bottomToAnchorTop: MenuPosition.Vertical
    private val centerToAnchorTop: MenuPosition.Vertical
    private val topToWindowTop: MenuPosition.Vertical
    private val bottomToWindowBottom: MenuPosition.Vertical

    init {
        // Horizontal position
        val contentOffsetX = with(density) { contentOffset.x.roundToPx() }
        startToAnchorStart = MenuPosition.startToAnchorStart(offset = contentOffsetX)
        endToAnchorEnd = MenuPosition.endToAnchorEnd(offset = contentOffsetX)
        leftToWindowLeft = MenuPosition.leftToWindowLeft(margin = 0)
        rightToWindowRight = MenuPosition.rightToWindowRight(margin = 0)
        // Vertical position
        val contentOffsetY = with(density) { contentOffset.y.roundToPx() }
        topToAnchorBottom = MenuPosition.topToAnchorBottom(offset = contentOffsetY)
        bottomToAnchorTop = MenuPosition.bottomToAnchorTop(offset = contentOffsetY)
        centerToAnchorTop = MenuPosition.centerToAnchorTop(offset = contentOffsetY)
        topToWindowTop = MenuPosition.topToWindowTop(margin = verticalMargin)
        bottomToWindowBottom = MenuPosition.bottomToWindowBottom(margin = verticalMargin)
    }

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val xCandidates = listOf(
            startToAnchorStart, endToAnchorEnd, if (anchorBounds.center.x < windowSize.width / 2) {
                leftToWindowLeft
            } else {
                rightToWindowRight
            }
        ).fastMap {
            it.position(
                anchorBounds = anchorBounds,
                windowSize = windowSize,
                menuWidth = popupContentSize.width,
                layoutDirection = layoutDirection
            )
        }
        val x = xCandidates.fastFirstOrNull {
            it >= 0 && it + popupContentSize.width <= windowSize.width
        } ?: xCandidates.last()

        val yCandidates = listOf(
            topToAnchorBottom,
            bottomToAnchorTop,
            centerToAnchorTop,
            if (anchorBounds.center.y < windowSize.height / 2) {
                topToWindowTop
            } else {
                bottomToWindowBottom
            }
        ).fastMap {
            it.position(
                anchorBounds = anchorBounds,
                windowSize = windowSize,
                menuHeight = popupContentSize.height
            )
        }
        val y = yCandidates.fastFirstOrNull {
            it >= verticalMargin && it + popupContentSize.height <= windowSize.height - verticalMargin
        } ?: yCandidates.last()

        val menuOffset = IntOffset(x, y)
        onPositionCalculated(/* anchorBounds = */anchorBounds,/* menuBounds = */
            IntRect(offset = menuOffset, size = popupContentSize)
        )
        return menuOffset
    }
}

@Composable
fun DropdownMenuContent(
    expandedState: MutableTransitionState<Boolean>,
    scrollState: ScrollState,
    transformOrigin: TransformOrigin,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visibleState = expandedState,
        label = "Dropdown menu animation",
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = DURATION_ENTER, easing = EmphasizedDecelerate
            ),
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = DURATION_ENTER, easing = EmphasizedDecelerate
            ),
            initialScale = 0.8f,
            transformOrigin = if (LocalLayoutDirection.current == LayoutDirection.Ltr)
                TransformOrigin(1f, 0f)
            else
                TransformOrigin(0f, 0f),
            // TODO: use transformOrigin if necessary
            //       it has a little issue currently on enter so fixed value has been used
        ),
        exit = fadeOut(
            animationSpec = tween(
                delayMillis = 10,
                durationMillis = DURATION_EXIT_SHORT,
                easing = LinearEasing,
            ),
        ) + scaleOut(
            animationSpec = tween(
                durationMillis = DURATION_EXIT,
                easing = LinearEasing,
            ),
            targetScale = 0.8f,
            transformOrigin = transformOrigin,
        ),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = ElevationTokens.Level2.dp,
            shadowElevation = ElevationTokens.Level1.dp,
        ) {
            AnimatedVisibility(
                visibleState = expandedState,
                label = "",
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = DURATION_ENTER, easing = EmphasizedDecelerate
                    ),
                    expandFrom = Alignment.Top
                ) + slideInVertically(
                    animationSpec = tween(
                        durationMillis = DURATION_ENTER_SHORT, easing = EmphasizedDecelerate
                    ),
                    initialOffsetY = { -it / 10 },
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = DURATION_EXIT_SHORT, easing = EmphasizedAccelerate
                    ),
                    targetHeight = { it / 3 },
                    shrinkTowards = Alignment.Top,
                ),
                modifier = Modifier
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = DropdownMenuVerticalPadding)
                        .width(IntrinsicSize.Max)
                        .verticalScroll(scrollState),
                    content = content
                )
            }
        }
    }
}


// Size defaults.
internal val MenuVerticalMargin = 48.dp
internal val DropdownMenuVerticalPadding = 8.dp
