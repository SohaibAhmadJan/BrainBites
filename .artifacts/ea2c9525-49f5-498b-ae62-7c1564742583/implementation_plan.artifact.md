# Implementation Plan - Fix QuoteCard Export Squishing

The user reported that sharing the QuoteCard results in a highly squished, tall, portrait image where text wraps improperly, rather than a 1080x1080 square.

## Root Cause Analysis
The `QuoteCard` is rendered off-screen so its pixel data can be captured and shared to WhatsApp/Instagram.
While we explicitly set `.size(1080.dp)` on the card, it is placed inside the root `Box` of `FactDetailScreen` and `HomeScreen`, which are constrained by the physical phone screen dimensions (e.g., ~400x800).
When Compose measures the off-screen layout, the parent screen forces its maximum width constraint onto the off-screen box. This "squishes" the 1080.dp card down to the phone's portrait width, causing the text to wrap wildly and the layout to break.

## Proposed Changes

### [MODIFY] [FactDetailScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/facts/FactDetailScreen.kt)
- Update the custom `.layout` modifier for the off-screen capture box.
- Ignore the incoming phone screen constraints.
- Force `Constraints.fixed(1080.dp.roundToPx(), 1080.dp.roundToPx())` during the `measurable.measure()` phase.

### [MODIFY] [HomeScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/home/HomeScreen.kt)
- Apply the identical fix to the capture wrapper used on the Home screen.

## Verification
- Rebuild the app. Generating a share card should now consistently output a perfect 1080x1080 square image, completely unconstrained by the physical device screen.
