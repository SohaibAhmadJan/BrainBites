# Implementation Plan - "Misty Forest" Background Refinement

Refine the Lottie background to use semi-transparency for all land elements (mountains and trees). This ensures 100% text readability while keeping the "Forest Serenity" atmosphere as a subtle, moving "mist."

## User Review Required

> [!TIP]
> I will set the **Mountains** and **Trees** to **20% opacity**.
> This makes them look like ghostly shapes in the background, preventing them from competing with your UI text.
> The **Sun/Moon** and **Clouds** will remain fully visible to keep the sky alive.

## Proposed Changes

### UI Components

#### [MODIFY] [LottieBackground.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/LottieBackground.kt)
- Target all mountain layers (`m1`, `m2`, `m3`, `m4`, `m5`, `m6`, `m31`, `m34`) with `LottieProperty.OPACITY` and set to `20`.
- Target all tree layers (`tree1` through `tree6`) with `LottieProperty.OPACITY` and set to `20`.
- This will fix the blending issue permanently because the background will be 80% solid theme color.

## Verification Plan

### Manual Verification
- Verify text contrast in Dark Mode.
- Ensure the animation still feels "Serene" and premium.
