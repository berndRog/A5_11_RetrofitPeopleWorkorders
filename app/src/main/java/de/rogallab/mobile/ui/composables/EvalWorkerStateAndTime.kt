package de.rogallab.mobile.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.entities.WorkState
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.zonedDateTimeString

@Composable
fun evalWorkorderStateAndTime(workorder: Workorder): Pair<String, String> {

   val context = LocalContext.current
   var time = ""
   var state = ""

   if (workorder.state == WorkState.Default) {
      state = context.getString(R.string.workstate_default)
      time = zonedDateTimeString(workorder.created)
   } else if (workorder.state == WorkState.Assigned) {
      state = context.getString(R.string.workstate_assigned)
      time = zonedDateTimeString(workorder.created)
   } else if (workorder.state == WorkState.Started) {
      state = context.getString(R.string.workstate_started)
      time = zonedDateTimeString(workorder.started)
   } else if (workorder.state == WorkState.Completed) {
      state = context.getString(R.string.workstate_completed)
      time = zonedDateTimeString(workorder.completed)
   }
   return Pair(state, time)
}