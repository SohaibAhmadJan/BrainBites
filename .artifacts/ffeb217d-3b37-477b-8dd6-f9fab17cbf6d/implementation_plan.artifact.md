# Version 3.6.1: Automated Lottie Background Integration

This plan automatically integrates a high-quality Lottie background using a direct public URL, ensuring it works instantly without any manual downloads or file management.

## User Review Required

> [!IMPORTANT]
> - **Zero-Effort Setup**: I have found a reliable, public Lottie URL for a forest-themed background. You do not need to download or move any files.
> - **Instant Visibility**: I will ensure all UI layers are properly transparent so the animation is clearly visible behind all screens.

## Proposed Changes

### Infrastructure

#### [MODIFY] [libs.versions.toml](file:///F:/BrainBites/gradle/libs.versions.toml) & [build.gradle.kts](file:///F:/BrainBites/app/build.gradle.kts)
- Restore the `lottie-compose` dependency.
- Update version to **3.6.1** (Version Code 19).

### UI Components

#### [NEW] [LottieBackground.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/LottieBackground.kt)
- Create a component that loads the animation from a **Direct Public URL**.
- **Animation URL**: `https://assets3.lottiefiles.com/packages/lf20_sk7z8wis.json` (A professional, calming Forest/Nature animation).
- Set to loop infinitely and fill the entire background.

#### [MODIFY] [MainScaffold.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/main/MainScaffold.kt)
- Replace `LivingEmeraldBackground()` with the new `LottieBackground()`.
- Ensure the root `Box` has no solid background color that could hide the animation.

#### [MODIFY] [BrainBitesNavGraph.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/navigation/BrainBitesNavGraph.kt)
- Set the root `Surface` to `Color.Transparent` to ensure the absolute bottom layer is reached.

## Verification Plan

### Manual Verification
- **Run Immediately**: The animation should start playing automatically as soon as the app launches (requires internet for the first load).
- **Global Presence**: Verify the animation is visible behind all tabs (Home, Explore, etc.).
- **Smoothness**: Confirm the app remains responsive while the animation plays.
