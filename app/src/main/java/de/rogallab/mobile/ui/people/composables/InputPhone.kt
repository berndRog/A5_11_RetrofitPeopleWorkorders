package de.rogallab.mobile.ui.people.composables

import android.content.Context
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
import androidx.compose.ui.Modifier
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
import de.rogallab.mobile.ui.people.PeopleViewModel

@Composable
fun InputPhone(
   phone: String?,                           // State ↓
   onPhoneChange: (String) -> Unit,          // Event ↑
) {
// val tag = "ok>InputNameMailPhone ."
   val context = LocalContext.current
   val focusManager = LocalFocusManager.current

   val textPhone = stringResource(R.string.phone)
   val message = stringResource(R.string.errorPhone)

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
               errorText = message
            } else {
               isError = false
               errorText = ""
            }
            isFocus = focusState.isFocused
         },
      value = phone ?: "",
      onValueChange = { onPhoneChange(it) }, // Event ↑
      label = { Text(text = textPhone) },
      textStyle = MaterialTheme.typography.bodyLarge,
      leadingIcon = {
         Icon(
            imageVector = Icons.Outlined.Phone,
            contentDescription = textPhone)
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(
         keyboardType = KeyboardType.Phone,
         imeAction = ImeAction.Done
      ),
      // check when keyboard action is clicked
      keyboardActions = KeyboardActions(
         onDone = {
            if (validatePhone(phone)) {
               isError = true
               errorText = message
            } else {
               isError = false
               errorText = ""
            }
            if(!isError) focusManager.clearFocus() // close keyboard
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