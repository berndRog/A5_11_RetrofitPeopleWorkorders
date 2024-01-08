package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IWorkordersDao
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.WorkorderDto
import de.rogallab.mobile.domain.IWorkordersRepository
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.mapping.toPerson
import de.rogallab.mobile.domain.mapping.toWorkorder
import de.rogallab.mobile.domain.mapping.toWorkorderDto
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class WordordersRepositoryImpl @Inject constructor(
   private val _workordersDao: IWorkordersDao,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : IWorkordersRepository {

            //12345678901234567890123
   val tag = "ok>WorkorderReposImpl ."

   override fun readAll(): Flow<List<Workorder>> {
      // throw Exception("Error thrown in selectAll()")
      logDebug(tag, "selectAll()")
      // selectAll() returns a Flow of DTOs
      val flowWordorderDtos: Flow<MutableList<WorkorderDto>> = _workordersDao.selectAll()
      // Transform the DTOs to domain entities
      return flowWordorderDtos.map { workorderDtos: MutableList<WorkorderDto> ->
         workorderDtos.map { workorderDto: WorkorderDto -> toWorkorder(workorderDto)}
      }
   }

   override suspend fun findById(id: UUID): Workorder? =
      withContext(_dispatcher + _exceptionHandler) {
//       throw Exception("Error thrown in findById()")
         val workorderDto = _workordersDao.selectById(id)
         val workorder = workorderDto?.let{ toWorkorder(it)}
         logDebug(tag, "findById() ${workorder?.asString()}")
         return@withContext workorder
      }

   override suspend fun count(): Int =
      withContext(_dispatcher+_exceptionHandler) {
         val records = _workordersDao.count()
         logDebug(tag,"count() $records")
//       throw Exception("Error thrown in count()")
         return@withContext records
   }

   override suspend fun add(workorder: Workorder): Boolean =
      withContext(_dispatcher + _exceptionHandler) {
//       throw Exception("Error thrown in add()")
         val workorderDto = workorder.toWorkorderDto()
         _workordersDao.insert(workorderDto)
         logDebug(tag, "insert() ${workorder.asString()}")
         return@withContext true
      }

   override suspend fun addAll(workorders: List<Workorder>): Boolean =
      withContext(_dispatcher + _exceptionHandler) {
//       throw Exception("Error thrown in addAll()")
         val workorderDtos = workorders.map { it.toWorkorderDto() }
         _workordersDao.insertAll(workorderDtos)
         logDebug(tag, "addAll()")
         return@withContext true
      }

   override suspend fun update(workorder: Workorder): Boolean =
      withContext(_dispatcher + _exceptionHandler) {
//       throw Exception("Error thrown in update()")
         val workorderDto = workorder.toWorkorderDto()
         _workordersDao.update(workorderDto)
         logDebug(tag, "update()")
         return@withContext true
      }

   override suspend fun remove(workorder: Workorder): Boolean =
      withContext(_dispatcher + _exceptionHandler) {
//       throw Exception("Error thrown in remove()")
         val workorderDto = workorder.toWorkorderDto()
         _workordersDao.delete(workorderDto)
         logDebug(tag, "remove()")
         return@withContext true
      }

   override suspend fun findByIdWithPerson(id: UUID): Map<Workorder, Person?> =
      withContext(_dispatcher + _exceptionHandler) {
         // selectByIdWithPerson() returns a Map of DTOs
         val mapDtos: Map<WorkorderDto, PersonDto?> = _workordersDao.findByIdWithPerson(id)
         // Transform the DTOs to domain entities
         val map: Map<Workorder, Person?> =
            mapDtos.mapKeys { entry -> toWorkorder(entry.key) }
                   .mapValues { entry -> entry.value?.let { toPerson(it) } }
         logDebug(tag, ",loadWorkorderWithPerson()")
         return@withContext map
      }
}