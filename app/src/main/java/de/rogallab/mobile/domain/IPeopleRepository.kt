package de.rogallab.mobile.domain

import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.WorkorderDto
import de.rogallab.mobile.domain.entities.Person
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IPeopleRepository {

   // database
   fun selectAll(): Flow<Resource<List<Person>>>
   suspend fun findById(id: UUID): Resource<Person?>
   suspend fun count(): Resource<Int>
   suspend fun add(person: Person): Resource<Unit>
   suspend fun addAll(people: List<Person>): Resource<Unit>
   suspend fun update(person: Person): Resource<Unit>
   suspend fun remove(person: Person): Resource<Unit>

   suspend fun selectByIdWithWorkorders(id: UUID): Person?
   suspend fun findByIdWithWorkorders(id:UUID): Map<PersonDto, List<WorkorderDto>>

   // Webservice
   suspend fun getAll(): Flow<Resource<List<Person>>>
   suspend fun getById(id: UUID): Resource<Person?>
   suspend fun post(person: Person): Resource<Unit>
   suspend fun put(person: Person): Resource<Unit>
   suspend fun delete(person: Person): Resource<Unit>
}

