package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IWorkordersDao
import de.rogallab.mobile.data.IWorkordersWebservice
import de.rogallab.mobile.data.ImagesWebservice
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.WorkorderDto
import de.rogallab.mobile.data.network.httpStatusMessage
import de.rogallab.mobile.domain.IWorkordersRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.mapping.toPerson
import de.rogallab.mobile.domain.mapping.toWorkorder
import de.rogallab.mobile.domain.mapping.toWorkorderDto
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class WordordersRepositoryImpl @Inject constructor(
   private val _dao: IWorkordersDao,
   private val _webservice: IWorkordersWebservice,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : IWorkordersRepository {

            //12345678901234567890123
   val tag = "ok>WorkorderReposImpl ."

   // L O C A L   D A T A B A S E
   override fun selectAll(): Flow<ResultData<List<Workorder>>>  = flow {
      try {
         val flowWordorderDtos: Flow<MutableList<WorkorderDto>> = _dao.selectAll()
         flowWordorderDtos.collect{ workorderDtos: MutableList<WorkorderDto> ->
            val workorders: List<Workorder> =
               workorderDtos.map { workorderDto: WorkorderDto -> toWorkorder(workorderDto) }
            logDebug(tag, "selectAll()")
            emit(ResultData.Success(workorders))
         }
      } catch (t: Throwable) {
        emit(ResultData.Failure(t))
      }
   }

   override suspend fun findById(id: UUID): ResultData<Workorder?> =
      withContext(_dispatcher) {
         try {
            //throw Exception("Test Error thrown in findById()")
            _dao.selectById(id)?.let { dto: WorkorderDto ->
               val workorder: Workorder = toWorkorder(dto)
               logDebug(tag, "findById() success")
               return@withContext ResultData.Success(workorder)
            } ?: run {
               val message = "Workorder with given id not found"
               return@withContext ResultData.Failure(Exception(message))
            }
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun count(): ResultData<Int> =
      withContext(_dispatcher) {
         try {
            val records = _dao.count()
            logDebug(tag, "count() $records")
//       throw Exception("Error thrown in count()")
            return@withContext ResultData.Success(records)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun add(workorder: Workorder): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            // throw Exception("Error thrown in add()")
            logDebug(tag, "insert() ${workorder.asString()}")
            val dto = toWorkorderDto(workorder)
            _dao.insert(dto)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun update(workorder: Workorder): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            // throw Exception("Error thrown in update()")
            logDebug(tag, "update()")
            val dto = toWorkorderDto(workorder)
            _dao.update(dto)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun remove(id: UUID): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            // throw Exception("Error thrown in remove()")
            logDebug(tag, "remove()")
            _dao.delete(id)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }


   // W E B S E R V I C E
   override fun getAll(): Flow<ResultData<List<Workorder>>> = flow {
      try {
         val response: Response<List<WorkorderDto>> = _webservice.getAll()
         logResponse(tag, response)

         if (response.isSuccessful) {
            response.body()?.let { workordersDto: List<WorkorderDto> ->
               val workorders: List<Workorder> = workordersDto.map { workorderDto -> toWorkorder(workorderDto) }
               emit(ResultData.Success(workorders))
            } ?: run {
               emit(ResultData.Failure(IOException("response.body() is null")))
            }
         } else {
            emit(ResultData.Failure(
               IOException("response is not successful ${httpStatusMessage(response.code())}")))
         }
      }  catch(t: Throwable) {
         emit(ResultData.Failure(t))
      }
   }  .catch { t: Throwable ->
      emit(ResultData.Failure(t))
   }  .flowOn(_dispatcher)

   override suspend fun getById(id: UUID): ResultData<Workorder?> =
      withContext(_dispatcher) {
         try {
            val response = _webservice.getById(id)
            logResponse(tag, response)
            if (! response.isSuccessful)
               return@withContext ResultData.Failure(IOException("${httpStatusMessage(response.code())}"))

            response.body()?.let { workorderDto ->
               return@withContext ResultData.Success(toWorkorder(workorderDto))
            } ?: run {
               return@withContext ResultData.Success(null)
            }
         } catch(t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun post(workorder: Workorder): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            val workorderDto = toWorkorderDto(workorder)
            val response = _webservice.post(workorderDto)
            // logResponse(tag, response)
            if (response.isSuccessful) {
               return@withContext ResultData.Success(Unit)
            } else {
               return@withContext ResultData.Failure(IOException("${httpStatusMessage(response.code())}"))
            }
         }
         catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun put(workorder: Workorder): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            // throw Exception("Error thrown in update()")
            logDebug(tag, "update()")
            val dto = toWorkorderDto(workorder)
            _webservice.put(dto.id, dto)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun delete(id: UUID): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            // throw Exception("Error thrown in remove()")
            logDebug(tag, "remove()")
            _webservice.delete(id)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }
}