
package de.rogallab.mobile.ui.workorders

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
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.zonedDateTimeNow
import de.rogallab.mobile.domain.utilities.zonedDateTimeString
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.composables.HandleUiStateError
import de.rogallab.mobile.ui.composables.LogUiStates

// https://www.facilityapps.com/de/workbook-app

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkorderInputScreen(
   navController: NavController,
   viewModel: WorkordersViewModel
) {
            //12345678901234567890123
   val tag = "ok>WorkorderInputScr  ."

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
            title = { Text(text = stringResource(R.string.workorder_input)) },
            navigationIcon = {
               IconButton(onClick = {
                  logDebug(tag, "TopAppBar onClickHandler() -> add workOrder")
                  viewModel.add()
                  navController.navigate(route = NavScreen.WorkordersList.route) {
                     popUpTo(route = NavScreen.WorkorderInput.route) { inclusive = true }
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
               //modifier =  Modifier.border(2.dp, MaterialTheme.colors.secondary),
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      },
   ) { innerPaddings ->

      Column(
         modifier = Modifier
            .padding(top = innerPaddings.calculateTopPadding())
            .padding(bottom = innerPaddings.calculateBottomPadding())
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
      ) {

         viewModel.onCreatedChange(zonedDateTimeNow())

         Column(modifier = Modifier.padding(top = 8.dp)) {

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
               label = { Text(text = stringResource(id = R.string.title)) },
               textStyle = MaterialTheme.typography.bodyMedium,
               singleLine = true
            )

            OutlinedTextField(
               value = viewModel.description,                          // State ↓
               onValueChange = { viewModel.onDescriptionChange(it) },  // Event ↑
               modifier = Modifier.fillMaxWidth(),
               label = { Text(text = stringResource(id = R.string.description)) },
               singleLine = false,
               textStyle = MaterialTheme.typography.bodyMedium
            )
         }
      }

   }
}