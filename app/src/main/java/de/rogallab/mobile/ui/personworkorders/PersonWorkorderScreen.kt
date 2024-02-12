package de.rogallab.mobile.ui.personworkorders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.WorkState
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.zonedDateTimeString
import de.rogallab.mobile.ui.base.ErrorParams
import de.rogallab.mobile.ui.base.showAndRespondToError
import de.rogallab.mobile.ui.composables.InputStartWorkorder
import de.rogallab.mobile.ui.composables.InputWorkorderCompleted
import de.rogallab.mobile.ui.composables.PersonCard
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.workorders.WorkorderUiEvent
import kotlinx.coroutines.delay
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun PersonWorkorderScreen(
   workorderId: UUID?,
   navController: NavController,
   viewModel: PersonWorkordersViewModel,
) {        // 12345678901234567890123
   val tag = "ok>PersonWorkorderScr ."


   //region Back handler for back navigation
   BackHandler(
      enabled = true,
      onBack = {
         logInfo(tag, "Back Navigation (Abort)")
         navController.navigate(route = NavScreen.PeopleList.route) {
            popUpTo(route = NavScreen.PersonWorkorderOverview.route) { inclusive = true }
         }
      }
   )
   //endregion

   //region read workorder by id with person
   val toggleRead: MutableState<Boolean> = remember { mutableStateOf(true) }
   workorderId?.let { id ->
      LaunchedEffect(toggleRead.value) {
         logDebug(tag, "readWorkorderByIdWithPerson()")
         viewModel.readWorkorderByIdWithPerson(id)
      }
   } ?: run {
      viewModel.showAndNavigateBackOnFailure(
         Exception("No id for workorder is given"))
   }
   //endregion

   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.personwork_detail)) },
            navigationIcon = {
               IconButton(onClick = {
                  viewModel.update()
                  navController.navigate(route = NavScreen.PeopleList.route) {
                     popUpTo(route = NavScreen.PersonWorkorderDetail.route) { inclusive = true }
                  }
               }) {
                  Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back))
               }
            }
         )
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(snackbarData = data, actionOnNewLine = true)
         }
      }
   ) { innerPadding ->
   //
      Column(
         modifier = Modifier
            .padding(paddingValues = innerPadding)
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
      ) {
         // assigned person if state is not default
         viewModel.workorderStateValue.person?.let { it: Person ->
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
               PersonCard(
                  firstName = it.firstName,
                  lastName = it.lastName,
                  email = it.email,
                  phone = it.phone,
                  imagePath = it.getActualImagePath()
               )
            }
         }
         val focusManager: FocusManager = LocalFocusManager.current
         val keyboardController = LocalSoftwareKeyboardController.current

         Text(
            text = zonedDateTimeString(viewModel.workorderStateValue.created),
            modifier = Modifier
               .fillMaxWidth()
               .align(Alignment.End),
            style = MaterialTheme.typography.bodySmall,
         )

         //region title and description
         OutlinedTextField(
            value = viewModel.workorderStateValue.title,                      // State ↓
            onValueChange = {                                                 // Event ↑
               viewModel.onWorkorderUiEventChange(WorkorderUiEvent.State, it) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = viewModel.workorderStateValue.state != WorkState.Default,
            label = { Text(stringResource(R.string.title)) },
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy( imeAction = ImeAction.Next ),
            keyboardActions = KeyboardActions(
               onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
         )
         OutlinedTextField(
            value = viewModel.workorderStateValue.description,                // State ↓
            onValueChange = {                                                 // Event ↑
               viewModel.onWorkorderUiEventChange(WorkorderUiEvent.Description, it) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = viewModel.workorderStateValue.state != WorkState.Default,
            label = { Text(stringResource(R.string.description)) },
            singleLine = false,
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions.Default.copy( imeAction = ImeAction.Next ),
            keyboardActions = KeyboardActions(
               onNext = {
                  focusManager.moveFocus(FocusDirection.Down)
                  keyboardController?.hide()
               }
            ),
         )
         //endregion

         InputStartWorkorder(
            state = viewModel.workorderStateValue.state,          // State ↓
            onStateChange = viewModel::onWorkorderUiEventChange,  // Event ↑
            started = viewModel.workorderStateValue.started,      // State ↓
            onStartedChange = viewModel::onWorkorderUiEventChange,// Event ↑
            onUpdate = viewModel::update,                         // Event ↑
            modifier = Modifier.padding(top = 8.dp)               // State ↓
         )

         if (viewModel.workorderStateValue.state == WorkState.Started ||
             viewModel.workorderStateValue.state == WorkState.Completed) {
            OutlinedTextField(
               value = viewModel.workorderStateValue.remark,                  // State ↓
               onValueChange = {                                              // Event ↑
                  viewModel.onWorkorderUiEventChange(WorkorderUiEvent.Remark, it) },
               modifier = Modifier.fillMaxWidth(),
               readOnly = viewModel.workorderStateValue.state != WorkState.Started,
               label = { Text(stringResource(R.string.remark)) },
               singleLine = false,
               textStyle = MaterialTheme.typography.bodyMedium,
               keyboardActions = KeyboardActions(
                  onNext = { keyboardController?.hide() }
               ),
            )
            InputWorkorderCompleted(
               state = viewModel.workorderStateValue.state,                // State ↓
               onStateChange = viewModel::onWorkorderUiEventChange,        // Event ↑
               completed = viewModel.workorderStateValue.completed,        // State ↓
               onCompletedChange = viewModel::onWorkorderUiEventChange,    // Event ↑
               onUpdate = viewModel::update,                               // Event ↑
               onNavEvent = viewModel::onNavEvent,                         // Event ↑
            )
         }
      }
   }

   //region error handling
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
   //endregion
}

class StateMachine(
   var started: Boolean = false,
   private var paused: Boolean = false,
   private var restarted: Boolean = false,
   var completed: Boolean = false,
) {
   fun start() {
      if (!started) {
         started = true
         paused = false
         restarted = false
         completed = false
      } else {
         logError("ok>State", "Can't start, is already started")
      }
   }

   fun pause() {
      if (started || restarted) {
         started = false
         restarted = false
         paused = true
      } else {
         logError("ok>State", "Can't pause, if not started or restarted")
      }
   }

   fun restart() {
      if (paused) {
         restarted = true
         paused = false
      } else {
         logError("ok>State", "Can't restart, if not paused")
      }
   }

   fun complete() {
      if (started || paused) {
         started = false
         paused = false
         restarted = false
         completed = true
      } else {
         logError("ok>State", "Can't complete, if not started or paused")
      }
   }
}
