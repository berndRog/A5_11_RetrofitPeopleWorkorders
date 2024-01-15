package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IPeopleDao
import de.rogallab.mobile.data.IPeopleWebservice
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.PersonDtoWithWorkorderDtos
import de.rogallab.mobile.data.network.httpStatusMessage
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.mapping.toPeopleDto
import de.rogallab.mobile.domain.mapping.toPerson
import de.rogallab.mobile.domain.mapping.toPersonDto
import de.rogallab.mobile.domain.mapping.toWorkorder
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.domain.utilities.maxValues
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class PeopleRepositoryImpl @Inject constructor(
   private val _peopleDao: IPeopleDao,
   private val _peopleWebservice: IPeopleWebservice,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : IPeopleRepository {

   override fun selectAll(): Flow<ResultData<List<Person>>> = flow {
      try {
         var flowPeopleDto: Flow<List<PersonDto>> = _peopleDao.selectAll()
         flowPeopleDto.collect { peopleDto: List<PersonDto> ->
            val people: List<Person> = peopleDto.map { it -> toPerson(it) }
            logDebug(tag, "selectAll() ${people.size} items")
            emit(ResultData.Success(people))
         }
      } catch (t: Throwable) {
         emit(ResultData.Failure(t))
      }
   }.flowOn(_dispatcher)

   override suspend fun findById(id: UUID): ResultData<Person?> =
      withContext(_dispatcher) {
         try {
            //throw Exception("Test Error thrown in findById()")
            _peopleDao.selectById(id)?.let { dto: PersonDto ->
               val person: Person = toPerson(dto)
               logDebug(tag, "findById() success")
               return@withContext ResultData.Success(person)
            } ?: run {
               val message = "Item with given id not found"
               return@withContext ResultData.Failure(Exception(message), tag)
            }
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun count(): ResultData<Int> =
      withContext(_dispatcher + _exceptionHandler) {
         try {
            val records = _peopleDao.count()
            logDebug(tag, "count() $records")
            return@withContext ResultData.Success(records)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t, tag)
         }
      }

   override suspend fun add(person: Person): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            val personDto = toPersonDto(person)
//            throw Exception("Test Error thrown in add")
            logDebug(tag, "insert() ${personDto.asString()}")
            _peopleDao.insert(personDto)
            return@withContext ResultData.Success(Unit)
         } catch(t: Throwable) {
            return@withContext ResultData.Failure(t, tag)
         }
      }

   override suspend fun addAll(people: List<Person>): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            val peopleDto = toPeopleDto(people)
            logDebug(tag, "insertAll() ${people.size}")
            _peopleDao.insertAll(peopleDto)
            return@withContext ResultData.Success(Unit)
         } catch(t: Throwable) {
            return@withContext ResultData.Failure(t, tag)
         }
      }

   override suspend fun update(person: Person): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            val personDto = toPersonDto(person)
            logDebug(tag, "update() ${personDto.asString()}")
