# Admin Registry UI Restoration

This plan addresses the UI inconsistencies in the "Register Agent" drawer, ensuring it perfectly matches the design and quality of the Fact and Category editors.

## Proposed Changes

### UI Components

#### [MODIFY] [AdminEditorDrawer.tsx](file:///F:/webBasedAdminPanel/src/pages/admins/AdminEditorDrawer.tsx)
- **Role Selection**: Replace the native `<select>` dropdown with a custom, high-fidelity card-based selection grid (matches the style of Fact Category selection).
- **Status Selection**: Replace the single toggle with a "Node State" switch group (Active vs Restricted) that uses the emerald/red color palette.
- **Visual Refinement**:
    - Update spacing and font sizes to match the Fact editor exactly.
    - Ensure all icons and labels align with the 3-column matrix design language.
    - Remove unused imports and redundant styles.

### Registry Page

#### [MODIFY] [AdminsPage.tsx](file:///F:/webBasedAdminPanel/src/pages/admins/AdminsPage.tsx)
- Minor styling cleanup to ensure the high-fidelity header and table match the new drawer perfectly.

## Verification Plan

### Manual Verification
1.  **Role Interaction**: Click different roles (Analyst, Admin, etc.) and verify they highlight correctly with the new card style.
2.  **Status Toggle**: Click "Active" or "Restricted" and verify the visual feedback (emerald pulse vs red pulse).
3.  **UI Consistency**: Compare the Admin drawer side-by-side with the Fact editor to ensure identical spacing, font weights, and border radii.
4.  **Sync Test**: Register a new agent and verify the data saves correctly to Firestore.
