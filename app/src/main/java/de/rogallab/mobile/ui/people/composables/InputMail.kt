package de.rogallab.mobile.ui.people.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Email
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug

@Composable
fun InputMail(
   email: String?,                           // State ↓
   onEmailChange: (String) -> Unit,          // Event ↑
) {
// val tag = "ok>InputNameMailPhone ."

   val label = stringResource(R.string.email)
   val message = stringResource(R.string.errorEmail)
   val context = LocalContext.current
   val focusManager = LocalFocusManager.current

   var isError by rememberSaveable { mutableStateOf(false) }
   var isFocus by rememberSaveable { mutableStateOf(false) }
   var errorText by rememberSaveable { mutableStateOf("") }

   OutlinedTextField(
      modifier = Modifier
         .padding(horizontal = 8.dp)
         .fillMaxWidth()
         .onFocusChanged { focusState ->
            if (!focusState.isFocused && isFocus && validateEmail(email)) {
               isError = true
               errorText = message
            } else {
               isError = false
               errorText = ""
            }
            isFocus = focusState.isFocused
         },
      value = email ?: "",
      onValueChange = { onEmailChange(it) }, // Event ↑
      label = { Text(text = label) },
      textStyle = MaterialTheme.typography.bodyLarge,
      leadingIcon = {
         Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = label
         )
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(
         keyboardType = KeyboardType.Email,
         imeAction = ImeAction.Next
      ),
      // check if keyboard action is clicked
      keyboardActions = KeyboardActions(
         onNext = {
            if (validateEmail(email)) {
               isError = true
               errorText = message
            } else {
               isError = false
               errorText = ""
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
               Icons.Filled.Error,
               contentDescription = errorText,
               tint = MaterialTheme.colorScheme.error
            )
      },
   )

}

fun validateEmail(email: String?): Boolean =
   email?.let {
      ! android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
   } ?: false