package de.rogallab.mobile.ui.people

import android.util.Patterns
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rogallab.mobile.AppStart
import de.rogallab.mobile.data.io.deleteFileOnInternalStorage
import de.rogallab.mobile.data.io.exitsFileOnInternalStorage
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.IPeopleUseCases
import de.rogallab.mobile.domain.ImagesRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Image
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.resources.PeopleErrorMessages
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.getLocalOrRemoteImagePath
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.base.ErrorParams
import de.rogallab.mobile.ui.base.ErrorState
import de.rogallab.mobile.ui.base.NavState
import de.rogallab.mobile.ui.base.getPersonFromState
import de.rogallab.mobile.ui.navigation.NavScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
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
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class PeopleViewModel @Inject constructor(
   private val _useCases: IPeopleUseCases,
   private val _repository: IPeopleRepository,
   private val _imagesRepository: ImagesRepository,
   val errorMessages: PeopleErrorMessages,
//   private val _savedStateHandle: SavedStateHandle,
   private val _dispatcher: CoroutineDispatcher
) : ViewModel() {

   //region Observer (DataBinding), Observable is a Person object
   private val _personState: MutableState<Person> =  mutableStateOf(Person())
   // external access to the observable
   val personStateValue: Person
      get() = _personState.value  // Observable (DataBinding)

   fun onPersonUiEventChange(event: PersonUiEvent, value: Any) {
      _personState.value = when (event) {
         PersonUiEvent.Id            -> _personState.value.copy(id        = value as UUID)
         PersonUiEvent.FirstName     -> _personState.value.copy(firstName = value as String)
         PersonUiEvent.LastName      -> _personState.value.copy(lastName  = value as String)
         PersonUiEvent.Email         -> _personState.value.copy(email     = value as String?)
         PersonUiEvent.Phone         -> _personState.value.copy(phone     = value as String?)
         PersonUiEvent.ImagePath     -> _personState.value.copy(imagePath = value as String?)
         PersonUiEvent.RemoteUriPath -> _personState.value.copy(remoteUriPath = value as String?)

      }
   }
   //endregion

   //region Coroutine handling
   // ExceptionHandler
   private val _exceptionHandler = CoroutineExceptionHandler { _, exception ->
      //showOnFailure(exception)
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

   //region Navigation State = ViewModel (one time) UI event
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

   //region Error State = ViewModel (one time) events
   // https://developer.android.com/topic/architecture/ui-layer/events#handle-viewmodel-events
   private val _errorState: MutableState<ErrorState> 
      = mutableStateOf(ErrorState(onErrorHandled = ::onErrorEventHandled))
   val errorStateValue: ErrorState
      get() = _errorState.value
   fun showOnFailure(throwable: Throwable) {
      when (throwable) {
         is CancellationException -> Unit
         else -> showOnError(throwable.localizedMessage ?: "Unknown error")
      }
   }
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
   // error handling
   fun onErrorAction() {
      logDebug(tag, "onErrorAction()")
      // toDo
   }
   //endregion

   //region Fetch people from local database or remote webservice
   // trigger for refresh
   private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
   val stateFlowPeople: StateFlow<PeopleUiState> = _refreshTrigger
      .onStart { emit(Unit) } // Emit an initial value to start the flow
      // start lastest flow
      .flatMapLatest {
         flow {
            try {
               var fetch: Flow<ResultData<List<Person>>>
               if(AppStart.isWebservice) {
                  logDebug(tag,"fetchPeopleFromWeb()")
                  fetch = _useCases.getPeople()
               }
               else   {
                  logDebug(tag,"fetchPeopleFromDb()")
                  fetch = _useCases.selectPeople()
               }

               fetch.collect() { result: ResultData<List<Person>> ->
                  when (result) {
                     is ResultData.Loading -> {
                        emit(PeopleUiState(isLoading = true,
                           isSuccessful = false, people = emptyList()))
                     }
                     is ResultData.Success -> {
                        emit(PeopleUiState(isLoading = false,
                           isSuccessful = true, people = result.data))
                     }
                     is ResultData.Failure -> {
                        showOnFailure(result.throwable)
                     }
                     else -> Unit
                  }
               }
            } catch (e: CancellationException) {
               // flatMapLatestHandle cancellation specifically
               logVerbose(tag, "${e.localizedMessage}")
            } catch (t: Throwable) {
               showOnFailure(t)
            }
         }
      }
      .flowOn(_dispatcher)
      .stateIn(_coroutineScope, SharingStarted.WhileSubscribed(1000), PeopleUiState())

   fun refreshStateFlowPeople() {
      logDebug(tag,"refreshStateFlowPeople()")
      _refreshTrigger.tryEmit(Unit)
   }
   //endregion

   //region Read a person by id
   fun readById(id: UUID) {
      viewModelScope.launch(_dispatcher) {
         logDebug(tag, "readById(${id.as8()}) isWebservice=${AppStart.isWebservice}")
         // image is read by coil from webserver
         var result: ResultData<Person?>
         if(AppStart.isWebservice) result = _repository.getById(id)
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
   //endregion

   //region Add a person
   fun add(p: Person? = null) {
      if(p != null) setStateFromPerson(p)

      viewModelScope.launch(_dispatcher) {

         if (AppStart.isWebservice) {
            // upload local image to remote web server
            val localImagePath = _personState.value.imagePath
            logDebug(tag, "put imagePath:$localImagePath")

            // is there a new image to upload?
            _personState.value.imagePath?.let { imagePath ->
               if (exitsImage(imagePath)) {
                  // upload image to web server and wait for completion
                  postImage(imagePath).join()
               }
            }
            // post person to remote webservice
            storePerson(getPersonFromState(_personState.value)) { it: Person ->
               _repository.post(it) 
            }
         }
         else {
            // insert person to local database
            storePerson(getPersonFromState(_personState.value)) { it: Person ->
               _repository.add(it)
            }
         }
      }
   }
   //endregion

   //region Update a person
   fun update() {
      viewModelScope.launch(_dispatcher) {
         if(AppStart.isWebservice) {
            // upload new locol image image to web server
            val localImagePath = _personState.value.imagePath
            val remoteUriPath = _personState.value.remoteUriPath
            logDebug(tag, "put imagePath:$localImagePath")
            logDebug(tag, "put remoteUriPath:$remoteUriPath")

            // is there a new image to upload?
            val imageJob: Job? = localImagePath?.let{ imagePath: String ->
               if(exitsImage(imagePath)) {
                  // exists a remote image?
                  when (remoteUriPath) {
                     // no -> post image
                     null, "" -> postImage(imagePath)
                     // yes -> put image, i.e. replace the image on the web server
                     else -> putImage(imagePath, remoteUriPath)
                  }
               } else null
            }
            imageJob?.join() // wait for the image upload

            // post person to remote webservice
            storePerson(getPersonFromState(_personState.value)) { it: Person ->
               _repository.put(it)
            }
         }
         // update person in local database
         else {
            storePerson(getPersonFromState(_personState.value)) { it: Person ->
               _repository.update(it)
            }
         }
      }
   }
   //endregion

   //region Eliminate a person, i.e remove from local database and delete on remote webservice
   fun eliminate(id: UUID) {
      viewModelScope.launch(_dispatcher) {
         logDebug(tag, "remove(${id.as8()})")
         if(AppStart.isWebservice) {
            // webserver is handling the deletion a the imageFile and the image object
            eliminatePerson(id) { it: UUID ->
               _repository.delete(it)
            }
         } else {
            // delete image from local storage
            _personState.value.imagePath?.let {
               deleteFileOnInternalStorage(it)
            }
            eliminatePerson(id) { it: UUID ->
               _repository.remove(it)
            }
         }
      }
   }
   //endregion

   //region Clear PersonState
   fun clearState() {
      logDebug(tag, "clearState")
      _personState.value = Person()
      onNavEvent(route = NavScreen.PersonInput.route, clearBackStack = false)
   }
   // endregion

   fun getActualImagePath(): String? =
      getLocalOrRemoteImagePath(_personState.value.imagePath,
         _personState.value.remoteUriPath)

   //region validate Input before add or update
   fun validateAndNavigate(
      isInput:Boolean,
      charMin: Int = 2,
      charMax: Int = 16
   ) {
      // firstName or lastName too short
      if (_personState.value.firstName.isEmpty() ||
          _personState.value.firstName.length < charMin) {
         val message = errorMessages.firstNameTooShort
         showOnError(message)
         return
      } else if (_personState.value.lastName.isEmpty() ||
                 _personState.value.lastName.length < charMin) {
         val message = errorMessages.lastNameTooShort
         showOnError(message)
         return
      }
      // firstName or lastName too long
      else if (_personState.value.firstName.length > charMax) {
         val message = errorMessages.firstNameTooLong
         showOnError(message)
         return
      } else if (_personState.value.lastName.length > charMax) {
         val message = errorMessages.lastNameTooLong
         showOnError(message)
         return
      } else if (_personState.value.email != null &&
         !Patterns.EMAIL_ADDRESS.matcher(_personState.value.email!!).matches()) {
         val message = errorMessages.emailInValid
         this.showOnError(message)
         return
      } else if (personStateValue.phone != null &&
         !Patterns.PHONE.matcher(_personState.value.phone!!).matches()) {
         val message: String = errorMessages.phoneInValid
         this.showOnError(message)
         return
      } else {
         if (isInput) this.add()
         else this.update()
      }
   }

   //region Store person to local database or remote webserver
   private suspend fun storePerson(
      person: Person,
      store: suspend (Person) -> ResultData<Unit>
   ) {
      when (val result: ResultData<Unit> = store(person)) {
         is ResultData.Success -> {
            refreshStateFlowPeople()
            onNavEvent(route = NavScreen.PeopleList.route)
         }
         is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
         else -> Unit
      }
   }
   //endregion

   // region eliminate person from local database or remote webserver
   private suspend fun eliminatePerson(
      id: UUID,
      delete: suspend (UUID) -> ResultData<Unit>
   ) {
      when (val result: ResultData<Unit> = delete(id)) {
         is ResultData.Success -> {
            refreshStateFlowPeople()
            onNavEvent(route = NavScreen.PeopleList.route)
         }
         is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
         else -> Unit
      }
   }
   //endregion

   //region Image handling exists, post, put
   private fun exitsImage(localImagePath: String): Boolean =
      exitsFileOnInternalStorage(localImagePath)

   private suspend fun postImage(
      localImagePath: String
   ): Job = _coroutineScope.launch {
      logDebug(tag, "update() -> postImage()")
      // post imageFile to remote server
//    val resultImage: ResultData<Image> = _coroutineScope.async {
//       _imagesRepository.post(localImagePath)
//    }.await()
      val resultImage: ResultData<Image> = withContext(_dispatcher) {
         _imagesRepository.post(localImagePath)
      }

      when (resultImage) {
         is ResultData.Success -> {
            deleteFileOnInternalStorage(localImagePath)
            _personState.value = _personState.value.copy(
               imagePath = null,
               remoteUriPath = resultImage.data.remoteUriPath,
               imageId = resultImage.data.id
            )
         }
         is ResultData.Failure -> showAndNavigateBackOnFailure(resultImage.throwable)
         else -> Unit
      }
   }

   private suspend fun putImage(
      localImagePath: String,
      remoteUriPath: String
   ): Job = _coroutineScope.launch {
      // replace imageFile on remote server
      logDebug(tag, "update() -> putImage()")
      val resultImage: ResultData<Image> = _coroutineScope.async {
         _imagesRepository.put(localImagePath, remoteUriPath)
      }.await()

      when (resultImage) {
         is ResultData.Success -> {
            deleteFileOnInternalStorage(localImagePath)
            _personState.value = _personState.value.copy(
               imagePath = null,
               remoteUriPath = resultImage.data.remoteUriPath,
               imageId = resultImage.data.id
            )
         }
         is ResultData.Failure -> showAndNavigateBackOnFailure(resultImage.throwable)
         else -> Unit
      }
   }
   //endregion

   private fun setStateFromPerson(person: Person) {
      _personState.value = person.copy()
   }

   companion object {
      private const val tag = "ok>PeopleViewModel    ."
   }
}