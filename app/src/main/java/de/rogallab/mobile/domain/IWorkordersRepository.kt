package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.Workorder
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IWorkordersRepository {

   fun readAll(): Flow<List<Workorder>>
   suspend fun findById(id: UUID): Workorder?
   suspend fun count(): Int

   suspend fun add(workorder: Workorder): Boolean
   suspend fun addAll(workorder: List<Workorder>): Boolean
   suspend fun update(workorder: Workorder): Boolean
   suspend fun remove(workorder: Workorder): Boolean

   suspend fun findByIdWithPerson(id: UUID): Map<Workorder, Person?>
}