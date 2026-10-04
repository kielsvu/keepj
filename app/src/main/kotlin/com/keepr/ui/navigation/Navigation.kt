package com.keepr.ui.navigation

sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object Lock : Screen("lock")
    object Vault : Screen("vault")
    object Detail : Screen("detail/{entryId}") {
        fun createRoute(entryId: String) = "detail/$entryId"
    }
    object AddEntry : Screen("add_entry")
    object EditEntry : Screen("edit_entry/{entryId}") {
        fun createRoute(entryId: String) = "edit_entry/$entryId"
    }
    object Generator : Screen("generator")
    object Settings : Screen("settings")
    object ChangePin : Screen("change_pin")
}
