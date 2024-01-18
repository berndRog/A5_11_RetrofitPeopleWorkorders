package de.rogallab.mobile.domain.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers

@Module
//@InstallIn(SingletonComponent::class)
@InstallIn(ViewModelComponent::class)
object ProvideDomainModules {
                          //12345678901234567890123
   private const val tag = "ok>ProvideDomainModules"

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

}