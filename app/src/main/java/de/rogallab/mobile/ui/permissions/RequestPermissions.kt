package de.rogallab.mobile.ui.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.MainViewModel
import de.rogallab.mobile.ui.openAppSettings

@Composable
fun RequestPermissions(
   viewModel: MainViewModel = hiltViewModel()
) {
   val tag = "ok>RequestPermissions ."

   val activity: Activity = (LocalContext.current as Activity)
   val context: Context = activity.applicationContext

   // Setup multiple permission request launcher
   val multiplePermissionsRequestLauncher = rememberLauncherForActivityResult(

      contract = ActivityResultContracts.RequestMultiplePermissions(),

      onResult = { permissionMap: Map<String, @JvmSuppressWildcards Boolean> ->
         logVerbose(tag, "rememberLauncherForActivityResult.onResult")
         permissionMap.keys.forEach { permission ->
            viewModel.addPermission(
               permission = permission,
               isGranted = permissionMap[permission] == true
            )
         }
      }
   )

   if (!arePermissionsAlreadyGranted(context, viewModel.permissionsToRequest)) {
      LaunchedEffect(true) {
         logVerbose(tag, "rememberLauncherForActivityResult.launch()")
         multiplePermissionsRequestLauncher.launch(
            viewModel.permissionsToRequest.toTypedArray()
         )
      }
   }

   // if a requested permission is not granted -> ask again or goto appsettings
   viewModel.visiblePermissionQueue
      .reversed()
      .forEach { permission ->

         logDebug(tag, "permissionQueue $permission")

         var isPermanentlyDeclined: Boolean =
            !shouldShowRequestPermissionRationale(activity, permission)
         logVerbose(tag, "isPermanentlyDeclined $isPermanentlyDeclined")

         val permissionText: String =
            viewModel.getPermissionText(permission, isPermanentlyDeclined)
         logVerbose(tag, "permisionText $permissionText")

//         PermissionDialog(
//            permissionText = permissionText,
//            isPermanentlyDeclined = isPermanentlyDeclined,
//            onDismiss = viewModel::dismissDialog,
//            onOkClick = {
//               viewModel.dismissDialog()
//               multiplePermissionsRequestLauncher.launch(
//                  arrayOf(permission)
//               )
//            },
//            onGoToAppSettingsClick = {
//               activity.openAppSettings()
//               activity.finish()
//            }
//         )

         AlertDialog(
            onDismissRequest = {
               logVerbose(tag,"onDismissRequest()")
               viewModel.dismissDialog()
            },
            confirmButton = {
               TextButton(
                  onClick = {
                     if (isPermanentlyDeclined) {
                        logVerbose(tag, "confirmButton -> finish()")
                        activity.finish()
                     } else {
                        logVerbose(tag, "confirmButton -> onOkClick()")
                        viewModel.dismissDialog()
                        // request permission again
                        multiplePermissionsRequestLauncher.launch(
                           arrayOf(permission)
                        )
                     }
                  }
               ) {
                  Text(text = if (isPermanentlyDeclined) {
                                 "App beenden"
                              } else {
                                 "Zustimmen"
                              }
                  )
               }
            },
            dismissButton = {
               TextButton(
                  onClick = {
                     if (isPermanentlyDeclined) {
                        logVerbose(tag,"dismissButton -> onGotoAppSettingsClick()")
                        activity.openAppSettings()

                     } else {
                        logVerbose(tag,"dismissButton -> finish()")
                        viewModel.dismissDialog()
                        activity.finish()
                        // request permission again
                        multiplePermissionsRequestLauncher.launch(
                           arrayOf(permission)
                        )
                     }

                  }
               ) {
                  Text(text = if (isPermanentlyDeclined) {
                                 "App Berechtigungen zeigen"
                              } else {
                                 "App beenden"
                              }
                  )
               }
            },
            title = {
               Text(text = "Berechtigung erforderlich")
            },
            text = {
               Text(
                  text = permissionText
               )
            }
         )

      }
}


fun arePermissionsAlreadyGranted(
   context: Context,
   permissionsToRequest: List<String>
): Boolean {
   permissionsToRequest.forEach { permission ->
      if (ContextCompat.checkSelfPermission(context, permission)
         == PackageManager.PERMISSION_GRANTED) {
         logVerbose("ok>arePermissionsGranted", "already granted: $permission")
      } else {
         // permission must be requested
         return false
      }
   }
   // all permission are already granted
   return true
}


