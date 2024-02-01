package de.rogallab.mobile.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.people.PeopleListScreen
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.people.PersonScreen
import de.rogallab.mobile.ui.people.PersonWorkorderScreen
import de.rogallab.mobile.ui.people.PersonWorkorderOverviewScreen
import de.rogallab.mobile.ui.workorders.WorkorderScreen
import de.rogallab.mobile.ui.workorders.WorkordersListScreen
import de.rogallab.mobile.ui.workorders.WorkordersViewModel
import java.util.UUID

@Composable
fun AppNavHost(
   peopleViewModel: PeopleViewModel = hiltViewModel(),
   workordersViewModel: WorkordersViewModel = hiltViewModel()
) {

   val tag = "ok>AppNavHost()    ."
   val navController: NavHostController = rememberNavController()
   val duration = 500  // in ms

   // Observing the navigation state and handle navigation
   LaunchedEffect(peopleViewModel.navState.route) {
      peopleViewModel.navState.route?.let { route ->
         if(peopleViewModel.navState.clearBackStack) {
            // Clearing the Back Stack
            navController.navigate(route) {
               popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
         } else {
            // Current destination saved to the Back Stack
            navController.navigate(route)
         }
         // Reset the navigation event
         peopleViewModel.onNavEventHandled()
      }
   }

   LaunchedEffect(workordersViewModel.navStateValue.route) {
      workordersViewModel.navStateValue.route?.let { route ->
         if (workordersViewModel.navStateValue.clearBackStack) {
            // Clearing the Back Stack
            navController.navigate(route) {
               popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
         } else {
            // Current destination saved to the Back Stack
            navController.navigate(route)
         }
         // Reset the navigation event
         workordersViewModel.onNavEventHandled()
      }
   }

   NavHost(
      navController = navController,
      startDestination = NavScreen.PeopleList.route,
      enterTransition = { enterTransition(duration) },
      exitTransition = { exitTransition(duration) },
      popEnterTransition = { popEnterTransition(duration) },
      popExitTransition = { popExitTransition(duration) }
   ) {

      composable(NavScreen.PeopleList.route) {
         logDebug(tag, "navigate -> PeopleListScreen()")
         PeopleListScreen(
            navController = navController,
            viewModel = peopleViewModel
         )
      }

      composable(NavScreen.PersonInput.route) {
         logDebug(tag, "navigate -> PeopleScreen() isInput true")

         PersonScreen(
            isInputScreen = true,
            id = null,
            navController = navController,
            viewModel = peopleViewModel
         )
      }

      composable(
         route = NavScreen.PersonDetail.route + "/{personId}",
         arguments = listOf(navArgument("personId") { type = NavType.StringType })
      ) { backStackEntry ->
         val id = backStackEntry.arguments?.getString("personId")?.let {
            UUID.fromString(it)
         }
         logDebug(tag, "navigate -> PeopleScreen() isInput false ${id?.as8()}")
         PersonScreen(
            isInputScreen = false,
            id = id,
            navController = navController,
            viewModel = peopleViewModel
         )
      }

      composable(
         route = NavScreen.PersonWorkorderOverview.route + "/{personId}",
         arguments = listOf(navArgument("personId") { type = NavType.StringType })
      ) { backStackEntry ->
         val id = backStackEntry.arguments?.getString("personId")?.let {
            UUID.fromString(it)
         }
         logDebug(tag, "navigate -> PersonWorkorderOverview() ${id?.as8()}")

         PersonWorkorderOverviewScreen(
            id = id,
            navController = navController,
            peopleViewModel = peopleViewModel,
            workordersViewModel = workordersViewModel
         )
      }

      composable(
         route = NavScreen.PersonWorkorderDetail.route + "/{workorderId}",
         arguments = listOf(navArgument("workorderId") { type = NavType.StringType })
      ) { backStackEntry ->
         val id = backStackEntry.arguments?.getString("workorderId")?.let {
            UUID.fromString(it)
         }
         logDebug(tag, "navigate -> PersonWorkorderScreen() ${id?.as8()}")
         PersonWorkorderScreen(
            workorderId = id,
            navController = navController,
            viewModel = workordersViewModel
         )
      }

      composable(
         route = NavScreen.WorkordersList.route,
      ) {
         logDebug(tag, "navigate -> WorkorderListScreen()")
         WorkordersListScreen(
            navController = navController,
            viewModel = workordersViewModel
         )
      }

      composable(
         route = NavScreen.WorkorderInput.route,
      ) {
         logDebug(tag, "navigate -> WorkorderScreen() isInput true")
         WorkorderScreen(
            isInputScreen = true,
            id = null,
            navController = navController,
            viewModel = workordersViewModel
         )
      }

      composable(
         route = NavScreen.WorkorderDetail.route + "/{taskId}",
         arguments = listOf(navArgument("taskId") { type = NavType.StringType })
      ) { backStackEntry ->
         val id = backStackEntry.arguments?.getString("taskId")?.let {
            UUID.fromString(it)
         }
         logDebug(tag, "navigate -> WorkorderScreen() isInput false ${id?.as8()}")
         WorkorderScreen(
            isInputScreen = false,
            id = id,
            navController = navController,
            viewModel = workordersViewModel,
         )
      }
   }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(
   duration: Int
) = fadeIn(animationSpec = tween(duration)) + slideIntoContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Left
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(
   duration: Int
) = fadeOut(animationSpec = tween(duration)) + slideOutOfContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Left
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(
   duration: Int
) = fadeIn(animationSpec = tween(duration)) + slideIntoContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Up
)

private fun popExitTransition(
   duration: Int
) = fadeOut(animationSpec = tween(duration))