package de.rogallab.mobile.ui.workorders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.WorkState
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.zonedDateTimeString
import de.rogallab.mobile.ui.base.ErrorParams
import de.rogallab.mobile.ui.base.showAndRespondToError
import de.rogallab.mobile.ui.composables.PersonCard
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.composables.evalWorkorderStateAndTime
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkorderScreen(
   isInputScreen: Boolean,
   id: UUID?,
   navController: NavController,
   viewModel: WorkordersViewModel,
) {
          // 12345678901234567890123
   var tag = "ok>WorkorderInputScr  ."
   val isInput:Boolean by rememberSaveable { mutableStateOf(isInputScreen) }

   if (! isInput) {
      tag = "ok>WorkorderDetailScr ."
      id?.let {
         LaunchedEffect(Unit) {
            logDebug(tag, "ReadById()")
            viewModel.readById(id)
         }
      } ?: run {
//         viewModel.onTriggerErrorEvent(
//            message = "No id for workorder is given", isNavigation = true)
      }
   }

   BackHandler(
      enabled = true,
      onBack = {
         logInfo(tag, "Back Navigation (Abort)")
         navController.popBackStack(
            route = NavScreen.WorkordersList.route,
            inclusive = false
         )
      }
   )

   val snackbarHostState = remember { SnackbarHostState() }
   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.workorder_detail)) },
            navigationIcon = {
               IconButton(onClick = {
                  if(isInput) viewModel.add() else viewModel.update()
                  //if(errorState.up) {
                     navController.navigate(route = NavScreen.WorkordersList.route) {
                        popUpTo(route = NavScreen.WorkorderDetail.route) { inclusive = true }
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

      Column(
         modifier = Modifier
            .padding(top = innerPadding.calculateTopPadding())
            .padding(bottom = innerPadding.calculateBottomPadding())
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
      ) {

         if(isInput) {

            Column(modifier = Modifier.padding(top = 8.dp)) {
               Text(
                  text = zonedDateTimeString(viewModel.workorderStateValue.created),   // State ↓
                  modifier = Modifier
                     .fillMaxWidth()
                     .align(Alignment.End),
                  style = MaterialTheme.typography.bodySmall,
               )
               OutlinedTextField(
                  value = viewModel.workorderStateValue.title,                         // State ↓
                  onValueChange = {                                                    // Event ↑
                     viewModel.onWorkorderUiEventChange(WorkorderUiEvent.Title, it) },
                  modifier = Modifier.fillMaxWidth(),
                  label = { Text(stringResource(R.string.title)) },
                  textStyle = MaterialTheme.typography.bodyMedium,
                  singleLine = true
               )
               OutlinedTextField(
                  value = viewModel.workorderStateValue.description,                          // State ↓
                  onValueChange = {
                     viewModel.onWorkorderUiEventChange(WorkorderUiEvent.Description, it) },  // Event ↑
                  modifier = Modifier.fillMaxWidth(),
                  label = { Text(stringResource(R.string.description)) },
                  singleLine = false,
                  textStyle = MaterialTheme.typography.bodyMedium
               )
            }
         } else {


            if (viewModel.workorderStateValue.state != WorkState.Default) {
               viewModel.workorderStateValue.person?.let { it: Person ->
                  PersonCard(
                     firstName = it.firstName,
                     lastName = it.lastName,
                     email = it.email,
                     phone = it.phone,
                     imagePath = it.getActualImagePath()
                  )
               }
            }

            Text(
               text = zonedDateTimeString(viewModel.workorderStateValue.created),   // State ↓
               modifier = Modifier
                  .fillMaxWidth()
                  .align(Alignment.End),
               style = MaterialTheme.typography.bodySmall,
            )
            OutlinedTextField(
               value = viewModel.workorderStateValue.title,                          // State ↓
               onValueChange = {
                  viewModel.onWorkorderUiEventChange(WorkorderUiEvent.Title, it) },  // Event ↑
               modifier = Modifier.fillMaxWidth(),
               readOnly = viewModel.workorderStateValue.state != WorkState.Default,
               label = { Text(text = stringResource(id = R.string.title)) },
               textStyle = MaterialTheme.typography.bodyMedium,
               singleLine = true
            )
            OutlinedTextField(
               value = viewModel.workorderStateValue.description,                   // State ↓
               onValueChange = {                                                    // Event ↑
                  viewModel.onWorkorderUiEventChange(WorkorderUiEvent.Description, it) },
               modifier = Modifier.fillMaxWidth(),
               readOnly = viewModel.workorderStateValue.state != WorkState.Default,
               label = { Text(text = stringResource(id = R.string.description)) },
               singleLine = false,
               textStyle = MaterialTheme.typography.bodyMedium
            )

            if(viewModel.workorderStateValue.state != WorkState.Default) {
               val (state, time) = evalWorkorderStateAndTime(viewModel.workorderStateValue)

               Row(modifier = Modifier.padding(top = 16.dp),
                  horizontalArrangement = Arrangement.Absolute.Right,
                  verticalAlignment = Alignment.CenterVertically
               ) {
                  Text(
                     text = time,
                     style = MaterialTheme.typography.bodyMedium,
                     modifier = Modifier
                        .padding(start = 4.dp)
                        .weight(0.6f)
                  )
                  FilledTonalButton(
                     onClick = {},
                     enabled = false,
                     modifier = Modifier
                        .padding(end = 4.dp)
                        .weight(0.4f)
                  ) {
                     Text(
                        text = state,
                        style = MaterialTheme.typography.bodyMedium,
                     )
                  }
               }
            }
         }
      }
   }

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