package de.rogallab.mobile.domain.usecases.people

import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetPeople @Inject constructor(
   private val _peopleRepository: IPeopleRepository,
   private val _dispatcher: CoroutineDispatcher
) {
   operator fun invoke(): Flow<ResultData<List<Person>>> = flow {
      logDebug(tag, "invoke()")
      emit(ResultData.Loading(true))
      // read from webservice
      _peopleRepository.getAll().collect { it: ResultData<List<Person>> ->
         emit(it)
      }
      // write to local database
      // _peopleRepository.addAll(people)
   } .catch { e: Throwable ->
      emit(ResultData.Failure(e))
   } .flowOn(_dispatcher)

   companion object {       // 12345678901234567890123
      private const val tag = "ok>UC.GetPeople       ."
   }
}