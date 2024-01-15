package de.rogallab.mobile.ui.composables

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.launch


@Composable
fun <T> HandleStateError(
   errorMessage: String,                           // State ↓
   backHandler: Boolean,                           // State ↓
   actionLabel: String?,                           // State ↓
   onErrorAction: () -> Unit,                      // Event ↑
   snackbarHostState: SnackbarHostState,           // State ↓
   navController: NavController,                   // State ↓
   routePopBack: String,                           // State ↓
   onUiStateFlowChange: (UiState<T>) -> Unit,      // Event ↑
   tag: String,                                    // State ↓
) {

   val coroutineScope = rememberCoroutineScope()
   LaunchedEffect(errorMessage) {
      val job = coroutineScope.launch {
         showErrorMessage(
            snackbarHostState = snackbarHostState,
            errorMessage = errorMessage,
            actionLabel = actionLabel,
            onErrorAction = { onErrorAction() }
         )
      }
      coroutineScope.launch {
         job.join()
         if (backHandler) {
            logInfo(tag, "Back Navigation (Abort)")
            navController.popBackStack(
               route = routePopBack,
               inclusive = false
            )
         }
         onUiStateFlowChange( UiState.Empty )
      }
   }
}