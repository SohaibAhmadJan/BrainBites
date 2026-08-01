# Achievement Cards Layout Fix

I have adjusted the `AchievementCard` layout to ensure that cards in the Profile screen occupy the full width, removing the previous vacant space on the right.

## Changes Made

### Component Decoupling
- **AchievementCard.kt**: Removed the hardcoded `.width(280.dp)` from the base component. This makes the card flexible and reusable in different layout contexts.

### Screen-Specific Styling
- **ProfileScreen.kt**: Updated the `AchievementsSection` to pass `Modifier.fillMaxWidth()` to each `AchievementCard`. This ensures they stretch to fit the screen, creating a balanced and professional vertical list.
- **HomeScreen.kt**: Updated the horizontal `LazyRow` to explicitly set `Modifier.width(280.dp)` for each card. This preserves the original "carousel" behavior on the home screen while benefiting from the flexible component design.

## Verification Results

### Automated Tests
- Successfully ran `app:assembleDebug`.

### Manual Verification Path
- **Profile Hub**: Verified that achievements now appear as full-width cards, matching the width of other sections like "Account Settings."
- **Home Hub**: Verified that achievements still scroll horizontally with their original dimensions, maintaining the "hero" section aesthetic.

render_diffs(file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/AchievementCard.kt)
render_diffs(file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/profile/ProfileScreen.kt)
render_diffs(file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/home/HomeScreen.kt)
