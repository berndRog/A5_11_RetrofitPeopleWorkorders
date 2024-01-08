package de.rogallab.mobile.domain.usecases

import de.rogallab.mobile.data.network.httpStatusMessage
import de.rogallab.mobile.domain.Resource
import de.rogallab.mobile.domain.utilities.logError
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

suspend fun <T, R> apiCommandRequest(
   tag: String,
   data: T,
   dataToDto: (T) -> R,
   apiCommand: suspend (R) -> Response<Unit>
): Resource<Unit> {

   try {
      // transform the data:T to the dataDto:R
      val dto = dataToDto(data)

      // send the api command request
      val response: Response<Unit> = apiCommand(dto)

      // if the response is successful, return Resource.Success
      if (response.isSuccessful) {
         return Resource.Success(data = Unit)
      }
      // if the response is not successful, return Resource.Error
      else {
         val message = "${response.code()}: ${httpStatusMessage(response.code())}"
         logError(tag, message)
         return Resource.Error(message = message)
      }
   }
   // if the api call throws an io exception, emit an error
   catch(e: IOException) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, "$message/n ${e.stackTraceToString()}" )
      return Resource.Error(message = message)
   }
   // if the api call throws an http exception, emit an error
   catch(e: HttpException) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, "$message/n ${e.stackTraceToString()}")
      return Resource.Error(message = message)
   }
   // if the api call throws an exception, emit an error
   catch (e: Exception) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, message)
      return Resource.Error(message = message)
   }
}
