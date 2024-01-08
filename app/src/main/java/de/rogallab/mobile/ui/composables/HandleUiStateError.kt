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
fun <T> HandleUiStateError(
   uiStateFlow: UiState<T>,                        // State ↓
   actionLabel: String?,                           // State ↓
   onErrorAction: () -> Unit,                      // Event ↑
   snackbarHostState: SnackbarHostState,           // State ↓
   navController: NavController,                   // State ↓
   routePopBack: String,                           // State ↓
   onUiStateFlowChange: (UiState<T>) -> Unit,      // Event ↑
   tag: String,                                    // State ↓
) {

   val coroutineScope = rememberCoroutineScope()
   LaunchedEffect(uiStateFlow is UiState.Error) {
      val message = (uiStateFlow as UiState.Error).message
      val backHandler = uiStateFlow.backHandler
      val job = coroutineScope.launch {
         showErrorMessage(
            snackbarHostState = snackbarHostState,
            errorMessage = message,
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