package de.rogallab.mobile.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SetSwipeBackgroud(dismissBoxState: SwipeToDismissBoxState) {

   val (colorBox, colorIcon, alignment, icon, description, scale) =
      determineSwipeProperties(dismissBoxState)

   Box(
      Modifier
         .fillMaxSize()
         .background(
            color = colorBox,
            shape = RoundedCornerShape(15.dp)
         )
         .padding(horizontal = 20.dp),
      contentAlignment = alignment
   ) {
      Icon(
         icon,
         contentDescription = description,
         modifier = Modifier.scale(scale),
         tint = colorIcon
      )
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun determineSwipeProperties(
   dismissBoxState: SwipeToDismissBoxState
): SwipeProperties {

   val colorBox: Color = when (dismissBoxState.targetValue) {
      SwipeToDismissBoxValue.Settled -> Color.LightGray
      SwipeToDismissBoxValue.StartToEnd -> Color.Green    // move to right
      SwipeToDismissBoxValue.EndToStart -> Color.Red      // move to left
   }

   val colorIcon: Color = when (dismissBoxState.targetValue) {
      SwipeToDismissBoxValue.Settled -> Color.Black
      else -> Color.DarkGray
   }

   val alignment: Alignment = when (dismissBoxState.dismissDirection) {
      SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
      SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
      else -> Alignment.Center
   }

   val icon: ImageVector = when (dismissBoxState.dismissDirection) {
      SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit   // left
      SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete // right
      else -> Icons.Default.Info
   }

   val description: String = when (dismissBoxState.dismissDirection) {
      SwipeToDismissBoxValue.StartToEnd -> "Editieren"
      SwipeToDismissBoxValue.EndToStart -> "Löschen"
      else -> "Unknown Action"
   }

   val scale = if (dismissBoxState.targetValue == SwipeToDismissBoxValue.Settled)
      1.25f else 1.5f

   return SwipeProperties(
      colorBox, colorIcon, alignment, icon, description, scale)
}

data class SwipeProperties(
   val colorBox: Color,
   val colorIcon: Color,
   val alignment: Alignment,
   val icon: ImageVector,
   val description: String,
   val scale: Float
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun setCardElevation(dismissBoxState: SwipeToDismissBoxState) =
   CardDefaults.cardElevation(
      defaultElevation = 8.dp,
      pressedElevation =  8.dp,
      focusedElevation =  8.dp,
      hoveredElevation = 8.dp,
      draggedElevation =  8.dp,
      disabledElevation = 0.dp
   )


