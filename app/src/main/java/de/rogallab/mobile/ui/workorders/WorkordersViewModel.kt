package de.rogallab.mobile.ui.workorders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rogallab.mobile.domain.IWorkordersRepository
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
   val created: ZonedDateTime = _created
   fun onCreatedChange(value: ZonedDateTime) {
      if(value != _created) _created = value
   }

   private var _title: String by mutableStateOf(value = "")
   val title: String = _title
   fun onTitleChange(value: String) {
      if(value != _title) _title = value
   }

   private var _description: String by mutableStateOf(value = "")
   val description: String = _description
   fun onDescriptionChange(value: String) {
      if(value != _description) _description = value
   }

   private var _started: ZonedDateTime by mutableStateOf(zonedDateTimeNow())
   val started: ZonedDateTime = _started
   fun onStartedChange(value: ZonedDateTime) {
      _state = WorkState.Started
      _started = value
   }

   private var _completed: ZonedDateTime by mutableStateOf(zonedDateTimeNow())
   val completed: ZonedDateTime = _completed
   fun onCompletedChange(value: ZonedDateTime) {
      _state = WorkState.Completed
      _completed = value
      _duration = Duration.between( _started.toInstant() ,
         _completed.toInstant() )
   }

   private var _state: WorkState by mutableStateOf(WorkState.Default)
   val state = _state

   private var _duration: Duration = Duration.ZERO

   private var _remark: String by mutableStateOf(value = "")
   val remark: String = _remark
   fun onRemarkChange(value: String) {
      if(value != _remark) _remark = value
   }

   private var _imagePath: String? by mutableStateOf(value = null)
   val imagePath = _imagePath
   fun onImagePathChange(value: String?) {
      if(value != _imagePath )  _imagePath = value
   }

   private var _assignedPerson: Person? = null
   val assignedPerson = _assignedPerson

   // Coroutine ExceptionHandler
   private val _exceptionHandler = CoroutineExceptionHandler { _, exception ->
      exception.localizedMessage?.let {
         logError(tag, it)
         _uiStateWorkordereFlow.value = UiState.Error(it, true)
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
      logDebug(tag,"Cancel all child coroutines")
      _coroutineContext.cancelChildren()
      _coroutineContext.cancel()
   }

   // StateFlow for Input&Detail Screens
   private var _uiStateWorkordereFlow: MutableStateFlow<UiState<Workorder>> =
      MutableStateFlow(value = UiState.Empty)
   val uiStateWorkorderFlow: StateFlow<UiState<Workorder>>
      get() = _uiStateWorkordereFlow
   fun onUiStateWorkorderFlowChange(uiState: UiState<Workorder>) {
      _uiStateWorkordereFlow.value = uiState
      if(uiState is UiState.Error) {
         logError(tag,uiState.message)
      }
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
      _repository.readAll().collect{ workorders: List<Workorder> ->
         logDebug(tag, "readAll() ${workorders.size}")
         emit(UiState.Success(workorders))
      }
   }.flowOn(_dispatcher+_exceptionHandler)

   fun readById(id: UUID)  {
      logDebug(tag, "readById()")
      try {
         _coroutineScope.launch {
            val workorder = _coroutineScope.async {
               return@async _repository.findById(id)
            }.await()
            workorder?.let{ it:Workorder ->
               setStateFromWorkorder(it)
               logDebug(tag, "readById() ${workorder.asString()}")
               _uiStateWorkordereFlow.value = UiState.Empty  // no return neeeded
               dbChanged = false
            } ?: run {
               throw Exception("Person with given id not found")
            }
         }
      }
      catch (e: Exception) {
         val message = e.localizedMessage ?: e.stackTraceToString()
         logError(tag,message)
         _uiStateWorkordereFlow.value =  UiState.Error(message)
      }
   }

   fun readByIdWithPerson(id: UUID)  {
      logDebug(tag, "readByIdWithPerson()")
      try {
         _coroutineScope.launch {
            val map: Map<Workorder, Person?> = _coroutineScope.async {
               return@async _repository.findByIdWithPerson(id)
            }.await()
            val workorder = map.keys.first()
            setStateFromWorkorder(workorder)
            _assignedPerson = map.values.firstOrNull()
            logDebug(tag, "readById() ${workorder.asString()}")
            _uiStateWorkordereFlow.value = UiState.Empty  // no return neeeded
            dbChanged = false
         }
      }
      catch (e: Exception) {
         val message = e.localizedMessage ?: e.stackTraceToString()
         logError(tag,message)
         _uiStateWorkordereFlow.value =  UiState.Error(message)
      }
   }

   fun add(w:Workorder? = null) {
      try {
         val workorder = w ?: getWorkorderFromState()
         _coroutineScope.launch {
            val result = _coroutineScope.async {
               _repository.add(workorder)
            }.await()
            if (result) {
               logVerbose(tag, "add() ${workorder.asString()}")
               _uiStateWorkordereFlow.value = UiState.Empty
               dbChanged = true
            } else {
               val message = "Error in add()"
               logError(tag, message)
               _uiStateWorkordereFlow.value = UiState.Error(message,false,true)
            }
         }
      } catch (e: Exception) {
         val message = e.localizedMessage ?: e.stackTraceToString()
         logError(tag, message)
         _uiStateWorkordereFlow.value = UiState.Error(message, false, true)
      }
   }

   fun update(id:UUID) {
      try {
         val upWorkOrder = getWorkorderFromState(id)
         _coroutineScope.launch {
            val result = _coroutineScope.async {
               _repository.update(upWorkOrder)
            }.await()
            if(result) {
               logVerbose(tag, "update() ${upWorkOrder.asString()}")
               _uiStateWorkordereFlow.value = UiState.Empty
               dbChanged = true
            } else {
               val message = "Error in update()"
               logError(tag, message)
               _uiStateWorkordereFlow.value = UiState.Error(message, false, true)
            }
         }
      } catch (e: Exception) {
         val message = e.localizedMessage ?: e.stackTraceToString()
         logError(tag,message)
         _uiStateWorkordereFlow.value =  UiState.Error(message, false, true)
      }
   }

   fun remove(id:UUID) {
      try {
         _coroutineScope.launch {
            val workorderDto = _coroutineScope.async {
               return@async _repository.findById(id)
            }.await()
            workorderDto?.let{
               val result = _coroutineScope.async {
                  _repository.remove(workorderDto)
               }.await()
               if(result) {
                  logVerbose(tag, "removed() ${workorderDto.asString()}")
                  _uiStateWorkordereFlow.value = UiState.Success(null)
                  dbChanged = true
               } else {
                  val message = "Error in remove()"
                  logError(tag, message)
                  _uiStateWorkordereFlow.value = UiState.Error(message, false, true)
               }
            } ?: run {
               throw Exception("remove(): Workorder with given id not found")
            }
         }
      } catch (e: Exception) {
         val message = e.localizedMessage ?: e.stackTraceToString()
         logError(tag,message)
         _uiStateWorkordereFlow.value =  UiState.Error(message, false, true)
      }
   }

   fun onErrorAction() {
      logDebug(tag, "onErrorAction()")
      // toDo
   }

   fun clearState() {
      _created = zonedDateTimeNow()
      _title =  ""
      _description = ""
      _started =  _created
      _completed = _created
      _state = WorkState.Default
      _remark = ""
      _imagePath = null
      _id = UUID.randomUUID()
      _assignedPerson = null
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

   fun getWorkorderFromState(
      id:UUID? = null
   ): Workorder =
      id?.let {
         return@let Workorder(_title, _description, _created, _started, _completed,
            _duration, _remark, _imagePath, _state, id, _assignedPerson, _assignedPerson?.id)
      } ?: run {
         return@run Workorder(_title, _description, _created, _started, _completed,
            _duration, _remark, _imagePath, _state, _id, _assignedPerson, _assignedPerson?.id)
      }

   companion object {
                             //12345678901234567890123
      private const val tag = "ok>WorkordersViewModel."
   }
}