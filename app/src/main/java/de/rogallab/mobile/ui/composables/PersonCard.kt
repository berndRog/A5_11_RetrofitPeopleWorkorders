package de.rogallab.mobile.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun PersonCard(
   firstName: String,
   lastName: String,
   email: String?,
   phone: String?,
   imagePath: String?,
   modifier: Modifier = Modifier,
   elevation: CardElevation = CardDefaults.cardElevation()
) {

   Card(
      modifier = modifier.fillMaxWidth(),
      elevation = elevation,
   ) {
      Row(
         modifier = Modifier.padding(vertical = 4.dp).padding(horizontal = 8.dp),
         verticalAlignment = Alignment.CenterVertically,
      ) {
         Column(
            modifier = Modifier.weight(0.85f)
         ) {
            Text(
               text = "$firstName $lastName",
               style = MaterialTheme.typography.bodyLarge,
            )
            email?.let {
               Text(
                  text = it,
                  style = MaterialTheme.typography.bodyMedium
               )
            }
            phone?.let {
               Text(
                  text = phone,
                  style = MaterialTheme.typography.bodyMedium,
               )
            }
         }
         Column(modifier = Modifier.weight(0.15f)) {
            imagePath?.let { path: String ->
               AsyncImage(
                  model = path,
                  contentDescription = "Bild der Person",
                  modifier = Modifier
                     .size(width = 60.dp, height = 75.dp)
                     .clip(RoundedCornerShape(percent = 5)),
                  alignment = Alignment.Center,
                  contentScale = ContentScale.Crop
               )
            }
         }
      }
   }
}