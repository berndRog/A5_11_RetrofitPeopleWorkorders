package de.rogallab.mobile.data.network

import de.rogallab.mobile.AppStart
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class ApiKey : Interceptor {

   private val _key = AppStart.api_key

   override fun intercept(chain: Interceptor.Chain): Response {
      val original: Request = chain.request()
      if(_key.isNullOrEmpty()) return chain.proceed(original)

      val request: Request = original.newBuilder()
         .header("X-API-Key",_key)
         //          .header("X-Session", getServerSession())
         .method(original.method, original.body)
         .build()
      return chain.proceed(request)
   }
}