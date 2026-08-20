# Implementation Plan - Remove Dashboard Quick Actions

This plan removes the "Protocol Logs" and "New Sequence" shortcut buttons from the top-right header of the Dashboard to achieve a more minimal "Clean \u0026 Lean" aesthetic.

## User Review Required

> [!NOTE]
> **Minimalist Header**: After this change, the Dashboard header will only contain the "Bite Controller" title and the insight management subtitle. The quick navigation remains available via the sidebar.

## Proposed Changes

### 1. Dashboard Component (`DashboardPage.tsx`)
- **[DELETE]**: Remove the `div` containing the two `ElasticButton` components from the header section (lines 173-183).

---

## Task List

- `[ ]` Remove quick action buttons from `src/pages/dashboard/DashboardPage.tsx`.
- `[ ]` Verify layout alignment.
- `[ ]` Run production build.

## Definition of Done
- [ ] The top-right area of the Dashboard header is empty.
- [ ] No broken navigation or logic remains.
- [ ] `npx tsc` passes.
