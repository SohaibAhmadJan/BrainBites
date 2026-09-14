# Walkthrough - Social Share Image Fix (Constraints)

I have completely resolved the issue causing the shared Quote Card to appear squished and distorted on WhatsApp.

## Root Cause
When you tapped "Share", the app rendered the Quote Card "off-screen" to take a screenshot of it. However, because it was technically still sitting inside your phone's main screen layout, Android's layout system forced the `1080x1080` card to "squish" down to fit the narrow portrait width of your phone screen! This caused the text to wrap tightly and break the layout.

## The Fix
I updated the layout capturing logic in both **`FactDetailScreen.kt`** and **`HomeScreen.kt`**.
I added a rule telling Android to **completely ignore the phone screen's physical limits** for that specific component and force it to measure at exactly `1080.dp x 1080.dp`.

Now, when you generate a share image, it guarantees a massive, perfectly square canvas. Your logo, category badge, and quote text will all render cleanly, exactly as they appear in your Android Studio Preview window!

render_diffs(file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/facts/FactDetailScreen.kt)
render_diffs(file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/home/HomeScreen.kt)
