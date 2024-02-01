package de.rogallab.mobile.ui.people

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rogallab.mobile.data.io.deleteFileOnInternalStorage
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.IPeopleUseCases
import de.rogallab.mobile.domain.ImagesRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.resources.PeopleErrorMessages
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.base.ErrorParams
import de.rogallab.mobile.ui.base.ErrorState
import de.rogallab.mobile.ui.base.NavState
import de.rogallab.mobile.ui.navigation.NavScreen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PeopleViewModel @Inject constructor(
   private val _useCases: IPeopleUseCases,
   private val _repository: IPeopleRepository,
   private val _imagesRepository: ImagesRepository,
   val errorMessages: PeopleErrorMessages,
//   private val _savedStateHandle: SavedStateHandle,
   private val _dispatcher: CoroutineDispatcher
) : ViewModel() {

   var dbChanged: Boolean = false
   var isWebservice: Boolean = false
   
   // Observer (DataBinding), Observable is a Person object
   private val _personState: MutableState<Person> =  mutableStateOf(Person())
   // access to the observable
   val personStateValue: Person
      get() = _personState.value  // Observable (DataBinding)

   fun onPersonUiEventChange(event: PersonUiEvent, value: Any) {
      _personState.value = when (event) {
         PersonUiEvent.Id        -> personStateValue.copy(id        = value as UUID)
         PersonUiEvent.FirstName -> personStateValue.copy(firstName = value as String)
         PersonUiEvent.LastName  -> personStateValue.copy(lastName  = value as String)
         PersonUiEvent.Email     -> personStateValue.copy(email     = value as String?)
         PersonUiEvent.Phone     -> personStateValue.copy(phone     = value as String?)
         PersonUiEvent.ImagePath -> personStateValue.copy(imagePath = value as String?)
      }
   }

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
   //
   // Navigation State = ViewModel (one time) UI event
   //
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
   //
   // Error State = ViewModel (one time) events
   //
   // https://developer.android.com/topic/architecture/ui-layer/events#handle-viewmodel-events
   private val _errorState: MutableState<ErrorState> 
      = mutableStateOf(ErrorState(onErrorHandled = ::onErrorEventHandled))
   val errorState: ErrorState
      get() = _errorState.value

   fun showOnFailure(throwable: Throwable) =
      showOnError(throwable.localizedMessage ?: "Unknown error")
   fun showOnError(errorMessage: String) {
      logError(tag, errorMessage)
      _errorState.value = errorState.copy(
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
      _errorState.value = errorState.copy(
         errorParams = ErrorParams(
            message = errorMessage,
            isNavigation = true,
            route = NavScreen.PeopleList.route
         )
      )
   }
   fun onErrorEventHandled() {
      logDebug(tag, "onErrorEventHandled()")
      _errorState.value = errorState.copy(errorParams = null)
   }
   // error handling
   fun onErrorAction() {
      logDebug(tag, "onErrorAction()")
      // toDo
   }

   // State for PeopleList from local database
   // private var _savedState: StateFlow<PeopleUiState> = _savedStateHandle.getStateFlow("peopleState", PeopleUiState())
   private var _statePeopleValueDb: PeopleUiState by mutableStateOf(PeopleUiState())
   private val _stateFlowPeopleDb: StateFlow<PeopleUiState> = fetchPeopleFromDatabase()

   // fetch data from local database
   private fun fetchPeopleFromDatabase(): StateFlow<PeopleUiState> = flow {
      logDebug(tag, "fetchPeoepleFromDatabase" )
      _useCases.selectPeople().collect { result: ResultData<List<Person>> ->
         when (result) {
            is ResultData.Loading -> {
               _statePeopleValueDb = _statePeopleValueDb
                  .copy(isLoading = true, isSuccessful = false)
               emit(_statePeopleValueDb)
            }
            is ResultData.Success -> {
               _statePeopleValueDb = _statePeopleValueDb
                  .copy(isLoading = false, isSuccessful = true, people = result.data)
               emit(_statePeopleValueDb)
            }
            is ResultData.Failure -> showOnFailure(result.throwable)
         }
      }
   }  .catch { showOnFailure(it) }.flowOn(_dispatcher)
      .stateIn(
         viewModelScope,
         SharingStarted.WhileSubscribed(1_000),
         PeopleUiState()
      )
   // refresh
   fun refreshPeopleFromDatabase() {
      _statePeopleValueDb = PeopleUiState() // Reset the state
      fetchPeopleFromDatabase() // Re-invoke the flow
   }
   
   // fetch data from remote webservice
   private var _statePeopleValueWeb: PeopleUiState by mutableStateOf(PeopleUiState())
   private var _stateFlowPeopleWeb = MutableStateFlow(PeopleUiState( ))

   private fun fetchPeopleFromWeb() {
      if(!isWebservice) return
      _coroutineScope.launch {
         logDebug(tag, "refresh from webservice")
         _useCases.getPeople().collect { result: ResultData<List<Person>> ->
            when (result) {
               is ResultData.Loading -> {
                  _statePeopleValueWeb = _statePeopleValueWeb
                     .copy(isLoading = true, isSuccessful = false)
                  _stateFlowPeopleWeb.update { _statePeopleValueWeb }
               }
               is ResultData.Success -> {
                  _statePeopleValueWeb = _statePeopleValueWeb
                     .copy(isLoading = false, isSuccessful = true, people = result.data)
                  _stateFlowPeopleWeb.update { _statePeopleValueWeb }
               }
               is ResultData.Failure -> showOnFailure(result.throwable)
            }
         }
      }
   }
   // refresh
   fun refreshPeopleFromWeb() {
      _statePeopleValueWeb = PeopleUiState() // Reset the state
      fetchPeopleFromWeb()
   }

   // Combine both StateFlows (i.e. caching strategy is needed for combining)
   val stateFlowPeople: StateFlow<PeopleUiState> = combine(
      _stateFlowPeopleDb,
      _stateFlowPeopleWeb
   ) { stateFromDb: PeopleUiState, stateFromWeb: PeopleUiState ->
      // Here you decide how to combine both states.
      // For instance, you might want to prioritize remote data over local data or vice versa.
      when {
         stateFromWeb.isSuccessful  -> stateFromWeb // remote data is prioritized
         stateFromDb.isSuccessful -> stateFromDb
         else -> PeopleUiState(isLoading = true) // or any other default state
      }
   }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(1_000),
      PeopleUiState()
   )

   fun readById(id: UUID) {
      _coroutineScope.launch {
         logDebug(tag, "readById(${id.as8()}) isWebservice=$isWebservice")
         // image is read by coil from webserver
         var result: ResultData<Person?>
         if(isWebservice) result = _repository.getById(id)
         else             result = _repository.findById(id)
         when (result) {
            is ResultData.Success -> {
               result.data?.let { p: Person -> setStateFromPerson(p) }
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

   fun add(p: Person? = null) {
      if(p != null) setStateFromPerson(p)
      _coroutineScope.launch {
         logDebug(tag, "add() imagePath=${personStateValue.imagePath} " +
            "isWebservice=$isWebservice")
         // upload image to server
         if (isWebservice && personStateValue.imagePath != null) {
            val imagePath: String = personStateValue.imagePath!!
            val resultImage = _imagesRepository.post(imagePath)
            when (resultImage) {
               is ResultData.Success -> {
                  deleteFileOnInternalStorage(imagePath)
                  _personState.value =
                     personStateValue.copy(imagePath = resultImage.data.remoteUriPath)
               }
               is ResultData.Failure -> showAndNavigateBackOnFailure(resultImage.throwable)
               else -> Unit
            }
         }
         // add person to database
         val person = getPersonFromState()
         val result: ResultData<Unit>
         if (isWebservice) result = _repository.post(person)
         else              result = _repository.add(person)
         when (result) {
            is ResultData.Success -> {
               if(isWebservice) fetchPeopleFromWeb()
               else dbChanged = true
               onNavEvent(route = NavScreen.PeopleList.route)
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

   fun update() {
      _coroutineScope.launch {
         // upload image to server
         if (isWebservice && personStateValue.imagePath != null) {
            val imagePath: String = personStateValue.imagePath!!
            logDebug(tag, "update() imagePath=${imagePath} " +
               "isWebservice=$isWebservice")
            _personState.value = personStateValue.copy(imagePath = imagePath)
            // we have a new local image to upload
            val resultImage = _imagesRepository.post(imagePath)
            when (resultImage) {
               is ResultData.Success -> {
                  deleteFileOnInternalStorage(imagePath)
                  _personState.value = personStateValue
                     .copy(imagePath = resultImage.data.remoteUriPath)
               }
               is ResultData.Failure -> showAndNavigateBackOnFailure(resultImage.throwable)
               else -> Unit
            }
         }
         // update person in database
         val person = getPersonFromState()
         var result: ResultData<Unit>
         if(isWebservice) result = _repository.put(person)
         else             result = _repository.update(person)
         when (result) {
            is ResultData.Success -> {
               if(isWebservice) fetchPeopleFromWeb()
               else dbChanged = true
               onNavEvent(route = NavScreen.PeopleList.route)
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

   fun remove(id: UUID) {
      _coroutineScope.launch {
         logDebug(tag, "remove(${id.as8()})")
         //if (isWebservice && person.imagePath != null) {
            //val resultImage = _imagesRepository.delete(person.imagePath!!)
            //when (resultImage) {
            //   is ResultData.Failure -> showAndNavigateBackOnFailure(resultImage.throwable)
            //   else -> Unit
            //}
         //}
         var result: ResultData<Unit>
         if(isWebservice) result = _repository.delete(id)
         else             result = _repository.remove(id)
         when (result) {
            is ResultData.Success -> {
               if(isWebservice) fetchPeopleFromWeb()
               else dbChanged = true
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

   fun readByIdWithWorkorders(id: UUID) {
      _coroutineScope.launch {
         logDebug(tag, "readByIdWithWorkorders(${id.as8()}) isWebservice=$isWebservice")

         var result: ResultData<Person?>
         if(isWebservice) result = _repository.getByIdWithWorkorders(id)
         else             result = _repository.findByIdWithWorkorders(id)
         when (result) {
            is ResultData.Success -> {
               result.data?.let { person: Person ->
                  setStateFromPerson(person)
               }
               //onNavEvent(route = NavScreen.PeopleList.route)
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

   fun assign(workorder: Workorder) {
      personStateValue.addWorkorder(workorder)
   }

   fun unassign(workorder: Workorder) {
      personStateValue.removeWorkorder(workorder)
   }

   fun clearState() {
      logDebug(tag, "clearState")
      _personState.value = Person()
      onNavEvent(route = NavScreen.PersonInput.route, clearBackStack = false)
   }

   private fun getPersonFromState(): Person =
      Person(
         firstName     = personStateValue.firstName,
         lastName      = personStateValue.lastName,
         email         = personStateValue.email,
         phone         = personStateValue.phone,
         imagePath     = personStateValue.imagePath,
         id            = personStateValue.id,
         imageId       = personStateValue.imageId,
      )

   private fun setStateFromPerson(person: Person) {
      _personState.value = personStateValue.copy(
         firstName  = person.firstName,
         lastName   = person.lastName,
         email         = person.email,
         phone         = person.phone,
         imagePath     = person.imagePath,
         id            = person.id,
         imageId       = person.imageId,
         )
   }


   companion object {
      private const val tag = "ok>PeopleViewModel    ."
   }
}