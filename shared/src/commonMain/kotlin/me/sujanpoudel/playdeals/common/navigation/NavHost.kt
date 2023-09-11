package me.sujanpoudel.playdeals.common.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue

@Composable
fun NavHost(navigator: Navigator) {
  val currentEntry by navigator.currentEntry
  val backStackSize by navigator.backStackCount
  val backDispatchConsumer = LocalBackPressConsumer.current

  // we don't need to handle back press if backstack only has one entry
  if (backStackSize > 1) {
    DisposableEffect(Unit) {
      backDispatchConsumer.addListener(navigator::pop)

      onDispose {
        backDispatchConsumer.removeListener(navigator::pop)
      }
    }
  }

  CompositionLocalProvider(
    Navigator.Local provides navigator,
    LocalViewModelFactory provides navigator.viewModelFactory,
  ) {
    val transitionSpec: AnimatedContentTransitionScope<NavEntry>.() -> ContentTransform = {
      if (initialState.id < targetState.id) { // pushing new entry
        ContentTransform(
          targetState.enter(this),
          targetState.exitTransition(this),
        )
      } else { // popping old entry
        ContentTransform(
          initialState.popEnter(this),
          initialState.popExit(this),
          targetContentZIndex = -1f,
        )
      }
    }

    currentEntry?.let { entry ->
      AnimatedContent(entry, transitionSpec = transitionSpec) {
        it.destination.content()
      }
    }
  }
}
