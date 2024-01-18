package de.rogallab.mobile.ui.workorders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.composables.EventEffect
import de.rogallab.mobile.ui.composables.SetCardElevation
import de.rogallab.mobile.ui.composables.SetSwipeBackgroud
import de.rogallab.mobile.ui.composables.WorkorderCard
import de.rogallab.mobile.ui.composables.showErrorMessage
import de.rogallab.mobile.ui.navigation.AppNavigationBar
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.people.ErrorState
import de.rogallab.mobile.ui.people.composables.evalWorkorderStateAndTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun WorkordersListScreen(
   navController: NavController,
   viewModel: WorkordersViewModel
) {         //12345678901234567890123
   val tag = "ok>WorkordersListScr  ."

   val workordersState: WorkorderUiState by viewModel.stateFlowWorkorders.collectAsStateWithLifecycle()
   val errorState: ErrorState by viewModel.stateFlowError.collectAsStateWithLifecycle()

   BackHandler(
      enabled = true,
      onBack = {
         logInfo(tag, "Back Navigation (Abort)")
         navController.popBackStack(
            route = NavScreen.PeopleList.route,
            inclusive = false
         )
      }
   )

   val snackbarHostState = remember { SnackbarHostState() }
   val coroutineScope = rememberCoroutineScope()

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.workorders_list)) },
            navigationIcon = {
               IconButton(
                  onClick = {
                     logInfo(tag, "Lateral Navigation (Home)")
                     navController.navigate(route = NavScreen.PeopleList.route) {
                        popUpTo(route = NavScreen.PeopleList.route) { inclusive = true }
                     }
                  }) {
                  Icon(
                     imageVector = Icons.Default.ArrowBack,
                     contentDescription = stringResource(R.string.back)
                  )
               }
            }
         )
      },
      bottomBar = {
         AppNavigationBar(navController = navController)
      },
      floatingActionButton = {
         FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.tertiary,
            onClick = {
               // FAB clicked -> InputScreen initialized
               logDebug(tag, "Forward Navigation: FAB clicked")
               viewModel.clearState()
               navController.navigate(route = NavScreen.WorkorderInput.route)
            }
         ) {
            Icon(Icons.Default.Add, "Add a workorder")
         }
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }) { innerPadding ->
      if (workordersState.isLoading) {
         logDebug(tag, "uiStatePeople.Loading")
         Column(
            modifier = Modifier
               .padding(bottom = innerPadding.calculateBottomPadding())
               .padding(horizontal = 8.dp)
               .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
         ) {
            CircularProgressIndicator(modifier = Modifier.size(160.dp))
         }
      } else if (workordersState.isSuccessful && workordersState.workorders.isNotEmpty()) {
         var items: MutableList<Workorder> = workordersState.workorders
            .sortedBy { it.state } as MutableList<Workorder>

         LazyColumn(
            modifier = Modifier
               .padding(top = innerPadding.calculateTopPadding())
               .padding(bottom = innerPadding.calculateBottomPadding())
               .padding(horizontal = 8.dp),
            state = rememberLazyListState()
         ) {
            items(items = items) { workorder ->
               val (state, time) = evalWorkorderStateAndTime(workorder)
               val dismissState = rememberDismissState(
                  confirmValueChange = {
                     if (it == DismissValue.DismissedToEnd) {
                        logDebug("ok>SwipeToDismiss", "-> Detail ${workorder.id}")
                        navController.navigate(NavScreen.WorkorderDetail.route + "/${workorder.id}")
                        return@rememberDismissState true
                     } else if (it == DismissValue.DismissedToStart) {
                        logDebug("==>SwipeToDismiss().", "-> Delete")
                        viewModel.remove(workorder.id)
                        val job = coroutineScope.launch {
                           showErrorMessage(
                              snackbarHostState = snackbarHostState,
                              errorMessage = "Wollen die Arbeitsaufgabe wirklich löschen?",
                              actionLabel = "nein",
                              onErrorAction = {
                                 viewModel.add(workorder)
                              }
                           )
                        }
                        coroutineScope.launch {
                           job.join()
                           navController.navigate(NavScreen.WorkordersList.route)
                        }
                        return@rememberDismissState true
                     }
                     return@rememberDismissState false
                  }
               )

               SwipeToDismiss(
                  state = dismissState,
                  modifier = Modifier.padding(vertical = 4.dp),
                  directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
                  background = {
                     SetSwipeBackgroud(dismissState)
                  },
                  dismissContent = {
                     Column {
                        WorkorderCard(
                           time = time,
                           state = state,
                           title = workorder.title,
                           elevation = SetCardElevation(dismissState)
                        )
                     }
                  }
               )
            } // items
         } // lazy Column
      } // state successful
   } // Scaffold

   EventEffect(
      event = errorState.errorEvent,
      onHandled = viewModel::onErrorEventHandled
   ) { it: String ->
      val job = coroutineScope.launch {
         snackbarHostState.showSnackbar(
            message = it,
            actionLabel = "ok",
            withDismissAction = false,
            duration = SnackbarDuration.Short
         )
      } // show snackbar
      coroutineScope.launch {
         // wait for snackbar to disappear
         job.join()
         // error event handled
         viewModel.onErrorEventHandled()
         // navigate back
         if (errorState.back) {
            logInfo(tag, "Back Navigation (Abort)")
            navController.popBackStack(
               route = NavScreen.PeopleList.route,
               inclusive = false
            )
         }
      } // navigate
   }
}