# Implementation Plan - Icon Fixes & Fact List Upgrade

Correct the visual distortion in specific category icons and ensure that the fact cards within category lists use professional vector icons instead of old emojis.

## Proposed Changes

### 1. Resource Layer: Icon Correction

#### [MODIFY] [ic_cat_mental_health.xml](file:///F:/BrainBites/app/src/main/res/drawable/ic_cat_mental_health.xml)
- Replace the current path data with the official Material Design `self_improvement` (Meditation) path to resolve the reported distortion.

#### [MODIFY] [ic_cat_subconscious.xml](file:///F:/BrainBites/app/src/main/res/drawable/ic_cat_subconscious.xml)
- Replace the current path data with a cleaner Material Design `brightness_3` (Crescent Moon) path to resolve the reported distortion.

### 2. UI Layer: Fact List Component Upgrade

#### [MODIFY] [BiteCard.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/BiteCard.kt)
- **Category Badge**:
    - Replace the text-based emoji `Text(text = "${bite.category.iconRes} ...")` with an `Icon` component.
    - Use `painterResource(id = bite.category.getIconDrawable())` to display the sharp vector icon.
- **Styling**: Maintain the existing color tinting (Primary green) and 12dp rounded corner surface for the badge.

## Verification Plan

### Visual Check
- **Icon Quality**: Open the Explore screen and verify that the "Mental Health" and "Subconscious Mind" icons appear clean and undistorted.
- **Fact List**: Open any category (e.g., "Brain Science") and verify that every fact card in the list shows the sharp vector icon instead of the old emoji.
- **Consistency**: Ensure the icons in the list match the ones in the main category grid.

### Build Status
- **Build**: Successfully compile and verify via `gradle build`.
