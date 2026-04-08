# AGENTS.md

## General

- You are Jules, a software engineer here to assist the user with coding tasks, debugging, features, and answers.
- You should work iteratively and verify your work after changes (e.g., compile the app and run tests to ensure your changes are correct and haven't caused regressions).
- Before creating a plan, investigate thoroughly using available tools.

## The Codebase

- This project is an Android Native app (built with Kotlin/Gradle).
- You can build the app using `cd App && gradle assembleDebug`.
- You can test the app using `cd App && gradle test`.
- It fetches high school timetable data from the NEIS Open API.
- Use API Key: `b1631ef776724f03a6925ba7b6daea99`.

## Workflow

1.  **Understand:** Read user instructions and examine the provided API endpoints and references.
2.  **Plan:** State steps explicitly and concisely in Markdown format, then wait for feedback and user approval before proceeding.
3.  **Execute & Verify:** Apply code changes, ensuring you run build/tests constantly, specifically for any new UI or widgets.
4.  **Pre-Commit:** Always use the `pre_commit_instructions` tool and complete all required checks.
5.  **Submit:** Commit your final work with a clear message to `submit()`.

## Current Tasks

1.  **Meal Widget Updates:** Apply a regex `Regex("\\([^)]*\\)")` in `MealWidgetProvider.kt` to strip allergy info. Edit `widget_meal.xml` to set `android:autoSizeMinTextSize="20sp"`, `android:autoSizeMaxTextSize="64sp"`, and `android:textStyle="bold"` for the meal info text view.
2.  **Schedule Widget Visual Optimization:** In `item_widget_row.xml`, set `android:autoSizeMinTextSize="14sp"` and `android:autoSizeMaxTextSize="18sp"` for period and subject text views, and add padding. In `widget_timetable.xml`, adjust padding to 16dp and margins to 4dp.
3.  **Schedule Widget Refresh Button:** Add an `ImageView` to `widget_timetable.xml` at the top right. In `TimetableWidgetProvider.kt`, handle the click using `setOnClickPendingIntent` with an intent to `AlarmReceiver` (`ACTION_UPDATE_WIDGET`).
4.  **App Screen - Meal Plan Table:** Add a "급식 보기" `Button` to `activity_main.xml` next to the "재검색" button. Add a `TableLayout` to display the monthly meal plan. Fetch data using `NeisApiService` in `MainActivity.kt`. Update `MealWidgetProvider.kt` to launch `MainActivity` with an extra (`SHOW_MEAL=true`) to show the meal view.
5.  **30-minute refresh check:** Verified in `AlarmReceiver.kt` (`30 * 60 * 1000L`).
