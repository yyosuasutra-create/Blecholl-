package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.chat.ChatScreen
import com.example.ui.chat.ChatViewModel
import com.example.ui.game.GameStudioScreen
import com.example.ui.game.GameViewModel
import com.example.ui.mvp.MvpDashboardScreen
import com.example.ui.photo.PhotoEditorScreen
import com.example.ui.photo.PhotoViewModel
import com.example.ui.theme.CyanPrimary

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    object Photo : BottomNavItem("photo_editor", "Edit Foto", Icons.Default.AutoFixHigh, "nav_tab_photo")
    object Chat : BottomNavItem("ai_chat", "Tanya Jawab", Icons.Default.ChatBubble, "nav_tab_chat")
    object Game : BottomNavItem("game_studio", "Buat Game", Icons.Default.SportsEsports, "nav_tab_game")
    object Mvp : BottomNavItem("mvp_hub", "MVP Hub", Icons.Default.RocketLaunch, "nav_tab_mvp")
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()

    val photoViewModel: PhotoViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val gameViewModel: GameViewModel = viewModel()

    val navItems = listOf(
        BottomNavItem.Photo,
        BottomNavItem.Chat,
        BottomNavItem.Game,
        BottomNavItem.Mvp
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Photo.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Photo.route) {
                PhotoEditorScreen(viewModel = photoViewModel)
            }
            composable(BottomNavItem.Chat.route) {
                ChatScreen(viewModel = chatViewModel)
            }
            composable(BottomNavItem.Game.route) {
                GameStudioScreen(viewModel = gameViewModel)
            }
            composable(BottomNavItem.Mvp.route) {
                MvpDashboardScreen(
                    onNavigateToPhoto = { navController.navigate(BottomNavItem.Photo.route) },
                    onNavigateToChat = { navController.navigate(BottomNavItem.Chat.route) },
                    onNavigateToGame = { navController.navigate(BottomNavItem.Game.route) }
                )
            }
        }
    }
}
