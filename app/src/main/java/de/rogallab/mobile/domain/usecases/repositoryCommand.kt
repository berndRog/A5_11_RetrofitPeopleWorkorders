package de.rogallab.mobile.domain.usecases

import de.rogallab.mobile.domain.Resource
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import java.io.IOException

suspend fun <T, R> repositoryCommand(
   tag: String,
   data: T,
   toDto: (T) -> R,
   command: suspend (R) -> Unit
): Resource<Unit> {
   return try {
      val dataDto = toDto(data)
      command(dataDto)
      logDebug(tag, "repositoryCommand() success")
      Resource.Success(data = Unit)
   }
   catch(e: IOException) {
      val message = e.message ?: e.stackTraceToString()
      logError(tag, "$message/n ${e.stackTraceToString()}" )
      Resource.Error(message = message)
   }
   catch (e: Exception) {
      val message = e.localizedMessage ?: e.stackTraceToString()
      logError(tag, message)
      Resource.Error(message = message)
   }
}
