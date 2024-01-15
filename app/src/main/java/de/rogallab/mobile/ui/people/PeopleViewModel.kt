package de.rogallab.mobile.ui.people

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.IPeopleUseCases
import de.rogallab.mobile.domain.IWorkordersRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.composables.handled
import de.rogallab.mobile.ui.composables.trigger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PeopleViewModel @Inject constructor(
   private val _useCases: IPeopleUseCases,
   private val _repository: IPeopleRepository,
   private val _workordersRepository: IWorkordersRepository,
   dispatcher: CoroutineDispatcher
) : ViewModel() {

   var isDetail: Boolean = false
   var dbChanged: Boolean = false

   private var _id: UUID = UUID.randomUUID()
   val id
      get() = _id

   // State = Observables (DataBinding)
   private var _firstName: String by mutableStateOf("")
   val firstName: String
      get() = _firstName
   fun onFirstNameChange(value: String) {
      if (value != _firstName) _firstName = value
   }

   private var _lastName: String by mutableStateOf("")
   val lastName: String
      get() = _lastName
   fun onLastNameChange(value: String) {
      if (value != _lastName) _lastName = value
   }

   private var _email: String? by mutableStateOf(null)
   val email: String?
      get() = _email
   fun onEmailChange(value: String) {
      if (value != _email) _email = value
   }

   private var _phone: String? by mutableStateOf(null)
   val phone: String?
      get() = _phone
   fun onPhoneChange(value: String) {
      if (value != _phone) _phone = value
   }

   private var _imagePath: String? by mutableStateOf(null)
   val imagePath: String?
      get() = _imagePath
   fun onImagePathChange(value: String?) {
      if (value != _imagePath) _imagePath = value
   }

   private var _person: Person? = null

   // Coroutine ExceptionHandler
   private val _exceptionHandler = CoroutineExceptionHandler { _, exception ->
      exception.localizedMessage?.let { message ->
         logError(tag, message)
         triggerErrorEvent(message = message, up = false, back = false)
      } ?: run {
         exception.stackTrace.forEach {
            logError(tag, it.toString())
         }
      }
   }
   // Coroutine Context
   private val _coroutineContext = SupervisorJob() + dispatcher + _exceptionHandler
   // Coroutine Scope
   private val _coroutineScope = CoroutineScope(_coroutineContext)

   override fun onCleared() {
      // cancel all coroutines, when lifecycle of the viewmodel ends
      logDebug(tag, "Cancel all child coroutines")
      _coroutineContext.cancelChildren()
      _coroutineContext.cancel()
   }

   // StateFlow for List Screens
   val uiStateListFlow: StateFlow<UiState<List<Person>>> = flow {
      _useCases.readPeople().collect { it ->
         emit(it)
      }
   }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(1_000),
      UiState.Empty
   )

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
   // error handling
   fun onErrorAction() {
      logDebug(tag, "onErrorAction()")
      // toDo
   }


   // StateFlow for List Screens
   private var _stateFlowPeopleUi: MutableStateFlow<PeopleListUiState> =
      MutableStateFlow(value = PeopleListUiState())
   val stateFlowPeople: StateFlow<PeopleListUiState>
      get() = _stateFlowPeopleUi

   private var _statePeople: PeopleListUiState by mutableStateOf(value = PeopleListUiState())

   val peopleFlow: StateFlow<PeopleListUiState> = flow {
      _useCases.getPeople().collect { result: ResultData<List<Person>> ->
         when (result) {
            is ResultData.Loading -> {
               _statePeople = _statePeople.copy(isLoading = true)
               emit(_statePeople)
            }
            is ResultData.Success -> {
               val people: List<Person> = result.data
               _statePeople = _statePeople.copy(people = people)
               logDebug(tag, "uiflow: ${people.size}")
               emit(_statePeople)
            }

            is ResultData.Failure -> {
               _statePeople = _statePeople.copy(error = result.errorMessageOrNull()
                  ?: "Unknown error")
               emit(_statePeople)
            }
         }
      }
   }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(1_000),
      PeopleListUiState()
   )

   fun readById(id: UUID) {
      _coroutineScope.launch {
         when (val result = _repository.findById(id)) {
            is ResultData.Success -> {
               result.data?.let { person: Person ->
                  setStateFromPerson(person)
                  dbChanged = false
               }
            }
            is ResultData.Failure -> {
               val message = result.errorMessageOrNull() ?: "Unknown error"
               logError(tag, message)
               triggerErrorEvent(message = message, up = false, back = true)
            }
            else -> Unit
         }
      }
   }

   fun add(p: Person? = null) {
      val person = p ?: getPersonFromState()
      _coroutineScope.launch {
//       val resource = _peopleRepository.post(person)
         when (val result = _repository.add(person)) {
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

   fun update(id: UUID) {
      val person = getPersonFromState(id)
      _coroutineScope.launch {
//       val result = _peopleRepository.put(person)
         when (val result: ResultData<Unit> = _repository.update(person)) {
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

   fun remove(id: UUID) {
      val person = getPersonFromState(id)
      _coroutineScope.launch {
         when(val result: ResultData<Unit> = _repository.remove(person)) {
            is ResultData.Success -> {
               dbChanged = true
            }
            is ResultData.Failure -> {
               val message = result.errorMessageOrNull() ?: "unknown error"
               triggerErrorEvent(message = message, up = false, back = true)
            }
            else -> Unit
         }
      }
   }

   // workorder for the actual person
   private var _workorders: MutableList<Workorder> by mutableStateOf(value = mutableListOf<Workorder>())
   val workorders = _workorders

   fun readByIdWithWorkorders(id: UUID) {
      _coroutineScope.launch {
         when (val result = _repository.selectByIdWithWorkorders(id)) {
            is ResultData.Success -> {
               result.data?.let { person: Person ->
                  setStateFromPerson(person)
                  _person = person
                  dbChanged = false
               }
            }
            is ResultData.Failure -> {
               val message = result.errorMessageOrNull() ?: "Unknown error"
               logError(tag, message)
               triggerErrorEvent(message = message, up = false, back = true)
            }
            else -> Unit
         }
      }
   }

   // add workorder to person and change state to WorkState.Assigned
   fun addWorkorder(workorder: Workorder) {

      _person?.addWorkorder(workorder)

      _coroutineScope.launch {
         when (val result = _workordersRepository.update(workorder)) {
            is ResultData.Failure -> {
               val message = result.errorMessageOrNull() ?: "Unknown error"
               logError(tag, message)
               triggerErrorEvent(message = message, up = false, back = true)
            }
            else -> Unit
         }
      }
   }

   // remove workorder from person and change state to WorkState.Default
   fun removeWorkorder(workorder: Workorder) {

      _person?.removeWorkorder(workorder)

      _coroutineScope.launch {
         when (val result = _workordersRepository.update(workorder)) {
            is ResultData.Failure -> {
               val message = result.errorMessageOrNull() ?: "Unknown error"
               logError(tag, message)
               triggerErrorEvent(message = message, up = false, back = true)
            }
            else -> Unit
         }
      }
   }

   private fun getPersonFromState(id: UUID? = null): Person =
      id?.let {
         return@let Person(_firstName, _lastName, _email, _phone, _imagePath, id, _workorders)
      } ?: run {
         return@run Person(_firstName, _lastName, _email, _phone, _imagePath, _id, _workorders)
      }

   private fun setStateFromPerson(person: Person) {
      _firstName = person.firstName
      _lastName = person.lastName
      _email = person.email
      _phone = person.phone
      _imagePath = person.imagePath
      _id = person.id
      _workorders = person.workorders
   }

   fun clearState() {
      logDebug(tag, "clearState")
      _firstName = ""
      _lastName = ""
      _email = null
      _phone = null
      _imagePath = null
      _id = UUID.randomUUID()
      _person = null
      _workorders = mutableListOf()
   }

   companion object {
      private const val tag = "ok>PeopleViewModel    ."
   }
}