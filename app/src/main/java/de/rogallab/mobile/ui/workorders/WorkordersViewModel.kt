package de.rogallab.mobile.ui.workorders

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
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.zonedDateTimeNow
import de.rogallab.mobile.ui.base.ErrorParams
import de.rogallab.mobile.ui.base.ErrorState
import de.rogallab.mobile.ui.base.NavState
import de.rogallab.mobile.ui.base.getWorkorderFromState
import de.rogallab.mobile.ui.navigation.NavScreen
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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkordersViewModel @Inject constructor(
   private val _useCases: IWorkorderUseCases,
   private val _repository: IWorkordersRepository,
   private val _peopleRepository: IPeopleRepository,
   private val _dispatcher: CoroutineDispatcher
) : ViewModel() {

   //region Observer (DataBinding), Observable is a Workorder object
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
         WorkorderUiEvent.Completed ->  workorderStateValue.copy(completed = value as ZonedDateTime)
         WorkorderUiEvent.Duration -> workorderStateValue.copy(duration = Duration.between(
            workorderStateValue.started.toInstant(),
            workorderStateValue.started.toInstant())
         )
         WorkorderUiEvent.Remark -> workorderStateValue.copy(remark = value as String)
         WorkorderUiEvent.ImagePath -> workorderStateValue.copy(imagePath = value as String?)
         WorkorderUiEvent.Id -> workorderStateValue.copy(id = value as UUID)
         WorkorderUiEvent.Person -> workorderStateValue.copy(person = value as Person?)
         WorkorderUiEvent.PersonId -> workorderStateValue.copy(personId = value as UUID?)
      }
   }
   //endregion

   //region Coroutine
   // Coroutine ExceptionHandler
   private val _exceptionHandler = CoroutineExceptionHandler { _, exception ->
      exception.localizedMessage?.let { message ->
         logError(tag, message)
        // _stateFlowError.update{ it.copy(message = message, isNavigation = false) }
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
   val navStateValue: NavState
      get() =  _navState.value
   fun onNavEvent(route: String, clearBackStack: Boolean = true) {
      _navState.value = navStateValue.copy(route = route, clearBackStack = clearBackStack)
   }
   fun onNavEventHandled() {
      _navState.value = navStateValue.copy(route = null, clearBackStack = true )
   }
   //endregion

   //region Error State
   // Error State = ViewModel (one time) UI event
   private var _errorState: MutableState<ErrorState> =
      mutableStateOf(ErrorState(onErrorHandled = ::onErrorEventHandled))
   val errorStateValue: ErrorState
      get() = _errorState.value
   fun showOnFailure(throwable: Throwable) =
      showOnError(throwable.localizedMessage ?: "Unknown error")
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
      showAndNavigateBackOnFailure(throwable.localizedMessage ?: "Unknown error")
   fun showAndNavigateBackOnFailure(errorMessage: String) {
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
      _errorState.value = errorStateValue.copy(errorParams = null)
   }
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

   //region Fetch workorder by id
   fun readById(id: UUID) {
      _coroutineScope.launch {
         logDebug(tag,"readById(${id.as8()}) isWebservice=${AppStart.isWebservice}")
         var result: ResultData<Workorder?>
         if(AppStart.isWebservice) result = _repository.getById(id)
         else                      result = _repository.findById(id)
         when (result) {
            is ResultData.Success -> {
               result.data?.let { it: Workorder -> setStateFromWorkorder(it) }
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }
   //endregion

   //region Add workorder
   fun add(w: Workorder? = null) {
      val workorder: Workorder = w ?: getWorkorderFromState(workorderStateValue)
      _coroutineScope.launch {
         logDebug(tag,"add() isWebservice=${AppStart.isWebservice}")
         var result: ResultData<Unit>
         if(AppStart.isWebservice) result = _repository.post(workorder)
         else                      result = _repository.add(workorder)
         when (result) {
            is ResultData.Success -> {
               refreshStateFlowWorkorders()
               //onNavEvent(NavScreen.PeopleList.route)
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }
   //endregion

   //region Update workorder
   fun update(w: Workorder? = null, route:String? = null) {
      val workorder = w ?: getWorkorderFromState(workorderStateValue)
      _coroutineScope.launch {
         logDebug(tag,"update() isWebservice=${AppStart.isWebservice}")
         var result: ResultData<Unit>
         if(AppStart.isWebservice) result = _repository.put(workorder)
         else                      result = _repository.update(workorder)
         when (result) {
            is ResultData.Success -> {
               refreshStateFlowWorkorders()
               route?.let { onNavEvent(route) }
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }
   //endregion

   //region Remove workorder by id
   fun remove(id: UUID) {
      _coroutineScope.launch {
         logDebug(tag,"remove() isWebservice=$AppStart.isWebservice")
         var result: ResultData<Unit>
         if(AppStart.isWebservice) result = _repository.delete(id)
         else                      result = _repository.remove(id)
         when (result) {
            is ResultData.Success ->  {
               refreshStateFlowWorkorders()
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }
   //endregion
   /*
   fun readByIdWithPerson(id: UUID) {
      _coroutineScope.launch {
         logDebug(tag,"readByIdWithPerson(${id.as8()}) isWebservice=$isWebservice")
         // read workorder
         var resultWorkorder: ResultData<Workorder?>
         if(isWebservice) resultWorkorder = _repository.getById(id)
         else             resultWorkorder = _repository.findById(id)
         when (resultWorkorder) {
            is ResultData.Failure -> showAndNavigateBackOnFailure(resultWorkorder.throwable)
            is ResultData.Success -> {
               if(resultWorkorder.data == null) {
                  showAndNavigateBackOnFailure("Workorder not found")
                  return@launch
               }
               val workorder: Workorder = resultWorkorder.data!!
               setStateFromWorkorder(workorder)
               workorder.personId?.let { personId ->
                  // read assigned person
                  val resultPerson: ResultData<Person?>
                  if(isWebservice) resultPerson = _peopleRepository.getById(personId)
                  else             resultPerson = _peopleRepository.findById(personId)
                  when (resultPerson) {
                     is ResultData.Success -> {
                        // set assigned person
                       _workorderState.value =
                          workorderStateValue.copy(person = resultPerson.data)
                     }
                     is ResultData.Failure -> showAndNavigateBackOnFailure(resultPerson.throwable)
                     else -> Unit
                  }
               }
            }
            else -> Unit
         }
      }
   }
   */

   fun clearState() {
      logDebug(tag, "clearState")
      _workorderState.value = Workorder()
      _workorderState.value = _workorderState.value.copy(created = zonedDateTimeNow())
      onNavEvent(route = NavScreen.WorkorderInput.route, clearBackStack = false)
   }

   private fun setStateFromWorkorder(workorder: Workorder) {
      _workorderState.value = workorder.copy()
   }

   companion object {
      //12345678901234567890123
      private const val tag = "ok>WorkordersViewModel."
   }
}