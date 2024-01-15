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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.composables.EventEffect
import de.rogallab.mobile.ui.composables.SelectAndShowImage
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.people.composables.InputNameMailPhone
import de.rogallab.mobile.ui.people.composables.isInputValid
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(
   isInputScreen : Boolean,
   id: UUID?,
   navController: NavController,
   viewModel: PeopleViewModel,
) {
   var tag = "ok>PersonInputScreen ."
   val isInput:Boolean by rememberSaveable { mutableStateOf(isInputScreen) }

   val errorState: ErrorState by viewModel.stateFlowError.collectAsStateWithLifecycle()

   if (! isInput) {
      tag = "ok>PersonDetailScreen ."
      id?.let {
         LaunchedEffect(Unit) {
            logDebug(tag, "ReadById()")
            viewModel.readById(id)
         }
      } ?: run {
         viewModel.triggerErrorEvent(
            message = "No id for person is given", up = false, back = true)
      }
   }

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

   val context = LocalContext.current
   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.person_detail)) },
            navigationIcon = {
               IconButton(onClick = {
                  if (!isInputValid(context, viewModel)) {
                     if(isInput) viewModel.add() else viewModel.update(id!!)
                  }
                  if (errorState.up) {
                     navController.navigate(route = NavScreen.PeopleList.route) {
                        popUpTo(route = NavScreen.PeopleList.route) { inclusive = true }
                     }
                  }
                  if (errorState.back) {
                     navController.popBackStack(route = NavScreen.PeopleList.route,
                        inclusive = false)
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
            .verticalScroll(state = rememberScrollState())
      ) {
         InputNameMailPhone(
            firstName = viewModel.firstName,                         // State ↓
            onFirstNameChange = { viewModel.onFirstNameChange(it) }, // Event ↑
            lastName = viewModel.lastName,                           // State ↓
            onLastNameChange = { viewModel.onLastNameChange(it) },   // Event ↑
            email = viewModel.email,                                 // State ↓
            onEmailChange = { viewModel.onEmailChange(it) },         // Event ↑
            phone = viewModel.phone,                                 // State ↓
            onPhoneChange = { viewModel.onPhoneChange(it) }          // Event ↑
         )

         SelectAndShowImage(
            imagePath = viewModel.imagePath,                         // State ↓
            onImagePathChanged = { viewModel.onImagePathChange(it) } // Event ↑
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
