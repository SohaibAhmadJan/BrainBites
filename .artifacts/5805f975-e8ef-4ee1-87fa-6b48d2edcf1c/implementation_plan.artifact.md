# Implementation Plan - Add Logo to Home Heading

The user wants to add the Brain Bites logo to the "BrainBites" heading on the home screen, so it appears as **(Logo)BrainBites**. I will integrate the existing `BrainBitesLogo` component into the `BrandHeader` component, which is responsible for rendering the top bar titles.

## User Review Required

> [!IMPORTANT]
> I will be modifying the `BrainBitesLogo` component to allow external size control. This will affect its usage in the `SplashScreen`, ensuring it respects the requested 140.dp size instead of being forced to 120.dp.

## Proposed Changes

### UI Components

#### [MODIFY] [BrainBitesLogo.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/BrainBitesLogo.kt)
- Remove the hardcoded `.size(120.dp)` from the `Canvas` modifier to allow callers to define the size.
- Add a default `Modifier.size(120.dp)` to the parameter to maintain existing behavior for simple calls.

#### [MODIFY] [BrandHeader.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/BrandHeader.kt)
- Add `BrainBitesLogo` inside the `BrandHeader` layout.
- Conditionally show the logo only when the `title` is "BrainBites".
- Align the logo horizontally with the title text.

## Verification Plan

### Automated Tests
- I will run `app:assembleDebug` to ensure the project still compiles.

### Manual Verification
- I will use `render_compose_preview` on `BrandHeader` (after adding a preview) to verify the layout.
- I will also check `SplashScreen` to ensure the logo still looks correct.
