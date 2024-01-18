package de.rogallab.mobile.ui

import android.Manifest
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rogallab.mobile.data.seed.SeedDatabase
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.permissions.PermissionCamera
import de.rogallab.mobile.ui.permissions.PermissionCoarseLocation
import de.rogallab.mobile.ui.permissions.PermissionFineLocation
import de.rogallab.mobile.ui.permissions.PermissionTextNotFound
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
   private val _seedDatabase: SeedDatabase
) : ViewModel() {

   // required permissions
   val permissionsToRequest = mutableListOf<String>(
      Manifest.permission.ACCESS_NETWORK_STATE,
      Manifest.permission.ACCESS_WIFI_STATE,
      Manifest.permission.CAMERA,
//    Manifest.permission.ACCESS_COARSE_LOCATION,
//    Manifest.permission.ACCESS_FINE_LOCATION,
   )

   val visiblePermissionQueue: SnapshotStateList<String> = mutableStateListOf()

   init{
      _seedDatabase.initDatabase()
   }

   override fun onCleared() {
      super.onCleared()
      logInfo(tag, "onCleared()")
      _seedDatabase.disposeImages()
   }

   fun addPermission(
      permission: String,
      isGranted: Boolean
   ) {
      logVerbose(tag, "addPermission $permission $isGranted")
      if (!isGranted && !visiblePermissionQueue.contains(permission)) {
         // permission that have not been granted and are not in the queue
         visiblePermissionQueue.add(permission)
      }
   }

   fun dismissDialog() {
      visiblePermissionQueue.removeLast()
   }

   fun getPermissionText(permission: String, isPermanentlyDeclined: Boolean): String {
      val permissionText = when (permission) {
         Manifest.permission.CAMERA                 -> PermissionCamera()
         Manifest.permission.ACCESS_COARSE_LOCATION -> PermissionCoarseLocation()
         Manifest.permission.ACCESS_FINE_LOCATION   -> PermissionFineLocation()
         else                                       -> PermissionTextNotFound()
      }
      return permissionText.getDescription(isPermanentlyDeclined)
   }

   companion object {
      private const val tag: String = "ok>MainViewModel      ."
   }
}