# Unify Header Colors with Navigation Bar

This plan outlines the changes to unify the colors of the top header elements (Logo, Title, Profile, and Notifications) with the primary color used in the bottom navigation bar.

## User Review Required

> [!NOTE]
> I will update the header elements to use the **Primary Green** color (`MaterialTheme.colorScheme.primary`), which is the same color used for the active icons in the bottom navigation bar (like the "Home" icon when selected).

## Proposed Changes

### [UI Components]

#### [MODIFY] [BrandHeader.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/BrandHeader.kt)
- Update the `BrainBitesLogo` color to `MaterialTheme.colorScheme.primary`.
- Update the title `Text` color to `MaterialTheme.colorScheme.primary`.

### [Main Scaffold]

#### [MODIFY] [MainScaffold.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/main/MainScaffold.kt)
- Update the `IconButton` tints for both the **Profile** icon and the **Notifications** icon to use `MaterialTheme.colorScheme.primary`.
- Update the `TopAppBarDefaults` color settings to ensure consistency if needed.

## Verification Plan

### Manual Verification
1. Open the application.
2. Observe the top bar:
    - The BrainBites logo and text should now be Green (matching the Home icon).
    - The Profile and Notifications icons should also be Green.
3. Switch between bottom navigation tabs and verify that the header colors remain consistent and match the "Active" state of the bottom icons.
