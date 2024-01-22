package de.rogallab.mobile.ui.base

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.navigation.NavController

suspend fun showAndRespondToError(
   errorParams: ErrorParams,
   onErrorEventHandled: () -> Unit,
   snackbarHostState: SnackbarHostState,
   navController: NavController,
) {
   // show snackbar
   val result = snackbarHostState.showSnackbar(
      message = errorParams.message,
      actionLabel = errorParams.actionLabel,
      duration = errorParams.duration,
      withDismissAction = errorParams.withDismissAction,
   )
   // action on dismiss
   if (errorParams.withDismissAction &&
      result == SnackbarResult.ActionPerformed
   ) {
      errorParams.onDismissAction()
   }
   // if navigation is true, nevaigate to route
   if (errorParams.isNavigation) {
      errorParams.route?.let { route ->
         navController.navigate(route = route) {
            popUpTo(route = route) { inclusive = true }
         }
      }
   }

   // set ErrorEvent to handled
   onErrorEventHandled()
}
