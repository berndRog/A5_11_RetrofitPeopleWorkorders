package de.rogallab.mobile.ui.composables
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable

// https://github.com/leonard-palm/compose-state-events

@Immutable
sealed interface BaseEvent<out T>
@Immutable
data class TriggerBaseEvent<T>(val param: T) : BaseEvent<T>
@Immutable
object BaseEventHandled : BaseEvent<Nothing>
fun <T> trigger(param: T): BaseEvent<T> = TriggerBaseEvent(param)
fun handled() = BaseEventHandled

@Composable
@NonRestartableComposable
fun <T> EventEffect(
   event: BaseEvent<T>,         // event to observe
   onHandled: () -> Unit,       // action to perform when event is handled
   action: suspend (T) -> Unit, // action to perform when event is triggered
) {
   LaunchedEffect(key1 = event) {
      if (event is TriggerBaseEvent<T>) {
         action(event.param)
         onHandled()
      }
   }
}
@Composable
@NonRestartableComposable
fun <T> NavigationEventEffect(
   event: BaseEvent<T>,         // event to observe
   onHandled: ()  -> Unit,      // action to perform when event is handled
   action: suspend (T) -> Unit, // action to perform when event is triggered
) {
   LaunchedEffect(key1 = event) {
      if (event is TriggerBaseEvent<T>) {
         onHandled()          // order changed!
         action(event.param)  // onProcessed() is called before action(event.param)
      }
   }
}