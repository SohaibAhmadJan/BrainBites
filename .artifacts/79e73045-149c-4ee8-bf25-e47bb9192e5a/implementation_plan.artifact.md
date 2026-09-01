# Activate Automation Pulse (Notification Schedule)

Enable the Android app to automatically schedule and dispatch "Fact of the Day" notifications based on the remote configuration set in the Admin Panel.

## User Review Required

> [!IMPORTANT]
> This activation uses Android's **WorkManager** to ensure notifications are sent even if the app is closed or the device is restarted. The schedule will automatically update whenever you change the settings in the Admin Panel.

## Proposed Changes

### Android Application

#### [NEW] [DailyFactWorker.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/notifications/DailyFactWorker.kt)
- Create a background worker that:
    1.  Syncs the latest content from Firestore.
    2.  Selects the official "Fact of the Day".
    3.  Triggers a high-priority system notification.

#### [NEW] [AutomationManager.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/data/AutomationManager.kt)
- Create a central manager to observe remote settings:
    - **Logic**: If `automationEnabled` is ON, calculate the next dispatch time and schedule a periodic task.
    - **Dynamics**: Support Daily, Every 2 Days, and Weekly frequencies.
    - **Self-Healing**: If automation is toggled OFF, immediately cancel all pending background tasks.

#### [MODIFY] [MainActivity.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/MainActivity.kt)
- Initialize the `AutomationManager` at app startup to begin listening for remote schedule updates.

## Verification Plan

### Automated Verification
- Check Logcat for `AutomationManager: Scheduling automation` messages.
- Confirm the `DailyFactWorker` is successfully enqueued in the system database.

### Manual Verification
1.  Set the **Dispatch Time** in the Admin Panel to 2 minutes from now.
2.  Set **Automation** to **ON** and click **Execute Master Sync**.
3.  Close the Android app entirely.
4.  Verify that a notification titled "Your Daily Insight 🧠" appears at the specified time.
