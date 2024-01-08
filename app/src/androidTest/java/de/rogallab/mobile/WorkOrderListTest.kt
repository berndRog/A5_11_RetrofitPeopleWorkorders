package de.rogallab.mobile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkOrderListTest {

   @get:Rule
   val composeRule = createComposeRule()
/*
   @Test
   fun testTaskList() {
      val tasks = listOf(
         Task(id = 1, label = "Task 1"),
         Task(id = 2, label = "Task 2"),
         Task(id = 3, label = "Task 3"),
      )

      var toDelete = false

      composeRule.setContent {
         TaskList(
            tasks = tasks,
            onClicked = {},
            onClose = {}
         )
      }

      // Verify that the three tasks are rendered.
      composeRule.onNodeWithText("Task 1")
         .assertExists()
      composeRule.onNodeWithText("Task 2")
         .assertExists()
      composeRule.onNodeWithText("Task 3")
         .assertExists()
   }
 */
}