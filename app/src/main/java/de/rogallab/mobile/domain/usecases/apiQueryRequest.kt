package de.rogallab.mobile.domain.usecases

import de.rogallab.mobile.data.network.httpStatusMessage
import de.rogallab.mobile.domain.Resource
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.domain.utilities.max
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// T = Dto
// R = Entity
suspend fun <T,R> apiQueryRequest(
   tag: String,
   // toData is a function that converts the response.body():T to the return type R
   dataDtoToData: (T) -> R,
   // apiQuery is a function type that returns a Response<T?>
   apiQuery: suspend () -> Response<T?>,
): Resource<R?> =

   try {
      // send the api query to get dto: T?
      val response: Response<T?> = apiQuery()
      // log the response
      logResponse(tag, response)

      // if the response is successful, emit the body
      if (response.isSuccessful) {
         val body = response.body()
         // dataDto: T
         body?.let { dataDto: T ->
            // transform the dataDto:T to the data:R
            val data = dataDtoToData(dataDto)
            Resource.Success(data = data)
         }
         // if the response is successful, but the body is null, emit an error
         ?: run {
            val message = "isSuccessful is true, but body() is null"
            logError(tag, message)
            Resource.Error(message = message)
         }
      // if the response is not successful, emit an error
      } else {
         val message = "${httpStatusMessage(response.code())}"
         logError(tag, message)
         Resource.Error(message = message)
      }
   }
   // if the api call throws an io exception, emit an error
   catch(e: IOException) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, "$message/n ${e.stackTraceToString()}" )
      Resource.Error(message = message)
   }
   // if the api call throws an http exception, emit an error
   catch(e: HttpException) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, "$message/n ${e.stackTraceToString()}")
      Resource.Error(message)
   }
   // if the api call throws an exception, emit an error
   catch (e: Exception) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, message)
      Resource.Error(message)
   }

// helper function to log the response
private fun <T> logResponse(
   tag: String,
   response: Response<T>
) {
   logVerbose(tag, "Request ${response.raw().request.method} ${response.raw().request.url}")
   logVerbose(tag, "Request Headers")
   response.raw().request.headers.forEach {
      val text = "   %-15s %s".format(it.first, it.second )
      logVerbose(tag, "$text")
   }

   val ms = response.raw().receivedResponseAtMillis - response.raw().sentRequestAtMillis
   logVerbose(tag, "took $ms ms")
   logVerbose(tag, "Response isSuccessful ${response.isSuccessful()}")

   logVerbose(tag, "Response Headers")
   response.raw().headers.forEach {
      val text = "   %-15s %s".format(it.first, it.second)
      logVerbose(tag, "$text")
   }

   logVerbose(tag, "Response Body")
   logVerbose(tag, "   Status Code ${response.code().toString().max(100)}")
   logVerbose(tag, "   Status Message ${response.message().toString().max(100)}")
}