package de.rogallab.mobile.domain

import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.WorkorderDto
import de.rogallab.mobile.domain.entities.Person
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IPeopleRepository {

   // database
   fun selectAll(): Flow<ResultData<List<Person>>>
   suspend fun findById(id: UUID): ResultData<Person?>
   suspend fun count(): ResultData<Int>
   suspend fun add(person: Person): ResultData<Unit>
   suspend fun addAll(people: List<Person>): ResultData<Unit>
   suspend fun update(person: Person): ResultData<Unit>
   suspend fun remove(person: Person): ResultData<Unit>

   suspend fun selectByIdWithWorkorders(id: UUID): ResultData<Person?>
   suspend fun findByIdWithWorkorders(id:UUID): ResultData<Person?>

   // Webservice
   suspend fun getAll(): Flow<ResultData<List<Person>>>
   suspend fun getById(id: UUID): ResultData<Person?>
   suspend fun post(person: Person): ResultData<Unit>
   suspend fun put(person: Person): ResultData<Unit>
   suspend fun delete(person: Person): ResultData<Unit>
}

