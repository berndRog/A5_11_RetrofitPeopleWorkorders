package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IWorkordersDao
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.WorkorderDto
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class WordordersRepositoryImpl @Inject constructor(
   private val _workordersDao: IWorkordersDao,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : IWorkordersRepository {

            //12345678901234567890123
   val tag = "ok>WorkorderReposImpl ."

   override fun selectAll(): Flow<ResultData<List<Workorder>>>  = flow {
      try {
         val flowWordorderDtos: Flow<MutableList<WorkorderDto>> = _workordersDao.selectAll()
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
            _workordersDao.selectById(id)?.let { dto: WorkorderDto ->
               val workorder: Workorder = toWorkorder(dto)
               logDebug(tag, "findById() success")
               return@withContext ResultData.Success(workorder)
            } ?: run {
               val message = "Workorder with given id not found"
               return@withContext ResultData.Failure(Exception(message), tag)
            }
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun count(): ResultData<Int> =
      withContext(_dispatcher) {
         try {
            val records = _workordersDao.count()
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
            val workorderDto = workorder.toWorkorderDto()
            logDebug(tag, "insert() ${workorder.asString()}")
            _workordersDao.insert(workorderDto)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun addAll(workorders: List<Workorder>): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            // throw Exception("Error thrown in addAll()")
            val workorderDtos = workorders.map { it.toWorkorderDto() }
            logDebug(tag, "addAll()")
            _workordersDao.insertAll(workorderDtos)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun update(workorder: Workorder): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            // throw Exception("Error thrown in update()")
            val workorderDto = workorder.toWorkorderDto()
            logDebug(tag, "update()")
            _workordersDao.update(workorderDto)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun remove(workorder: Workorder): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            // throw Exception("Error thrown in remove()")
            val workorderDto = workorder.toWorkorderDto()
            logDebug(tag, "remove()")
            _workordersDao.delete(workorderDto)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun findByIdWithPerson(id: UUID): ResultData<Map<Workorder, Person?>> =
      withContext(_dispatcher) {
         try {
            // selectByIdWithPerson() returns a Map of DTOs
            logDebug(tag, ",loadWorkorderWithPerson()")
            val mapDtos: Map<WorkorderDto, PersonDto?> = _workordersDao.findByIdWithPerson(id)
            // Transform the DTOs to domain entities
            val map: Map<Workorder, Person?> = mapDtos
               .mapKeys { entry -> toWorkorder(entry.key) }
               .mapValues { entry -> entry.value?.let { toPerson(it) } }
         return@withContext ResultData.Success(map)
      } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }
}