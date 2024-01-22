package de.rogallab.mobile.ui.base

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun<T> showUndo(
   coroutineScope: CoroutineScope,
   snackbarHostState: SnackbarHostState,
   message: String,
   t: T,
   onUndoAction: (T) -> Unit
) = coroutineScope.launch {
   val snackbarResult = snackbarHostState.showSnackbar(
      message = message,
      actionLabel = "nein",
      withDismissAction = false,
      duration = SnackbarDuration.Short
   )
   if (snackbarResult == SnackbarResult.ActionPerformed) {
      onUndoAction(t)
   }
}
