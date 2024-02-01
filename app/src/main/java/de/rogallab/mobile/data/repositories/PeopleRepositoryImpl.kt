package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IPeopleDao
import de.rogallab.mobile.data.IPeopleWebservice
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.WorkorderDto
import de.rogallab.mobile.data.network.httpStatusMessage
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.mapping.toPerson
import de.rogallab.mobile.domain.mapping.toPersonDto
import de.rogallab.mobile.domain.mapping.toWorkorder
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
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

class PeopleRepositoryImpl @Inject constructor(
   private val _dao: IPeopleDao,
   private val _webservice: IPeopleWebservice,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : IPeopleRepository {

   // L O C A L   D A T A B A S E
   override fun selectAll(): Flow<ResultData<List<Person>>> = flow {
      try {
         var flowPeopleDto: Flow<List<PersonDto>> = _dao.selectAll()
         flowPeopleDto.collect { peopleDto: List<PersonDto> ->
            val people: List<Person> = peopleDto.map { it: PersonDto -> toPerson(it) }
            logDebug(tag, "selectAll() ${people.size} items")
            emit(ResultData.Success(people))
         }
      } catch (t: Throwable) {
         t.stackTrace.forEach { logError(tag, it.toString()) }
         emit(ResultData.Failure(t))
      }
   } .catch { t: Throwable ->
         emit(ResultData.Failure(t))
   } .flowOn(_dispatcher)

   //throw Exception("Test Error thrown in findById()")

   override suspend fun findById(id: UUID): ResultData<Person?> =
      withContext(_dispatcher) {
         try {
            _dao.selectById(id)?.let { dto: PersonDto ->
               val person: Person = toPerson(dto)
               logDebug(tag, "findById() success")
               return@withContext ResultData.Success(person)
            } ?: run {
               val message = "Item with given id not found"
               return@withContext ResultData.Failure(Exception(message))
            }
         } catch (t: Throwable) { return@withContext ResultData.Failure(t) }
      }

   override suspend fun count(): ResultData<Int> =
      withContext(_dispatcher + _exceptionHandler) {
         try {
            val records = _dao.count()
            return@withContext ResultData.Success(records)
         } catch (t: Throwable) { return@withContext ResultData.Failure(t) }
      }

   override suspend fun add(person: Person): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            logDebug(tag, "insert() ${person.asString()}")
            _dao.insert(toPersonDto(person))
            return@withContext ResultData.Success(Unit)
         } catch(t: Throwable) {  return@withContext ResultData.Failure(t)}
      }

   override suspend fun update(person: Person): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            logDebug(tag, "update() ${person.asString()}")
            _dao.update(toPersonDto(person))
            return@withContext ResultData.Success(Unit)
         } catch(t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun remove(id: UUID): ResultData<Unit> =
      withContext(_dispatcher ) {
         try {
            logDebug(tag, "remove() ${id.as8()}")
            _dao.delete(id)
            return@withContext ResultData.Success(Unit)
         } catch(t: Throwable) { return@withContext ResultData.Failure(t) }
      }

//   override suspend fun selectByIdWithWorkorders(id: UUID): ResultData<Person?> =
//      withContext(_dispatcher ) {
//         try {
//            val personDtoWithWorkorderDtos: PersonDtoWithWorkorderDtos? =
//               _peopleDao.loadPersonWithWorkorders(id)
//            val person: Person? = personDtoWithWorkorderDtos?.let{ toPerson(it) }
//            logDebug(tag, "findByIdWithWorkorders() " +
//                  "${person?.asString()}")
//            return@withContext ResultData.Success(person)
//         } catch(t: Throwable) {
//            return@withContext ResultData.Failure(t, tag)
//         }
//      }

   override suspend fun findByIdWithWorkorders(id: UUID): ResultData<Person?> =
      withContext(_dispatcher ) {
         try {
            var person: Person? = null
            _dao.findByIdWithWorkorders(id)?.map { (personDto, workordersDto) ->
               person = toPerson(personDto)
               workordersDto.map { person?.addWorkorder(toWorkorder(it)) }
            }
            logDebug(tag, "findByIdWithWorkorders() " +
                  "${person?.asString()} ${person?.workorders?.size} workorders")
            return@withContext ResultData.Success(person)
         }
         catch (t: Throwable) { return@withContext ResultData.Failure(t) }
      }

   // W E B S E R V I C E
   override suspend fun getAll(): Flow<ResultData<List<Person>>> = flow {
      try {
         val response: Response<List<PersonDto>> = _webservice.getAll()
         logResponse(tag, response)

         if (! response.isSuccessful) {
            emit(ResultData.Failure(IOException("${httpStatusMessage(response.code())}")))
            return@flow
         }
         val people = response.body()?.map {
            it: PersonDto -> toPerson(it)
         }  ?: run {
            emit(ResultData.Failure(IOException("response.body() is null")))
            return@flow
         }
         emit(ResultData.Success(people))

      }  catch(t: Throwable) { emit(ResultData.Failure(t)) }

   }.flowOn(_dispatcher).catch { t: Throwable -> emit(ResultData.Failure(t)) }

   override suspend fun getById(id: UUID): ResultData<Person> =
      withContext(_dispatcher) {
         try {
            val response = _webservice.getById(id)
            logResponse(tag, response)
            if (!response.isSuccessful)
               return@withContext ResultData.Failure(
                  IOException(" ${httpStatusMessage(response.code())}"))

            response.body()?.let { personDto ->
               val person: Person = toPerson(personDto)
               return@withContext ResultData.Success(person)
            } ?: return@withContext ResultData.Failure(
                                       IOException("response.body() is null"))

         } catch(t: Throwable) {  return@withContext ResultData.Failure(t) }
      }

   override suspend fun post(person: Person): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            val response = _webservice.post(toPersonDto(person))
            if (response.isSuccessful) {
               return@withContext ResultData.Success(Unit)
            } else {
               return@withContext ResultData.Failure(
                                     IOException("${httpStatusMessage(response.code())}"))
            }
         }
         catch (t: Throwable) { return@withContext ResultData.Failure(t) }
      }

   override suspend fun put(person: Person): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            val response = _webservice.put(person.id, toPersonDto(person))
            if (response.isSuccessful) {
               return@withContext ResultData.Success(Unit)
            } else {
               return@withContext ResultData.Failure(
                                     IOException("${httpStatusMessage(response.code())}"))
            }
         }
         catch (t: Throwable) { return@withContext ResultData.Failure(t) }
      }

   override suspend fun delete(id:UUID): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            val response = _webservice.delete(id)
            if (response.isSuccessful) {
               return@withContext ResultData.Success(Unit)
            } else {
               return@withContext ResultData.Failure(
                                    IOException("${httpStatusMessage(response.code())}"))
            }
         }
         catch (t: Throwable) { return@withContext ResultData.Failure(t) }
      }

   override suspend fun getByIdWithWorkorders(id: UUID): ResultData<Person?> =
      withContext(_dispatcher ) {
         try {
            // retrieve person
            var responsePerson = _webservice.getById(id)
            if (! responsePerson.isSuccessful) return@withContext ResultData.Failure(
               IOException("${httpStatusMessage(responsePerson.code())}"))

            val person = responsePerson.body()?.let { it: PersonDto ->  toPerson(it) }
               ?: return@withContext ResultData.Failure(
                  IOException("response is successful, but body() is null"))

            // then retrieve workorders for this person and add them to the person
            val responseWorkorders = _webservice.getByIdWithWorkorders(id)
            if(!responseWorkorders.isSuccessful) return@withContext ResultData.Failure(
               IOException("${httpStatusMessage(responseWorkorders.code())}"))

            responseWorkorders.body()?.forEach { it: WorkorderDto ->
               person.addWorkorder(toWorkorder(it))
            } ?: return@withContext ResultData.Failure(IOException("response.body() is null"))

            logDebug(tag, "getByIdWithWorkorders() " +
               "${person.asString()} ${person.workorders.size} workorders")
            return@withContext ResultData.Success(person)
         }
         catch (t: Throwable) {  return@withContext ResultData.Failure(t) }
      }

   companion object {
      //12345678901234567890123
      private const val tag = "ok>PeopleRepositoryImpl"
   }
}