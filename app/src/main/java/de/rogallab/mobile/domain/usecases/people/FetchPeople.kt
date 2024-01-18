package de.rogallab.mobile.domain.usecases.people

import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class FetchPeople @Inject constructor(
   private val _peopleRepository: IPeopleRepository,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) {
   operator fun invoke(
      isWebservice:Boolean = false
   ): Flow<ResultData<List<Person>>> = flow {
      logDebug(tag, "invoke()")
      emit(ResultData.Loading(true))

      if (isWebservice) {
         // read from webservice
         _peopleRepository.getAll().collect { it: ResultData<List<Person>> ->
            emit(it)
         }
         // write to local database
         // _peopleRepository.addAll(people)
      } else {
         // local database
         _peopleRepository.selectAll().collect { it: ResultData<List<Person>> ->
            emit(it)
         }
      }
   } .catch { e: Throwable ->
      emit(ResultData.Failure(e))
   } .flowOn(_dispatcher)

   companion object {       // 12345678901234567890123
      private const val tag = "ok>UC.GetPeople       ."
   }
}