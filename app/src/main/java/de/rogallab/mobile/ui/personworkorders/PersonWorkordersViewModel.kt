package de.rogallab.mobile.ui.personworkorders

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rogallab.mobile.AppStart
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.IWorkorderUseCases
import de.rogallab.mobile.domain.IWorkordersRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.WorkState
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.mapping.toPerson
import de.rogallab.mobile.domain.mapping.toWorkorder
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.base.ErrorParams
import de.rogallab.mobile.ui.base.ErrorState
import de.rogallab.mobile.ui.base.NavState
import de.rogallab.mobile.ui.base.getWorkorderFromState
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.workorders.WorkorderUiEvent
import de.rogallab.mobile.ui.workorders.WorkordersUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class PersonWorkordersViewModel @Inject constructor(
   private val _useCases: IWorkorderUseCases,
   private val _peopleRepository: IPeopleRepository,
   private val _workordersRepository: IWorkordersRepository,
   private val _dispatcher: CoroutineDispatcher
) : ViewModel() {

   //region  Observer (DataBinding), Observable is a Person object
   private val _personState: MutableState<Person> =  mutableStateOf(Person())
   // access to the observable
   val personStateValue: Person
      get() = _personState.value  // Observable (DataBinding)
   // no event handling needed
   //endregion

   //region Observer (DataBinding), Observable is a Person object
   private val _workorderState: MutableState<Workorder> =  mutableStateOf(Workorder())
   // access to the observable
   val workorderStateValue: Workorder
      get() = _workorderState.value  // Observable (DataBinding)
   fun onWorkorderUiEventChange(event: WorkorderUiEvent, value: Any) {
      _workorderState.value = when (event) {
         WorkorderUiEvent.Title -> _workorderState.value.copy(title = value as String)
         WorkorderUiEvent.Description -> _workorderState.value.copy(description = value as String)
         WorkorderUiEvent.State -> _workorderState.value.copy(state = value as WorkState)
         WorkorderUiEvent.Started -> _workorderState.value.copy(started = value as ZonedDateTime)
         WorkorderUiEvent.Created -> _workorderState.value.copy(created = value as ZonedDateTime)
         WorkorderUiEvent.Completed -> _workorderState.value.copy(completed = value as ZonedDateTime)
         WorkorderUiEvent.Duration -> _workorderState.value.copy(duration = value as Duration)
         WorkorderUiEvent.Remark -> _workorderState.value.copy(remark = value as String)
         WorkorderUiEvent.ImagePath -> _workorderState.value.copy(imagePath = value as String?)
         WorkorderUiEvent.Id -> _workorderState.value.copy(id = value as UUID)
         WorkorderUiEvent.Person -> _workorderState.value.copy(person = value as Person?)
         WorkorderUiEvent.PersonId -> _workorderState.value.copy(personId = value as UUID?)
      }
   }
   //endregion

   //region Coroutine
   // Coroutine ExceptionHandler
   private val _exceptionHandler = CoroutineExceptionHandler { _, exception ->
      showOnFailure(exception)
      exception.localizedMessage?.let { message ->
         logError(tag, message)
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
   //endregion

   //region Navigation State
   // Navigation State = ViewModel (one time) UI event
   private var _navState: MutableState<NavState> =
      mutableStateOf(NavState(onNavRequestHandled = ::onNavEventHandled))
   val navState: NavState
      get() = _navState.value
   fun onNavEvent(route: String, clearBackStack: Boolean = true) {
      _navState.value = navState.copy(route = route, clearBackStack = clearBackStack)
   }
   fun onNavEventHandled() {
      _navState.value = navState.copy(route = null, clearBackStack = true )
   }
   //endregion

   //region Error State
   // Error State = ViewModel (one time) events
   // https://developer.android.com/topic/architecture/ui-layer/events#handle-viewmodel-events
   private val _errorState: MutableState<ErrorState> 
      = mutableStateOf(ErrorState(onErrorHandled = ::onErrorEventHandled))
   val errorStateValue: ErrorState
      get() = _errorState.value
   fun showOnFailure(throwable: Throwable) =
      when (throwable) {
         is CancellationException -> Unit
         else -> showOnError(throwable.localizedMessage ?: "Unknown error")
      }
   fun showOnError(errorMessage: String) {
      logError(tag, errorMessage)
      _errorState.value = _errorState.value.copy(
         errorParams = ErrorParams(
            message = errorMessage,
            isNavigation = false
         ),
         onErrorHandled = ::onErrorEventHandled
      )
   }
   // show error and navigate back
   fun showAndNavigateBackOnFailure(throwable: Throwable) =
      showAndNavigateBackOnError(throwable.localizedMessage ?: "Unknown error")
   fun showAndNavigateBackOnError(errorMessage: String) {
      logError(tag, errorMessage)
      _errorState.value = _errorState.value.copy(
         errorParams = ErrorParams(
            message = errorMessage,
            isNavigation = true,
            route = NavScreen.PeopleList.route
         )
      )
   }
   fun onErrorEventHandled() {
      logDebug(tag, "onErrorEventHandled()")
      _errorState.value = _errorState.value.copy(errorParams = null)
   }
   // error handling
   fun onErrorAction() {
      logDebug(tag, "onErrorAction()")
      // toDo
   }
   //endregion

   //region Fetch workorders from local database or remote web service
   // trigger for refresh
   private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
   val stateFlowWorkorders: StateFlow<WorkordersUiState> = _refreshTrigger
      .onStart { emit(Unit) } // Emit an initial value to start the flow
      .flatMapLatest {
         flow {
            try {
               var fetch: Flow<ResultData<List<Workorder>>>
               if(AppStart.isWebservice) {
                  logDebug(tag,"fetchWorkordersFromWeb()")
                  fetch = _useCases.getWorkorders()
               }
               else   {
                  logDebug(tag,"fetchWorkordersFromDb()")
                  fetch = _useCases.selectWorkorders()
               }
               fetch.collect() { result: ResultData<List<Workorder>> ->
                  when (result) {
                     is ResultData.Loading -> emit(WorkordersUiState(isLoading = true,
                        isSuccessful = false, workorders = emptyList()))
                     is ResultData.Success -> emit(WorkordersUiState(isLoading = false,
                        isSuccessful = true, workorders = result.data))
                     is ResultData.Failure -> showOnFailure(result.throwable)
                     else -> Unit
                  }
               }
            } catch (e: Throwable) {
               showOnFailure(e)
            }
         }
      }
      .flowOn(_dispatcher)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), WorkordersUiState())

   fun refreshStateFlowWorkorders() {
      logDebug(tag,"refreshStateFlowWorkorders()")
      _refreshTrigger.tryEmit(Unit)
   }
   //endregion

   //region Read person by id with workorders
   fun readPersonByIdWithWorkorders(id: UUID) {
      _coroutineScope.launch {
         logDebug(tag, "readPersonByIdWithWorkorders(${id.as8()}) " +
            "isWebservice=${AppStart.isWebservice}")

         var result: ResultData<Person?>
         if(AppStart.isWebservice) result = _peopleRepository.getByIdWithWorkorders(id)
         else                      result = _peopleRepository.findByIdWithWorkorders(id)
         when (result) {
            is ResultData.Success -> {
               result.data?.let { person: Person ->
                  _personState.value = person.copy()
               }
               //onNavEvent(route = NavScreen.PeopleList.route)
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }
   //endregion

   //region Read workorder by id with assigned person
   fun readWorkorderByIdWithPerson(id: UUID) {
      _coroutineScope.launch {
         logDebug(tag, "readWorkorderByIdWithPerson(${id.as8()}) " +
            "isWebservice=${AppStart.isWebservice}")

         var result: ResultData<Map<Workorder, Person?>>
//         if(AppStart.isWebservice) result = _peopleRepository.getByIdWithWorkorders(id)
//         else                      result = _peopleRepository.findByIdWithWorkorders(id)
         result = _workordersRepository.findByIdWithPerson(id)
         when (result) {
            is ResultData.Success -> {
               result.data?.let { it: Map<Workorder, Person?> ->
                  val workorder: Workorder = it.keys.first()
                  _workorderState.value = workorder.copy()

                  val person: Person? = it.values.first()
                  person?.let{ p ->
                     _personState.value = p.copy() }
               }
               //onNavEvent(route = NavScreen.PeopleList.route)
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }
   //endregion

   //region Assign and unassign workorder
   fun assign(workorder: Workorder) {
      _personState.value.addWorkorder(workorder)
   }

   fun unassign(workorder: Workorder) {
      _personState.value.removeWorkorder(workorder)
   }
   //endregion

   //region Update workorder
   //
   fun update(w: Workorder? = null) {
      val workorder = w ?: getWorkorderFromState(_workorderState.value)
      _coroutineScope.launch {
         logDebug(tag,"update() isWebservice=${AppStart.isWebservice}")
         var result: ResultData<Unit>
         if(AppStart.isWebservice) result = _workordersRepository.put(workorder)
         else                      result = _workordersRepository.update(workorder)
         when (result) {
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

   fun getImagePath(): String? {
      if(_personState.value.imagePath == null && _personState.value.remoteUriPath != null)
         return personStateValue.remoteUriPath
      else if(_personState.value.imagePath != null && _personState.value.remoteUriPath == null)
         return _personState.value.imagePath
      else return null
   }

   companion object {
      private const val tag = "ok>PeopleViewModel    ."
   }
}