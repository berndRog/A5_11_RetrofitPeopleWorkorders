package de.rogallab.mobile


import de.rogallab.mobile.domain.ResultData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import retrofit2.HttpException
import retrofit2.Response

class ResultDataTest {

   @Test
   fun ResultDataResource_Success() {
      // Arrange
      val expected = "Test Data"
      // Act
      val result = ResultData.Success(expected)
      // Assert
      assertTrue(result is ResultData.Success)
      assertTrue(result.isSuccess)
      assertFalse(result.isError)
      assertFalse(result.isFailure)
      assertFalse(result.isLoading)

      val actual = result.getOrNull()
      assertEquals(expected, actual)
   }

   @Test
   fun ResultData_Failure_IOException() {
      // Arrange
      val errorMessage = "Network request failed"
      val exception = IOException(errorMessage)
      // Act
      val result = ResultData.Failure(exception)
      // Assert
      assertTrue(result.isFailure)
      val actual = result.failureOrNull()
      assertEquals(exception, actual)
      val message = result.errorMessageOrNull();
      assertEquals("IO Exception: "+errorMessage, message)
   }

   @Test
   fun ResultData_Failure_HttpException() {
      // Arrange
      val errorString = "Not Found"
      val responseBody = errorString.toResponseBody("text/plain".toMediaTypeOrNull())
      val response: Response<Any> = Response.error(404, responseBody)
      val exception = HttpException(response)
      // Act
      val result = ResultData.Failure(exception)
      // Assert
      assertTrue(result is ResultData.Failure)
      assertTrue(result.isFailure)
      val actual = result.failureOrNull()
      val message = result.errorMessageOrNull()
      assertEquals(exception, actual)
   }


   @Test
   fun ResultData_Loading_State() {
      // Act
      val result = ResultData.Loading(true)
      // Assert
      assertTrue(result is ResultData.Loading)
      assertTrue(result.isLoading)
   }

   @Test
   fun `Resource Empty should have null value`() {
      val emptyResultData = ResultData.Empty
      assertFalse(emptyResultData.isSuccess)
      assertFalse(emptyResultData.isFailure)
      assertFalse(emptyResultData.isLoading)
      assertNull(emptyResultData.getOrNull())
   }
}
