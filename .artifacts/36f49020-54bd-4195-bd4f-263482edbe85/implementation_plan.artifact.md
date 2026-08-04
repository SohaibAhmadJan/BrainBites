# Implementation Plan - Quick Action Card Color Refinement

Unify the color palette of the Quick Action cards (Quiz Mode & Daily Teaser) to match the high-contrast "Forest Serenity" aesthetic, ensuring they harmonize with the "Bite of the Day" hero card.

## User Review Required

> [!TIP]
> I will make the Quiz/Teaser cards semi-transparent (85%) to match the "Bite of the Day" card. This creates a unified "Glass Forest" design language across the home screen.

## Proposed Changes

### UI Components

#### [MODIFY] [QuickActionCard.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/QuickActionCard.kt)
- **Background**: Change `containerColor` to `containerColor.copy(alpha = 0.85f)`.
- **Text Unification**:
    - **Title**: Use solid `onPrimaryContainer`.
    - **Description**: Switch from 70% opacity to a solid `onSurfaceVariant` (Soft Sage) for a cleaner, professional look.

## Verification Plan

### Manual Verification
- Verify that the Quiz card and Bite of the Day card now share the same transparent "Forest" feel.
- Ensure the description text is crisp and easy to read.
