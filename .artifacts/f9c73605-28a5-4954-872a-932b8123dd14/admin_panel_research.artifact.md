# Admin Panel Research Notes

## Current Stack
- **Framework**: React 18
- **Build Tool**: Vite
- **Language**: TypeScript
- **Backend**: Firebase (Firestore & Auth)
- **Styling**: Raw CSS in `App.css` and `index.css`

## Project Structure
- `src/App.tsx`: Main logic and UI (Needs refactoring).
- `src/Auth.tsx`: Auth wrapper (Good state).
- `src/services/firestoreService.ts`: Firestore CRUD and listeners (Well implemented).
- `src/services/firebaseService.ts`: Firebase initialization (Missing Web App ID).
- `src/types.ts`: Shared data models (Matches Android app).

## Identified Issues
1. **Missing Configuration**: `VITE_FIREBASE_APP_ID` is a placeholder.
2. **Monolithic UI**: `App.tsx` is too large and hard to maintain.
3. **Styling**: Needs a professional design system (Tailwind CSS).
4. **Data Sync**: No active fallback if Firestore fails during initial load.

## Data Schema (Firestore)
- `/facts/{id}`: `BiteItem`
- `/collections/{id}`: `CollectionSet`
- `/notifications/{id}`: `AppNotification`
