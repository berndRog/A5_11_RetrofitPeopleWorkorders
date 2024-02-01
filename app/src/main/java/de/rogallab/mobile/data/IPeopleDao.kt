package de.rogallab.mobile.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.rogallab.mobile.data.models.ImageDto
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.WorkorderDto
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
   @Query("DELETE FROM people WHERE id = :id")
   suspend fun delete(id: UUID)                         // One-Shot Write

//   @Transaction
//   @Query("SELECT * FROM people WHERE id = :id")
//   suspend fun loadPersonWithWorkorders(id: UUID): PersonDtoWithWorkorderDtos?

   @Query(
      "SELECT * FROM people "
     +"  LEFT JOIN images ON people.imageId  = images.id "
     +"WHERE people.id = :id " +
      "  LIMIT 1")
   fun findByIdWithImage(id: UUID): Map<PersonDto, ImageDto?>

   @Query(
      "SELECT * FROM people "
      +"  LEFT JOIN workorders ON people.id = workorders.personId "
      +"WHERE people.id = :id"
      +"  LIMIT 1")
   suspend fun findByIdWithWorkorders(id:UUID): Map<PersonDto, List<WorkorderDto>>?

}