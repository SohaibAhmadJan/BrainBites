# Master Prompt for BrainBites Admin Panel (v2)

Copy and paste this entire prompt to your other agent in Antigravity/VS Code:

---

"Act as a Senior Web Developer. I need you to build a comprehensive **Admin Dashboard** for my Android app 'BrainBites'. The dashboard must be a React or Vue web app that connects to my existing **Firebase/Firestore** project.

### 🎯 Core Mission
The dashboard must allow me to manage psychological facts, quizzes, and collections, and send push notifications to my Android users.

### 🔑 Firebase Configuration
Use these credentials (I will provide the `appId` shortly):
- **API Key**: `AIzaSyCKDc4JlAWkzbVU7V5KAm-Avg1OOtHTyXI`
- **Auth Domain**: `brainbites-24332456.firebaseapp.com`
- **Project ID**: `brainbites-24332456`
- **Storage Bucket**: `brainbites-24332456.firebasestorage.app`
- **Messaging Sender ID**: `728212393989`

### 🏗️ Data Structure (Firestore)
The Android app expects these exact collections and fields:

1. **`facts`** (Collection):
   - `fact`: Short insight (String)
   - `fullFact`: Detailed explanation (String)
   - `whyItMatters`: Practical application (String)
   - `category`: String (Must be one of: `HUMAN_BEHAVIOR`, `MENTAL_HEALTH`, `BRAIN_SCIENCE`, `LOVE_ATTRACTION`, `PERSONALITY`, `BODY_LANGUAGE`, `SUBCONSCIOUS`, `SOCIAL_PSYCHOLOGY`, `HABITS_MOTIVATION`, `MEMORY_LEARNING`)
   - `quizQuestion`: Optional (String)
   - `quizOptions`: Array of 4 strings
   - `correctAnswerIndex`: Number (0-3)
   - `imageUrl`: String (URL)
   - `keywords`: Comma-separated search terms (String)
   - `readTimeMinutes`: Number

2. **`collections`** (Collection):
   - `title`, `description`, `icon`, `color`, `factIds` (Array of fact document IDs).

### 🚀 Key Features to Implement
1. **Full CRUD**: A clean UI to add, edit, and delete Facts and Collections.
2. **Bulk Import**: A feature to upload a `facts.json` file and batch-write them to Firestore.
3. **Notification Engine**: A form to send FCM messages with a `type` data field (NEW_FACT, ACHIEVEMENT, SYSTEM).
4. **Authentication**: Use Firebase Auth. Only my specific admin email can access this dashboard.
5. **Real-time Preview**: Show how a fact will look in a 'phone-sized' card as I type it.

Please start by setting up the project structure and the Firebase connection."

---
