package de.rogallab.mobile.ui.workorders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rogallab.mobile.domain.IWorkordersRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.WorkState
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.domain.utilities.zonedDateTimeNow
import de.rogallab.mobile.ui.composables.handled
import de.rogallab.mobile.ui.composables.trigger
import de.rogallab.mobile.ui.people.ErrorState
import de.rogallab.mobile.ui.people.PeopleViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WorkordersViewModel @Inject constructor(
   private val _repository: IWorkordersRepository,
   private val _dispatcher: CoroutineDispatcher
) : ViewModel() {

   var dbChanged: Boolean = false

   private var _id: UUID = UUID.randomUUID()

   // Observables (DataBinding)
   private var _created: ZonedDateTime by mutableStateOf(zonedDateTimeNow())
   val created: ZonedDateTime
      get() = _created
   fun onCreatedChange(value: ZonedDateTime) {
      if (value != _created) _created = value
   }

   private var _title: String by mutableStateOf(value = "")
   val title: String
      get() = _title
   fun onTitleChange(value: String) {
      if (value != _title) _title = value
   }

   private var _description: String by mutableStateOf(value = "")
   val description: String = _description
   fun onDescriptionChange(value: String) {
      if (value != _description) _description = value
   }

   private var _started: ZonedDateTime by mutableStateOf(zonedDateTimeNow())
   val started: ZonedDateTime
      get () = _started
   fun onStartedChange(value: ZonedDateTime) {
      _state = WorkState.Started
      _started = value
   }

   private var _completed: ZonedDateTime by mutableStateOf(zonedDateTimeNow())
   val completed: ZonedDateTime
      get() = _completed
   fun onCompletedChange(value: ZonedDateTime) {
      _state = WorkState.Completed
      _completed = value
      _duration = Duration.between(_started.toInstant(),
         _completed.toInstant())
   }

   private var _state: WorkState by mutableStateOf(WorkState.Default)
   val state: WorkState
      get() = _state

   private var _duration: Duration = Duration.ZERO

   private var _remark: String by mutableStateOf(value = "")
   val remark: String
      get() = _remark
   fun onRemarkChange(value: String) {
      if (value != _remark) _remark = value
   }

   private var _imagePath: String? by mutableStateOf(value = null)
   val imagePath
      get() = _imagePath
   fun onImagePathChange(value: String?) {
      if (value != _imagePath) _imagePath = value
   }

   private var _assignedPerson: Person? = null
   val assignedPerson: Person?
      get() = _assignedPerson

   // Coroutine ExceptionHandler
   private val _exceptionHandler = CoroutineExceptionHandler { _, exception ->
      exception.localizedMessage?.let {
         logError(tag, it)
         triggerErrorEvent(message = it, up = true, back = false)
      } ?: run {
         exception.stackTrace.forEach {
            logError(tag, it.toString())
         }
      }
   }
   // Coroutine Context
   private val _coroutineContext = SupervisorJob() + _dispatcher + _exceptionHandler
   // Coroutine Scope
   private val _coroutineScope = CoroutineScope(_coroutineContext)

   override fun onCleared() {
      // cancel all coroutines, when lifecycle of the viewmodel ends
      logDebug(tag, "Cancel all child coroutines")
      _coroutineContext.cancelChildren()
      _coroutineContext.cancel()
   }

   // Error State
   private var _stateFlowError: MutableStateFlow<ErrorState> = MutableStateFlow(ErrorState())
   val stateFlowError: StateFlow<ErrorState> = _stateFlowError.asStateFlow()

   fun triggerErrorEvent(message: String, up: Boolean, back: Boolean) {
      _stateFlowError.update { currentState ->
         currentState.copy(errorEvent = trigger(message), up, back)
      }
   }

   fun onErrorEventHandled() {
      _stateFlowError.update { currentState ->
         currentState.copy(errorEvent = handled(), up = true, back = false)
      }
   }

   val uiStateListWorkorderFlow: StateFlow<UiState<List<Workorder>>> =
      readAll()
         .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            UiState.Empty
         )

   private fun readAll(): Flow<UiState.Success<List<Workorder>>> = flow {
      _repository.selectAll().collect { result ->
         when (result) {
            is ResultData.Success -> {
               result.data?.let { it: List<Workorder> ->
                  emit(UiState.Success(it))
               }
            }
            is ResultData.Failure -> {
               val message = result.errorMessageOrNull() ?: "unknown error"
               logError(tag, message)
               triggerErrorEvent(message = message, up = false, back = true)
            }

            else -> Unit
         }
      }
   }.flowOn(_dispatcher)

   fun readById(id: UUID) {
      _coroutineScope.launch {
         when (val result = _repository.findById(id)) {
            is ResultData.Success -> {
               result.data?.let { it: Workorder ->
                  setStateFromWorkorder(it)
                  dbChanged = false
               }
            }

            is ResultData.Failure -> {
               val message = result.errorMessageOrNull() ?: "unknown error"
               logError(tag, message)
               triggerErrorEvent(message = message, up = false, back = true)
            }

            else -> Unit
         }
      }
   }

   fun readByIdWithPerson(id: UUID) {
      _coroutineScope.launch {
         when (val result: ResultData<Map<Workorder, Person?>> =
            _repository.findByIdWithPerson(id)) {
            is ResultData.Success -> {
               val map: Map<Workorder, Person?> = result.data
               val workorder: Workorder = map.keys.first()
               setStateFromWorkorder(workorder)
               _assignedPerson = map.values.firstOrNull()
               logDebug(tag, "readById() ${workorder.asString()}")
               dbChanged = false
            }
            is ResultData.Failure -> {
               val message = result.errorMessageOrNull() ?: "unknown error"
               logError(tag, message)
               triggerErrorEvent(message = message, up = false, back = true)
            }
            else -> Unit
         }
      }
   }

   fun add(w: Workorder? = null) {
      val workorder = w ?: getWorkorderFromState()
      _coroutineScope.launch {
         when (val result = _repository.add(workorder)) {
            is ResultData.Loading -> Unit
            is ResultData.Success -> dbChanged = true
            is ResultData.Failure -> {
               val message = result.errorMessageOrNull() ?: "unknown error"
               logError(tag, message)
               triggerErrorEvent(message = message, up = false, back = true)
            }
         }
      }
   }

   fun update(id: UUID) {
      val upWorkOrder = getWorkorderFromState(id)
      _coroutineScope.launch {
         when (val result = _repository.update(upWorkOrder)) {
            is ResultData.Loading -> Unit
            is ResultData.Success ->  dbChanged = true
            is ResultData.Failure -> {
               val message = result.errorMessageOrNull() ?: "unknown error"
               logError(tag, message)
               triggerErrorEvent(message = message, up = false, back = true)
            }
         }
      }
   }

   fun remove(id: UUID) {
      val workorder = getWorkorderFromState(id)
      _coroutineScope.launch {
         when (val result = _repository.remove(workorder)) {
            is ResultData.Success -> {
               dbChanged = true
            }

            is ResultData.Failure -> {
               val message = result.errorMessageOrNull() ?: "unknown error"
               logError(tag, message)
               triggerErrorEvent(message = message, up = false, back = true)
            }

            else -> Unit
         }
      }
   }

   fun onErrorAction() {
      logDebug(tag, "onErrorAction()")
      // toDo
   }

   fun getWorkorderFromState(
      id: UUID? = null
   ): Workorder =
      id?.let {
         return@let Workorder(_title, _description, _created, _started, _completed,
            _duration, _remark, _imagePath, _state, id, _assignedPerson, _assignedPerson?.id)
      } ?: run {
         return@run Workorder(_title, _description, _created, _started, _completed,
            _duration, _remark, _imagePath, _state, _id, _assignedPerson, _assignedPerson?.id)
      }

   private fun setStateFromWorkorder(
      workOrder: Workorder
   ) {
      _title = workOrder.title
      _description = workOrder.description
      _created = workOrder.created
      _started = workOrder.started
      _completed = workOrder.completed
      _state = workOrder.state
      _duration = workOrder.duration
      _remark = workOrder.remark
      _imagePath = workOrder.imagePath
      _id = workOrder.id
      _assignedPerson = workOrder.person
   }

   fun clearState() {
      _created = zonedDateTimeNow()
      _title = ""
      _description = ""
      _started = _created
      _completed = _created
      _state = WorkState.Default
      _remark = ""
      _imagePath = null
      _id = UUID.randomUUID()
      _assignedPerson = null
   }




   companion object {
      //12345678901234567890123
      private const val tag = "ok>WorkordersViewModel."
   }
}