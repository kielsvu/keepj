package com.keepr.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keepr.KeeprApplication
import com.keepr.ui.navigation.Screen
import com.keepr.ui.screens.detail.DetailScreen
import com.keepr.ui.screens.detail.DetailViewModel
import com.keepr.ui.screens.form.AccountFormScreen
import com.keepr.ui.screens.form.AccountFormViewModel
import com.keepr.ui.screens.generator.GeneratorScreen
import com.keepr.ui.screens.generator.GeneratorViewModel
import com.keepr.ui.screens.lock.LockScreen
import com.keepr.ui.screens.lock.LockViewModel
import com.keepr.ui.screens.settings.ChangePinScreen
import com.keepr.ui.screens.settings.ChangePinViewModel
import com.keepr.ui.screens.settings.SettingsScreen
import com.keepr.ui.screens.settings.SettingsViewModel
import com.keepr.ui.screens.setup.SetupScreen
import com.keepr.ui.screens.setup.SetupViewModel
import com.keepr.ui.screens.vault.VaultScreen
import com.keepr.ui.screens.vault.VaultViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun KeeprNavHost(app: KeeprApplication) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val isLocked by app.autoLockManager.isLocked.collectAsState()

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val pinSet = app.pinManager.isPinSet.first()
        startDestination = if (pinSet) Screen.Lock.route else Screen.Setup.route
    }

    LaunchedEffect(isLocked) {
        if (isLocked) {
            scope.launch {
                val pinSet = app.pinManager.isPinSet.first()
                if (pinSet) {
                    val current = navController.currentDestination?.route
                    if (current != Screen.Lock.route && current != Screen.Setup.route) {
                        navController.navigate(Screen.Lock.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    val start = startDestination ?: return

    NavHost(
        navController = navController,
        startDestination = start,
        enterTransition = { slideInHorizontally { it } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it / 2 } + fadeOut() },
        popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
        popExitTransition = { slideOutHorizontally { it } + fadeOut() }
    ) {
        composable(Screen.Setup.route) {
            val vm: SetupViewModel = viewModel(factory = KeeprViewModelFactory(app))
            SetupScreen(
                viewModel = vm,
                onSetupComplete = {
                    app.autoLockManager.unlock()
                    navController.navigate(Screen.Vault.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Lock.route) {
            val vm: LockViewModel = viewModel(factory = KeeprViewModelFactory(app))
            LockScreen(
                viewModel = vm,
                onUnlocked = {
                    navController.navigate(Screen.Vault.route) {
                        popUpTo(Screen.Lock.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Vault.route) {
            val vm: VaultViewModel = viewModel(factory = KeeprViewModelFactory(app))
            VaultScreen(
                viewModel = vm,
                onEntryClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                onAddEntry = { navController.navigate(Screen.AddEntry.route) },
                onSettings = { navController.navigate(Screen.Settings.route) },
                onLock = {
                    app.autoLockManager.lock()
                    navController.navigate(Screen.Lock.route) {
                        popUpTo(Screen.Vault.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStack ->
            val entryId = backStack.arguments?.getString("entryId") ?: return@composable
            val vm: DetailViewModel = viewModel(factory = KeeprViewModelFactory(app, entryId))
            DetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.EditEntry.createRoute(id)) },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(Screen.AddEntry.route) {
            val vm: AccountFormViewModel = viewModel(factory = KeeprViewModelFactory(app))
            AccountFormScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onOpenGenerator = { navController.navigate(Screen.Generator.route) }
            )
        }

        composable(
            route = Screen.EditEntry.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStack ->
            val entryId = backStack.arguments?.getString("entryId") ?: return@composable
            val vm: AccountFormViewModel = viewModel(factory = KeeprViewModelFactory(app, entryId))
            AccountFormScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onOpenGenerator = { navController.navigate(Screen.Generator.route) }
            )
        }

        composable(Screen.Generator.route) {
            val vm: GeneratorViewModel = viewModel(factory = KeeprViewModelFactory(app))
            GeneratorScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = viewModel(factory = KeeprViewModelFactory(app))
            SettingsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onChangePin = { navController.navigate(Screen.ChangePin.route) }
            )
        }

        composable(Screen.ChangePin.route) {
            val vm: ChangePinViewModel = viewModel(factory = KeeprViewModelFactory(app))
            ChangePinScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }
    }
}
