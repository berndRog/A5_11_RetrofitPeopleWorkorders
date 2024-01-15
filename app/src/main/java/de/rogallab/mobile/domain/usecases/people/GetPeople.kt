package de.rogallab.mobile.domain.usecases.people

import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetPeople @Inject constructor(
   private val _peopleRepository: IPeopleRepository,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) {
   operator fun invoke(): Flow<ResultData<List<Person>>> = flow {
      logDebug(tag,"invoke()")
      emit(ResultData.Loading(true))


      // make the api call

         _peopleRepository.getAll().collect{ it: ResultData<List<Person>> ->


            emit(it)
         }


//      // make the api call
//      safeApiQueryRequest(tag, _dispatcher,_exceptionHandler, ::toPerson) {
//         _peopleRepository.getAll()
//      }.collect{ it: UiState<List<Person>> ->
//            emit(it)
//      }
   }.flowOn(_dispatcher + _exceptionHandler)

   companion object {       // 12345678901234567890123
      private const val tag = "ok>UC.GetPeople       ."
   }
}