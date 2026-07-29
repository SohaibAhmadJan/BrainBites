# Walkthrough - Floating Back Button

I have implemented a global floating back button that appears on all "branch" screens (screens that are not the root pager). This button allows users to navigate back to the previous screen, eventually reaching the root screen as requested.

## Changes

### [MainScaffold.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/main/MainScaffold.kt)

- **Floating Back Button**: Added a `SmallFloatingActionButton` that floats at the top-left of the screen.
- **Adaptive Padding**: The `TopAppBar` title now automatically shifts to the right when the back button is visible to prevent any overlap with the `BrandHeader`.
- **Smooth Transitions**: Used `AnimatedVisibility` with scale and fade animations for a polished entry and exit of the back button.
- **Navigation Logic**: The button uses `navController.popBackStack()` to move back through the history, ensuring users can return to the root screen (`root_pager`) from any depth.

```kotlin
// Snippet of the implementation in MainScaffold.kt
AnimatedVisibility(
    visible = showBackButton,
    enter = fadeIn() + scaleIn(initialScale = 0.8f),
    exit = fadeOut() + scaleOut(targetScale = 0.8f),
    modifier = Modifier
        .statusBarsPadding()
        .padding(start = 12.dp, top = 8.dp)
        .align(Alignment.TopStart)
) {
    SmallFloatingActionButton(
        onClick = { navController.popBackStack() },
        // ... styling ...
    )
}
```

## Verification Results

### Manual Verification
- Navigated from Home -> Daily Teaser: Back button appeared and returned to Home.
- Navigated from Explore -> Categories -> Fact Detail: Back button allowed sequential return to Explore.
- Verified that on the main tabs (Home, Explore, Saved, Settings), the back button is hidden and the title returns to its original position.

> [!TIP]
> The back button is placed with `statusBarsPadding()`, ensuring it stays clear of the system status bar on all devices.
