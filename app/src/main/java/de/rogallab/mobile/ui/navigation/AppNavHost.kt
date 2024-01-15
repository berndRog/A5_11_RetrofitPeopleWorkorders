package de.rogallab.mobile.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.rogallab.mobile.ui.people.PeopleSwipeListScreen
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.people.PersonScreen
import de.rogallab.mobile.ui.people.PersonWorkorderDetailScreen
import de.rogallab.mobile.ui.people.PersonWorkorderOverviewScreen
import de.rogallab.mobile.ui.workorders.WorkorderScreen
import de.rogallab.mobile.ui.workorders.WorkorderInputScreen
import de.rogallab.mobile.ui.workorders.WorkordersSwipeListScreen
import de.rogallab.mobile.ui.workorders.WorkordersViewModel
import java.util.UUID

@Composable
fun AppNavHost(
   peopleViewModel: PeopleViewModel = hiltViewModel(),
   workordersViewModel: WorkordersViewModel = hiltViewModel()
) {
   val navHostController: NavHostController = rememberNavController()
   val duration = 500  // in ms

   NavHost(
      navController = navHostController,
      startDestination = NavScreen.PeopleList.route,
      enterTransition = { enterTransition(duration) },
      exitTransition = { exitTransition(duration) },
      popEnterTransition = { popEnterTransition(duration) },
      popExitTransition = { popExitTransition(duration) }
   ) {

      composable(NavScreen.PeopleList.route) {
         PeopleSwipeListScreen(
            navController = navHostController,
            viewModel = peopleViewModel
         )
      }

      composable(NavScreen.PersonInput.route) {
         PersonScreen(
            isInputScreen = true,
            id = null,
            navController = navHostController,
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
         PersonScreen(
            isInputScreen = false,
            id = id,
            navController = navHostController,
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
         PersonWorkorderOverviewScreen(
            personId = id,
            navController = navHostController,
            viewModel = peopleViewModel
         )
      }

      composable(
         route = NavScreen.PersonWorkorderDetail.route + "/{workorderId}",
         arguments = listOf(navArgument("workorderId") { type = NavType.StringType })
      ) { backStackEntry ->
         val id = backStackEntry.arguments?.getString("workorderId")?.let {
            UUID.fromString(it)
         }
         PersonWorkorderDetailScreen(
            workorderId = id,
            navController = navHostController,
            viewModel = workordersViewModel
         )
      }

      composable(
         route = NavScreen.WorkordersList.route,
      ) {
         WorkordersSwipeListScreen(
            navController = navHostController,
            viewModel = workordersViewModel
         )
      }

      composable(
         route = NavScreen.WorkorderInput.route,
      ) {
         WorkorderScreen(
            isInputScreen = false,
            id = null,
            navController = navHostController,
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
         WorkorderScreen(
            isInputScreen = false,
            id = id,
            navController = navHostController,
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