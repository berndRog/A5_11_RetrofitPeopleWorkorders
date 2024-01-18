package de.rogallab.mobile.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import de.rogallab.mobile.AppStart
import de.rogallab.mobile.data.IPeopleDao
import de.rogallab.mobile.data.IPeopleWebservice
import de.rogallab.mobile.data.IWorkordersDao
import de.rogallab.mobile.data.IWorkordersWebservice
import de.rogallab.mobile.data.ImagesWebservice
import de.rogallab.mobile.data.database.AppDatabase
import de.rogallab.mobile.data.network.ApiKey
import de.rogallab.mobile.data.network.BearerToken
import de.rogallab.mobile.data.network.NetworkConnection
import de.rogallab.mobile.data.network.NetworkConnectivity
import de.rogallab.mobile.data.network.WebserviceBuilder
import de.rogallab.mobile.data.seed.SeedDatabase
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.IWorkordersRepository
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers

@Module
//@InstallIn(SingletonComponent::class)
@InstallIn(ViewModelComponent::class)
object ProvideNetworkModules {
                          //12345678901234567890123
   private const val tag = "ok>ProvideNetworkModul."

   @Provides
   @ViewModelScoped
   fun provideNetworkConnection(
      context: Context
   ): NetworkConnection {
      logInfo(tag,"provideNetworkConnection()")
      return NetworkConnection(context)
   }

   @Provides
   @ViewModelScoped
   fun provideNetworkConnectivityInterceptor(
      networkConnection: NetworkConnection
   ): NetworkConnectivity {
      logInfo(tag,"provideNetworkConnectivityInterceptor()")
      return NetworkConnectivity(networkConnection)
   }

   @Provides
   @ViewModelScoped
   fun provideApiKeyInterceptor(
   ): ApiKey {
      logInfo(tag,"provideApiKeyInterceptor()")
      return ApiKey()
   }

   @Provides
   @ViewModelScoped
   fun provideBearerTokenInterceptor(
   ): BearerToken {
      logInfo(tag,"provideBearerTokenInterceptor()")
      return BearerToken()
   }

   @Provides
   @ViewModelScoped
   fun providePeopleWebservice(
      networkConnectivity: NetworkConnectivity,
      apiKey: ApiKey,
      bearerToken: BearerToken
   ): IPeopleWebservice {
      logInfo(tag,"providePeopleWebservice()")
      WebserviceBuilder(
         networkConnectivity,
         apiKey,
         bearerToken
      ).apply{
         return create(IPeopleWebservice::class.java, "PeopleWebservice")
      }
   }

   @Provides
   @ViewModelScoped
   fun provideWorkordersWebservice(
      networkConnectivity: NetworkConnectivity,
      apiKey: ApiKey,
      bearerToken: BearerToken
   ): IWorkordersWebservice {
      logInfo(tag,"provideWorkordersWebservice()")
      WebserviceBuilder(
         networkConnectivity,
         apiKey,
         bearerToken
      ).apply{
         return create(IWorkordersWebservice::class.java, "WorkordersWebservice")
      }
   }

   @Provides
   @ViewModelScoped
   fun provideImagesWebservice(
      networkConnectivity: NetworkConnectivity,
      apiKey: ApiKey,
      bearerToken: BearerToken
   ): ImagesWebservice {
      logInfo(tag,"provideImagesWebservice()")
      WebserviceBuilder(
         networkConnectivity,
         apiKey,
         bearerToken
      ).apply{
         return create(ImagesWebservice::class.java, "ImagesWebservice")
      }
   }
}