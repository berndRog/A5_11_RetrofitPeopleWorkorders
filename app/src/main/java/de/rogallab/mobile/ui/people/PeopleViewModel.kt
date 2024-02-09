package de.rogallab.mobile.ui.people

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

   // Observer (DataBinding), Observable is a Person object
   private val _personState: MutableState<Person> =  mutableStateOf(Person())
   // access to the observable
   val personStateValue: Person
      get() = _personState.value  // Observable (DataBinding)

   fun onPersonUiEventChange(event: PersonUiEvent, value: Any) {
      _personState.value = when (event) {
         PersonUiEvent.Id            -> personStateValue.copy(id        = value as UUID)
         PersonUiEvent.FirstName     -> personStateValue.copy(firstName = value as String)
         PersonUiEvent.LastName      -> personStateValue.copy(lastName  = value as String)
         PersonUiEvent.Email         -> personStateValue.copy(email     = value as String?)
         PersonUiEvent.Phone         -> personStateValue.copy(phone     = value as String?)
         PersonUiEvent.ImagePath     -> personStateValue.copy(imagePath = value as String?)
         PersonUiEvent.RemoteUriPath -> personStateValue.copy(remoteUriPath = value as String?)

      }
   }

   // Coroutine ExceptionHandler
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
   //
   // Fetch people from local database or remote webservice
   //
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
   //
   // Read person by id
   //
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
   //
   // Add person
   //
   fun add(p: Person? = null) {

      if(p != null) setStateFromPerson(p)

      viewModelScope.launch(_dispatcher) {
         // upload local image to remote web server
         if (AppStart.isWebservice) {
            logDebug(tag, "post imagePath:${_personState.value.imagePath}")
            logDebug(tag, "post remoteUriPath:${_personState.value.remoteUriPath}")

            var job: Job? = null
            if (exitsLocalImage(_personState.value.imagePath)) {
               job = postImage(_personState.value.imagePath!!)
            }
            job?.join() // wait for the image upload
            
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
   //
   // update person
   //
   fun update() {
      viewModelScope.launch(_dispatcher) {
         // upload image to server
         if(AppStart.isWebservice) {
            logDebug(tag, "put imagePath:${_personState.value.imagePath}")
            logDebug(tag, "put remoteUriPath:${_personState.value.remoteUriPath}")

            val job  = putOrPostImage()
            job?.join() // wait for the image upload

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

   private suspend fun storePerson(
      person: Person,
      send: suspend (Person) -> ResultData<Unit>
   ) {
      val result: ResultData<Unit> = send(person)
      when (result) {
         is ResultData.Success -> {
            refreshStateFlowPeople()
            onNavEvent(route = NavScreen.PeopleList.route)
         }
         is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
         else -> Unit
      }
   }

   private suspend fun putOrPostImage(): Job? =
      if (exitsLocalImage(_personState.value.imagePath) &&
         _personState.value.remoteUriPath == null) {
         postImage(_personState.value.imagePath!!)
      }
      // a remote image exists, update the remote image with the new local image
      else if (exitsLocalImage(_personState.value.imagePath) &&
         _personState.value.remoteUriPath != null) {
         putImage(_personState.value.imagePath!!, _personState.value.remoteUriPath!!)
      } else {
         null
      }


   fun remove(id: UUID) {
      viewModelScope.launch(_dispatcher) {
         logDebug(tag, "remove(${id.as8()})")
         //if (isWebservice && person.imagePath != null) {
            //val resultImage = _imagesRepository.delete(person.imagePath!!)
            //when (resultImage) {
            //   is ResultData.Failure -> showAndNavigateBackOnFailure(resultImage.throwable)
            //   else -> Unit
            //}
         //}
         var result: ResultData<Unit>
         if(AppStart.isWebservice) result = _repository.delete(id)
         else                      result = _repository.remove(id)
         when (result) {
            is ResultData.Success -> {
               refreshStateFlowPeople()
            }
            is ResultData.Failure -> showAndNavigateBackOnFailure(result.throwable)
            else -> Unit
         }
      }
   }
   private fun exitsLocalImage(localImagePath: String?): Boolean =
      personStateValue.imagePath != null &&
         exitsFileOnInternalStorage(personStateValue.imagePath!!)


   private suspend fun postImage(
      localImagePath: String
   ): Job = _coroutineScope.launch {
      logDebug(tag, "update() -> postImage()")
      // post imageFile to remote server
      val resultImage: ResultData<Image> = _coroutineScope.async {
         _imagesRepository.post(localImagePath)
      }.await()
      when (resultImage) {
         is ResultData.Success -> {
            deleteFileOnInternalStorage(localImagePath)
            _personState.value = personStateValue.copy(
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
      val resultImage: ResultData<Image> = _coroutineScope.async{
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

   fun getActualImagePath(): String? =
      getLocalOrRemoteImagePath(_personState.value.imagePath,
         _personState.value.remoteUriPath)

   fun clearState() {
      logDebug(tag, "clearState")
      _personState.value = Person()
      onNavEvent(route = NavScreen.PersonInput.route, clearBackStack = false)
   }

   private fun setStateFromPerson(person: Person) {
      _personState.value = person.copy()
   }

   companion object {
      private const val tag = "ok>PeopleViewModel    ."
   }
}