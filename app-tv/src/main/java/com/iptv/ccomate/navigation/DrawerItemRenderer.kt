package com.iptv.ccomate.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.iptv.ccomate.ui.screens.DrawerIconContent
import com.iptv.ccomate.model.DrawerItem
import com.iptv.ccomate.ui.theme.AppColors
import com.iptv.ccomate.ui.theme.AppDimensions
import com.iptv.ccomate.ui.theme.AppTypography

@Composable
fun NavigationDrawerScope.DrawerItemRenderer(
    items: List<DrawerItem>,
    selectedIndex: Int,
    bottomItemsCount: Int = 1,
    onItemClick: (Int, String) -> Unit
) {
    val topItems = items.dropLast(bottomItemsCount)
    val bottomItems = items.takeLast(bottomItemsCount)

    @Composable
    fun renderItem(index: Int, item: DrawerItem) {
        val isSelected = selectedIndex == index
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = AppDimensions.drawerItemSpacing / 2)
        ) {
            Box(
                Modifier
                    .width(AppDimensions.drawerSelectionIndicatorWidth)
                    .height(AppDimensions.drawerSelectionIndicatorHeight)
                    .background(
                        color = if (isSelected) AppColors.drawerSelectionIndicator else Color.Transparent,
                        shape = RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                    )
            )
            Spacer(Modifier.width(AppDimensions.spacing_sm))
            NavigationDrawerItem(
                modifier = Modifier.background(Color.Transparent),
                selected = isSelected,
                onClick = { onItemClick(index, item.route) },
                leadingContent = {
                    DrawerIconContent(
                        icon = item.icon,
                        label = item.label,
                        tint = if (isSelected) AppColors.selected else AppColors.textSecondary
                    )
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color.Transparent,
                    selectedContentColor = AppColors.selected,
                    focusedContainerColor = AppColors.drawerItemFocused,
                    focusedContentColor = AppColors.selected,
                    focusedSelectedContainerColor = AppColors.drawerItemFocused,
                    focusedSelectedContentColor = AppColors.selected
                )
            ) {
                Text(
                    text = item.label,
                    style = if (isSelected) AppTypography.drawerLabelSelected else AppTypography.drawerLabel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    Column(Modifier.fillMaxHeight()) {
        topItems.forEachIndexed { index, item -> renderItem(index, item) }
        Spacer(Modifier.weight(1f))
        bottomItems.forEachIndexed { i, item ->
            val realIndex = topItems.size + i
            renderItem(realIndex, item)
        }
    }
}
