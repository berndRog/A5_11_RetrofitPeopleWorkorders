package de.rogallab.mobile.data.devices.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rogallab.mobile.data.devices.IConnectivityObserver
import de.rogallab.mobile.data.devices.ILocationTracker
import de.rogallab.mobile.data.devices.location.AppLocationTracker
import de.rogallab.mobile.data.devices.network.NetworkConnectivityObserver
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DevicesModule {

   @Binds
   @Singleton
   abstract fun bindLocationTracker(
      appLocationTracker: AppLocationTracker
   ): ILocationTracker

   @Binds
   @Singleton
   abstract fun bindConnectivityTracker(
      networkConnectivityObserver: NetworkConnectivityObserver
   ): IConnectivityObserver
}