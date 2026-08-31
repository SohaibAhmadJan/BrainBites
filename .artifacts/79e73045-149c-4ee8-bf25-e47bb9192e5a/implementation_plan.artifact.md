# Refine Growth Virality Progress Bar & Benchmark

Update the **Growth Virality** card to include a visual benchmark marker (red line) and a matching red status indicator in the footer, as per the provided visual reference.

## User Review Required

> [!NOTE]
> To allow the progress bar to cross the benchmark line (as shown in your image), I will change the bar's scale. Instead of 10% being "full", 10% will now be at the **center mark (50%)**, and the bar will be able to show values up to **20%**.

## Proposed Changes

### Web Admin Pages

#### [MODIFY] [AnalyticsHub.tsx](file:///F:/webBasedAdminPanel/src/pages/analytics/AnalyticsHub.tsx)
- **Progress Bar Scale Update**:
    - Update the width calculation for the green progress bar: `${Math.min(100, (intel.virality / 20) * 100)}%`. (This makes the total bar represent 0-20% virality).
- **Benchmark Marker**:
    - Add a `div` with absolute positioning inside the progress bar container.
    - Style: `left-[50%]`, `w-[2px]`, `h-4`, `-top-1`, `bg-red-500`. This creates the thin vertical red line at the 10% mark.
- **Redesigned Footer**:
    - Replace the current "Benchmark (10%)" label with a new red version.
    - Style: Text `text-red-500`, including a small red circle (`w-2 h-2 rounded-full`) next to it to match your reference.
    - Ensure the "Exceeding/Targeting" label on the right stays aligned.

## Verification Plan

### Manual Verification
- [ ] Open the Analytics Hub and scroll to **Growth Virality**.
- [ ] Verify the vertical red line is visible exactly in the middle of the progress bar.
- [ ] Verify that if the virality index is > 10%, the green bar crosses the red line.
- [ ] Confirm the new red "Benchmark" label and dot are visible at the bottom left.
