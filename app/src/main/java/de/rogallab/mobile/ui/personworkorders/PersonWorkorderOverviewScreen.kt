package de.rogallab.mobile.ui.personworkorders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.entities.WorkState
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.composables.PersonCard
import de.rogallab.mobile.ui.composables.SetSwipeBackgroud
import de.rogallab.mobile.ui.composables.WorkorderCard
import de.rogallab.mobile.ui.composables.evalWorkorderStateAndTime
import de.rogallab.mobile.ui.composables.setCardElevation
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.workorders.WorkordersUiState
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonWorkorderOverviewScreen(
   id: UUID?,
   navController: NavController,
   viewModel: PersonWorkordersViewModel
) {         //1234567890123456780123
   val tag = "ok>PersonWorkOverview."

   if(id == null) {
      viewModel.showAndNavigateBackOnError("No id for person is given")
   }

   val personId: UUID by rememberSaveable { mutableStateOf(id!!)  }
   val toggleRead: MutableState<Boolean> = remember { mutableStateOf(true) }

   // readbyId is called when toggleRead is changed
   LaunchedEffect(toggleRead.value) {
      logDebug(tag, "ReadById(${personId.as8()})")
      viewModel.readByIdWithWorkorders(personId)
   }

   val workorderState: WorkordersUiState by viewModel.stateFlowWorkorders.collectAsStateWithLifecycle()
   LaunchedEffect(Unit) {
      viewModel.refreshStateFlowWorkorders() // Ensuring refresh is called at least once
   }

   BackHandler(
      enabled = true,
      onBack = {
         navController.navigate(route = NavScreen.PeopleList.route) {
            popUpTo(route = NavScreen.PersonWorkorderOverview.route) { inclusive = true }
         }
      }
   )

   val snackbarHostState = remember { SnackbarHostState() }
   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.personwork_overview)) },
            navigationIcon = {
               IconButton(onClick = {
                     navController.navigate(route = NavScreen.PeopleList.route) {
                        popUpTo(route = NavScreen.PeopleList.route) { inclusive = true }
                     }
               }) {
                  Icon(
                     imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                     contentDescription = stringResource(R.string.back)
                  )
               }
            }
         )
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }
   ) { innerPadding ->

      if (workorderState.isLoading) {
         logDebug(tag, "workorderState Loading")
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
      } else if (workorderState.isSuccessful && workorderState.workorders.isNotEmpty()) {

         val defaultWorkorders: List<Workorder> = workorderState.workorders
            .filter { it: Workorder -> it.state == WorkState.Default }
            .sortedBy { it.created }

         val assignedWorkorders = workorderState.workorders
            .filter { it: Workorder -> it.personId == personId }

         Column(
            modifier = Modifier
               .padding(top = innerPadding.calculateTopPadding(),
                  bottom = innerPadding.calculateBottomPadding())
               .padding(horizontal = 8.dp)
               .fillMaxWidth()
         ) {
            PersonCard(
               firstName = viewModel.personStateValue.firstName,
               lastName = viewModel.personStateValue.lastName,
               email = viewModel.personStateValue.email,
               phone = viewModel.personStateValue.phone,
               imagePath = viewModel.getImagePath()
            )

            if(assignedWorkorders.size == 0) {
               Text(
                  modifier = Modifier.padding(top = 16.dp),
                  text = "Keine Arbeitsaufgaben zugewiesen",
                  style = MaterialTheme.typography.titleMedium,
               )
            } else {
               AssignedWorkorders(
                  navController = navController,
                  personId = personId,
                  assignedWorkorders = assignedWorkorders.toMutableList(),
                  onUnAssignWorkorder = { workorder: Workorder ->
                     viewModel.unassign(workorder)
                     viewModel.update(workorder)
                  }
               )
            }

            DefaultWorkordersList(
               workorders = defaultWorkorders.toMutableList(),
               onAssignWorkorder = { workorder: Workorder ->
                  viewModel.assign(workorder)
                  viewModel.update(workorder)
               },
               toogleRead = toggleRead
            )
         }
      }
   }
}

