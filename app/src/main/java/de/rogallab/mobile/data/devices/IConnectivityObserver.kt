package de.rogallab.mobile.data.devices

import kotlinx.coroutines.flow.Flow

interface IConnectivityObserver {

   fun observe(): Flow<Status>

   enum class Status {
      Available, Unavailable, Losing, Lost
   }
}
