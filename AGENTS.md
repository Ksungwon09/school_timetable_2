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