@Composable
private fun DefaultWorkordersList(
   workorders: List<Workorder>,
   onAssignWorkorder: (Workorder) -> Unit,
   toogleRead: MutableState<Boolean>
) {

   workorders.filter {
      it.state == WorkState.Default
   }.also { it->
      val filteredWorkorder: MutableList<Workorder> = it.toMutableList()
      Text(
         modifier = Modifier.padding(top = 16.dp),
         text = "Nicht zugewiesene Arbeitsaufgaben",
         style = MaterialTheme.typography.titleMedium,
      )

      LazyColumn(
         modifier = Modifier.fillMaxWidth(),
         state = rememberLazyListState()
      ) {
         items(items = filteredWorkorder) { workorder ->
            val (state, time) = evalWorkorderStateAndTime(workorder)

            Column(Modifier.clickable {
               // assign the workorder to the person
               onAssignWorkorder(workorder)
               toogleRead.value = !toogleRead.value
            }) {
               WorkorderCard(
                  time = time,
                  state = state,
                  title = workorder.title,
                  modifier = Modifier.padding(top = 8.dp)
               )
            }
         }
      } // LazyColumn
   } // filteredList
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignedWorkorders(
   navController: NavController,
   personId: UUID,
   assignedWorkorders: MutableList<Workorder>,
   onUnAssignWorkorder: (Workorder) -> Unit
) {
           //12345678901234567890123
   //val tag ="ok>AssignedWorkorders ."

   if (assignedWorkorders.size > 0) {
      Text(
         modifier = Modifier.padding(top = 16.dp),
         text = "zugewiesene Arbeitsaufgaben",
         style = MaterialTheme.typography.titleMedium,
      )
      LazyColumn(
         modifier = Modifier.fillMaxWidth(),
         state = rememberLazyListState()
      ) {
         items(items = assignedWorkorders) { workorder ->
            val (state, time) = evalWorkorderStateAndTime(workorder)
//         val dismissState = rememberDismissState(
//               confirmValueChange = {
//                  if (it == DismissValue.DismissedToEnd) {
//                     navController.navigate(NavScreen.PersonWorkorderDetail.route + "/${workorder.id}")
//                     true
//                  } else if (it == DismissValue.DismissedToStart) {
//                     if(workorder.state != WorkState.Started &&
//                        workorder.state != WorkState.Completed)   {
//                        // unassign the workorder from the person
//                        // set the workorder to default
//                        // update the workorder in the database
//                        onUnAssignWorkorder(workorder)
//                        navController.navigate(NavScreen.PersonWorkorderOverview.route + "/$personId")
//                        return@rememberDismissState true
//                     }
//                     return@rememberDismissState false
//                  }
//                  else return@rememberDismissState false
//               }
//            )
//            SwipeToDismiss(
//               state = dismissState,
//               modifier = Modifier.padding(vertical = 4.dp),
//               directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
//               background = {
//                  SetSwipeBackgroud(dismissState)
//               },
//               dismissContent = {
//                  Column {
//                     WorkorderCard(
//                        time = time,
//                        state = state,
//                        title = workorder.title,
//                        elevation = setCardElevation(dismissState)
//                     )
//                  }
//               }
//            )

            val dismissBoxState: SwipeToDismissBoxState =
               rememberSwipeToDismissBoxState(
                  initialValue = SwipeToDismissBoxValue.Settled,
                  confirmValueChange = {
                     if (it == SwipeToDismissBoxValue.StartToEnd) {
                        navController.navigate(NavScreen.PersonWorkorderDetail.route + "/${workorder.id}")
                        return@rememberSwipeToDismissBoxState true
                     } else if (it == SwipeToDismissBoxValue.EndToStart) {
                        if(workorder.state != WorkState.Started &&
                           workorder.state != WorkState.Completed)   {
                           // unassign the workorder from the person
                           // set the workorder to default
                           // update the workorder in the database
                           onUnAssignWorkorder(workorder)
                           navController.navigate(NavScreen.PersonWorkorderOverview.route + "/$personId")
                           return@rememberSwipeToDismissBoxState true
                        }
                        return@rememberSwipeToDismissBoxState false
                     }
                     else return@rememberSwipeToDismissBoxState  false
                  },
                  positionalThreshold =  SwipeToDismissBoxDefaults.positionalThreshold,
               )

            SwipeToDismissBox(
               state = dismissBoxState,
               modifier = Modifier.padding(vertical = 4.dp),
               enableDismissFromStartToEnd = true,
               enableDismissFromEndToStart = true,
               backgroundContent = { SetSwipeBackgroud(dismissBoxState) }
            ) {
               Column {
                  WorkorderCard(
                     time = time,
                     state = state,
                     title = workorder.title,
                     elevation = setCardElevation(dismissBoxState)
                  )
               }
            }
         } // items
      }
   } // assignedWorkorders
}