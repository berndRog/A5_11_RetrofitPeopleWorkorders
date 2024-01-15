package de.rogallab.mobile.data.devices

import android.location.Location

interface ILocationTracker {
   suspend fun getCurrentLocation(): Location?
}