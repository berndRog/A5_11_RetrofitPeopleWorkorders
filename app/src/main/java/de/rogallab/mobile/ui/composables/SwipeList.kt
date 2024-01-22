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
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SetSwipeBackgroud(dismissState: DismissState) {

   val (colorBox, colorIcon, alignment, icon, description, scale) =
      determineSwipeProperties(dismissState)

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
   dismissState: DismissState
): SwipeProperties {

   val colorBox: Color = when (dismissState.targetValue) {
      DismissValue.Default -> Color.LightGray
      DismissValue.DismissedToEnd -> Color.Green
      DismissValue.DismissedToStart -> Color.Red
   }

   val colorIcon: Color = when (dismissState.targetValue) {
      DismissValue.Default -> Color.Black
      else -> Color.DarkGray
   }

   val alignment: Alignment = when (dismissState.dismissDirection) {
      DismissDirection.StartToEnd -> Alignment.CenterStart
      DismissDirection.EndToStart -> Alignment.CenterEnd
      else -> Alignment.Center
   }

   val icon: ImageVector = when (dismissState.dismissDirection) {
      DismissDirection.StartToEnd -> Icons.Default.Edit
      DismissDirection.EndToStart -> Icons.Default.Delete
      else -> Icons.Default.Info
   }

   val description: String = when (dismissState.dismissDirection) {
      DismissDirection.StartToEnd -> "Editieren"
      DismissDirection.EndToStart -> "Löschen"
      else -> "Unknown Action"
   }

   val scale = if (dismissState.targetValue == DismissValue.Default)
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
fun SetCardElevation(dismissState: DismissState) =
   CardDefaults.cardElevation(
      defaultElevation = 4.dp,
      pressedElevation = if (dismissState.dismissDirection != null) 8.dp else 4.dp,
      focusedElevation = if (dismissState.dismissDirection != null) 8.dp else 4.dp,
      hoveredElevation = 4.dp,
      draggedElevation = if (dismissState.dismissDirection != null) 8.dp else 4.dp,
      disabledElevation = 0.dp
   )


