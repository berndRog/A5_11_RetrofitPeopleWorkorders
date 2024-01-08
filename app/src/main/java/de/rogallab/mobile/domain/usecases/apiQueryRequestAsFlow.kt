package de.rogallab.mobile.domain.usecases

import de.rogallab.mobile.data.network.httpStatusMessage
import de.rogallab.mobile.domain.Resource
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.domain.utilities.max
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okio.IOException
import retrofit2.HttpException
import retrofit2.Response


// T = Dto
// R = Entity
suspend fun <T, R> apiQueryRequestAsFlow(
   tag: String,
   dispatcher: CoroutineDispatcher,
   exceptionHandler: CoroutineExceptionHandler,
   // toData is a function that converts the response.body():T to the return type R
   toData: (T) -> R,
   // api call is a function type that returns a Response<T>
   apiQuery: suspend () -> Response<List<T>>,
): Flow<Resource<List<R>>> = flow<Resource<List<R>>> {

   try {
      // send the api query
      val response: Response<List<T>> = apiQuery()  // Response<List<PersonDto>>>
      // log the response
      logResponse(tag, response)

      // if the response is successful, emit the body
      if (response.isSuccessful) {
         val body: List<T>? = response.body()
         body?.let { dataDtos: List<T> ->
            // transform the dataDto:T to the data:R
            val dataEntities: List<R> = dataDtos.map { dataDto: T -> toData(dataDto) }
            emit(Resource.Success(data = dataEntities))
         }
         // if the response is successful, but the body is null, emit an error
         ?: run {
            val message = "isSuccessful is true, but body() is null"
            logError(tag, message)
            emit(Resource.Error(message = message))
         }
      // if the response is not successful, emit an error
      } else {
         val message = "${httpStatusMessage(response.code())}"
         logError(tag, message)
         emit(Resource.Error(message = message))
      }
   }
   // if the api call throws an io exception, emit an error
   catch(e: IOException) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, "$message/n ${e.stackTraceToString()}" )
      emit(Resource.Error(message = message))
   }
   // if the api call throws an http exception, emit an error
   catch(e: HttpException) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, "$message/n ${e.stackTraceToString()}")
      emit(Resource.Error(message))
   }
   // if the api call throws an exception, emit an error
   catch (e: Exception) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, message)
      emit(Resource.Error(message))
   }
// flowOn the dispatcher and exceptionHandler
}.flowOn(dispatcher+exceptionHandler)


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