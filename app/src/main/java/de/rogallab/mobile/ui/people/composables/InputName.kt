package de.rogallab.mobile.ui.people.composables

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import de.rogallab.mobile.R
import de.rogallab.mobile.ui.people.PeopleViewModel

@Composable
fun InputName(
   name: String,                        // State ↓
   onNameChange: (String) -> Unit,      // Event ↑
   label: String = "Name",
   errorTooShort: String = "",
   errorTooLong: String = ""
) {
   val focusManager = LocalFocusManager.current

   val charMin = stringResource(R.string.errorCharMin).toInt()
   val charMax = stringResource(R.string.errorCharMax).toInt()

   var isError by rememberSaveable { mutableStateOf(false) }
   var isFocus by rememberSaveable { mutableStateOf(false) }
   var errorText by rememberSaveable { mutableStateOf("") }

   if(!isError) {
      OutlinedTextField(
         modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .onFocusChanged { focusState ->
               if (!focusState.isFocused && isFocus && isNameTooShort(name, charMin)) {
                  isError = true
                  errorText = errorTooShort
               }
               isFocus = focusState.isFocused
            },
         value = name,                 // State ↓
         onValueChange = {
            onNameChange(it)           // Event ↑
            if (isNameTooLong(it, charMax)) {
               isError = true
               errorText = errorTooShort
            }
         },
         label = { Text(text = label) },
         textStyle = MaterialTheme.typography.bodyLarge,
         leadingIcon = {
            Icon(imageVector = Icons.Outlined.Person,
               contentDescription = label)
         },
         singleLine = true,
         keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next
         ),
         keyboardActions = KeyboardActions(
            onNext = {
               if (isNameTooShort(name, charMin)) {
                  isError = true
                  errorText = errorTooShort
               }
               if (!isError) {
                  if (isNameTooLong(name, charMax))
                     isError = true
                  errorText = errorTooLong
               }
               if (!isError) focusManager.moveFocus(FocusDirection.Down)
            }
         )
      )
   } else {
      OutlinedTextField(
         modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .onFocusChanged { focusState ->
               if (!focusState.isFocused && isFocus && isNameTooShort(name, charMin)) {
                  isError = true
                  errorText = errorTooShort
               }
               isFocus = focusState.isFocused
            },
         value = name,                 // State ↓
         onValueChange = {
            onNameChange(it)           // Event ↑
            if (isNameTooLong(it, charMax)) {
               isError = true
               errorText = errorTooShort
            }
         },
         label = { Text(text = label) },
         textStyle = MaterialTheme.typography.bodyLarge,
         leadingIcon = {
            Icon(imageVector = Icons.Outlined.Person,
               contentDescription = label)
         },
         singleLine = true,
         keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next
         ),
         keyboardActions = KeyboardActions(
            onNext = {
               if (isNameTooShort(name, charMin)) {
                  isError = true
                  errorText = errorTooShort
               }
               if (!isError) {
                  if (isNameTooLong(name, charMax))
                     isError = true
                  errorText = errorTooLong
               }
               if (!isError) focusManager.moveFocus(FocusDirection.Down)
            }
         ),
         isError = isError,
         supportingText = {
            if (isError) {
               Text(
                  modifier = Modifier.fillMaxWidth(),
                  text = errorText,
                  color = MaterialTheme.colorScheme.error
               )
            }
         },
         trailingIcon = {
            if (isError)
               Icon(
                  imageVector = Icons.Filled.Error,
                  contentDescription = errorText,
                  tint = MaterialTheme.colorScheme.error
               )
         },
      )
   }
}

fun isNameTooShort(name: String, charMin: Int): Boolean =
   name.isEmpty() || name.length < charMin

fun isNameTooLong(name: String, charMax: Int): Boolean =
   name.length > charMax