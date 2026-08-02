# Implementation Plan - "Forest Serenity" Multi-Shade Mountain Refinement

Refine the Lottie background by explicitly targeting all remaining grey/purple areas and applying a variety of green shades to create a layered, identifiable mountain range.

## User Review Required

> [!IMPORTANT]
> I will use **distinct shades of green** for each mountain layer (`m1` through `m34`). This will ensure that all "greyish" areas are gone while keeping the mountains identifiable as separate elements.

> [!NOTE]
> As requested, I will **not** modify the Sun/Moon or the Clouds.

## Proposed Changes

### UI Components

#### [MODIFY] [LottieBackground.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/LottieBackground.kt)
- **Sky Harmony**: Ensure `Shape Layer 1` perfectly matches the app background.
- **Mountain Layering**: Apply different palette greens to each mountain layer:
    - **Close Mountains (`m1`, `m2`)**: `primaryContainer` (Lush green).
    - **Mid Mountains (`m3`, `m4`, `m5`)**: `secondary` (Misty green).
    - **Far Mountains (`m6`, `m31`, `m34`)**: `secondaryContainer` (Pale sage).
    - **Background Details (`Shape Layer 4, 11, 12, 13`)**: `surfaceVariant` or `outline` (Soft forest air).

## Proposed Changes

### Theme & Assets

#### [MODIFY] [Color.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/theme/Color.kt)
- Update all brand colors to the "Forest Serenity" palette provided (#2D6A4F, #F1FAEE, etc.).

#### [MODIFY] [LottieBackground.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/LottieBackground.kt)
- Use `rememberLottieDynamicProperties` to target specific `keyPath` groups:
    - `arrayOf("moon", "**")` -> `Tertiary`
    - `arrayOf("tree*", "**")` -> `Primary`/`Secondary`
    - `arrayOf("m*", "**")` -> `SecondaryContainer` (low alpha)
    - `arrayOf("c*", "**")` -> `OnBackground` (very low alpha)

#### [MODIFY] [MainScaffold.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/main/MainScaffold.kt)
- Replace or overlay `LivingEmeraldBackground()` with `LottieBackground()`.

## Verification Plan

### Automated Tests
- `gradlew assembleDebug` to ensure successful build.

### Manual Verification
- Deploy to device/emulator.
- Verify the animation plays smoothly in the background across different screens.
