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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.base.ErrorParams
import de.rogallab.mobile.ui.base.showAndRespondToError
import de.rogallab.mobile.ui.base.showUndo
import de.rogallab.mobile.ui.composables.PersonCard
import de.rogallab.mobile.ui.composables.SetSwipeBackgroud
import de.rogallab.mobile.ui.navigation.AppNavigationBar
import de.rogallab.mobile.ui.navigation.NavScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleListScreen(
   navController: NavController,
   viewModel: PeopleViewModel
) {
   val tag = "ok>PeopleListScreen   ."


//   val pullRefreshState = rememberPullToRefreshState()
//
//   PullToRefreshContainer(
//      state = pullRefreshState,
//      )

//
//   PullRefreshIndicator(
//      refreshing = viewModel.peopleStateValue.isLoading,
//      state = pullRefreshState,
//      modifier = Modifier.align(Alignment.TopCenter),
//      backgroundColor = if (viewModel.peopleStateValue.isLoading) Color.Red else Color.Green,
//   )

   val peopleState: PeopleUiState by viewModel.stateFlowPeople.collectAsStateWithLifecycle()
   LaunchedEffect(Unit) {
      viewModel.refreshStateFlowPeople() // Ensuring refresh is called at least once
   }

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
                     logDebug(tag, "Lateral Navigation -> Finish App")
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
               logDebug(tag, "FAB clicked --> PersonInput")
               viewModel.clearState()
            }
         ) {
            Icon(Icons.Default.Add, "Add a contact")
         }
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(snackbarData = data, actionOnNewLine = true)
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
            CircularProgressIndicator(modifier = Modifier.size(100.dp))
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
            items(items = items) { person: Person ->

               val dismissBoxState: SwipeToDismissBoxState =
                  rememberSwipeToDismissBoxState(
                     initialValue = SwipeToDismissBoxValue.Settled,
                     confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.StartToEnd) {
                           logDebug(tag, "PersonCard clicked -> PersonDetail")
                           navController.navigate(NavScreen.PersonDetail.route + "/${person.id}")
                           return@rememberSwipeToDismissBoxState true
                        } else if (it == SwipeToDismissBoxValue.EndToStart) {
                           viewModel.eliminate(person.id)
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
                              logDebug(tag, "Dismiss handled -> PersonList")
                              navController.navigate(NavScreen.PeopleList.route)
                           }
                           return@rememberSwipeToDismissBoxState true
                        } else return@rememberSwipeToDismissBoxState false
                     },
                     positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold,
                  )

               SwipeToDismissBox(
                  state = dismissBoxState,
                  modifier = Modifier.padding(vertical = 4.dp),
                  enableDismissFromStartToEnd = true,
                  enableDismissFromEndToStart = true,
                  backgroundContent = { SetSwipeBackgroud(dismissBoxState) }
               ) {
                  Column(modifier = Modifier.clickable {
                     logDebug(tag, "PersonCard clicked -> PersonWorkorderOverview ${person.id.as8()}")
                     navController.navigate(NavScreen.PersonWorkorderOverview.route + "/${person.id}")
                  }) {
                     PersonCard(
                        firstName = person.firstName,
                        lastName = person.lastName,
                        email = person.email,
                        phone = person.phone,
                        imagePath = person.getActualImagePath()
                     )
                  }
               }
            } // items
         } //
      } // state success
   } // Scaffold

   viewModel.errorStateValue.errorParams?.let { params: ErrorParams ->
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

