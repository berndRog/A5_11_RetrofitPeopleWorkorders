package de.rogallab.mobile.data.repositories

/*
import de.rogallab.mobile.data.network.httpStatusMessage
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.domain.utilities.maxValues
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okio.IOException
import retrofit2.Response


// T = Entity
// R = Dto
suspend fun <T, R> roomCommand(
   tag: String,
   data: T,
   toDto: (T) -> R,
   command: suspend (R) -> Unit
): ResultData<Unit> {
   return try {
      val dataDto = toDto(data)
      command(dataDto)
      logDebug(tag, "roomCommand() success")
      ResultData.Success(data = Unit)
   }
   catch (t: Throwable) {
      return ResultData.Failure(t)
   }
}

// T = Dto
// R = Entity
suspend fun <T, R> roomQuery(
   tag: String,
   toData: (T) -> R,
   query: suspend () -> T?
): ResultData<R?> {
   return try {
      query()?.let { dto: T ->
         val data: R = toData(dto)
         logDebug(tag, "roomQuery() success")
         ResultData.Success(data = data)
      } ?: run {
         ResultData.Failure(Exception("Item with given id not found"))
      }
   } catch (t: Throwable) {
      return ResultData.Failure(t)
   }
}

// T = Dto
// R = Entity
suspend fun <T, R> apiQueryRequestAsFlow(
   tag: String,
   dispatcher: CoroutineDispatcher,
   // toData is a function that converts the response.body():T to the return type R
   toData: (T) -> R,
   // api call is a function type that returns a Response<T>
   apiQuery: suspend () -> Response<List<T>>,
): Flow<ResultData<List<R>>> = flow<ResultData<List<R>>> {

   try {
      // send the api query
      val response: Response<List<T>> = apiQuery()  // Response<List<PersonDto>>>
      // log the response
      logResponse(tag, response)

      // if the response is successful, emit the body
      if (response.isSuccessful) {
         val body: List<T>? = response.body()
         body?.let { dtos: List<T> ->
            val dataEntities: List<R> = dtos.map { dto: T -> toData(dto) }
            emit(ResultData.Success(data = dataEntities))
         } ?: run {
            emit(ResultData.Failure(IOException("response.body() is null")))
         }
      } else {
         emit(ResultData.Failure(java.io.IOException("${httpStatusMessage(response.code())}")))
      }
   } catch (t: Throwable) {
      emit(ResultData.Failure(t))
   }
}.flowOn(dispatcher)

// T = Dto
// R = Entity
suspend fun <T, R> apiQueryRequest(
   tag: String,
   toData: (T) -> R,
   apiQuery: suspend () -> Response<T?>,
): ResultData<R> {

   return try {
      // send the api query to get dto: T?
      val response: Response<T?> = apiQuery()
      // log the response
      logResponse(tag, response)

      if (response.isSuccessful) {
         val body = response.body()
         body?.let { dto: T ->
            // transform the dataDto:T to the data:R
            val data = toData(dto)
            return ResultData.Success(data)
         } ?: run {
            ResultData.Failure(IOException("response.body() is null"))
         }
      } else {
         ResultData.Failure(java.io.IOException("${httpStatusMessage(response.code())}"))
      }
   } catch (t: Throwable) {
       ResultData.Failure(t)
   }
}

suspend fun <T, R> apiCommandRequest(
   data: T,
   toDto: (T) -> R,
   apiCommand: suspend (R) -> Response<Unit>
): ResultData<Unit> {

   return try {
      val dto = toDto(data)
      val response: Response<Unit> = apiCommand(dto)

      if (response.isSuccessful) {
         return ResultData.Success(data = Unit)
      } else {
         ResultData.Failure(java.io.IOException("${httpStatusMessage(response.code())}"))
      }
   } catch (t: Throwable) {
      return ResultData.Failure(t)
   }
}

 */