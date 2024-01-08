package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IPeopleDao
import de.rogallab.mobile.data.IPeopleWebservice
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.PersonDtoWithWorkorderDtos
import de.rogallab.mobile.data.models.WorkorderDto
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.Resource
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.mapping.toPeopleDto
import de.rogallab.mobile.domain.mapping.toPerson
import de.rogallab.mobile.domain.mapping.toPersonDto
import de.rogallab.mobile.domain.usecases.apiCommandRequest
import de.rogallab.mobile.domain.usecases.apiQueryRequest
import de.rogallab.mobile.domain.usecases.apiQueryRequestAsFlow
import de.rogallab.mobile.domain.usecases.repositoryCommand
import de.rogallab.mobile.domain.usecases.repositoryQuery
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class PeopleRepositoryImpl @Inject constructor(
   private val _peopleDao: IPeopleDao,
   private val _peopleWebservice: IPeopleWebservice,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : IPeopleRepository {

   override fun selectAll(): Flow<Resource<List<Person>>> = flow {
      try {
         var flowPeopleDto: Flow<List<PersonDto>> = _peopleDao.selectAll()
         flowPeopleDto.collect { peopleDto: List<PersonDto> ->
            val people: List<Person> = peopleDto.map { it -> toPerson(it) }
            logDebug(tag, "selectAll() ${people.size} items")
            emit(Resource.Success(people))
         }
      }
      catch (e: IOException) {
         val message = e.message ?: e.stackTraceToString()
         logError(tag, message)
         emit(Resource.Error(message = message))
      }
      catch (e: Exception) {
         val message = e.localizedMessage ?: e.stackTraceToString()
         logError(tag, message)
         emit(Resource.Error(message))
      }
   }.flowOn(_dispatcher + _exceptionHandler)

   override suspend fun findById(id: UUID): Resource<Person?> =
      withContext(_dispatcher + _exceptionHandler) {
         try {
            _peopleDao.selectById(id)?.let { personDto: PersonDto ->
               val person: Person = toPerson(personDto)
               logDebug(tag, "findById(${id.as8()}) ${person.asString()}")
               return@withContext Resource.Success(data = person)
            } ?: run {
               val message = "Item with given id not found"
               logError(tag, message)
               return@withContext Resource.Error(message = message)
            }
         }
         catch (e: IOException) {
            val message = e.message ?: e.stackTraceToString()
            logError(tag, message)
            return@withContext Resource.Error(message = message)
         }
         catch (e: Exception) {
            val message = e.localizedMessage ?: e.stackTraceToString()
            logError(tag, message)
            return@withContext Resource.Error(message)
         }
      }

   suspend fun xfindById(id: UUID): Resource<Person?> =
      withContext(_dispatcher + _exceptionHandler) {
         return@withContext repositoryQuery<PersonDto,Person>(  // <T,R>
            tag = tag,
            dataDtoToData = ::toPerson       // (T) -> R
         ) {
            logDebug(tag, "findById(${id.as8()})")
            _peopleDao.selectById(id)
         }
      }

   override suspend fun count(): Resource<Int> =
      withContext(_dispatcher + _exceptionHandler) {
         try {
            val records = _peopleDao.count()
            logDebug(tag, "count() $records")
            return@withContext Resource.Success(data = records)
         } catch(e: IOException) {
            val message = e.message ?: e.stackTraceToString()
            logError(tag, "$message/n ${e.stackTraceToString()}" )
            return@withContext Resource.Error(message = message)
         } catch (e: Exception) {
            val message = e.localizedMessage ?: e.stackTraceToString()
            logError(tag, message)
            return@withContext Resource.Error(message = message)
         }
      }


   override suspend fun add(person: Person): Resource<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
//       throw Exception("Error thrown in add()")
         try {
            val personDto = toPersonDto(person)
            logDebug(tag, "insert() ${personDto.asString()}")
            _peopleDao.insert(personDto)
            return@withContext Resource.Success(data = Unit)
         }
         catch(e: IOException) {
            val message = e.message ?: e.stackTraceToString()
            logError(tag, "$message/n ${e.stackTraceToString()}" )
            return@withContext Resource.Error(message = message)
         }
         catch (e: Exception) {
            val message = e.localizedMessage ?: e.stackTraceToString()
            logError(tag, message)
            return@withContext Resource.Error(message = message)
         }
      }

   suspend fun add2(person: Person): Resource<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
         return@withContext repositoryCommand(   // <T,R>
            tag = tag,
            data = person,                       //  T
            toDto = ::toPersonDto                // (T) -> R
         ) { personDto: PersonDto ->             // (R) -> Unit
            _peopleDao.insert(personDto)
         }
      }

   override suspend fun addAll(people: List<Person>): Resource<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
