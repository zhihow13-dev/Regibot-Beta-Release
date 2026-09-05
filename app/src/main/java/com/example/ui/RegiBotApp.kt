package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TestBenchScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.viewmodel.RegiBotViewModel

sealed class Screen(val title: String, val icon: ImageVector, val tag: String) {
    object Dashboard : Screen("Dashboard", Icons.Default.Dashboard, "nav_dashboard")
    object TestBench : Screen("Test Bench", Icons.Default.SportsEsports, "nav_testbench")
    object Settings : Screen("Settings", Icons.Default.Tune, "nav_settings")
    object Logs : Screen("Logs", Icons.Default.Terminal, "nav_logs")
}

@Composable
fun RegiBotApp(viewModel: RegiBotViewModel) {
    var selectedScreenIndex by rememberSaveable { mutableIntStateOf(0) }
    val screens = listOf(
        Screen.Dashboard,
        Screen.TestBench,
        Screen.Settings,
        Screen.Logs
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianCard,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                screens.forEachIndexed { index, screen ->
                    val isSelected = selectedScreenIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedScreenIndex = index },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanNeon,
                            selectedTextColor = CyanNeon,
                            indicatorColor = ObsidianBorder,
                            unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF8B949E),
                            unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF8B949E)
                        ),
                        modifier = Modifier.testTag(screen.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianDeep)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedScreenIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTestBench = { selectedScreenIndex = 1 }
                    )
                    1 -> TestBenchScreen(viewModel = viewModel)
                    2 -> SettingsScreen(viewModel = viewModel)
                    3 -> LogsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
