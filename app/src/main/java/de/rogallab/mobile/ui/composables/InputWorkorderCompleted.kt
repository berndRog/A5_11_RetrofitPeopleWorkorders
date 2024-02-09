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
import de.rogallab.mobile.ui.base.NavState
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.workorders.WorkorderUiEvent
import kotlinx.coroutines.delay
import java.time.ZonedDateTime

@Composable
fun InputWorkorderCompleted(
   state: WorkState,                                              // State ↓
   onStateChange: (WorkorderUiEvent, WorkState) -> Unit,          // Event ↑
   completed: ZonedDateTime,                                      // State ↓
   onCompletedChange: (WorkorderUiEvent, ZonedDateTime) -> Unit,  // Event ↑
   onUpdate: () -> Unit,                                          // Event ↑
   onNavEvent: (String, Boolean) -> Unit,                         // Event ↑
   modifier: Modifier = Modifier                                  // State ↓
) {
            //12345678901234567890123
   val tag = "ok>InputWorkOCompleted."

   Column(modifier = modifier) {
      Row(
         modifier = modifier.padding(top = 8.dp),
         horizontalArrangement = Arrangement.Absolute.Right,
         verticalAlignment = Alignment.CenterVertically
      ) {

         // State to hold the completion time
         var actualCompleted: ZonedDateTime by remember { mutableStateOf(completed) }
         // State to control the timer
         var isTimerRunning by remember { mutableStateOf(false) }
         if(state == WorkState.Started) isTimerRunning = true

         LaunchedEffect(isTimerRunning) {
            while (isTimerRunning) {
               delay(1000)
               // update the actualCompleted time
               actualCompleted = zonedDateTimeNow()
            }
         }

         Text(
            text = zonedDateTimeString(actualCompleted),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp).weight(0.6f)
         )
         FilledTonalButton(
            onClick = {
               logDebug(tag,"Completed clicked ${zonedDateTimeString(actualCompleted)}")
               isTimerRunning = false
               onCompletedChange(WorkorderUiEvent.Completed, actualCompleted) // duration is handled too
               onStateChange(WorkorderUiEvent.State, WorkState.Completed)
               onUpdate()                                   // update the workorder
               onNavEvent(NavScreen.PeopleList.route, true) // navigate back to PeopleList
            },
            enabled = state == WorkState.Started,
            modifier = Modifier.padding(end = 4.dp).weight(0.4f)
         ) {
            Text(
               text = "Beenden",
               style = MaterialTheme.typography.bodyMedium,
            )
         }
      }
   }
}