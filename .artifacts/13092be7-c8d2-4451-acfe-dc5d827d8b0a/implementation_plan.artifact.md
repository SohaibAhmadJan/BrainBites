# Personalize Home Screen based on Mood

This plan outlines the changes to make the "How are you feeling today?" section functional by providing a personalized "Mood Insight" when a mood is selected.

## User Review Required

> [!IMPORTANT]
> When a user selects a mood, the app will:
> 1.  Display a **personalized greeting** based on that mood.
> 2.  Immediately **refresh the "Bite of the Day" hero card** with a hand-picked psychology fact that aligns with that mood (e.g., Motivation facts for the "Motivated" mood).
> 3.  Keep the selected fact stable until the user selects a different mood or the app is restarted.

## Proposed Changes

### [Home Logic]

#### [MODIFY] [HomeViewModel.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/home/HomeViewModel.kt)
- Add a new `moodMessage: StateFlow<String?>` to store the personalized acknowledgement.
- Update `selectMood(mood: String)` to:
    - Determine a target `BiteCategory` based on the mood.
    - Select a random fact from that category and update `_rotatingFactId`.
    - Generate a friendly message (e.g., "Stay inspired! Here's something for your motivated mind.") and update `_moodMessage`.

### [Home UI]

#### [MODIFY] [HomeScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/home/HomeScreen.kt)
- Collect and display the `moodMessage` directly below the mood selector.
- Use `AnimatedVisibility` for a smooth entrance of the mood message.
- Style the message using the primary green color to keep it on-brand.

## Verification Plan

### Manual Verification
1.  Open the Home screen.
2.  Select "💡 Motivated".
3.  Verify that:
    -   A message like "Fuel your drive!" appears below the selector.
    -   The main card at the top changes to a fact from the "Habits & Motivation" category.
4.  Select a different mood and verify the card and message update again.
5.  Verify that the auto-rotation of facts is paused or reset when a mood is manually selected.
