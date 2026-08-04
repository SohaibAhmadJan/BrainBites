# Implementation Plan - Hero Card Alignment Refinement

Align the "Bite of the Day" card content with the rest of the Home Screen headings to create a unified vertical "line of sight" and a more professional layout.

## User Review Required

> [!IMPORTANT]
> I will make the **Bite of the Day** card span the **full width** of the screen. This allows the text inside to align perfectly with the headings below it, solving the "unequal" look shown in your screenshot.

## Proposed Changes

### UI Components

#### [MODIFY] [HomeScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/home/HomeScreen.kt)
- **Layout Restructure**:
    - Remove horizontal padding from the main `LazyColumn`.
    - Apply `padding(horizontal = 16.dp)` to all items **except** the Hero card.
- **FactOfTheDayCard Refinement**:
    - Set internal padding to `20.dp`.
    - Remove external horizontal padding.
    - Adjust corner radius to `20.dp` for a sleeker full-width appearance.

## Verification Plan

### Manual Verification
- Verify the vertical alignment of text inside the card vs. headings below.
- Ensure no other screen elements were accidentally shifted.
