package de.rogallab.mobile.ui.people.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.zonedDateTimeNow
import de.rogallab.mobile.domain.utilities.zonedDateTimeString
import de.rogallab.mobile.domain.entities.WorkState
import kotlinx.coroutines.delay
import java.time.ZonedDateTime
import androidx.compose.runtime.LaunchedEffect as LaunchedEffect

@Composable
fun InputStarted(
   state: WorkState,                     // State ↓
   started: ZonedDateTime,                    // State ↓
   onStartedChange: (ZonedDateTime) -> Unit,  // Event ↑
   modifier: Modifier = Modifier              // State ↓
) {
            //12345678901234567890123
   val tag = "ok>InputStarted       ."

   var actualStart by rememberSaveable { mutableStateOf(value = zonedDateTimeNow()) }

   Column(modifier = modifier) {

      Row(
         horizontalArrangement = Arrangement.Absolute.Right,
         verticalAlignment = Alignment.CenterVertically
      ) {
         if (state == WorkState.Started)
            actualStart = started
         else
            LaunchedEffect(key1 = actualStart) {
               delay(1000)
               actualStart = zonedDateTimeNow()
            }

         Text(
            text = zonedDateTimeString(actualStart),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
               .padding(start = 4.dp)
               .weight(0.6f)
         )
         FilledTonalButton(
            onClick = {
               onStartedChange(actualStart)  // state = started
               logDebug(tag,"Start clicked ${zonedDateTimeString(actualStart)}")
            },
            enabled = state == WorkState.Assigned,
            modifier = Modifier
               .padding(end = 4.dp)
               .weight(0.4f)
         ) {
            Text(
               text = "Starten",
               style = MaterialTheme.typography.bodyMedium,
            )
         }
      }
   }
}

@Composable
private fun actualZonedDateTime(
   start: ZonedDateTime
): ZonedDateTime? {
   var result = start
   LaunchedEffect(key1 = result) {
      delay(200)
      result = zonedDateTimeNow()
   }
   return result
}