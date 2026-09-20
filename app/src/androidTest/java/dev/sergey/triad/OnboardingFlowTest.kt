package dev.sergey.triad

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class OnboardingFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun creatingProfileShowsStudy() {
        rule.onNodeWithTag("name").performTextInput("Sergey")
        rule.onNodeWithTag("create_profile").assertExists()
    }
}
