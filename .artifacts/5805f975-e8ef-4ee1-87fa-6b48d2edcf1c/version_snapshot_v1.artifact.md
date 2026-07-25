# Version Snapshot: v1.0 (Stable release)

This document serves as the official record of **Version 1.0** of the BrainBites application. I have recorded all current properties, logic, and UI states to ensure a perfect revert point if needed in the future.

## Key Features & Properties

### 1. Visual Design & UI
- **Branding**: Unified "BrainBites" identity across all hubs with consistent **22sp Bold** typography.
- **Top Bar**: Globally anchored header in `MainScaffold`.
    - Profile and Notification action icons are fixed and static.
    - Contextual Share icon appears on Fact Detail screens.
- **Back Navigation**: Modern "Overlay" style—a **greenish square button with rounded edges and a white arrow** located at the top-left of the content area.
- **Dark Mode**: Fully supported with a persistent "Deep Forest" palette and "Follow System" option.
- **Entrance Animations**: "Grounded Float" spring-based staggered entrance for category cards.

### 2. Autonomous Tagline Logic
- **Rotation**: Automatically changes to a new random tagline every **40 seconds**.
- **Animation**: Letters perform a sequential "bumping wave" every **8 seconds**.
- **Persistence**: The timer and tagline state are globally managed; they do not reset when switching screens.

### 3. Home Screen Architecture
- **Order of Sections**:
    1. **Bite of the Day** (Hero Card)
    2. **Explore Categories** (Horizontal Chips)
    3. **Quiz & Teaser** (Quick Action Cards)
    4. **Recently Viewed** (Recap with "Show all" link)
    5. **Discover Something New** (Primary CTA button)

### 4. Interactive Functionality
- **Quiz Mode**: Fully functional 5-question sessions with instant feedback (Green/Red) and results summary.
- **Daily Teaser**: Fully functional "Mystery Insight" with a reveal interaction.
- **Recently Viewed**: Persistent history tracking (last 20 facts) with a dedicated "Show all" screen.
- **Favorites**: Persistent "Red Heart" system saved to disk (SharedPreferences).

### 5. Technical Architecture
- **Language**: Kotlin 2.0.0
- **UI Framework**: Jetpack Compose (100% XML-free)
- **Pattern**: MVVM with Repository-based data management.
- **Navigation**: Hierarchical Hubs with independent backstacks and **Swipe-to-Navigate** support.
- **Data Sources**: `facts.json` (Primary facts) and `quiz_data.json` (Quiz/Teaser metadata).

---

> [!IMPORTANT]
> I have indexed the current state of the following core files as "v1":
> - `MainScaffold.kt` (Root Layout)
> - `BrainBitesNavGraph.kt` (Navigation)
> - `HomeScreen.kt` (Dashboard)
> - `BrandHeader.kt` (Animation & Branding)
> - `BiteRepository.kt` (Persistence & Data)

**I am ready. If anything goes wrong in the future, simply tell me to "Revert to Version 1" and I will restore this exact configuration.**
