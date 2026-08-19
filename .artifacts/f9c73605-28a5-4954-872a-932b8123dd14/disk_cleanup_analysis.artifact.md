# C: Drive Space Analysis & Cleanup Recommendations

I have analyzed your **C: drive** (approx. 128 GB total capacity) and identified the primary space consumers. Your current free space is critically low (**~1.72 GB**).

## Disk Usage Summary

| Category / Location | Estimated Size | Description |
| :--- | :--- | :--- |
| **System Files (hiberfil.sys)** | 9.54 GB | Used for Hibernation. |
| **System Files (pagefile.sys)** | 12.64 GB | Virtual memory / Page file. |
| **C:\Program Files & (x86)** | 47.88 GB | Installed applications. |
| **C:\Windows** | 28.85 GB | Operating System files. |
| **Google Chrome Cache** | 6.25 GB | Browser history, cache, and data. |
| **Gradle & IDE Caches** | 2.50 GB | Build caches for Android/Kotlin projects. |
| **Downloads Folder** | 1.00 GB | User downloaded files. |
| **Temp Folders** | 0.31 GB | Temporary system and user files. |

---

## Recommended Cleanup Actions

Below are the recommended actions to reclaim space. **I will not execute any of these until you approve.**

### 1. High Impact / System Optimization
| Action | Savings | Safety | Description |
| :--- | :--- | :--- | :--- |
| **Disable Hibernation** | **~9.54 GB** | **SAFE** | Deletes `hiberfil.sys`. You will lose the "Hibernate" option, but "Sleep" will still work. |
| **Windows Update Cleanup** | **1 - 5 GB** | **VERY SAFE** | Removes old Windows Update files. Done via `cleanmgr`. |

### 2. Application & Developer Cleanup
| Action | Savings | Safety | Description |
| :--- | :--- | :--- | :--- |
| **Clear Chrome Cache** | **~5 GB** | **SAFE** | Clears temporary browser files. You may need to log back into some sites. |
| **Clean Gradle Cache** | **~1.8 GB** | **SAFE** | Deletes build caches. They will be re-downloaded next time you build a project. |
| **Remove Old IDE Folders** | **~0.8 GB** | **SAFE** | Deletes system folders for older Android Studio versions (e.g., v2024.1). |

### 3. User File Cleanup
| Action | Savings | Safety | Description |
| :--- | :--- | :--- | :--- |
| **Empty Downloads** | **~1.0 GB** | **USER DECISION** | Review and delete unnecessary files in your Downloads folder. |
| **Clear Temp Folders** | **~0.3 GB** | **VERY SAFE** | Deletes temporary files that are no longer in use. |

---

## Next Steps

Please let me know which of these actions you would like me to proceed with.

> [!WARNING]
> Disabling Hibernation or modifying Page Files should only be done if you don't rely on those specific Windows features.
