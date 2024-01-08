package de.rogallab.mobile.domain.usecases.people

import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.Resource
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ReadPeople @Inject constructor(
   private val _peopleRepository: IPeopleRepository,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) {

   operator fun invoke(): Flow<UiState<List<Person>>> = flow {
      emit(UiState.Loading)

      _peopleRepository.selectAll().collect { resource: Resource<List<Person>> ->
         delay(500)
         logDebug(tag, "ReadAll.invoke() emit success")
         when (resource) {
            is Resource.Success -> {
               val people = resource.data?.sortedBy { it.lastName }
               emit(UiState.Success(data = people))
            }
            is Resource.Error -> {
               val message = resource.message!!
               emit(UiState.Error(message = message))
            }
            is Resource.Loading -> {
               emit(UiState.Loading)
            }
         }
      }
   }.catch {
      val message = it.localizedMessage ?: it.stackTraceToString()
      logError(tag, message)
      emit(UiState.Error(message = message))
   }.flowOn(_dispatcher + _exceptionHandler)

   companion object {
                                     //12345678901234567890123
      private const val tag: String = "ok>PeopleReadAllUseCase"
   }
}