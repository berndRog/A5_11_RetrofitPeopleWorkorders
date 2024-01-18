package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.Workorder
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IWorkordersRepository {

   // L O C A L   D A T A B A S E
   fun selectAll(): Flow<ResultData<List<Workorder>>>
   suspend fun findById(id: UUID): ResultData<Workorder?>
   suspend fun count(): ResultData<Int>

   suspend fun add(workorder: Workorder): ResultData<Unit>
   suspend fun addAll(workorder: List<Workorder>): ResultData<Unit>
   suspend fun update(workorder: Workorder): ResultData<Unit>
   suspend fun remove(workorder: Workorder): ResultData<Unit>

   suspend fun findByIdWithPerson(id: UUID): ResultData<Map<Workorder, Person?>>

   // W E B S E R V I C E
   fun getAll(): Flow<ResultData<List<Workorder>>>
   suspend fun getById(id: UUID): ResultData<Workorder?>
   suspend fun post(workorder: Workorder): ResultData<Unit>
}