//package de.rogallab.mobile
//
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import org.junit.Rule
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class WorkOrderItem1Test {
//
//   @get:Rule
//   val composeRule = createComposeRule()
//
///*
//   @Test
//   fun testCheckboxIsChecked() {
//      composeRule.setContent {
//         TaskItem1(id = 1, label = "My task", onClose = {}, onClicked = {})
//      }
//
//      // Verify that the checkbox is not checked by default.
//      composeRule.onNodeWithTag("CheckBox1")
//         .assertExists()
//         .assertIsEnabled()
//         .assertIsOff()
//
//      // Click on the checkbox.
//      composeRule.onNodeWithTag("CheckBox1")
//         .performClick()
//
//      // Verify that the checkbox is now checked.
//      composeRule.onNodeWithTag("CheckBox1")
//         .assertIsOn()
//   }
//
//   @Test
//   fun testCloseButtonIsClicked() {
//
//      var clickedId = -1
//
//      composeRule.setContent {
//         WorkOrder(decription = "My task", onClose = {}, onClicked = {id -> clickedId = id})
//      }
//
//      // Verify that the close button is enabled.
//      composeRule.onNodeWithTag("CloseButton")
//         .assertExists()
//         .assertIsEnabled()
//
//      // Click on the close button.
//      composeRule.onNodeWithTag("CloseButton")
//         .performClick()
//         .assert(hasClickAction())
//
//      Truth.assertThat(clickedId).isEqualTo(1)
//
//   }
// */
//}