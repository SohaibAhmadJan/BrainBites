# Technical Prep: 20 Questions & Answers for Senior Meeting

This document provides a comprehensive breakdown of the **BrainBites** project (Version 2.8.1) to help you prepare for your technical review.

## 1. Project Scope & Navigation

**Q1: How many total logical screens are in the app?**
**A:** There are **12 logical screens**: Splash, Home, Explore, Saved, Settings, Profile, Notifications, Quiz, Daily Teaser, History, Fact List, and Fact Detail.

**Q2: How is the main navigation structured?**
**A:** We use a hybrid approach: a **HorizontalPager** for the 4 main hubs (Home, Explore, Saved, Settings) to allow swiping, wrapped inside a standard Jetpack Compose **NavHost** for deep navigation to screens like Profile or Fact Details.

**Q3: Describe the bottom navigation design.**
**A:** It’s a **Modern Floating Pill** design with Glassmorphism (semi-transparent background). It features a **Sliding Indicator Pill** that moves horizontally between tabs and **Adaptive Labels** that only appear for the active tab.

**Q4: How do you handle deep navigation transitions?**
**A:** We implemented **Directional Sliding Transitions**. If moving "forward" (e.g., Home to Settings), the screen slides from the right. Moving "backward" slides it from the left.

---

## 2. Data & API Usage

**Q5: How many external APIs are used for data?**
**A:** Currently, **0 traditional REST APIs**. All fact and quiz data is served from **Local JSON Assets** (`facts.json` and `quiz_data.json`) for speed and offline reliability.

**Q6: Are there any external network calls?**
**A:** Yes, we use the **LoremFlickr API** to dynamically fetch high-quality, category-specific images for each fact card to keep the UI fresh.

**Q7: What is the primary architecture of the app?**
**A:** We follow **MVVM (Model-View-ViewModel)**. We have dedicated ViewModels for each major screen and a Repository layer (`BiteRepository`) to abstract data access.

**Q8: How is the data reactive across the app?**
**A:** We use **Kotlin Coroutines and StateFlow**. For example, when a user favorites a fact in the `FactDetailScreen`, the `BiteRepository` updates a `MutableStateFlow`, which instantly updates the counts on the Home and Profile screens.

---

## 3. Persistence & Logic

**Q9: How is user preference data persisted?**
**A:** We use **SharedPreferences**, encapsulated in a `PreferenceManager` and `ThemeManager`. This handles everything from display names to haptic toggles.

**Q10: What specific data is tracked in the Repository?**
**A:** We track reading **History** (with timestamps), **Favorites** (Set of IDs), **Share Counts**, and **Notification states**.

**Q11: How does the Achievement system work?**
**A:** It’s a **Live Milestone System**. An `AchievementManager` calculates progress in real-time by checking history and favorites against 10 specific definitions (e.g., "The Scholar" for reading 10 facts).

**Q12: How are notifications generated?**
**A:** They are **In-App Notifications**. The `HomeViewModel` monitors achievement status; once a milestone is hit, it adds a `Notification` object to the `NotificationRepository`, which triggers the bell icon badge.

---

## 4. UI/UX & Customization

**Q13: What accessibility features have been implemented?**
**A:** We implemented **Universal Text Scaling**. Users can adjust a slider in Settings to scale all app text (headings, body, labels) proportionally by a percentage. This is handled globally at the Theme level.

**Q14: Describe the app's branding identity.**
**A:** We use a consistent **Horizontal Lockup** `(Logo) Title` in every screen header. The logo itself is a custom `Canvas` drawing, making it lightweight and scalable.

**Q15: How does the "Bite of Advice" differ from regular facts?**
**A:** Regular facts are information-based. The **Daily Insight (Bite of Advice)** provides practical, actionable psychology tips (like the 2-Minute Rule) that rotate every 24 hours based on the calendar date.

**Q16: What animations are used to give the app a premium feel?**
**A:** We use a **Staggered "Bubble" Entrance** animation. Every list item or section uses an `AnimatedEntrance` wrapper that applies a staggered fade, scale, and upward slide.

---

## 5. Technical Stabilization (v2.8.1)

**Q17: What was the most critical bug fixed in the latest version?**
**A:** The **Navigation Exit Bug**. Previously, clicking a bottom nav icon from a sub-page (like Profile) would close the app. We fixed this by implementing a "Pop to Hub" logic.

**Q18: How is the "Unlocked" stat on the profile calculated?**
**A:** It dynamically filters the master list of 10 achievements to count only those with a `status` of `COMPLETED`.

**Q19: How do you handle Haptic Feedback?**
**A:** We use `LocalHapticFeedback` in Compose, synced with a user toggle in Settings. It triggers a "LongPress" vibration sensation when users perform significant actions like favoriting a fact.

**Q20: What's the current version and its focus?**
**A:** **Version 2.8.1**. The focus was **Profile Mastery and Navigation Stability**, transforming the profile into a functional dashboard and hardening the navigation flow.
