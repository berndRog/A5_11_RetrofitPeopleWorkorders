package de.rogallab.mobile.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WorkorderCard(
   time: String,
   state: String,
   title: String,
   elevation: CardElevation = CardDefaults.cardElevation(),
   modifier: Modifier = Modifier
) {

   Card(
      modifier = modifier.fillMaxWidth(),
      elevation = elevation
   ) {
      Column(
         modifier = Modifier
            .padding(vertical = 4.dp)
            .padding(horizontal = 8.dp)
      ) {
         Row() {
            Text(
               modifier = Modifier.weight(0.7f),
               text = time,
               style = MaterialTheme.typography.bodyMedium,
            )
            Text(
               modifier = Modifier.weight(0.3f),
               textAlign = TextAlign.End,
               text = state,
               style = MaterialTheme.typography.bodyMedium,
            )
         }
         Text(
            modifier = modifier.padding(top = 4.dp),
            text = if(title.length<=40) title
            else title.slice(0..39),
            style = MaterialTheme.typography.bodyLarge,
         )
      }
   }
}