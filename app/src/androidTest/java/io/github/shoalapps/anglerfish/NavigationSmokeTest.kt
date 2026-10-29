package io.github.shoalapps.anglerfish

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @org.junit.Before fun dismissReleaseNotes() {
        compose.waitForIdle()
        if (compose.onAllNodesWithText("Let's dive in").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Let's dive in").performClick()
            compose.waitForIdle()
        }
    }

    @Test fun searchRequiresAQueryAndCanBeCancelled() {
        compose.onNodeWithText("Search").performClick()
        compose.onNodeWithText("Search Reddit").assertExists()
        val submit = hasText("Search") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
        compose.onNode(submit).assertIsNotEnabled()
        compose.onNode(hasSetTextAction()).performTextInput("archlinux")
        compose.onNode(submit).assertIsEnabled()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Search Reddit").assertDoesNotExist()
    }

    @Test fun settingsOpens() {
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("Settings").assertExists()
    }
}
