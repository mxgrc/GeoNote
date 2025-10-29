package com.example.geonote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geonote.ui.NoteCreateScreen
import com.example.geonote.ui.NoteListScreen
import com.example.geonote.viewmodel.NoteVMFactory
import com.example.geonote.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {

    private val vm: NoteViewModel by viewModels { NoteVMFactory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val nav = rememberNavController()
                    val notes by vm.notes.collectAsStateWithLifecycle()

                    NavHost(navController = nav, startDestination = "list") {
                        
                        composable(
                            route = "list",
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) }
                        ) {
                            NoteListScreen(
                                notes = notes,
                                onAdd = { nav.navigate("create") },
                                onArchive = { vm.archive(it) }
                            )
                        }

                        composable(
                            route = "create",
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) {
                            NoteCreateScreen(
                                viewModel = vm,
                                onBack = { nav.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
