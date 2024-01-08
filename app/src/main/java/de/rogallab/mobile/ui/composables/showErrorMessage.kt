package de.rogallab.mobile.ui.composables

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import de.rogallab.mobile.domain.utilities.logDebug

suspend fun showErrorMessage(
   snackbarHostState: SnackbarHostState, // State ↓
   errorMessage: String,                 // State ↓
   actionLabel: String?,                 // State ↓
   onErrorAction: () -> Unit             // Event ↑
) {

   val tag = "ok>ShowErrorMessage   ."
   logDebug(tag, "Start")

   val snackbarResult = snackbarHostState.showSnackbar(
      message = errorMessage,
      actionLabel = actionLabel,
      withDismissAction = false,
      duration = SnackbarDuration.Short
   )
   if (snackbarResult == SnackbarResult.ActionPerformed) {
      logDebug(tag, "SnackbarResult.ActionPerformed")
      onErrorAction()
   }
}