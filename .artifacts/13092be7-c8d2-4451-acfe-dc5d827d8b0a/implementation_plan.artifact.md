# Add Quiz Timer

Add a per-question countdown timer to the Quiz screen, placed between the header and the question progress.

## User Review Required

> [!IMPORTANT]
> I will implement a **15-second countdown timer** for each question.
>
> **Behavior when time runs out:**
> - The question will be marked as "timed out" (no answer selected).
> - The "Next Question" button will appear, allowing the user to proceed.
> - The user will not be able to select an answer once the timer hits zero.

## Proposed Changes

### [Quiz Logic]

#### [MODIFY] [QuizViewModel.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/quiz/QuizViewModel.kt)
- Add `remainingTime: Int` and `isTimerRunning: Boolean` to `QuizUiState`.
- Implement a coroutine-based timer that updates `remainingTime` every second.
- Reset the timer to 15s whenever a new question is shown.
- Stop the timer when an option is selected or when the quiz finishes.

### [Quiz UI]

#### [MODIFY] [QuizScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/quiz/QuizScreen.kt)
- Create a `QuizTimer` composable that displays the remaining time with a clock icon.
- Insert this `QuizTimer` at the top of the `Column` in `QuizQuestionView`, before the `LinearProgressIndicator`.
- Style the timer to be prominent (e.g., using the primary color).

## Verification Plan

### Manual Verification
1. Start a quiz and verify the timer starts at 15s.
2. Select an answer and verify the timer stops.
3. Click "Next Question" and verify the timer resets to 15s.
4. Let the timer run to 0s and verify:
    - Options become disabled.
    - "Next Question" button appears.
    - The score does not increase.
