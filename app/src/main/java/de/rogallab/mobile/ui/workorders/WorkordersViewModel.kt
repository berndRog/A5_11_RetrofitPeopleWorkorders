package de.rogallab.mobile.ui.workorders

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.people.PersonUiEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WorkordersViewModel @Inject constructor(
   private val _useCases: IWorkorderUseCases,
   private val _repository: IWorkordersRepository,
   private val _peopleRepository: IPeopleRepository,
   private val _dispatcher: CoroutineDispatcher
) : ViewModel() {

   var isWebservice: Boolean = false

   // Observer (DataBinding), Observable is a Person object
   private val _workorderState: MutableState<Workorder> =  mutableStateOf(Workorder())
   // access to the observable
   val workorderStateValue: Workorder
      get() = _workorderState.value  // Observable (DataBinding)
   fun onWorkorderUiEventChange(event: WorkorderUiEvent, value: Any) {
      _workorderState.value = when (event) {
         WorkorderUiEvent.Title -> workorderStateValue.copy(title = value as String)
         WorkorderUiEvent.Description -> workorderStateValue.copy(description = value as String)
         WorkorderUiEvent.State -> workorderStateValue.copy(state = value as WorkState)
         WorkorderUiEvent.Started -> workorderStateValue.copy(started = value as ZonedDateTime)
         WorkorderUiEvent.Created -> workorderStateValue.copy(created = value as ZonedDateTime)
         WorkorderUiEvent.Completed -> workorderStateValue.copy(completed = value as ZonedDateTime)
         WorkorderUiEvent.Duration -> workorderStateValue.copy(duration = value as Duration)
         WorkorderUiEvent.Remark -> workorderStateValue.copy(remark = value as String)
         WorkorderUiEvent.ImagePath -> workorderStateValue.copy(imagePath = value as String?)
         WorkorderUiEvent.Id -> workorderStateValue.copy(id = value as UUID)
         WorkorderUiEvent.Person -> workorderStateValue.copy(person = value as Person?)
         WorkorderUiEvent.PersonId -> workorderStateValue.copy(personId = value as UUID?)
      }
   }

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
   //
   // Navigation State = ViewModel (one time) UI event
   //
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
   //
   // Error State = ViewModel (one time) UI event
   //
   private var _errorState: MutableState<ErrorState> =
      mutableStateOf(ErrorState(onErrorHandled = ::onErrorEventHandled))
   val errorStateValue: ErrorState
      get() = _errorState.value
   fun showOnFailure(throwable: Throwable) =
      showOnError(throwable.localizedMessage ?: "Unknown error")
   fun showOnError(errorMessage: String) {
      logError(tag, errorMessage)
      _errorState.value = errorStateValue.copy(
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
      _errorState.value = errorStateValue.copy(
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

   // StateFlow for List Screens
   private var _stateWorkordersValueDb: WorkordersUiState by mutableStateOf(WorkordersUiState())
   private val _stateFlowWorkordersDb: StateFlow<WorkordersUiState> = fetchWorkordersFromDb()

   fun fetchWorkordersFromDb(): StateFlow<WorkordersUiState> = flow {
      logDebug(tag,"fetchWorkordersFromDb()")
      _useCases.selectWorkorders().collect { result: ResultData<List<Workorder>> ->
         when (result) {
            is ResultData.Loading -> {
               _stateWorkordersValueDb = _stateWorkordersValueDb.copy(isLoading = true,
                  isSuccessful = false, workorders = emptyList())
               emit(_stateWorkordersValueDb)
            }
            is ResultData.Success -> {
               _stateWorkordersValueDb = _stateWorkordersValueDb.copy(isLoading = false,
                  isSuccessful = true, workorders = result.data)
               emit(_stateWorkordersValueDb)
            }
            is ResultData.Failure -> showOnFailure(result.throwable)
         }
      }
   }  .catch { throwable ->
      showOnFailure(throwable)
   }  .flowOn(_dispatcher)
      .stateIn(
         viewModelScope,
         SharingStarted.WhileSubscribed(1_000),
         WorkordersUiState()
      )
   // refresh
   fun refreshFromDb() {
      _stateWorkordersValueDb = WorkordersUiState() // Reset the state
      fetchWorkordersFromDb() // Re-invoke the flow
   }
   
   private var _stateWorkordersValueWeb: WorkordersUiState by mutableStateOf(WorkordersUiState( ))
   private var _stateFlowWorkordersWeb: MutableStateFlow<WorkordersUiState> = MutableStateFlow(WorkordersUiState())

   fun fetchWorkordersFromWeb(): Flow<WorkordersUiState> = flow {
      if(!isWebservice) emit(WorkordersUiState())
      logDebug(tag, "refreshFromWebservice()")
      _useCases.getWorkorders().collect { result: ResultData<List<Workorder>> ->
         when (result) {
            is ResultData.Loading -> {
               _stateWorkordersValueWeb = _stateWorkordersValueWeb.copy(isLoading = true,
                  isSuccessful = false, workorders = emptyList())
               emit(_stateWorkordersValueWeb)
            }
            is ResultData.Success -> {
               _stateWorkordersValueWeb = _stateWorkordersValueWeb.copy(isLoading = true,
                  isSuccessful = false, workorders = result.data)
               emit(_stateWorkordersValueWeb)
            }
            is ResultData.Failure -> showOnFailure(result.throwable)
         }
      }
   }  .catch { throwable ->
      showOnFailure(throwable)
   }  .flowOn(_dispatcher)
      .stateIn(
         viewModelScope,
         SharingStarted.WhileSubscribed(1_000),
         WorkordersUiState()
      )

   // refresh
   fun refreshWorkordersFromWeb() {
      _stateWorkordersValueWeb = WorkordersUiState() // Reset the state
      fetchWorkordersFromWeb()
   }

   // Combine both StateFlows (i.e. caching strategy is needed for combining)
   val stateFlowWorkorders: StateFlow<WorkordersUiState> = combine(
      _stateFlowWorkordersDb,
      _stateFlowWorkordersWeb
   ) { stateFromDb: WorkordersUiState, stateFromWeb: WorkordersUiState ->
      // Here you decide how to combine both states.
      // For instance, you might want to prioritize remote data over local data or vice versa.
      when {
         stateFromWeb.isSuccessful  -> stateFromWeb // remote data is prioritized
         stateFromDb.isSuccessful -> stateFromDb
         else -> WorkordersUiState(isLoading = true) // or any other default state
      }
   }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(1_000),
      WorkordersUiState()
   )

   fun readById(id: UUID) {
      _coroutineScope.launch {
         logDebug(tag,"readById(${id.as8()}) isWebservice=$isWebservice")
         var result: ResultData<Workorder?>
         if(isWebservice) result = _repository.getById(id)
         else             result = _repository.findById(id)
         when (result) {
            is ResultData.Success -> {
               result.data?.let { it: Workorder -> setStateFromWorkorder(it) }
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

   fun add(w: Workorder? = null) {
      val workorder = w ?: getWorkorderFromState()
      _coroutineScope.launch {
         logDebug(tag,"add() isWebservice=$isWebservice")
         var result: ResultData<Unit>
         if(isWebservice) result = _repository.post(workorder)
         else             result = _repository.add(workorder)
         when (result) {
            is ResultData.Loading -> Unit
            is ResultData.Success -> {
               if(isWebservice) fetchWorkordersFromWeb()
               else refreshFromDb()
               //onNavEvent(NavScreen.PeopleList.route)
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
         }
      }
   }

   fun update(w: Workorder? = null, route:String? = null) {
      val workorder = w ?: getWorkorderFromState()
      _coroutineScope.launch {
         logDebug(tag,"update() isWebservice=$isWebservice")
         var result: ResultData<Unit>
         if(isWebservice) result = _repository.put(workorder)
         else             result = _repository.update(workorder)
         when (result) {
            is ResultData.Loading -> Unit
            is ResultData.Success -> {
               if(isWebservice) fetchWorkordersFromWeb()
               else refreshFromDb()
               route?.let {
                  onNavEvent(route)
               }

            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
         }
      }
   }

   fun remove(id: UUID) {
      _coroutineScope.launch {
         logDebug(tag,"remove() isWebservice=$isWebservice")
         var result: ResultData<Unit>
         if(isWebservice) result = _repository.delete(id)
         else             result = _repository.remove(id)
         when (result) {
            is ResultData.Success ->  {
               if(isWebservice) fetchWorkordersFromWeb()
               else refreshFromDb()
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

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


   fun clearState() {
      logDebug(tag, "clearState")
      _workorderState.value = Workorder()
      onNavEvent(route = NavScreen.WorkorderInput.route, clearBackStack = false)
   }

   private fun getWorkorderFromState(): Workorder =
      Workorder(
         title         = workorderStateValue.title,
         description   = workorderStateValue.description,
         imagePath     = workorderStateValue.imagePath,
         state         = workorderStateValue.state,
         created       = workorderStateValue.created,
         started       = workorderStateValue.started,
         completed     = workorderStateValue.completed,
         duration      = workorderStateValue.duration,
         remark        = workorderStateValue.remark,
         id            = workorderStateValue.id,
         person        = workorderStateValue.person,
         personId      = workorderStateValue.personId
      )

   private fun setStateFromWorkorder(workorder: Workorder) {
      _workorderState.value = workorderStateValue.copy(
         title        = workorder.title,
         description  = workorder.description,
         imagePath    = workorder.imagePath,
         state        = workorder.state,
         created      = workorder.created,
         started      = workorder.started,
         completed    = workorder.completed,
         duration     = workorder.duration,
         remark       = workorder.remark,
         id           = workorder.id,
         person       = workorder.person,
         personId     = workorder.personId
      )
   }

   companion object {
      //12345678901234567890123
      private const val tag = "ok>WorkordersViewModel."
   }
}