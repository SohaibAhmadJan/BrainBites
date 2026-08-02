# Fix Personalized Goals Layout

Improve the "Daily Reading Goal" selector on the Settings screen to display the options in a balanced 2x2 grid instead of an uneven 3-on-top, 1-on-bottom layout.

## User Review Required

> [!NOTE]
> I will replace the current `FlowRow` with a structured 2x2 grid using nested `Row` and `Column` layouts. Each goal chip will be set to `weight(1f)` so they all occupy exactly the same amount of space, creating a professional and balanced look.

## Proposed Changes

### Settings UI

#### [MODIFY] [SettingsScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/settings/SettingsScreen.kt)
- Locate the `GoalSelector` composable.
- Replace the `FlowRow` with a `Column`.
- Inside the `Column`, create two `Row`s.
- Distribute the goal options (3, 5, 10, 20) evenly:
    - Row 1: "3 Facts" and "5 Facts"
    - Row 2: "10 Facts" and "20 Facts"
- Apply `Modifier.weight(1f)` to each chip to ensure they are equal in width.

## Verification Plan

### Manual Verification
- Navigate to the **Settings** screen.
- Scroll to the **Personalized Goals** section.
- Verify that the goal chips (3, 5, 10, 20) are now perfectly aligned in two rows of two.
- Confirm that selecting a chip still updates the preference correctly.
