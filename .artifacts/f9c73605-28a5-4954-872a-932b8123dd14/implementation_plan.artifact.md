# Implementation Plan - Visual Vector Library Integration

This plan addresses the need for a selectable list of icons for the "Vector ID" field in the Category Editor, replacing manual text entry with a high-fidelity visual selector.

## Proposed Changes

### [MODIFY] [CategoriesPage.tsx](file:///F:/webBasedAdminPanel/src/pages/categories/CategoriesPage.tsx)

#### 1. Comprehensive Icon Registry
- **Expanded IconMap**: Import and register a wide variety of `lucide-react` icons suitable for psychological categories (e.g., `Target`, `Shield`, `Zap`, `Star`, `Compass`, `Atom`, `Biohazard`, `Dna`, etc.).
- **Vector Library Constant**: Create an array of available icon keys for the selector to map over.

#### 2. Vector Icon Matrix Selector
- **Interactive Trigger**: Convert the "Vector ID" text input into a "Selection Trigger" that opens a visual grid.
- **Glass Popover UI**:
    - Implement a floating glass-themed grid that appears when the field is clicked.
    - **Search Functionality**: Add a small, high-speed filter bar to find icons by name.
    - **Visual Grid**: Display a grid of icons (e.g., 6 columns) where each item shows the icon and its ID on hover.
    - **Selection Logic**: Clicking an icon automatically updates the `formData.vectorIcon` and provides immediate feedback through the "Live Preview".

#### 3. UX & Animation
- **Proximity Placement**: Anchor the selector grid directly below the Vector ID field.
- **Micro-Animations**: Use Framer Motion to animate the entry and exit of the selector (scale-up/fade-in).
- **Clearance**: Ensure the selector stays within the 16px absolute boundary and doesn't trigger scrollbars.

## Verification Plan

### Manual Verification
1. **Interactive Trigger Test**: Click the "Vector ID" field and verify that the visual icon matrix opens immediately.
2. **Search Efficiency**: Type "Brain" or "Users" in the selector's filter and verify that only relevant icons are shown.
3. **Selection Success**: Select a new icon (e.g., `Zap`) and verify that:
    - The `formData.vectorIcon` string updates correctly.
    - The "Live Card Preview" icon changes instantly.
    - The selector closes automatically (or via clicking outside).
4. **Visual Alignment Check**: Confirm that the selector doesn't break the "sequential alignment" of Section 1.
