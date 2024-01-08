@file:Suppress("RemoveEmptyParenthesesFromLambdaCall")

package de.rogallab.mobile.ui.navigation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.rogallab.mobile.domain.utilities.logDebug

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationBar(
   navController: NavController
) {
   val items = listOf(
      NavScreen.PeopleList,
      NavScreen.WorkordersList,
   )
   NavigationBar(
//    containerColor = MaterialTheme.colorScheme.primary
//    contentColor = MaterialTheme.colors.onSecondary
   ) {
               //12345678901234567890123
      val tag = "ok>AppNavigationBar   ."

      val navBackStackEntry by navController.currentBackStackEntryAsState()
      val currentRoute = navBackStackEntry?.destination?.route

      items.forEach {  item ->

         NavigationBarItem(
            icon = {

               BadgedBox(
                  badge = {
                     if(item.badgeCount != null) {
                        Badge {
                           Text(text = item.badgeCount.toString() )
                        }
                     } else if(item.hasNews) {
                        Badge()
                     }
                  }
               ) {
                  Icon(
                     imageVector =
                        if(currentRoute == item.route) item.selectedIcon
                        else                           item.unSelectedIcon,
                     contentDescription = item.title
                  )
               }
            },
            label = { Text(text = item.title) },
            alwaysShowLabel = true,
            selected = currentRoute == item.route,
            onClick = {
               logDebug(tag,"navigateTo ${item.route}")
               navController.navigate(item.route) {
                  navController.graph.startDestinationRoute?.let { route ->
                     popUpTo(route) { saveState = true  }
                  }
                  // Avoid multiple copies of the same destination when
                  // reselecting the same item
                  launchSingleTop = true
                  // Restore state when reselecting a previously selected item
                  restoreState = true
               }
            }
         )
      }
   }
}