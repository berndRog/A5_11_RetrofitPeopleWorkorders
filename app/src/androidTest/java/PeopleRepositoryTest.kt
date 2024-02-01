//package de.rogallab.mobile
//
//import de.rogallab.mobile.data.IPeopleDao
//import de.rogallab.mobile.data.IPeopleWebservice
//import de.rogallab.mobile.domain.IPeopleRepository
//import de.rogallab.mobile.domain.ResultData
//import io.mockk.coEvery
//import io.mockk.mockk
//import junit.framework.TestCase.assertEquals
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.runTest
//import org.junit.Before
//import org.junit.Test
//import java.io.IOException
//
//@ExperimentalCoroutinesApi
//class PeopleRepositoryTest {
//
//   private lateinit var repository: IPeopleRepository
//   private val peopleDao = mockk<IPeopleDao>()
//   private val peopleWebService = mockk<IPeopleWebservice>()
//
//   @Before
//   fun setup() {
//      //repository = PeopleRepositoryImpl(peopleDao, peopleWebService, Dispatchers.Unconfined, /* other dependencies */)
//   }
//
//   @Test
//   fun count_success() = runTest {
//
//      coEvery { peopleDao.count() } returns 10
//
//      val result = repository.count()
//
//      assert(result is ResultData.Success)
//      assertEquals(10, (result as ResultData.Success).data)
//   }
//
//   @Test
//   fun count_with_IOException() = runTest {
//      coEvery { peopleDao.count() } throws IOException("Network Error")
//
//      val result = repository.count()
//
//      assert(result is ResultData.Error)
//      assertEquals("Network Error", (result as ResultData.Error).message)
//   }
//
//   @Test
//   fun count_with_Exception() = runTest {
//      coEvery { peopleDao.count() } throws Exception("Generic Error")
//
//      val result = repository.count()
//
//      assert(result is ResultData.Error)
//      assertEquals("Generic Error", (result as ResultData.Error).message)
//   }
//}