//       throw Exception("Error thrown in addAll()")
         return@withContext repositoryCommand(
            tag = tag,
            data = people,
            toDto = ::toPeopleDto
         ) { it: List<PersonDto> ->
            _peopleDao.insertAll(it)
         }
      }

   override suspend fun update(person: Person): Resource<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
//       throw Exception("Error thrown in update()")
         logDebug(tag, "update()")
         return@withContext repositoryCommand(
            tag = tag,
            data = person,
            toDto = ::toPersonDto
         ) { personDto: PersonDto ->
            _peopleDao.update(personDto)
         }
      }

   override suspend fun remove(person: Person): Resource<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
//       throw Exception("Error thrown in remove()")
         return@withContext repositoryCommand(
            tag = tag,
            data = person,
            toDto = ::toPersonDto
         ) { personDto: PersonDto ->
            logDebug(tag, "remove()")
            _peopleDao.delete(personDto)
         }
      }

   override suspend fun selectByIdWithWorkorders(id: UUID): Person? =
      withContext(_dispatcher + _exceptionHandler) {
         val personDtoWithWorkorderDtos: PersonDtoWithWorkorderDtos? =
            _peopleDao.findbyIdWithWorkorders(id)
         val person: Person? = personDtoWithWorkorderDtos?.let{ toPerson(it) }
         logDebug(tag, "findByIdWithWorkorders() " +
            "${person?.asString()}")
//          throw Exception("Error thrown in findById()")
         return@withContext person
      }

   override suspend fun findByIdWithWorkorders(id: UUID): Map<PersonDto, List<WorkorderDto>> =
      withContext(_dispatcher + _exceptionHandler) {
         val map = _peopleDao.loadPersonWithWorkorders(id)
         logDebug(tag, "findByIdWithWorkorders()")
         return@withContext map
      }

   override suspend fun getAll(): Flow<Resource<List<Person>>> = flow {
      apiQueryRequestAsFlow(
         tag = tag,
         dispatcher = _dispatcher,
         exceptionHandler = _exceptionHandler,
         toData = ::toPerson
      ) {
         logDebug(tag, "suspend getAll()")
         _peopleWebservice.getAll()
      }.collect { it: Resource<List<Person>> ->
         emit(it)
      }
   }

   override suspend fun getById(id: UUID): Resource<Person?> =
      withContext(_dispatcher + _exceptionHandler) {
         //             <T        ,R     >
         apiQueryRequest<PersonDto,Person>(
            tag = tag,
            dataDtoToData = ::toPerson  // (T) -> R
         ) {
            logDebug(tag, "suspend getById(${id.as8()})")
            _peopleWebservice.getById(id)
         }
      }


   override suspend fun post(person: Person): Resource<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
         apiCommandRequest(
            tag = tag,
            data = person,
            dataToDto = ::toPersonDto
         ) { personDto: PersonDto ->
            logDebug(tag, "suspend post()")
            _peopleWebservice.post(personDto)
         }
      }

   override suspend fun put(person: Person): Resource<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
         apiCommandRequest(
            tag = tag,
            data = person,
            dataToDto = ::toPersonDto
         ) { personDto: PersonDto ->
            logDebug(tag, "suspend put(${personDto.id.as8()})")
            _peopleWebservice.put(personDto.id, personDto)
         }
      }

   override suspend fun delete(person: Person): Resource<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
         apiCommandRequest(
            tag = tag,
            data = person,
            dataToDto = ::toPersonDto
         ) { personDto: PersonDto ->
            logDebug(tag, "suspend delete($personDto.id.as8()})")
            _peopleWebservice.delete(personDto.id)
         }
      }
   companion object {
      //12345678901234567890123
      private const val tag = "ok>PeopleRepositoryImpl"
   }
}

