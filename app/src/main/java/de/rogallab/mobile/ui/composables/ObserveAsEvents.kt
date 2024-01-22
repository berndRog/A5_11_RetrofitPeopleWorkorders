package de.rogallab.mobile.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


// https://itnext.io/exercises-in-futility-one-time-events-in-android-ddbdd7b5bd1c


@Composable
// function collects a flow of events and calls onEvent for each event.
fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: (T) -> Unit) {
   val lifecycleOwner = LocalLifecycleOwner.current
   LaunchedEffect(flow, lifecycleOwner.lifecycle) {
      lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
         // https://www.youtube.com/watch?v=njchj9d_Lf8   21:30
         // to prevent that events are lost when the composable is recomposed
         withContext(Dispatchers.Main.immediate) {
            flow.collect { event ->
               onEvent(event)
            }
         }
      }
   }
}