package de.rogallab.mobile.ui.people

import android.app.Activity
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.composables.HandleUiStateError
import de.rogallab.mobile.ui.composables.LogUiStates
import de.rogallab.mobile.ui.composables.PersonCard
import de.rogallab.mobile.ui.composables.SetCardElevation
import de.rogallab.mobile.ui.composables.SetSwipeBackgroud
import de.rogallab.mobile.ui.composables.showErrorMessage
import de.rogallab.mobile.ui.navigation.AppNavigationBar
import de.rogallab.mobile.ui.navigation.NavScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleSwipeListScreen(
   navController: NavController,
   viewModel: PeopleViewModel
) {
   val tag = "ok>PeopleListScreen   ."

   val uiStatePersonFlow: UiState<Person> by viewModel.uiStatePersonFlow.collectAsStateWithLifecycle()
   LogUiStates(uiStatePersonFlow,"UiState <Person>", tag )

   val uiStateListPersonFlow: UiState<List<Person>> by viewModel.uiStateListFlow.collectAsStateWithLifecycle()
   LogUiStates(uiStateListPersonFlow,"UiState List<Person>", tag )

   val snackbarHostState = remember { SnackbarHostState() }
   val coroutineScope = rememberCoroutineScope()

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.people_list)) },
            navigationIcon = {
               val activity = LocalContext.current as Activity
               IconButton(
                  onClick = {
                     logDebug(tag, "Lateral Navigation: finish app")
                     activity.finish()
                  }) {
                  Icon(imageVector = Icons.Default.Menu,
                     contentDescription = stringResource(R.string.back))
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
               // Navigate to PersonDetail and put PeopleList on the back stack
               navController.navigate(route = NavScreen.PersonInput.route)
            }
         ) {
            Icon(Icons.Default.Add, "Add a contact")
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

      if (uiStateListPersonFlow == UiState.Empty) {
         logDebug(tag, "uiStatePeople.Empty")
         // nothing to do
      } else if (uiStateListPersonFlow == UiState.Loading) {
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
      } else if (uiStateListPersonFlow is UiState.Success<List<Person>> ||
         uiStateListPersonFlow is UiState.Error ||
         uiStatePersonFlow is UiState.Error) {

         var list: MutableList<Person> = remember { mutableListOf<Person>() }
         if (uiStateListPersonFlow is UiState.Success) {
            (uiStateListPersonFlow as UiState.Success<List<Person>>).data?.let{
               if(it.size > 0) {
                  list = it as MutableList<Person>
                  logVerbose(tag, "uiStatePeople.Success items.size ${list.size}")
               }
            }
         }

         LazyColumn(
            modifier = Modifier
               .padding(top = innerPadding.calculateTopPadding())
               .padding(bottom = innerPadding.calculateBottomPadding())
               .padding(horizontal = 8.dp),
            state = rememberLazyListState()
         ) {
            items(items = list) { person ->

               val dismissState = rememberDismissState(
                  confirmValueChange = {
                     if (it == DismissValue.DismissedToEnd) {
                        logDebug("==>SwipeToDismiss().", "-> Edit")
                        navController.navigate(NavScreen.PersonDetail.route + "/${person.id}")
                        return@rememberDismissState true
                     } else if (it == DismissValue.DismissedToStart) {
                        logDebug("==>SwipeToDismiss().", "-> Delete")
//                      personRemoved = person
                        viewModel.remove(person.id)

                        val job = coroutineScope.launch {
                           showErrorMessage(
                              snackbarHostState = snackbarHostState,
                              errorMessage = "Wollen Sie die Person wirklich löschen?",
                              actionLabel = "nein",
                              onErrorAction = { viewModel.add(person) }
                           )
                        }
                        coroutineScope.launch {
                           job.join()
                           navController.navigate(NavScreen.PeopleList.route)
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
                     Column(modifier = Modifier.clickable {
                        navController.navigate(NavScreen.PersonWorkorderOverview.route + "/${person.id}")
                     }) {
                        PersonCard(
                           firstName = person.firstName,
                           lastName = person.lastName,
                           email = person.email,
                           phone = person.phone,
                           imagePath = person.imagePath ?: "",
                           elevation = SetCardElevation(dismissState)
                        )
                     }
                  }
               )
            }
         } // lazy Column

         if (uiStateListPersonFlow is UiState.Error) {
            HandleUiStateError(
               uiStateFlow = uiStateListPersonFlow,
               actionLabel = "Ok",
               onErrorAction = { },
               snackbarHostState = snackbarHostState,
               navController = navController,
               routePopBack = NavScreen.PeopleList.route,
               onUiStateFlowChange = { },
               tag = tag
            )
         }
         if (uiStatePersonFlow is UiState.Error) {
            HandleUiStateError(
               uiStateFlow = uiStatePersonFlow,
               actionLabel = "Ok",
               onErrorAction = { },
               snackbarHostState = snackbarHostState,
               navController = navController,
               routePopBack = NavScreen.PeopleList.route,
               onUiStateFlowChange = { viewModel.onUiStatePersonFlowChange(it) },
               tag = tag
            )
         }
      }
   }
}