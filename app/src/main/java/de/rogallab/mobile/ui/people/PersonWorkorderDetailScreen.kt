package de.rogallab.mobile.ui.people

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.entities.WorkState
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.zonedDateTimeString
import de.rogallab.mobile.ui.composables.HandleUiStateError
import de.rogallab.mobile.ui.composables.LogUiStates
import de.rogallab.mobile.ui.composables.PersonCard
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.people.composables.InputCompleted
import de.rogallab.mobile.ui.people.composables.InputStarted
import de.rogallab.mobile.ui.workorders.WorkordersViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonWorkorderDetailScreen(
   workorderId: UUID?,
   navController: NavController,
   viewModel: WorkordersViewModel,
) {        // 12345678901234567890123
   val tag = "ok>PersonWorkDetailScr."

   BackHandler(
      enabled = true,
      onBack = {
         logInfo(tag, "Back Navigation (Abort)")
         navController.popBackStack(
            route = NavScreen.PersonWorkorderOverview.route + "",
            inclusive = false
         )
      }
   )

   val uiStateWorkorder by viewModel.uiStateWorkorderFlow.collectAsStateWithLifecycle()
   LogUiStates(uiStateWorkorder,"UiState Workorder", tag )

   val snackbarHostState = remember { SnackbarHostState() }

   workorderId?.let {
      LaunchedEffect(viewModel.dbChanged) {
         logDebug(tag, "readByIdWithPerson()")
         viewModel.readByIdWithPerson(workorderId)
      }
   } ?: run {
      viewModel.onUiStateWorkorderFlowChange(UiState.Error("No id for person is given"))
   }

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.personwork_detail)) },
            navigationIcon = {
               IconButton(onClick = {
                  viewModel.update(workorderId!!)
                  if(viewModel.uiStateWorkorderFlow.value.upHandler) {
                     logInfo(tag, "Reverse Navigation (Up) viewModel.update()")
                     navController.navigate(
                        route = NavScreen.PersonWorkorderOverview.route + "/${viewModel.assignedPerson!!.id}") {
                        popUpTo(route = NavScreen.PersonWorkorderDetail.route) { inclusive = true }
                     }
                  }
                  if(viewModel.uiStateWorkorderFlow.value.backHandler) {
                     logInfo(tag, "Back Navigation, Error in viewModel.add()")
                     navController.popBackStack(
                        route = NavScreen.PersonWorkorderOverview.route + "/${viewModel.assignedPerson!!.id}",
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
            .padding(top = innerPadding.calculateTopPadding())
            .padding(bottom = innerPadding.calculateBottomPadding())
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
      ) {

         viewModel.assignedPerson?.let {
            Column(modifier = Modifier.padding(bottom=16.dp)) {
               PersonCard(
                  firstName = it.firstName,
                  lastName = it.lastName,
                  email = it.email,
                  phone = it.phone,
                  imagePath = it.imagePath
               )
            }
         }

         Text(
            text = zonedDateTimeString(viewModel.created),
            modifier = Modifier
               .fillMaxWidth()
               .align(Alignment.End),
            style = MaterialTheme.typography.bodySmall,
         )
         OutlinedTextField(
            value = viewModel.title,                          // State ↓
            onValueChange = { viewModel.onTitleChange(it) },  // Event ↑
            modifier = Modifier.fillMaxWidth(),
            readOnly = viewModel.state != WorkState.Default,
            label = { Text(text = stringResource(id = R.string.title)) },
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true
         )
         OutlinedTextField(
            value = viewModel.description,                          // State ↓
            onValueChange = { viewModel.onDescriptionChange(it) },  // Event ↑
            modifier = Modifier.fillMaxWidth(),
            readOnly = viewModel.state != WorkState.Default,
            label = { Text(text = stringResource(id = R.string.description)) },
            singleLine = false,
            textStyle = MaterialTheme.typography.bodyMedium
         )
         InputStarted(
            state = viewModel.state,
            started = viewModel.started,
            onStartedChange = { viewModel.onStartedChange(it) },
            modifier = Modifier.padding(top = 8.dp)
         )

         if(viewModel.state == WorkState.Started || viewModel.state == WorkState.Completed) {
            OutlinedTextField(
               value = viewModel.remark,                          // State ↓
               onValueChange = { viewModel.onRemarkChange(it) },  // Event ↑
               modifier = Modifier.fillMaxWidth(),
               //readOnly = viewModel.state != WorkState.Started,
               label = { Text(text = stringResource(id = R.string.remark)) },
               singleLine = false,
               textStyle = MaterialTheme.typography.bodyMedium
            )
            InputCompleted(
               state = viewModel.state,
               completed = viewModel.completed,
               onCompletedChange = { viewModel.onCompletedChange(it) },
            )
         }
      }
   }

   if (uiStateWorkorder is UiState.Error) {
      HandleUiStateError(
         uiStateFlow = uiStateWorkorder,
         actionLabel = "Ok",
         onErrorAction = { },
         snackbarHostState = snackbarHostState,
         navController = navController,
         routePopBack = NavScreen.WorkordersList.route,
         onUiStateFlowChange = { viewModel.onUiStateWorkorderFlowChange(it) },
         tag = tag
      )
   }
}


class StateMachine(
   var started: Boolean = false,
   private var paused: Boolean = false,
   private var restarted: Boolean = false,
   var completed: Boolean = false,
) {

   fun start() {
      if( !started ) {
         started = true
         paused = false
         restarted = false
         completed = false
      } else {
         logError("ok>State","Can't start, is already started")
      }
   }

   fun pause() {
      if(started || restarted) {
         started = false
         restarted = false
         paused = true
      } else {
         logError("ok>State","Can't pause, if not started or restarted")
      }
   }

   fun restart() {
      if(paused) {
         restarted = true
         paused = false
      } else {
         logError("ok>State","Can't restart, if not paused")
      }
   }

   fun complete() {
      if(started || paused) {
         started = false
         paused = false
         restarted = false
         completed = true
      } else {
         logError("ok>State","Can't complete, if not started or paused")
      }
   }

}
