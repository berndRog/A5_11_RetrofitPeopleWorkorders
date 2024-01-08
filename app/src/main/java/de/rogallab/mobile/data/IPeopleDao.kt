package de.rogallab.mobile.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.PersonDtoWithWorkorderDtos
import de.rogallab.mobile.data.models.WorkorderDto
import de.rogallab.mobile.domain.Resource
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface IPeopleDao {
   // QUERIES ---------------------------------------------
   @Query("SELECT * FROM people")
   fun selectAll(): Flow<List<PersonDto>>               // Observable Read
   @Query("SELECT * FROM people WHERE id = :id")        // One-Shot Read
   suspend fun selectById(id: UUID): PersonDto?
   @Query("SELECT COUNT(*) FROM people")
   suspend fun count(): Int                             // One-shot read

   // COMMANDS --------------------------------------------
   @Insert(onConflict = OnConflictStrategy.ABORT)       // One-Shot Write
   suspend fun insert(personDto: PersonDto)
   @Insert(onConflict = OnConflictStrategy.ABORT)
   suspend fun insertAll(peopleDto: List<PersonDto>)    // One-shot write
   @Update
   suspend fun update(personDto: PersonDto)             // One-Shot Write
   @Delete
   suspend fun delete(personDto: PersonDto)             // One-Shot Write
   @Query("DELETE FROM people")
   suspend fun deleteAll()

   @Transaction
   @Query("SELECT * FROM people WHERE id = :id")
   suspend fun findbyIdWithWorkorders(id: UUID): PersonDtoWithWorkorderDtos?

   @Query("SELECT * FROM people "
         +   "LEFT JOIN workorders "
         +      "ON   people.id = workorders.personId "
         +   "WHERE people.id = :id")
   suspend fun loadPersonWithWorkorders(id:UUID): Map<PersonDto, List<WorkorderDto>>
}