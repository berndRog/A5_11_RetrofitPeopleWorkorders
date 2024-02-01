package de.rogallab.mobile.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.people.PersonUiEvent

@Composable
fun SelectAndShowImage(
   imagePath: String?,                                  // State ↓
   onImagePathChanged: (PersonUiEvent, String) -> Unit  // Event ↑
) {
   val tag = "ok>SelectAndShowImage ."

   Row(
      modifier = Modifier
         .padding(vertical = 8.dp)
         .fillMaxWidth()
   ) {
      imagePath?.let { path:String ->                  // State ↓
         logDebug(tag, "ContactImage $path")
         AsyncImage(
            model = path,
            contentDescription = "Bild des Kontakts",
            modifier = Modifier
               .size(width = 150.dp, height = 200.dp)
               .clip(RoundedCornerShape(percent = 5)),
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop
         )
      }
//    ?: run {          // else ... show chips
      .run {            // and ... always show chips
         Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
         ) {

            SelectPhotoFromGallery(
               onImagePathChanged = {
                  onImagePathChanged(PersonUiEvent.ImagePath, it) // Event ↑
              }
            )

            Spacer(modifier = Modifier.padding(vertical = 4.dp))

            TakePhotoWithCamera(
               onImagePathChanged = {
                  onImagePathChanged(PersonUiEvent.ImagePath, it) // Event ↑
               }
            )
         }
      }
   }
}