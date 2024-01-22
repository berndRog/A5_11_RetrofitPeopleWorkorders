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
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.base.ErrorParams
import de.rogallab.mobile.ui.base.showAndRespondToError
import de.rogallab.mobile.ui.composables.SelectAndShowImage
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.people.composables.InputMail
import de.rogallab.mobile.ui.people.composables.InputName
import de.rogallab.mobile.ui.people.composables.InputPhone
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
   val isInput: Boolean by rememberSaveable { mutableStateOf(isInputScreen) }

   if (!isInput) {
      tag = "ok>PersonDetailScreen ."
      id?.let {
         LaunchedEffect(Unit) {
            logDebug(tag, "ReadById()")
            viewModel.readById(id)
            viewModel.onIdChange(id)
         }
      } ?: run {
         viewModel.showAndNavigateBackOnFailure("No id for person is given")
      }
   }

   BackHandler(
      enabled = true,
      onBack = {
         logInfo(tag, "Back Navigation (Abort)")
         navController.navigate(route = NavScreen.PeopleList.route) {
            popUpTo(route = NavScreen.PersonDetail.route) { inclusive = true }
         }
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
                  if (!viewModel.isValid(context, viewModel)) {
                     if (isInput) viewModel.add() else viewModel.update(id!!)
                     navController.navigate(route = NavScreen.PeopleList.route) {
                        popUpTo(route = NavScreen.PersonDetail.route) { inclusive = true }
                     }
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
            .padding(paddingValues = innerPadding)
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
      ) {
         InputName(
            name = viewModel.firstName,                  // State ↓
            onNameChange = viewModel::onFirstNameChange, // Event ↑
            errorTooShort = stringResource(R.string.errorFirstNameTooShort),
            errorTooLong = stringResource(R.string.errorFirstNameTooLong)
         )
         InputName(
            name = viewModel.lastName,                   // State ↓
            onNameChange = viewModel::onLastNameChange,  // Event ↑
            errorTooShort = stringResource(R.string.errorLastNameTooShort),
            errorTooLong = stringResource(R.string.errorLastNameTooLong)
         )
         InputMail(
            email = viewModel.email,                     // State ↓
            onEmailChange = viewModel::onEmailChange,    // Event ↑
         )
         InputPhone(
            phone = viewModel.phone,                     // State ↓
            onPhoneChange = viewModel::onPhoneChange     // Event ↑
         )
         SelectAndShowImage(
            imagePath = viewModel.imagePath,                  // State ↓
            onImagePathChanged = viewModel::onImagePathChange // Event ↑
         )
      }
   }

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