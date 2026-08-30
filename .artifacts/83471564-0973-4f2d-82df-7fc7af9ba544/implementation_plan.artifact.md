# Professionalize Analytics Hub UI

The goal is to align the Analytics Hub with the newly established professional UI standards (tightened spacing, standardized radii, and clear terminology) while maintaining all existing data visualization logic.

## Proposed Changes

### UI Components

#### [MODIFY] [AnalyticsHub.tsx](file:///F:/webBasedAdminPanel/src/pages/analytics/AnalyticsHub.tsx)
- **Header Standardization**:
    - Rename "Intelligence Hub" to "**Analytics Hub**".
    - Update radius from `rounded-[3.5rem]` to `rounded-2xl`.
    - Reduce padding from `p-10` to `p-6`.
    - Adjust title size from `text-5xl` to `text-3xl` to match other page headers.
- **Navigation & Tabs**:
    - Standardize tab buttons to use consistent padding and fonts.
- **Overview Module**:
    - Update KPI cards to match the Dashboard's compact stat cards.
    - Reduce "User Growth" card padding and radius.
- **Engagement Module**:
    - Rename "Activity Sequence" to "**Daily Activity**".
    - Rename "Interests" to "**Category Distribution**".
    - Update container radii and padding to match the global 24px grid.
    - **Table Cleanup**: Reduce cell padding from `p-8` to `p-4` to match the Users directory. Rename "Top Performing Sequences" to "**Top Facts**".
- **Content Module**:
    - Tighten the category performance cards to match the new high-density look.
- **Loading States**:
    - Simplify the loading message to "Syncing data matrix...".

## Verification Plan

### Manual Verification
1. Open the Admin Panel and navigate to **Analytics Hub**.
2. Verify that the header and cards align perfectly with the Dashboard and Users pages.
3. Confirm that switching between Overview, Engagement, and Content tabs remains smooth and functional.
4. Check that charts and tables are correctly scaled for the new tighter layout.
