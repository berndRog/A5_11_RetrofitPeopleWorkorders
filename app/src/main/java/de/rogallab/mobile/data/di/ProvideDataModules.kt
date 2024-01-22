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
import de.rogallab.mobile.data.IWorkordersDao
import de.rogallab.mobile.data.database.AppDatabase
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
object ProvideDataModules {
                          //12345678901234567890123
   private const val tag = "ok>ProvidedataModules ."

   @Provides
   @ViewModelScoped
   fun provideSeedDatabase(
      application: Application, // provided by Hilt
      peopleRepository: IPeopleRepository,
      workordersRepository: IWorkordersRepository,
      dispatcher: CoroutineDispatcher,
      exceptionHandler: CoroutineExceptionHandler,
   ): SeedDatabase {
      logInfo(tag, "providesSeedDatabase()")
      return SeedDatabase(
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
}