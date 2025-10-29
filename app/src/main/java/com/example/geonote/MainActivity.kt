package com.example.geonote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.geonote.ui.*
import com.example.geonote.ui.theme.GeoNoteTheme
import com.example.geonote.viewmodel.NoteVMFactory
import com.example.geonote.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {

    private val vm: NoteViewModel by viewModels { NoteVMFactory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(vm)
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Notes : Screen("notes_tab", "Notas", Icons.Default.EditNote)
    object Map : Screen("map_tab", "Mapa", Icons.Default.Map)
}

@Composable
fun MainScreen(viewModel: NoteViewModel) {
    val navController = rememberNavController()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(Screen.Notes, Screen.Map)

    // Determinar si mostrar bottom nav (ocultar en pantallas secundarias)
    val showBottomNav = remember(currentDestination?.route) {
        currentDestination?.route in listOf("notes_list", "notes_tab", "map_tab")
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Notes.route,
            modifier = Modifier.padding(padding)
        ) {
            // Tab de Notas (con sub-navegación)
            navigation(
                startDestination = "notes_list",
                route = Screen.Notes.route
            ) {
                composable(
                    route = "notes_list",
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() }
                ) {
                    NoteListScreen(
                        notes = notes,
                        onNoteClick = { noteId ->
                            navController.navigate("note_detail/$noteId")
                        },
                        onAdd = {
                            navController.navigate("note_create")
                        },
                        onArchive = { viewModel.archive(it) }
                    )
                }

                composable(
                    route = "note_create",
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) }
                ) {
                    NoteCreateScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "note_detail/{noteId}",
                    arguments = listOf(navArgument("noteId") { type = NavType.LongType }),
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                    NoteDetailScreen(
                        noteId = noteId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // Tab de Mapa
            composable(
                route = Screen.Map.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                MapScreen(notes = notes)
            }
        }
    }
}