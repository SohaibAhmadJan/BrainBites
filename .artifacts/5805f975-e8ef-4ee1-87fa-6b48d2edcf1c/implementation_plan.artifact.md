# Implementation Plan - Fact Detail Background Refinement

Apply a premium, category-aware background to the `FactDetailScreen` that replicates the "Ghost Emoji Gradient" style used on the Explore category cards.

## Proposed Changes

### 1. UI Layer: Dynamic Thematic Background

#### [MODIFY] [FactDetailScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/facts/FactDetailScreen.kt)
- **Background Component**: Implement a background layer for the `FactPage` that includes:
    - **Category Gradient**: A vertical gradient brush that starts with the theme's background color at the top and transitions into a soft version of the category's specific color (e.g., `#A8DADC` for Human Behavior) at the bottom.
    - **Ghost Emojis**: Place a few large, semi-transparent (alpha ~0.08), and rotated category emojis (the `iconRes`) in the background to create a subtle, premium pattern.
- **Layout Adjustments**:
    - Wrap the current content in a `Box` to allow for the layered background.
    - Ensure the content (Image Card, Text, Buttons) remains sharp and legible over the new background.

## Verification Plan

### Manual Verification
- **Category Transition**: Open facts from different categories (e.g., "Love & Attraction" vs. "Brain Science"). Verify that the background colors and "Ghost Emojis" change correctly to match the category.
- **Legibility Check**: Confirm that the fact text and action buttons are easy to read against the gradient.
- **Visual Consistency**: Compare the background with the category cards in the Explore screen to ensure the "BrainBites Design Language" is consistent.
- **Dark Mode Check**: Verify the gradient and ghost emojis look high-end in the "Deep Forest" dark theme.
