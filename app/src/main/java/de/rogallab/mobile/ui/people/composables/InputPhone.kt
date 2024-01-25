package de.rogallab.mobile.ui.people.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.rogallab.mobile.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputPhone(
   phone: String?,                           // State ↓
   onPhoneChange: (String) -> Unit,          // Event ↑
) {
// val tag = "ok>InputNameMailPhone ."
   val focusManager = LocalFocusManager.current
   val keyboardController = LocalSoftwareKeyboardController.current

   val label = stringResource(R.string.phone)
   val errorMessage = stringResource(R.string.errorPhone)

   var isError by rememberSaveable { mutableStateOf(false) }
   var isFocus by rememberSaveable { mutableStateOf(false) }
   var errorText by rememberSaveable { mutableStateOf("") }

   OutlinedTextField(
      modifier = Modifier
         .padding(horizontal = 8.dp)
         .fillMaxWidth()
         .onFocusChanged { focusState ->
            if (!focusState.isFocused && isFocus && validatePhone(phone)) {
               isError = true
               errorText = errorMessage
            } else {
               isError = false
               errorText = ""
            }
            isFocus = focusState.isFocused
         },
      value = phone ?: "",
      onValueChange = { onPhoneChange(it) }, // Event ↑
      label = { Text(text = label) },
      textStyle = MaterialTheme.typography.bodyLarge,
      leadingIcon = {
         Icon(
            imageVector = Icons.Outlined.Phone,
            contentDescription = label)
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(
         keyboardType = KeyboardType.Phone,
         imeAction = ImeAction.Done
      ),
      // check when keyboard action is clicked
      keyboardActions = KeyboardActions(
         onDone = {
            keyboardController?.hide()
            if (validatePhone(phone)) {
               isError = true
               errorText = errorMessage
            } else {
               isError = false
               errorText = ""
            }
            if(!isError) {
               focusManager.clearFocus()
            } // close keyboard
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

fun validatePhone(phone: String?): Boolean =
   phone?.let {
      !android.util.Patterns.PHONE.matcher(it).matches()
   } ?: true