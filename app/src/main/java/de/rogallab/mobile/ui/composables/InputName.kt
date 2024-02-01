package de.rogallab.mobile.ui.composables

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.rogallab.mobile.R
import de.rogallab.mobile.ui.people.PersonUiEvent
import de.rogallab.mobile.ui.base.validateName
import de.rogallab.mobile.ui.base.validateNameTooLong
import de.rogallab.mobile.ui.base.validateNameTooShort

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputName(
   firstName: String,                                    // State ↓
   onFirstNameChange: (PersonUiEvent, String) -> Unit,   // Event ↑
   lastName: String,
   onLastNameChange: (PersonUiEvent, String) -> Unit,
   charMin: Int,
   charMax: Int
) {
   val focusManager: FocusManager = LocalFocusManager.current
   val keyboardController = LocalSoftwareKeyboardController.current

   val labelFirst = stringResource(R.string.firstName)
   val eFirstTooShort = stringResource(R.string.errorFirstNameTooShort)
   val eFirstTooLong = stringResource(R.string.errorFirstNameTooLong)

   var isErrorFirst by rememberSaveable { mutableStateOf(false) }
   var isFocusFirst by rememberSaveable { mutableStateOf(false) }
   var errorTextFirst by rememberSaveable { mutableStateOf("") }

   OutlinedTextField(
      modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth()
         .onFocusChanged { focusState ->
            if (!focusState.isFocused && isFocusFirst) {
               val (e, t) = validateNameTooShort(firstName, charMin, eFirstTooShort)
               isErrorFirst = e
               errorTextFirst = t
            }
            isFocusFirst = focusState.isFocused
         },
      value = firstName,                                       // State ↓
      onValueChange = {
         onFirstNameChange(PersonUiEvent.FirstName, it) // Event ↑
         val (e, t) = validateNameTooLong(it, charMax, eFirstTooLong)
         isErrorFirst = e
         errorTextFirst = t
      },
      label = { Text(text = labelFirst) },
      textStyle = MaterialTheme.typography.bodyLarge,
      leadingIcon = { Icon(Icons.Outlined.Person, labelFirst) },
      singleLine = true,
      keyboardOptions = KeyboardOptions.Default.copy( imeAction = ImeAction.Next ),
      keyboardActions = KeyboardActions(
         onNext = {
            val (e, t) = validateName(firstName, charMin, charMax,
               eFirstTooShort, eFirstTooLong)
            isErrorFirst = e
            errorTextFirst = t
            if (!isErrorFirst) {
               keyboardController?.hide()
               focusManager.moveFocus(FocusDirection.Down)
            }
         }
      ),
      isError = isErrorFirst,
      supportingText = {
         if (isErrorFirst) Text(
            modifier = Modifier.fillMaxWidth(),
            text = errorTextFirst,
            color = MaterialTheme.colorScheme.error
         )
      },
      trailingIcon = {
         if (isErrorFirst) Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = errorTextFirst,
            tint = MaterialTheme.colorScheme.error
         )
      },
   )

   val labelLast = stringResource(R.string.lastName)
   val eLastTooShort = stringResource(R.string.errorLastNameTooShort)
   val eLastTooLong = stringResource(R.string.errorLastNameTooLong)

   var isErrorLast by rememberSaveable { mutableStateOf(false) }
   var isFocusLast by rememberSaveable { mutableStateOf(false) }
   var errorTextLast by rememberSaveable { mutableStateOf("") }

   OutlinedTextField(
      modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth()
         .onFocusChanged { focusState ->
            if (!focusState.isFocused && isFocusLast) {
               val (e, t) = validateNameTooShort(lastName, charMin, eLastTooShort)
               isErrorLast = e
               errorTextLast = t
            }
            isFocusLast = focusState.isFocused
         },
      value = lastName,                                       // State ↓
      onValueChange = {
         onLastNameChange(PersonUiEvent.LastName, it) // Event ↑
         val (e, t) = validateNameTooLong(lastName, charMax, eLastTooLong)
         isErrorLast = e
         errorTextLast = t
      },
      label = { Text(text = labelLast) },
      textStyle = MaterialTheme.typography.bodyLarge,
      leadingIcon = { Icon(Icons.Outlined.Person, labelLast) },
      singleLine = true,
      keyboardOptions = KeyboardOptions.Default.copy( imeAction = ImeAction.Next ),
      keyboardActions = KeyboardActions(
         onNext = {
            keyboardController?.hide()
            val (e, t) = validateName(lastName, charMin, charMax,
               eLastTooShort, eLastTooLong)
            isErrorLast = e
            errorTextLast = t
            if (!isErrorLast) {
               keyboardController?.hide()
               focusManager.moveFocus(FocusDirection.Down)
            }
         }
      ),
      isError = isErrorLast,
      supportingText = {
         if (isErrorLast) Text(
            modifier = Modifier.fillMaxWidth(),
            text = errorTextLast,
            color = MaterialTheme.colorScheme.error
         )
      },
      trailingIcon = {
         if (isErrorLast) Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = errorTextFirst,
            tint = MaterialTheme.colorScheme.error
         )
      },
   )
}