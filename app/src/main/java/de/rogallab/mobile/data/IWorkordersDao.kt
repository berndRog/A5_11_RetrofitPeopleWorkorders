package de.rogallab.mobile.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.WorkorderDto
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface IWorkordersDao {
   // QUERIES ---------------------------------------------
   @Query("SELECT * FROM workorders")                          // Observable Read
   fun selectAll(): Flow<MutableList<WorkorderDto>>

   @Query("SELECT * FROM workorders WHERE id = :id")           // One-Shot Read
   suspend fun selectById(id: UUID): WorkorderDto?

   @Query("SELECT COUNT(*) FROM workorders")                   // One-Shot Read
   suspend fun count(): Int

   // COMMANDS --------------------------------------------
   @Insert(onConflict = OnConflictStrategy.ABORT)              // One-Shot Write
   suspend fun insert(workorderDto: WorkorderDto): Long

   @Update(onConflict = OnConflictStrategy.ABORT)
   suspend fun update(workorderDto: WorkorderDto)              // One-Shot Write

   @Query("DELETE FROM workorders WHERE id = :id")
   suspend fun delete(id: UUID)                                // One-Shot Write
}