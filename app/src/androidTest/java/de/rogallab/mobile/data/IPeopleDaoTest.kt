package de.rogallab.mobile.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rogallab.mobile.data.database.AppDatabase
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.seed.Seed
import de.rogallab.mobile.domain.mapping.toPersonDto
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IPeopleDaoTest {

   private lateinit var database: AppDatabase
   private lateinit var peopleDao: IPeopleDao
   private lateinit var seed: Seed

   @Before
   fun setup() {
      // Create an in-memory version of the database
      database = Room.inMemoryDatabaseBuilder(
         ApplicationProvider.getApplicationContext(),
         AppDatabase::class.java
      )  .allowMainThreadQueries()
         .build()

//
//      database = Room.databaseBuilder(
//         ApplicationProvider.getApplicationContext(),
//         AppDatabase::class.java,
//         "A05_11_WorkmanagerTest"
//      )  .allowMainThreadQueries()
//         .build()


      peopleDao = database.createPeopleDao()
      seed = Seed(ApplicationProvider.getApplicationContext())
   }

   @After
   fun closeDb() {
      database.clearAllTables()
      database.close()
   }

   @Test
   fun selectAll_returnsAllPeople() = runTest {
      // Arrange
      val peopleDto = seed.people.map { it -> toPersonDto(it) }
      peopleDao.insertAll(peopleDto)
      // Act
      var actual: List<PersonDto> = emptyList()
      peopleDao.selectAll().collect{ it: List<PersonDto> ->
         actual = it
      }
      // Assert
      assert(actual.size == peopleDto.size)
      assert(actual.containsAll(peopleDto))
   }

//   @Test
//   fun selectById_excludesSpecifiedId() = runTest {
//      val person1 = PersonDto(id = 1, /* ... */)
//      val person2 = PersonDto(id = 2, /* ... */)
//      peopleDao.insert(person1, person2)
//
//      val people = peopleDao.selectById(1).first()
//
//      assert(people.size == 1)
//      assert(people.first().id == 2)
//   }
}
