# Admin Panel Technical Specification: BrainBites

## Project Overview
**BrainBites** is an Android application that delivers psychological insights (Facts) and challenges users with quizzes. The current implementation is a Native Kotlin app using Jetpack Compose and local JSON data.

**Goal**: Build a web-based Admin Panel to manage this content remotely.

---

## 1. Core Data Models (Kotlin Reference)

### Fact Item (BiteItem)
```kotlin
data class BiteItem(
    val id: String,                  // Unique ID
    val fact: String,                // The core insight (short)
    val fullFact: String,            // Detailed explanation
    val category: BiteCategory,      // Enum (see below)
    val whyItMatters: String,        // Practical application
    val quizQuestion: String?,       // Associated challenge question
    val quizOptions: List<String>?,  // 4 Multiple choice options
    val correctAnswerIndex: Int?,    // 0-3 index
    val imageUrl: String?,           // Content image
    val keywords: String?,           // Comma-separated for search
    val readTimeMinutes: Int         // Estimated time
)
```

### Categories (BiteCategory)
The admin panel must use these exact keys:
- `HUMAN_BEHAVIOR` (#A8DADC)
- `MENTAL_HEALTH` (#457B9D)
- `BRAIN_SCIENCE` (#E9C46A)
- `LOVE_ATTRACTION` (#E76F51)
- `PERSONALITY` (#F4A261)
- `BODY_LANGUAGE` (#2A9D8F)
- `SUBCONSCIOUS` (#264653)
- `SOCIAL_PSYCHOLOGY` (#8AB17D)
- `HABITS_MOTIVATION` (#B5838D)
- `MEMORY_LEARNING` (#6D6875)

---

## 2. Required Admin Operations (CRUD)

### [A] Content Management
1. **Fact Editor**: Full CRUD (Create, Read, Update, Delete) for psychological facts.
2. **Quiz Logic**: Every fact can optionally have one quiz. The admin must be able to edit the question, options, and verify exactly one correct answer.
3. **Bulk Import**: Ability to upload a `facts.json` file to populate the database.

### [B] Collection Management
**CollectionSet** groups facts by ID.
- Fields: `id`, `title`, `description`, `icon`, `color`, `factIds` (Array of Strings).
- Admin should be able to pick existing facts to add to a collection.

### [C] Notification Engine
- Send "Push Notifications" to the app.
- Fields: `title`, `message`, `type` (NEW_FACT, ACHIEVEMENT, SYSTEM).

---

## 3. Implementation Requirements
- **Backend**: Preferred bridge is **Firebase (Firestore)** to allow real-time updates to the Android app.
- **Frontend**: [INSERT PREFERRED WEB FRAMEWORK, e.g., React or Vue].
- **Auth**: Only authorized admins can access the panel.

---

## 4. Sample JSON Format
The Admin Panel should produce/consume data in this structure:
```json
{
  "id": "1",
  "fact": "Mirroring body language builds trust.",
  "category": "HUMAN_BEHAVIOR",
  "quizQuestion": "What does mirroring body language achieve?",
  "quizOptions": ["Aggression", "Boredom", "Trust", "Confusion"],
  "correctAnswerIndex": 2
}
```
