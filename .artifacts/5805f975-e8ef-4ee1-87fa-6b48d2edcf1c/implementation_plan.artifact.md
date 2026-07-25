# Implementation Plan - Refined Recently Viewed & Global Header

Refine the Recently Viewed screen and the entire application's navigation feel by implementing a fixed global header and a modern floating back navigation overlay.

## Proposed Changes

### 1. Global Navigation & Brand Continuity

#### [MODIFY] [MainScaffold.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/main/MainScaffold.kt)
- **Anchored Top Bar**: Restore the `TopAppBar` at the root level. This ensures the header (title and tagline) remains perfectly static during all swiping and navigation.
- **Dynamic Contextual Title**: Automatically switch the title text (e.g., "Explore", "Recently Viewed") based on the current navigation route.
- **Modern Overlay Back Button**:
    - Place a circular, elevated floating button in the top-left of the content area.
    - It will sit **below** the header area.
    - Only visible when the user can navigate back.

#### [MODIFY] [BrandHeader.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/BrandHeader.kt)
- Focus purely on branding (Title + Tagline).
- Ensure it continues to observe the global `TaglineManager` for perfect 40s/8s synchronization.

### 2. Screen Optimization (Removing Local Headers)

#### [MODIFY] All Screens
- Remove local `Scaffold` and `TopAppBar` from:
    - `HomeScreen.kt`
    - `CategoryListScreen.kt`
    - `FavoritesScreen.kt`
    - `HistoryScreen.kt`
    - `FactListScreen.kt`
    - `FactDetailScreen.kt`
- This eliminates "Double Headers" and ensures the content always starts exactly where it should.

### 3. Visual Polish for "Recently Viewed"

#### [MODIFY] [HistoryScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/history/HistoryScreen.kt)
- Refine the list layout to utilize the new global structure.
- Ensure the "Recently Viewed" title is elegantly displayed in the fixed top bar.

## Verification Plan

### Manual Verification
- **Header Stability**: Swipe between tabs and verify the tagline/branding does not move or flicker.
- **Back Navigation**: Verify the floating circular arrow appears only when needed and stays below the main brand header.
- **Alignment**: Confirmed "Recently Viewed" title is correctly positioned in the global bar.
- **Sync**: Verify tagline follows 40s/8s rule globally.
