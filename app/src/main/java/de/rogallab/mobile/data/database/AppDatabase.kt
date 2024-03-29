package de.rogallab.mobile.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rogallab.mobile.AppStart
import de.rogallab.mobile.data.IPeopleDao
import de.rogallab.mobile.data.IWorkordersDao
import de.rogallab.mobile.data.models.ImageDto
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.WorkorderDto

@Database(
   entities = [PersonDto::class, WorkorderDto::class, ImageDto::class],
   version = AppStart.database_version,
   exportSchema = false
)
@TypeConverters(ZonedDateTimeConverters::class, DurationConverters::class, UUIDConverter::class)
abstract class AppDatabase : RoomDatabase() {
   // The database exposes DAOs through an abstract "getter" method for each @Dao.
   abstract fun createPeopleDao(): IPeopleDao
   abstract fun createWordordersDao(): IWorkordersDao
}