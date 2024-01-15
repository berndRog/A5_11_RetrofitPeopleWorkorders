package de.rogallab.mobile.ui.people

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.entities.WorkState
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.composables.EventEffect
import de.rogallab.mobile.ui.composables.PersonCard
import de.rogallab.mobile.ui.composables.SetSwipeBackgroud
import de.rogallab.mobile.ui.composables.WorkorderCard
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.people.composables.evalWorkorderStateAndTime
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonWorkorderOverviewScreen(
   personId: UUID?,
   navController: NavController,
   viewModel: PeopleViewModel
) {         //1234567890123456780123
   val tag = "ok>PersonWorkOverview."

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

   personId?.let {
      LaunchedEffect(viewModel.dbChanged) {
         logDebug(tag, "readByIdWithWorkorders()")
         viewModel.readByIdWithWorkorders(personId)
         //workordersViewModel.readAll()
      }
   } ?: run {
      viewModel.triggerErrorEvent(
         message ="No id for person is given", up = false, back = true)
   }

   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.personwork_overview)) },
            navigationIcon = {
               IconButton(onClick = {
                  if (errorState.up) {
                     logInfo(tag, "Reverse Navigation (Up) viewModel.update()")
                     navController.navigate(route = NavScreen.PeopleList.route) {
                        popUpTo(route = NavScreen.PeopleList.route) { inclusive = true }
                     }
                  }
                  if (errorState.back) {
                     logInfo(tag, "Back Navigation, Error in viewModel.update()")
                     navController.popBackStack(
                        route = NavScreen.PeopleList.route,
                        inclusive = false
                     )
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
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }
   ) { innerPadding ->

      Column(
         modifier = Modifier
            .padding(top = innerPadding.calculateTopPadding(),
               bottom = innerPadding.calculateBottomPadding())
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
      ) {
         PersonCard(
            firstName = viewModel.firstName,
            lastName = viewModel.lastName,
            email = viewModel.email,
            phone = viewModel.phone,
            imagePath = viewModel.imagePath
         )

         AssignedWorkorders(
            navController = navController,
            personId = personId!!,
            assignedWorkorders = viewModel.workorders,
            onRemoveWorkorder = { viewModel.removeWorkorder(it) }
         )
         DefaultWorkordersList(
            workorders = viewModel.workorders,
            onAddWorkorder = { viewModel.addWorkorder(it)},
         )
      }

      EventEffect(
         event = errorState.errorEvent,
         onHandled = viewModel::onErrorEventHandled
      ){ errorMessage: String ->
         snackbarHostState.showSnackbar(
            message = errorMessage,
            actionLabel = "ok",
            withDismissAction = false,
            duration = SnackbarDuration.Short
         )
         if (errorState.back) {
            logInfo(tag, "Back Navigation (Abort)")
            navController.popBackStack(
               route = NavScreen.PeopleList.route,
               inclusive = false
            )
         }
      }
   }
}

@Composable
private fun DefaultWorkordersList(
   workorders: List<Workorder>,
   onAddWorkorder: (Workorder) -> Unit
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
               onAddWorkorder(workorder)
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
   onRemoveWorkorder: (Workorder) -> Unit
) {
           //12345678901234567890123
   val tag ="ok>AssignedWorkorders ."

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

            val dismissState = rememberDismissState(
               confirmValueChange = {
                  if (it == DismissValue.DismissedToEnd) {
                     logDebug("ok>SwipeToDismiss", "-> Detail ${workorder.id}")
                     navController.navigate(NavScreen.PersonWorkorderDetail.route + "/${workorder.id}")
                     return@rememberDismissState true
                  } else if (it == DismissValue.DismissedToStart) {
                     logDebug("ok>SwipeToDismiss", "-> Delete")
                     if(workorder.state != WorkState.Started &&
                        workorder.state != WorkState.Completed)   {
                        onRemoveWorkorder(workorder)
                        navController.navigate(NavScreen.PersonWorkorderOverview.route + "/$personId")
                        return@rememberDismissState true
                     }                  }
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
                     logVerbose(tag, "assigned ${workorder.asString()}")
                     WorkorderCard(
                        time = time,
                        state = state,
                        title = workorder.title,
                        modifier = Modifier.padding(top = 8.dp)
                     )
                  }
               }
            )
         }
      }
   } // assignedWorkorders
}