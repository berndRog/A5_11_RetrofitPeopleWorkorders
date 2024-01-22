package de.rogallab.mobile.ui.people

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.IPeopleUseCases
import de.rogallab.mobile.domain.ImagesRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.base.ErrorParams
import de.rogallab.mobile.ui.base.ErrorState
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
//   private val _savedStateHandle: SavedStateHandle,
   private val _dispatcher: CoroutineDispatcher
) : ViewModel() {


   var dbChanged: Boolean = false
   var isWebservice: Boolean = true

   private var _id: UUID = UUID.randomUUID()
   val id
      get() = _id

   fun onIdChange(value: UUID) {
      if (value != _id) _id = value
   }
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

   private var _person: Person? by mutableStateOf(null)
   val person: Person?
      get() = _person
   // Coroutine ExceptionHandler
   private val _exceptionHandler = CoroutineExceptionHandler { _, exception ->
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

   // Error State = ViewModel (one time) events
   // https://developer.android.com/topic/architecture/ui-layer/events#handle-viewmodel-events
   var errorState by mutableStateOf(ErrorState(onErrorHandled = ::onErrorEventHandled))
      private set

   fun showOnFailure(throwable: Throwable) =
      showOnError(throwable.localizedMessage ?: "Unknown error")
   fun showOnError(errorMessage: String) {
      logError(tag, errorMessage)
      errorState = errorState.copy(
         errorParams = ErrorParams(
            message = errorMessage,
            actionLabel = "ok",
            duration = SnackbarDuration.Short,
            withDismissAction = false,
            onDismissAction = {},
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
      errorState = errorState.copy(
         errorParams = ErrorParams(
            message = errorMessage,
            actionLabel = "ok",
            duration = SnackbarDuration.Short,
            withDismissAction = false,
            onDismissAction = {},
            isNavigation = true,
            route = NavScreen.PeopleList.route
         )
      )
   }

   fun onErrorEventHandled() {
      logDebug(tag, "onErrorEventHandled()")
      errorState = errorState.copy(errorParams = null)
   }
   // error handling
   fun onErrorAction() {
      logDebug(tag, "onErrorAction()")
      // toDo
   }

   // State for PeopleList
   // private var _savedState: StateFlow<PeopleUiState> = _savedStateHandle.getStateFlow("peopleState", PeopleUiState())


   private var _statePeople: PeopleUiState by mutableStateOf(PeopleUiState())

   val stateFlowPeople: StateFlow<PeopleUiState> = flow {
      _useCases.fetchPeople(isWebservice).collect { result: ResultData<List<Person>> ->
         when (result) {
            is ResultData.Loading -> {
               _statePeople = _statePeople.loading()
               emit(_statePeople)
            }
            is ResultData.Success -> {
               _statePeople = _statePeople.success(result.data)
               emit(_statePeople)
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
         PeopleUiState()
      )

   fun readById(id: UUID) {
      _coroutineScope.launch {
         var result: ResultData<Person?>
         if(isWebservice) result = _repository.getById(id)
         else             result = _repository.findById(id)

         when (result) {
            is ResultData.Success -> {
               result.data?.let { p: Person ->
                  setStateFromPerson(p)
                  dbChanged = false
               }
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

   fun add(p: Person? = null) {
      val person = p ?: getPersonFromState()
      _coroutineScope.launch {
         val result: ResultData<Unit>
         if (isWebservice) result = _repository.post(person)
         else              result = _repository.add(person)
         when (result) {
            is ResultData.Success -> dbChanged = true
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
         if(isWebservice) {
            person.imagePath?.let{
               _imagesRepository.upload(it)
           }
         }
      }
   }

   fun update(id: UUID) {
      val person = getPersonFromState(id)
      _coroutineScope.launch {
         var result: ResultData<Unit>
         if(isWebservice) result = _repository.put(person)
         else             result = _repository.update(person)
         when (result) {
            is ResultData.Success -> dbChanged = true
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

   fun remove(id: UUID) {
      val person = getPersonFromState(id)
      _coroutineScope.launch {
         var result: ResultData<Unit>
         if(isWebservice) result = _repository.delete(person)
         else             result = _repository.remove(person)
         when (result) {
            is ResultData.Success -> dbChanged = true
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

   fun readByIdWithWorkorders(id: UUID) {
      _coroutineScope.launch {
         when (val result: ResultData<Person?> = _repository.findByIdWithWorkorders(id)) {
            is ResultData.Success -> {
               result.data?.let { person: Person ->
                  setStateFromPerson(person)
                  _person = person
               }
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }

   fun assign(workorder: Workorder) {
      _person?.addWorkorder(workorder)
   }

   fun unassign(workorder: Workorder) {
      _person?.removeWorkorder(workorder)
   }

   private fun getPersonFromState(id: UUID? = null): Person =
      id?.let {
         return@let Person(_firstName, _lastName, _email, _phone, _imagePath, id)
      } ?: run {
         return@run Person(_firstName, _lastName, _email, _phone, _imagePath, _id)
      }

   private fun setStateFromPerson(person: Person) {
      _firstName = person.firstName
      _lastName = person.lastName
      _email = person.email
      _phone = person.phone
      _imagePath = person.imagePath
      _id = person.id
   }

   fun clearState() {
      logDebug(tag, "clearState")
      _firstName = ""
      _lastName = ""
      _email = null
      _phone = null
      _imagePath = null
      _id = UUID.randomUUID()
   }


   fun isValid(context: Context, viewModel: PeopleViewModel): Boolean {

      if (viewModel.firstName.isEmpty() || viewModel.firstName.length < 2) {
         val message = ContextCompat.getString(context, R.string.errorFirstNameTooShort)
         viewModel.showOnError(message)
         return true
      }
      if (viewModel.lastName.isEmpty() || viewModel.lastName.length < 2) {
         val message = ContextCompat.getString(context, R.string.errorLastNameTooShort)
         viewModel.showOnError(message)
         return true
      }
      viewModel.email?.let {
         if (!android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
            val message = ContextCompat.getString(context, R.string.errorEmail)
            viewModel.showAndNavigateBackOnFailure(message)
            return true
         }
      }
      viewModel.phone?.let {
         if (!android.util.Patterns.PHONE.matcher(it).matches()) {
            val message = ContextCompat.getString(context, R.string.errorPhone)
            viewModel.showAndNavigateBackOnFailure(message)
            return true
         }
      }
      return false
   }


   companion object {
      private const val tag = "ok>PeopleViewModel    ."
   }
}