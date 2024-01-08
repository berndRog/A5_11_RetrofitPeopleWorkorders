package de.rogallab.mobile.ui.composables

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.rogallab.mobile.R
import de.rogallab.mobile.data.io.writeImageToInternalStorage
import de.rogallab.mobile.domain.utilities.logDebug

// https://fvilarino.medium.com/using-activity-result-contracts-in-jetpack-compose-14b179fb87de

@Composable
fun TakePhotoWithCamera(
   onImagePathChanged: (String?) -> Unit,  // Event ↑
) {
   val tag = "ok>TakePhotoWithCamera."
   val context = LocalContext.current

   // callback camera
   val bitmapState = remember { mutableStateOf<Bitmap?>(value = null) }

   val cameraLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.TakePicturePreview()
   ) { // it:Bitmap? ->
      logDebug(tag, "Photo as bitmap ${it?.byteCount}")
      bitmapState.value = it

      // save bitmap to internal storage of the app
      bitmapState.value?.let { bitmap ->
         writeImageToInternalStorage(context, bitmap)?.let { uriPath: String? ->
            logDebug(tag, "Path $uriPath")
            onImagePathChanged(uriPath) // Event ↑
         }
      }
   }

   Button(
      modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth(),
      onClick = {
         logDebug("ok>Take a photo       .", "Click")
         cameraLauncher.launch()
      }
   ) {
      Row(
         verticalAlignment = Alignment.CenterVertically
      ) {
         Icon(imageVector = Icons.Outlined.AddAPhoto,
            contentDescription = stringResource(R.string.back))

         Text(
            modifier = Modifier.padding(start= 8.dp),
            text = "Bitte nehmen Sie ein Foto auf",
            style = MaterialTheme.typography.bodyMedium
         )
      }
   }
}