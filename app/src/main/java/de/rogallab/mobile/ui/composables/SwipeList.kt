package de.rogallab.mobile.ui.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SetSwipeBackgroud(dismissState: DismissState) {
   val direction = dismissState.dismissDirection ?: return

   val colorBox by animateColorAsState(
      when (dismissState.targetValue) {
         DismissValue.Default -> Color.LightGray
         DismissValue.DismissedToEnd -> Color.Green
         DismissValue.DismissedToStart -> Color.Red
      },
      label = ""
   )
   val colorIcon: Color by animateColorAsState(
      when (dismissState.targetValue) {
         DismissValue.Default -> Color.Black
         DismissValue.DismissedToEnd -> Color.DarkGray
         DismissValue.DismissedToStart -> Color.DarkGray //Color.White
      },
      label = ""
   )
   val alignment = when (direction) {
      DismissDirection.StartToEnd -> Alignment.CenterStart
      DismissDirection.EndToStart -> Alignment.CenterEnd
   }
   val icon = when (direction) {
      DismissDirection.StartToEnd -> Icons.Default.Edit
      DismissDirection.EndToStart -> Icons.Default.Delete
   }
   val scale by animateFloatAsState(
      if (dismissState.targetValue == DismissValue.Default) 1.25f else 2.0f,
      label = ""
   )

   Box(
      Modifier
         .fillMaxSize()
         .background(colorBox)
         .padding(horizontal = 20.dp),
      contentAlignment = alignment
   ) {
      Icon(
         icon,
         contentDescription = "Localized description",
         modifier = Modifier.scale(scale),
         tint = colorIcon
      )
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetCardElevation(dismissState: DismissState) =
   CardDefaults.cardElevation(
      defaultElevation = 4.dp,
      pressedElevation = if (dismissState.dismissDirection != null) 8.dp else 0.dp,
      focusedElevation = if (dismissState.dismissDirection != null) 8.dp else 0.dp,
      hoveredElevation = 0.dp,
      draggedElevation = if (dismissState.dismissDirection != null) 8.dp else 0.dp,
      disabledElevation = 0.dp
   )
