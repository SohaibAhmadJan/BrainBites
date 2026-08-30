# Expanded UI Options: Fact Editor Refinement

I've developed a more comprehensive set of options to fix the layout issues in the Fact Editor. You can mix and match these to find the perfect balance for your vision.

## Issue 1: Scrollbar Out of Boundary
*The scrollbar in the "Short Fact Summary" box looks like it's pushing outside the design or overlapping the edges.*

### Option 1: The "Premium Inset" (Custom CSS)
Add a custom styled scrollbar that is thinner and slightly "inset" from the edge.
- **Visual**: A tiny, dark green bar that stays inside the dark input box.
- **Logic**: Use `scrollbar-width: thin` and `scrollbar-gutter: stable` to prevent the layout from "jumping" when the bar appears.

### Option 2: The "Elastic Input" (Auto-Resize)
Make the textarea grow in height as you type, rather than scrolling.
- **Visual**: No scrollbar ever appears in that box. The box just gets taller to show all text.
- **Logic**: Removes `overflow-y-auto` and uses a dynamic height calculation.
- **Risk**: This will push the "Live Status" button even further down, making Issue 2 more critical.

### Option 3: The "Margin Buffer"
Increase the right padding of the textarea specifically to create a "safe zone" for the default browser scrollbar.
- **Visual**: The scrollbar stays where it is, but we move the text away from it so it doesn't look "cramped."
- **Logic**: Add `pr-12` to the textarea.

---

## Issue 2: "Live Status" Button Covered
*The button at the bottom of the left column is clipped on shorter screens.*

### Option 1: Independent Column Scrolling (Category Style)
Make the entire "Basics" column scrollable independently.
- **Experience**: The "Basics" header stays fixed at the top, but you can scroll the cards underneath it.
- **Pros**: It matches the Category Editor logic perfectly (Consistency).
- **Cons**: You have to scroll to see the status.

### Option 2: Sticky Global Footer
Move the "Live Status" toggle and the "Save Changes" button into a permanent bar at the very bottom of the drawer.
- **Experience**: The most important buttons never move and are never covered. The rest of the form scrolls behind them.
- **Pros**: Maximum usability. No one ever "misses" the save or status buttons.
- **Cons**: It changes the "High-Fidelity" look of the current header.

### Option 3: "Basics" Card Internal Scroll
Keep the column fixed, but make *only the inside* of the dark Basics card scrollable.
- **Experience**: The card acts like a "window." The outer layout is 100% static, but you scroll inside the card to see the lower inputs.
- **Pros**: Keeps the background and structure perfectly stable.
- **Cons**: Small scroll areas can sometimes feel "fidgety."

### Option 4: Horizontal "Basics" Layout
Reorganize the Basics card to be wider or use 2 smaller columns *inside* that one card (e.g., ID and Category side-by-side).
- **Experience**: By putting things side-by-side, the card becomes much shorter.
- **Pros**: Might fit everything on the screen without any scrollbars.
- **Cons**: Might make the inputs feel too small/crowded.

---

### My "Perfect" Combination Suggestion:
1.  **Issue 1**: **Option 1 (Premium Inset)** — It keeps the high-fidelity look of the dark input box.
2.  **Issue 2**: **Option 2 (Sticky Global Footer)** — Moving the "Save" and "Live Status" to a permanent footer is the industry standard for pro-level admin tools. It ensures you always know the status and can always save instantly.

**Which of these options do you find most appealing?**
