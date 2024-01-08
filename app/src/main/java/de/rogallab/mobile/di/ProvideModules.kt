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
import de.rogallab.mobile.data.database.AppDatabase
import de.rogallab.mobile.data.network.ApiKey
import de.rogallab.mobile.data.network.BearerToken
import de.rogallab.mobile.data.network.NetworkConnection
import de.rogallab.mobile.data.network.NetworkConnectivity
import de.rogallab.mobile.data.network.WebserviceBuilder
import de.rogallab.mobile.data.seed.Seed
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
object ProvideModules {
                          //12345678901234567890123
   private const val tag = "ok>AppProvidesModules ."

   @Provides
   @ViewModelScoped
   fun provideContext(
      application: Application // provided by Hilt
   ): Context {
      logInfo(tag, "providesContext()")
      return application.applicationContext
   }

   @Provides
   @ViewModelScoped
   fun provideCoroutineExceptionHandler(
   ): CoroutineExceptionHandler {
      logInfo(tag, "providesCoroutineExceptionHandler()")
      return CoroutineExceptionHandler { _, exception ->
         exception.localizedMessage?.let {
            logError("ok>CoroutineException", it)
         } ?: run {
            exception.stackTrace.forEach {
               logError("ok>CoroutineException", it.toString())
            }
         }
      }
   }
   @Provides
   @ViewModelScoped
   fun provideCoroutineDispatcher(
   ): CoroutineDispatcher {
      logInfo(tag, "providesCoroutineDispatcher()")
      return Dispatchers.IO
   }

   @Provides
   @ViewModelScoped
   fun provideSeed(
      application: Application,
      peopleRepository: IPeopleRepository,
      workordersRepository: IWorkordersRepository,
      dispatcher: CoroutineDispatcher,
      exceptionHandler: CoroutineExceptionHandler
   ): Seed {
      logInfo(tag, "providesSeed()")
      return Seed(
         application,
         peopleRepository,
         workordersRepository,
         dispatcher,
         exceptionHandler
      )
   }

   @Provides
   @ViewModelScoped
   fun providePeopleDao(
      database: AppDatabase
   ): IPeopleDao {
      logInfo(tag, "providesIPeopleDao()")
      return database.createPeopleDao()
   }

   @Provides
   @ViewModelScoped
   fun provideWorkOrdersDao(
      database: AppDatabase
   ): IWorkordersDao {
      logInfo(tag, "providesIWorkordersDao()")
      return database.createWordordersDao()
   }

   @Provides
   @ViewModelScoped
   fun provideAppDatabase(
      application: Application // provided by Hilt
   ): AppDatabase {
      logInfo(tag, "providesAppDatabase()")
      return Room.databaseBuilder(
         application.applicationContext,
         AppDatabase::class.java,
         AppStart.database_name
      ).fallbackToDestructiveMigration()
         .build()
   }

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
      logInfo(tag,"provideNewsWebservice()")
      WebserviceBuilder(
         networkConnectivity,
         apiKey,
         bearerToken
      ).apply{
         return create(IPeopleWebservice::class.java, "PeopleWebservice")
      }
   }
}