package de.rogallab.mobile.data.network

import de.rogallab.mobile.AppStart
import de.rogallab.mobile.domain.utilities.logDebug
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WebserviceBuilder @Inject constructor(
   networkConnectivity: NetworkConnectivity,
   apiKeyInterceptor: ApiKey,
   bearerToken: BearerToken
) {
   private val tag = "ok>WebserviceBuilder"

   var _loggingInterceptor = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
   }

   // OKHttp Client -----------------------------------------------------------
   val okHttpClient = OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(5, TimeUnit.SECONDS)
      .writeTimeout(5, TimeUnit.SECONDS)
      //.addInterceptor(bearerToken)
      //.addInterceptor(apiKeyInterceptor)
      .addInterceptor(networkConnectivity)
      .addInterceptor(_loggingInterceptor)
      .build()

   //-- Retrofit Client -------------------------------------------------------
   val retrofit = Retrofit.Builder()
      .baseUrl(AppStart.base_url)
      .addConverterFactory(GsonConverterFactory.create())
      .client(okHttpClient)
      .build()

   // factory to create the Webservice
   fun <T> create(serviceType: Class<T>, serviceName: String): T {
      logDebug(tag, "create $serviceName")
      return retrofit.create(serviceType)
   }
}