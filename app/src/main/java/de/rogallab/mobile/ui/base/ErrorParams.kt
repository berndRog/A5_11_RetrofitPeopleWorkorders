package de.rogallab.mobile.ui.base

import androidx.compose.material3.SnackbarDuration

data class ErrorParams(
   // Snackbar Parameter
   val message: String = "",
   val actionLabel: String? = null,
   val duration: SnackbarDuration = SnackbarDuration.Short,
   val withDismissAction: Boolean = false,
   val onDismissAction: () -> Unit = {}, // default action: do nothing

   // Navigation Parameter (true : up navigation, false: back navigation)
   val isNavigation: Boolean = true,
   // navigation to route, or if null navigateUp
   val route: String? = null
)
