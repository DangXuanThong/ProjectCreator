package com.dangxuanthong.projectcreator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.dangxuanthong.projectcreator.ui.screen.AndroidProjectScreen
import com.dangxuanthong.projectcreator.ui.screen.MultiplatformProjectScreen
import com.dangxuanthong.projectcreator.ui.theme.ProjectCreatorTheme

@Composable
fun App() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    ProjectCreatorTheme {
        Surface {
            Scaffold(
                topBar = {
                    PrimaryTabRow(selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Android project") }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Multiplatform project") }
                        )
                    }
                }
            ) { innerPadding ->
                AnimatedContent(
                    selectedTabIndex,
                    transitionSpec = {
                        val animSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntOffset.VisibilityThreshold
                        )
                        if (initialState < targetState) {
                            slideIntoContainer(towards = SlideDirection.Left, animSpec) togetherWith
                                slideOutOfContainer(towards = SlideDirection.Left, animSpec)
                        } else {
                            slideIntoContainer(SlideDirection.Right, animSpec) togetherWith
                                slideOutOfContainer(towards = SlideDirection.Right, animSpec)
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                ) { tab ->
                    when (tab) {
                        0 -> AndroidProjectScreen(Modifier.fillMaxSize())
                        1 -> MultiplatformProjectScreen(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}
