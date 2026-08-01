# Fix Navigation Bar Overlap

Add consistent bottom padding to all scrollable screens to ensure the last element is not hidden behind the floating navigation bar.

## User Review Required

> [!NOTE]
> The floating navigation bar has a total footprint of approximately 96dp (72dp height + 24dp bottom padding). I will add 112dp of bottom padding/spacing to all scrollable screens to ensure a comfortable margin.

## Proposed Changes

### UI Components

#### [MODIFY] [HomeScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/home/HomeScreen.kt)
- Update `LazyColumn` `contentPadding` to include `bottom = 112.dp`.

#### [MODIFY] [CategoryListScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/categories/CategoryListScreen.kt)
- Update `LazyColumn` `contentPadding` bottom from `80.dp` to `112.dp`.

#### [MODIFY] [FavoritesScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/favorites/FavoritesScreen.kt)
- Update `LazyColumn` `contentPadding` bottom from `80.dp` to `112.dp`.

#### [MODIFY] [SettingsScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/settings/SettingsScreen.kt)
- Update `LazyColumn` `contentPadding` bottom from `8.dp` to `112.dp`.

#### [MODIFY] [HistoryScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/history/HistoryScreen.kt)
- Update `LazyColumn` `contentPadding` bottom from `80.dp` to `112.dp`.

#### [MODIFY] [FactListScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/facts/FactListScreen.kt)
- Update `LazyColumn` `contentPadding` bottom from `80.dp` to `112.dp`.

#### [MODIFY] [ProfileScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/profile/ProfileScreen.kt)
- Update the bottom `Spacer` height from `20.dp` to `112.dp`.

#### [MODIFY] [NotificationsScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/notifications/NotificationsScreen.kt)
- Update `LazyColumn` `contentPadding` bottom from `16.dp` to `112.dp`.

#### [MODIFY] [CollectionDetailScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/collections/CollectionDetailScreen.kt)
- Update the bottom `Spacer` height from `40.dp` to `112.dp`.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to each screen.
- Scroll to the very bottom of each screen (Home, Explore, Saved, Settings, Profile, etc.).
- Verify that the last element (like a card, button, or tagline) is fully visible above the floating navigation bar.
