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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.base.ErrorParams
import de.rogallab.mobile.ui.base.showAndRespondToError
import de.rogallab.mobile.ui.base.showUndo
import de.rogallab.mobile.ui.composables.PersonCard
import de.rogallab.mobile.ui.composables.SetCardElevation
import de.rogallab.mobile.ui.composables.SetSwipeBackgroud
import de.rogallab.mobile.ui.navigation.AppNavigationBar
import de.rogallab.mobile.ui.navigation.NavScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleListScreen(
   navController: NavController,
   viewModel: PeopleViewModel
) {
   val tag = "ok>PeopleListScreen   ."

  // https://alexzh.com/jetpack-compose-pull-to-refresh/
  // https://canlioya.medium.com/customise-pull-to-refresh-on-android-with-jetpack-compose-24a7119a4b94
//   val pullRefreshState = rememberPullRefreshState(
//      refreshing = state.isLoading,
//      onRefresh = viewModel::loadOrders
//   )

   val peopleState: PeopleUiState by viewModel.stateFlowPeople.collectAsStateWithLifecycle()

   val coroutineScope = rememberCoroutineScope()
   val snackbarHostState = remember { SnackbarHostState() }
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
      // nothing to do
      if (peopleState.isLoading) {
         logVerbose(tag, "peopleState Loading")
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
      } else if (peopleState.isSuccessful && peopleState.people.isNotEmpty()) {
         val items = peopleState.people
            .sortedBy { it.lastName } as MutableList<Person>
         logVerbose(tag, "peopleState Success items ${items.size}")

         LazyColumn(
            modifier = Modifier
               .padding(paddingValues = innerPadding)
               .padding(horizontal = 8.dp),
            state = rememberLazyListState()
         ) {
            items(items = items) { person ->

               val dismissState = rememberDismissState(
                  confirmValueChange = { dismissValue: DismissValue ->
                     when (dismissValue) {
                        DismissValue.DismissedToEnd -> {
                           navController.navigate(NavScreen.PersonDetail.route + "/${person.id}")
                           true
                        }
                        DismissValue.DismissedToStart -> {
                           viewModel.remove(person.id)
                           // undo delete
                           val job = showUndo(
                              coroutineScope = coroutineScope,
                              snackbarHostState = snackbarHostState,
                              message = "Wollen Sie die Person wirklich löschen?",
                              t = person,
                              onUndoAction = viewModel::add
                           )
                           coroutineScope.launch {
                              job.join()
                              navController.navigate(NavScreen.PeopleList.route)
                           }
                           true
                        }
                        else -> false
                     } // when
                  } // confirmValueChange
               ) // rememberDismissState

               SwipeToDismiss(
                  state = dismissState,
                  modifier = Modifier.padding(vertical = 4.dp),
                  directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
                  background = { SetSwipeBackgroud(dismissState) },
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
         }
      } // state success
   } // Scaffold

   viewModel.errorState.errorParams?.let { params: ErrorParams ->
      LaunchedEffect(params) {
         showAndRespondToError(
            errorParams = params,
            snackbarHostState = snackbarHostState,
            navController = navController,
            onErrorEventHandled = viewModel::onErrorEventHandled
         )
      }
   }
}