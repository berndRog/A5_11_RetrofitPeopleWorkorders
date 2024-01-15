package de.rogallab.mobile.domain

import de.rogallab.mobile.data.network.httpStatusMessage
import de.rogallab.mobile.domain.utilities.logError
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

//sealed class Resource<T>(val data: T? = null, val message: String? = null) {
//   class Success<T>(data: T?): Resource<T>(data)
//   class Error<T>(message: String, data: T? = null): Resource<T>(data, message)
//   class Loading<T>(val isLoading: Boolean = true): Resource<T>(null)
//}



sealed class ResultData<out T>(
   val value: Any?,
   protected val _tag: String = "ResultData"
) {
   val isSuccess: Boolean = this is Success<*> && value != null
   //val isError:   Boolean = this is Error && value != null
   val isFailure: Boolean = this is Failure && value != null
   val isLoading: Boolean = this is Loading

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
//               is CancellationException -> {
//                  "CancellationException: ${value.localizedMessage}"
//
//               }
               is Exception -> "Exception: ${value.localizedMessage}"
               else -> "Unknown error"
            }
         }
     //    is Error -> message
         else -> null
      }
      return message
   }

   data class  Loading       (val loading:Boolean     ): ResultData<Nothing>(null)
   data class  Success<out T>(val data: T             ): ResultData<T>(data)
   //data class  Error         (val message: String,      val tag:String = "ok>ResultData         .")
   //   : ResultData<Nothing>(message, tag)

   data class  Failure(
      val throwable: Throwable,
      val tag:String = "ok>ResultData         ."
   ) : ResultData<Nothing>(throwable,tag) {
      init {
         this.errorMessageOrNull()?.let{ logError(_tag, it) }
      }
   }

   data object Empty                                   : ResultData<Nothing>(null)   // Singleton
}