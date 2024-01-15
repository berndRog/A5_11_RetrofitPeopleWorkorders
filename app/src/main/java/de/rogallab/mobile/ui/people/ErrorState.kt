package de.rogallab.mobile.ui.people

import de.rogallab.mobile.ui.composables.BaseEvent
import de.rogallab.mobile.ui.composables.handled

// One time event
data class ErrorState(
   val errorEvent: BaseEvent<String> = handled(),
   var up: Boolean = true,    // up   navigation = true: default operation
   var back: Boolean = false, // back navigation = true: abort operation
)



