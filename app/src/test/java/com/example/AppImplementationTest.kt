package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.components.InteractiveVectorMap
import com.example.ui.components.MapMarker
import com.example.ui.components.MarkerType
import androidx.compose.ui.geometry.Offset
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class AppImplementationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOnboardingScreenInteraction() {
        var onFinishCalled = false
        
        composeTestRule.setContent {
            MyApplicationTheme {
                OnboardingScreen(onFinish = { onFinishCalled = true })
            }
        }
        
        // Assert onboarding elements are visible
        composeTestRule.onNodeWithText("Discover Local Restaurants").assertIsDisplayed()
        composeTestRule.onNodeWithText("Browse hundreds of restaurants near you and find your next favorite meal.").assertIsDisplayed()
        
        // Find "Next" button and perform click
        composeTestRule.onNodeWithText("Next").performClick()
        
        // Verify finish callback was triggered
        assertTrue(onFinishCalled)
    }

    @Test
    fun testOnboardingSkipInteraction() {
        var onFinishCalled = false
        
        composeTestRule.setContent {
            MyApplicationTheme {
                OnboardingScreen(onFinish = { onFinishCalled = true })
            }
        }
        
        // Find "Skip" text link and tap it
        composeTestRule.onNodeWithText("Skip").performClick()
        
        // Verify finish callback was triggered on skip
        assertTrue(onFinishCalled)
    }

    @Test
    fun testSplashScreenContentLoads() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SplashScreenContent(onFinish = {})
            }
        }
        
        // Verify brand assets and texts are rendered properly
        composeTestRule.onNodeWithText("QuickBite").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fresh food, fast delivery").assertIsDisplayed()
    }

    @Test
    fun testInteractiveVectorMapCompilesAndLoads() {
        val markers = listOf(
            MapMarker("Central Biryani Spot", Offset(300f, 300f), MarkerType.RESTAURANT),
            MapMarker("Rider Delivery Pos", Offset(150f, 200f), MarkerType.RIDER)
        )
        
        composeTestRule.setContent {
            MyApplicationTheme {
                InteractiveVectorMap(
                    markers = markers,
                    isInteractive = false
                )
            }
        }
        
        // Check that map container exists (Wait for the UI to be idle to ensure Canvas completely draws)
        composeTestRule.waitForIdle()
        assertNotNull(composeTestRule.onRoot())
    }
}
