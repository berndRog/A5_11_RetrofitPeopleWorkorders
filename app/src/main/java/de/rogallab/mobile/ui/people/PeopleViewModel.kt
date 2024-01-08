package de.rogallab.mobile.ui.people

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.IPeopleUseCases
import de.rogallab.mobile.domain.Resource
import de.rogallab.mobile.domain.StateUiScreen
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PeopleViewModel @Inject constructor(
   private val _useCases: IPeopleUseCases,
   private val _peopleRepository: IPeopleRepository,
   dispatcher: CoroutineDispatcher
) : ViewModel() {

   private var _id: UUID = UUID.randomUUID()
   val id
      get() = _id

   // State = Observables (DataBinding)
   private var _firstName: String by mutableStateOf(value = "")
   val firstName
      get() = _firstName
   fun onFirstNameChange(value: String) {
      if(value != _firstName )  _firstName = value 
   }

   private var _lastName: String by mutableStateOf(value = "")
   val lastName
      get() = _lastName
   fun onLastNameChange(value: String) {
      if(value != _lastName )  _lastName = value
   }

   private var _email: String? by mutableStateOf(value = null)
   val email
      get() = _email
   fun onEmailChange(value: String) {
      if(value != _email )  _email = value
   }

   private var _phone: String? by mutableStateOf(value = null)
   val phone
      get() = _phone
   fun onPhoneChange(value: String) {
      if(value != _phone )  _phone = value
   }

   private var _imagePath: String? by mutableStateOf(value = null)
   val imagePath
      get() = _imagePath
   fun onImagePathChange(value: String?) {
      if(value != _imagePath )  _imagePath = value
   }

   private var _workorders: MutableList<Workorder> by mutableStateOf(value = mutableListOf<Workorder>())
   val workorders
      get() = _workorders

   fun addWorkorder(workorder: Workorder) {
      try {
         val person = getPersonFromState(id)
         _useCases.workorderAdd(person, workorder)
      } catch(e:Exception) {
         val message = e.localizedMessage ?: e.stackTraceToString()
         logError(tag, message)
         _uiStatePersonFlow.value = UiState.Error(message, false, true)
      }
   }
   fun removeWorkorder(workorder: Workorder) {
      try {
         val person = getPersonFromState(id)
         _useCases.workorderRemove(person, workorder)
      } catch(e:Exception) {
         val message = e.localizedMessage ?: e.stackTraceToString()
         logError(tag, message)
         _uiStatePersonFlow.value = UiState.Error(message, false, true)
      }
   }

   private var _dbChanged = false
   val dbChanged
      get() = _dbChanged
   fun onDbChanged(value: Boolean) {
      _dbChanged = value
   }

   // error handling
   fun onErrorAction() {
      logDebug(tag, "onErrorAction()")
      // toDo
   }

   // Coroutine ExceptionHandler
   private val _exceptionHandler = CoroutineExceptionHandler { _, exception ->
      exception.localizedMessage?.let {
         logError(tag, it)
         _uiStatePersonFlow.value = UiState.Error(it, true)
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
      logDebug(tag,"Cancel all child coroutines")
      _coroutineContext.cancelChildren()
      _coroutineContext.cancel()
   }

   // mutableStateList with observer
   // var snapShotPeople: SnapshotStateList<Person> = mutableStateListOf<Person>()

   // StateFlow for Input&Detail Screens
   private var _uiStatePersonFlow: MutableStateFlow<UiState<Person>> =
      MutableStateFlow(value = UiState.Empty)
   val uiStatePersonFlow: StateFlow<UiState<Person>>
      get() = _uiStatePersonFlow
   fun onUiStatePersonFlowChange(uiState: UiState<Person>) {
      _uiStatePersonFlow.value = uiState
      if(uiState is UiState.Error) {
         logError(tag,uiState.message)
      }
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

   var state: StateUiScreen<List<Person>> by mutableStateOf(value = StateUiScreen<List<Person>>())

   val uiflow: StateFlow<StateUiScreen<List<Person>>> = flow<StateUiScreen<List<Person>>> {
      _useCases.getPeople().collect { result: Resource<List<Person>> ->
         when (result) {
            is Resource.Loading -> {
               state = state.copy(isLoading = true)
               emit(state)
            }
            is Resource.Success -> {
               result.data?.let { people: List<Person> ->
                  state = state.copy(data = people)
                  emit(state)
               }
            }
            else -> {

            }
         }
      }
   }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(1_000),
      StateUiScreen()
   )

   fun readById(id: UUID) {
      _coroutineScope.launch {
         when (val result = _peopleRepository.findById(id)) {
            is Resource.Success -> {
               result.data?.let { person: Person ->
                  setStateFromPerson(person)
                  _dbChanged = false
                  _uiStatePersonFlow.value = UiState.Success(person)
               }
            }
            is Resource.Error -> {
               _uiStatePersonFlow.value = UiState.Error(result.message!!, true)
            }
            else -> Unit
         }
      }
   }

   fun readByIdWithWorkorders(id:UUID) {

         //_peopleRepository.selectByIdWithWorkorders(id)

   }

   fun add(p: Person? = null) {
      val person = p ?: getPersonFromState()
      _coroutineScope.launch {
        val resource = _peopleRepository.add(person)
//      val resource = _peopleRepository.post(person)
         when(resource){
            is Resource.Success -> {
               _dbChanged = true
            }
            is Resource.Error -> {
               state = state.copy(error = resource.message!!)
            }
            else -> Unit
         }
      }
   }

   fun update(id:UUID) {
      val person = getPersonFromState(id)
      _coroutineScope.launch {
         val resource: Resource<Unit> = _peopleRepository.update(person)
//       val resource: Resource<Unit> = _peopleRepository.put(person)
         when(resource) {
            is Resource.Success -> {
               _dbChanged = true
            }
            is Resource.Error -> {
               state = state.copy(error = resource.message!!)
            }
            else -> Unit
         }
      }
   }

   fun remove(id:UUID) {
      _coroutineScope.launch {
         val person = getPersonFromState(id)
         val resource: Resource<Unit> = _peopleRepository.remove(person)
         when(resource) {
            is Resource.Success -> {
               _dbChanged = true
            }
            is Resource.Error -> {
               state = state.copy(error = resource.message!!)
            }
            else -> Unit
         }
      }
   }

   private fun getPersonFromState(id:UUID? = null): Person =
      id?.let {
         return@let Person(_firstName, _lastName, _email, _phone, _imagePath, id, _workorders)
      } ?: run {
         return@run Person(_firstName, _lastName, _email, _phone, _imagePath, _id, _workorders)
      }

   private fun setStateFromPerson(person: Person) {
      _firstName  = person.firstName
      _lastName   = person.lastName
      _email      = person.email
      _phone      = person.phone
      _imagePath  = person.imagePath
      _id         = person.id
      _workorders = person.workorders
   }

   fun clearState() {
      logDebug(tag, "clearState")
      _firstName  = ""
      _lastName   = ""
      _email      = null
      _phone      = null
      _imagePath  = null
      _id         = UUID.randomUUID()
      _workorders = mutableListOf()
   }

   companion object {
      private const val tag = "ok>PeopleViewModel    ."
   }
}


data class PeopleListState(
   val people: List<Person> = emptyList(),
   val isLoading: Boolean = false,
   val error: String = ""
)

data class PersonDetailState(
   val person: Person = Person(),
   val isLoading: Boolean = false,
   val error: String = ""
)