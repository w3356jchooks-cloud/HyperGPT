# HyperGPT

A clean, independent Android-first AI workspace starter.

## What's included
- Native Android app (Kotlin + Jetpack Compose), min Android 8 / target SDK 35.
- Overview, tablet folder picker, terminal launch hand-off, X11 launch hand-off, encrypted local API-key vault, abilities, integrations, MCP/API, approvals and settings screens.
- Foreground service starter with persistent notification.
- Node.js MCP/HTTP backend starter with two MCP tools.
- OpenAPI contract for GPT Actions integration.
- ChatGPT App integration placeholder.
- GitHub Actions workflow to compile a debug APK.

## Build the Android APK
Open this folder in Android Studio (Hedgehog or newer) with JDK 17 and Android SDK 35, or run `gradle assembleDebug` on a configured Android build machine.
APK output: `app/build/outputs/apk/debug/app-debug.apk`.

A GitHub Actions workflow is included. Push this project to a new repository and run Actions -> Build HyperGPT Android APK. The APK is uploaded as a workflow artifact.

## Termux / Termux:X11
This project launches the separately installed apps when present. It does NOT bundle, copy, or redistribute Termux or Termux:X11. For actual command execution, a future local bridge must be explicitly enabled in Termux, authenticated, restricted to an allowlist, and audited. X11 live-screen viewing needs a separately implemented and secured display transport; launching the app is not the same as capturing its screen.

## Important limitations of this starter
This is a buildable foundation, not a finished super-agent. The local Android app does not yet expose an authenticated network bridge, execute shell commands, stream X11, sync files to a cloud backend, or connect external coding/web services. The backend MCP route is a development skeleton: add authentication, session handling, authorization, rate limiting, audit logging, secure deployment and tests before exposing it publicly.

## Android permissions
The app requests only internet, notification and foreground-service permissions. Folder access is granted through Android's Storage Access Framework. Android does not provide a legitimate "all permissions" switch; each sensitive capability has its own permission and OS restrictions. No root or stealth operation is included.

## Secrets
The Android vault encrypts values using an AES-GCM key held by Android Keystore. Never commit keys. Cloud integrations should use OAuth or a server-side encrypted vault; do not send device secrets to a backend without explicit user action.

## Next milestones
1. Add local pairing protocol and authenticated device bridge.
2. Implement allowlisted Termux RUN_COMMAND integration and command approval UI.
3. Implement X11 session status and a secure display transport.
4. Implement SAF file operations and optional encrypted sync.
5. Finish ability schema validation, sandboxed execution and audit events.
6. Complete Apps SDK widget and production MCP authentication.
7. Add connector OAuth flows and integration tests.
8. Add release signing, privacy policy, data deletion and Play-distribution checks.
