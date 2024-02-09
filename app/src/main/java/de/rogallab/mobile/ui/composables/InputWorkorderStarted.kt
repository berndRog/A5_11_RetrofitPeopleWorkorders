package de.rogallab.mobile.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.rogallab.mobile.domain.entities.WorkState
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.zonedDateTimeNow
import de.rogallab.mobile.domain.utilities.zonedDateTimeString
import de.rogallab.mobile.ui.workorders.WorkorderUiEvent
import kotlinx.coroutines.delay
import java.time.ZonedDateTime

@Composable
fun InputStartWorkorder(
   state: WorkState,                                           // State ↓
   onStateChange: (WorkorderUiEvent, WorkState) -> Unit,       // Event ↑
   started: ZonedDateTime,                                     // State ↓
   onStartedChange: (WorkorderUiEvent, ZonedDateTime) -> Unit, // Event ↑
   onUpdate: () -> Unit,                                       // Event ↑
   modifier: Modifier = Modifier                               // State ↓
) {
   //12345678901234567890123
   val tag = "ok>InputStarted       ."

   Column(modifier = modifier) {
      Row(
         horizontalArrangement = Arrangement.Absolute.Right,
         verticalAlignment = Alignment.CenterVertically
      ) {
         // State to hold the completion time
         var actualStart: ZonedDateTime by remember { mutableStateOf(started) }
         // State to control the timer
         var isTimerRunning by remember { mutableStateOf(false) }
         if(state == WorkState.Assigned) isTimerRunning = true

         LaunchedEffect(isTimerRunning) {
            while (isTimerRunning) {
               delay(1000)
               actualStart = zonedDateTimeNow()
            }
         }

         Text(
            text = zonedDateTimeString(actualStart),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
               .padding(start = 4.dp).weight(0.6f)
         )
         FilledTonalButton(
            onClick = {
               isTimerRunning = false
               onStartedChange(WorkorderUiEvent.Started, actualStart)  // state = started
               onStateChange(WorkorderUiEvent.State, WorkState.Started)
               onUpdate()
               logDebug(tag, "Start clicked ${zonedDateTimeString(actualStart)}")
            },
            enabled = state == WorkState.Assigned,
            modifier = Modifier.padding(end = 4.dp).weight(0.4f)
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