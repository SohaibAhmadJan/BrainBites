# Fix Achievement Cards Layout

Adjust the `AchievementCard` component and its usages to ensure it fills the available width in the Profile screen, removing the vacant space on the right.

## User Review Required

> [!NOTE]
> I am removing the hardcoded `280.dp` width from the `AchievementCard` component itself. Callers will now be responsible for providing width constraints. This allows the card to be full-width in the Profile screen and maintain its fixed size in the Home screen's horizontal list.

## Proposed Changes

### UI Components

#### [MODIFY] [AchievementCard.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/AchievementCard.kt)
- Remove `.width(280.dp)` from the internal `Card` modifier.

#### [MODIFY] [ProfileScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/profile/ProfileScreen.kt)
- Update `AchievementsSection` to pass `Modifier.fillMaxWidth()` to `AchievementCard`.

#### [MODIFY] [HomeScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/home/HomeScreen.kt)
- Update the `AchievementCard` call in the `LazyRow` to include `Modifier.width(280.dp)` to maintain the existing horizontal scroll behavior.

## Verification Plan

### Manual Verification
- **Profile Screen**: Navigate to the Profile screen and verify that achievement cards now stretch to fill the entire width of the screen.
- **Home Screen**: Navigate to the Home screen and verify that the achievements in the horizontal list still have their 280dp width and scroll correctly.
