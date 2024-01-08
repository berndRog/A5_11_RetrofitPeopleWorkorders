package de.rogallab.mobile.domain.usecases.people

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import de.rogallab.mobile.domain.IWorkordersRepository
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.WorkState
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.logError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Remove Workorder from person and update Workorder in database
 */
class WorkorderRemove @Inject constructor(
   private val _workordersRepository: IWorkordersRepository,
   dispatcher: CoroutineDispatcher,
   exceptionHandler: CoroutineExceptionHandler
) {
   private val _coroutineContext = SupervisorJob() + dispatcher + exceptionHandler
   private val _coroutineScope = CoroutineScope(_coroutineContext)

   operator fun invoke(person: Person, workorder: Workorder): State<UiState<Person>> {
      val uiState: MutableState<UiState<Person>> = mutableStateOf(value = UiState.Empty)

      try {
         person.workorders.remove(workorder)
         workorder.state = WorkState.Default
         workorder.personId = null
         workorder.person = null

         _coroutineScope.launch {
            val result = _coroutineScope.async {
               return@async _workordersRepository.update(workorder)
            }.await()
            if (result) {
               uiState.value =  UiState.Success(person)
            } else {
               val message = "Error in UC.RemoveWorkorder()"
               logError(tag, message)
               uiState.value = UiState.Error(message, false, true)
            }
         }
      } catch (e: Exception) {
         val message = e.localizedMessage ?: e.stackTraceToString()
         logError(tag, message)
         uiState.value = UiState.Error(message)
      }
      return uiState

   }

   companion object {       // 12345678901234567890123
      private const val tag = "ok>UC.RemoveWorkorder ."
   }
}
