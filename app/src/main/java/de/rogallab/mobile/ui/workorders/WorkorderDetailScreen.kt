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
import androidx.compose.material.icons.filled.ArrowBack
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
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.zonedDateTimeString
import de.rogallab.mobile.ui.composables.HandleUiStateError
import de.rogallab.mobile.ui.composables.LogUiStates
import de.rogallab.mobile.ui.composables.PersonCard
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.people.composables.evalWorkorderStateAndTime
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkorderDetailScreen(
   id: UUID?,
   navController: NavController,
   viewModel: WorkordersViewModel,
) {        // 12345678901234567890123
   val tag = "ok>WorkorderDetailScr ."

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

   val uiStateWorkorderFlow by viewModel.uiStateWorkorderFlow.collectAsStateWithLifecycle()
   LogUiStates(uiStateWorkorderFlow,"UiState Workorder", tag )

   val snackbarHostState = remember { SnackbarHostState() }

   id?.let {
      LaunchedEffect(viewModel.dbChanged) {
         logDebug(tag, "ReadById()")
         viewModel.readByIdWithPerson(id)
      }
   } ?: run {
      viewModel.onUiStateWorkorderFlowChange(UiState.Error("No id for person is given"))
   }

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.workorder_detail)) },
            navigationIcon = {
               IconButton(onClick = {
                  viewModel.update(id!!)
                  if(viewModel.uiStateWorkorderFlow.value.upHandler) {
                     logInfo(tag, "Reverse Navigation (Up) viewModel.update()")
                     navController.navigate(route = NavScreen.WorkordersList.route) {
                        popUpTo(route = NavScreen.WorkorderDetail.route) { inclusive = true }
                     }
                  }
                  if(viewModel.uiStateWorkorderFlow.value.backHandler) {
                     logInfo(tag, "Back Navigation, Error in viewModel.add()")
                     navController.popBackStack(
                        route = NavScreen.WorkordersList.route,
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

         val workorder = viewModel.getWorkorderFromState()

         if(workorder.state != WorkState.Default) {
            workorder.person?.let {
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


         if(workorder.state != WorkState.Default) {
            val (state, time) = evalWorkorderStateAndTime(workorder)

            Row(modifier = Modifier.padding(top=16.dp),
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

   if (uiStateWorkorderFlow is UiState.Error) {
      HandleUiStateError(
         uiStateFlow = uiStateWorkorderFlow,
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