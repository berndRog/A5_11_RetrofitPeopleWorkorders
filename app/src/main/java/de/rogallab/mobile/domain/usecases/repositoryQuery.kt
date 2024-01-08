package de.rogallab.mobile.domain.usecases

import de.rogallab.mobile.domain.Resource
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import java.io.IOException

// T = Dto
// R = Entity
suspend fun <T, R> repositoryQuery(
   tag: String,
   dataDtoToData: (T) -> R,
   query: suspend () -> T?
): Resource<R?> {
   return try {
      query()?.let { dataDto: T ->
         val data: R = dataDtoToData(dataDto)
         logDebug(tag, "repositoryQuery() success")
         Resource.Success(data = data)
      } ?: run {
         val message = "Item with given id not found"
         logError(tag, message)
         Resource.Error(message = message)
      }
   }
   catch (e: IOException) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, message)
      Resource.Error(message = message)
   }
   catch (e: Exception) {
      val message = e.localizedMessage ?: e.stackTraceToString()
      logError(tag, message)
      Resource.Error(message)
   }
}
