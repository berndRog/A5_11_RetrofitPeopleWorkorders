package de.rogallab.mobile.domain

import de.rogallab.mobile.data.network.httpStatusMessage
import de.rogallab.mobile.domain.utilities.logError
import retrofit2.HttpException
import java.io.IOException

//sealed class Resource<T>(val data: T? = null, val message: String? = null) {
//   class Success<T>(data: T?): Resource<T>(data)
//   class Error<T>(message: String, data: T? = null): Resource<T>(data, message)
//   class Loading<T>(val isLoading: Boolean = true): Resource<T>(null)
//}



sealed class ResultData<out T>(
   val value: Any?,
   protected val _tag: String = "ResultData"
) {
   val isLoading: Boolean = this is Loading
   val isSuccess: Boolean = this is Success<*> && value != null
   val isFailure: Boolean = this is Failure && value != null

   fun getOrNull(): T? =
      when(this) {
         is Success -> value as T
         else -> null
      }

   fun failureOrNull(): Throwable? =
      when(this) {
         is Failure -> value as Throwable
         else -> null
      }

   fun errorMessageOrNull(): String? {
      val message = when (this) {
         is Failure -> {
            when (value) {
               is IOException -> "IO Exception: ${value.localizedMessage}"
               is HttpException -> "HTTP Exception: ${httpStatusMessage(value.code())}"
               is Exception -> "Exception: ${value.localizedMessage}"
               else -> "Unknown error"
            }
         }
         else -> null
      }
      return message
   }

   data class  Loading       (val loading:Boolean     ): ResultData<Nothing>(null)
   data class  Success<out T>(val data: T             ): ResultData<T>(data)
   data class  Failure       (val throwable: Throwable): ResultData<Nothing>(throwable)
}