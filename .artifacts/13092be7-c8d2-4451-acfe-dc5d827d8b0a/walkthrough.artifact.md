# Quiz Screen Refinement Walkthrough

I have improved the Quiz screen to make it more intuitive and functional. The primary changes focus on the "Next" button visibility and the flow after answering a question.

## Changes Made

### 1. New "Next Question" Button
- Removed the Floating Action Button (FAB) that was hidden in the corner.
- Added a full-width **"Next Question"** button directly below the answer options.
- The button uses `AnimatedVisibility` to appear only after the user has selected an answer or the timer runs out, providing clear guidance on what to do next.

### 2. Per-Question Quiz Timer
- Added a **15-second countdown timer** for each question.
- The timer is placed at the top of the question area, styled with a pill-shaped background and a clock icon.
- **Dynamic Styling:** The timer turns red when less than 5 seconds remain, creating a sense of urgency.
- **Auto-Timeout:** If the timer reaches zero:
    - Options are disabled.
    - A "Time's Up!" message appears along with the correct answer.
    - The "Next Question" button becomes visible.

### 3. Dynamic Button Text
- The button text changes to **"Finish Quiz"** when the user reaches the final question.

### 4. Improved Answer Feedback
- Correct answers are highlighted in green, and incorrect selections in red.
- If time runs out, the correct answer is revealed automatically.

### 5. Layout Fix for Navigation Bar
- Increased the bottom spacing in both `QuizQuestionView` and `QuizResultView` to **140.dp**.
- This ensures that all interactive elements, including the "Next Question" and "Try Another Quiz" buttons, can be scrolled completely above the floating navigation bar.

## Verification Results

### Manual Verification
- [x] Timer start: Starts at 15s when a question is shown.
- [x] Timer stop: Stops immediately when an answer is selected.
- [x] Timeout: When reaching 0s, options disable and the correct answer is shown.
- [x] Reset: Advancing to the next question resets the timer to 15s.
- [x] Final question: The button says "Finish Quiz" and leads to the results screen.

render_diffs(file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/quiz/QuizScreen.kt)
render_diffs(file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/quiz/QuizViewModel.kt)
