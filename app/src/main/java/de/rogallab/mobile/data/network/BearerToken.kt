package de.rogallab.mobile.data.network

import de.rogallab.mobile.AppStart
import okhttp3.Interceptor
import okhttp3.Response

class BearerToken(): Interceptor {

   private val _token = AppStart.bearer_token

   override fun intercept(chain: Interceptor.Chain): Response {
      var request = chain.request()
      if(_token.isNullOrEmpty()) return chain.proceed(request)

      if (request.header("No-Authentication") == null) {
         if (!_token.isNullOrEmpty()) {
            val finalToken = "Bearer " + _token
            request = request.newBuilder()
               .addHeader("Authorization", finalToken)
               .build()
         }
      }
      return chain.proceed(request)
   }
}
