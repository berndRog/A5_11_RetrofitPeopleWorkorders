package de.rogallab.mobile.domain.usecases.workorders

import de.rogallab.mobile.domain.IWorkordersRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class FetchWorkorders @Inject constructor(
   private val _repository: IWorkordersRepository,
   private val _dispatcher: CoroutineDispatcher
) {
   operator fun invoke(isWebservice:Boolean): Flow<ResultData<List<Workorder>>> = flow {
      logDebug(tag, "invoke()")
      emit(ResultData.Loading(true))

      if (isWebservice) {
         // make the api call
         _repository.getAll().collect { it: ResultData<List<Workorder>> ->
            emit(it)
         }
      } else {
         // read from local database
         _repository.selectAll().collect { it: ResultData<List<Workorder>> ->
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