//          throw Exception("Error thrown in update()")
            _peopleDao.update(personDto)
            return@withContext ResultData.Success(Unit)
         } catch(t: Throwable) {
            return@withContext ResultData.Failure(t, tag)
         }
      }

   override suspend fun remove(person: Person): ResultData<Unit> =
      withContext(_dispatcher ) {
         try {
            val personDto = toPersonDto(person)
            logDebug(tag, "remove() ${personDto.asString()}")
            _peopleDao.delete(personDto)
            return@withContext ResultData.Success(Unit)
         } catch(t: Throwable) {
            return@withContext ResultData.Failure(t, tag)
         }
      }

   override suspend fun selectByIdWithWorkorders(id: UUID): ResultData<Person?> =
      withContext(_dispatcher ) {
         try {
            val personDtoWithWorkorderDtos: PersonDtoWithWorkorderDtos? =
               _peopleDao.findbyIdWithWorkorders(id)
            val person: Person? = personDtoWithWorkorderDtos?.let{ toPerson(it) }
            logDebug(tag, "findByIdWithWorkorders() " +
                  "${person?.asString()}")
            return@withContext ResultData.Success(person)
         } catch(t: Throwable) {
            return@withContext ResultData.Failure(t, tag)
         }
      }

   override suspend fun findByIdWithWorkorders(id: UUID): ResultData<Person?> =
      withContext(_dispatcher ) {
         try {
            var person: Person? = null
            _peopleDao.loadPersonWithWorkorders(id)?.map { (personDto, workordersDto) ->
               person = toPerson(personDto)
               workordersDto.map { it ->  person?.addWorkorder(toWorkorder(it)) }
            }
            logDebug(tag, "findByIdWithWorkorders() " +
                  "${person?.asString()} ${person?.workorders?.size} workorders")
            return@withContext ResultData.Success(person)
         }
         catch (t: Throwable) {
            return@withContext ResultData.Failure(t, tag)
         }
      }

   override suspend fun getAll(): Flow<ResultData<List<Person>>> = flow {
      try {
         val response: Response<List<PersonDto>> = _peopleWebservice.getAll()
         logResponse(tag, response)

         if (response.isSuccessful) {
            response.body()?.let { peopleDto: List<PersonDto> ->
               val people: List<Person> = peopleDto.map { personDto -> toPerson(personDto) }
               emit(ResultData.Success(people))
            } ?: run {
               emit(ResultData.Failure(
                  IOException("response is successful, but body() is null"), tag))
            }
         } else {
            emit(ResultData.Failure(
               IOException("respnse is not successful ${httpStatusMessage(response.code())}"), tag))
         }
      }  catch(t: Throwable) {
         emit(ResultData.Failure(t, tag))
      }
   }.flowOn(_dispatcher + _exceptionHandler)

   override suspend fun getById(id: UUID): ResultData<Person> =
      withContext(_dispatcher + _exceptionHandler) {
         try {
            val response = _peopleWebservice.getById(id)
            logResponse(tag, response)

            if (response.isSuccessful) {
               response.body()?.let { personDto ->
                  val person: Person = toPerson(personDto)
                  return@withContext ResultData.Success(person)
               } ?: run {
                  return@withContext ResultData.Failure(
                     IOException("response is successful, but body() is null"), tag)
               }
            } else {
               return@withContext ResultData.Failure(IOException("response is not successful" +
                  " ${httpStatusMessage(response.code())}"), tag)
            }
         }  catch(t: Throwable) {
               return@withContext ResultData.Failure(t, tag)
         }
      }

   override suspend fun post(person: Person): ResultData<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
         try {
            val personDto = toPersonDto(person)
            val response = _peopleWebservice.post(personDto)
            // logResponse(tag, response)
            if (response.isSuccessful) {
               return@withContext ResultData.Success(Unit)
            } else {
               return@withContext ResultData.Failure(IOException("response is not successful " +
                  "${httpStatusMessage(response.code())}"), tag)
            }
         }
         catch (t: Throwable) {
            return@withContext ResultData.Failure(t, tag)
         }
      }

   override suspend fun put(person: Person): ResultData<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
         try {
            val personDto = toPersonDto(person)
            val response = _peopleWebservice.put(person.id, personDto)
            // logResponse(tag, response)
            if (response.isSuccessful) {
               return@withContext ResultData.Success(Unit)
            } else {
               return@withContext ResultData.Failure(IOException("response is not successful " +
                  "${httpStatusMessage(response.code())}"), tag)
            }
         }
         catch (t: Throwable) {
            return@withContext ResultData.Failure(t, tag)
         }
      }

   override suspend fun delete(person: Person): ResultData<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
         try {
            val response = _peopleWebservice.delete(person.id)
            // logResponse(tag, response)
            if (response.isSuccessful) {
               return@withContext ResultData.Success(Unit)
            } else {
               return@withContext ResultData.Failure(IOException("response is not successful " +
                  "${httpStatusMessage(response.code())}"), tag)
            }
         }
         catch (t: Throwable) {
            return@withContext ResultData.Failure(t, tag)
         }
      }

   // helper function to log the response
   private fun <T> logResponse(
      tag: String,
      response: Response<T>
   ) {
      logVerbose(tag, "Request ${response.raw().request.method} ${response.raw().request.url}")
      logVerbose(tag, "Request Headers")
      response.raw().request.headers.forEach {
         val text = "   %-15s %s".format(it.first, it.second )
         logVerbose(tag, "$text")
      }

      val ms = response.raw().receivedResponseAtMillis - response.raw().sentRequestAtMillis
      logVerbose(tag, "took $ms ms")
      logVerbose(tag, "Response isSuccessful ${response.isSuccessful()}")

      logVerbose(tag, "Response Headers")
      response.raw().headers.forEach {
         val text = "   %-15s %s".format(it.first, it.second)
         logVerbose(tag, "$text")
      }

      logVerbose(tag, "Response Body")
      logVerbose(tag, "   Status Code ${response.code().toString().maxValues(100)}")
      logVerbose(tag, "   Status Message ${response.message().toString().maxValues(100)}")
   }

   companion object {
      //12345678901234567890123
      private const val tag = "ok>PeopleRepositoryImpl"
   }
}