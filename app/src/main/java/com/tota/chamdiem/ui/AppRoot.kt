package com.tota.chamdiem.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tota.chamdiem.ui.screens.AddEntryScreen
import com.tota.chamdiem.ui.screens.HistoryScreen
import com.tota.chamdiem.ui.screens.HomeScreen
import com.tota.chamdiem.ui.screens.MembersScreen
import com.tota.chamdiem.ui.screens.ReportScreen
import com.tota.chamdiem.ui.screens.SettingsScreen

private sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Dest("home", "Trang chủ", Icons.Filled.Home)
    data object Add : Dest("add", "Nhập điểm", Icons.Filled.AddCircle)
    data object Report : Dest("report", "Tổng hợp", Icons.Filled.BarChart)
    data object History : Dest("history", "Lịch sử", Icons.AutoMirrored.Filled.List)
    data object Settings : Dest("settings", "Cài đặt", Icons.Filled.Settings)
}

private val bottomDests = listOf(Dest.Home, Dest.Add, Dest.Report, Dest.History, Dest.Settings)

@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val current = backStackEntry?.destination
                bottomDests.forEach { dest ->
                    val selected = current?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(inner),
        ) {
            composable(Dest.Home.route) {
                HomeScreen(vm, onOpenAdd = { navController.navigate(Dest.Add.route) })
            }
            composable(Dest.Add.route) {
                AddEntryScreen(vm)
            }
            composable(Dest.Report.route) {
                ReportScreen(vm)
            }
            composable(Dest.History.route) {
                HistoryScreen(vm)
            }
            composable(Dest.Settings.route) {
                SettingsScreen(vm, onOpenMembers = { navController.navigate("members") })
            }
            composable("members") {
                MembersScreen(vm, onBack = { navController.popBackStack() })
            }
        }
    }
}
