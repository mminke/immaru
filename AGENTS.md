# Immaru project layout

This file is intended as a quick orientation guide for AI agents and contributors working in this repository.

## What this project is

Immaru is a Kotlin Multiplatform media library application composed of:

- `shared/core` — common domain logic and models
- `frontend/composeApp` — Compose Multiplatform user interface
- `frontend/androidApp` — Android launcher shell for the shared UI
- `backend/server` — Ktor-based JVM backend API and persistence layer

The project uses Gradle Kotlin DSL and a central `settings.gradle.kts` to wire module names to physical directories.

## Repository map

### Root files you should know

- `build.gradle.kts` — root plugin declarations shared by all modules
- `settings.gradle.kts` — module registration and directory mapping
- `gradle/libs.versions.toml` — dependency and plugin versions
- `docker-compose.yml` — local Postgres stack for development
- `README.md` — developer setup and local run instructions
- `CONCEPTS.md` — project domain glossary

### Main modules

#### `shared/core`

Common Kotlin Multiplatform code used by both frontend and backend.

Important source areas:

- `shared/core/src/commonMain/kotlin/com/earthrevealed/immaru/assets` — asset model and asset repository abstractions
- `shared/core/src/commonMain/kotlin/com/earthrevealed/immaru/collections` — collection model and repository
  abstractions
- `shared/core/src/commonMain/kotlin/com/earthrevealed/immaru/common` — shared infrastructure types such as IDs, audit
  fields, exceptions, clocks, and HTTP client helpers

This is the best place for domain changes that must be visible on multiple platforms.

#### `frontend/composeApp`

Compose Multiplatform frontend.

Important source areas:

- `frontend/composeApp/src/commonMain/kotlin/com.earthrevealed.immaru` — shared UI entry points and app wiring
- `frontend/composeApp/src/commonMain/kotlin/com.earthrevealed.immaru/asset` — asset-related UI
- `frontend/composeApp/src/commonMain/kotlin/com.earthrevealed.immaru/collections` — collection UI
- `frontend/composeApp/src/commonMain/kotlin/com.earthrevealed.immaru/configuration` — app configuration screens and
  data store setup
- `frontend/composeApp/src/commonMain/kotlin/com.earthrevealed.immaru/lightbox` — lightbox and browsing UI
- `frontend/composeApp/src/jvmMain/kotlin/main.kt` — JVM desktop launcher
- `frontend/composeApp/src/androidMain` — Android-specific Compose resources and platform wiring
- `frontend/composeApp/src/iosMain` and `frontend/composeApp/src/wasmJsMain` — platform-specific source sets if needed

If you are changing UI behavior, start in `commonMain` unless the change is platform-specific.

#### `frontend/androidApp`

Android entry module.

- `frontend/androidApp/src/main/kotlin/com/earthrevealed/immaru/MainActivity.kt` is the Android activity that boots the
  shared Compose app.

This module should stay thin; most logic belongs in `shared/core` or `frontend/composeApp`.

#### `backend/server`

JVM backend service built with Ktor.

Important source areas:

- `backend/server/src/main/kotlin/com/earthrevealed/immaru/ImmaruServer.kt` — backend entry point, Ktor setup, DI, CORS,
  JSON, routing, and Flyway migration startup
- `backend/server/src/main/kotlin/com/earthrevealed/immaru/di` — dependency injection wiring
- `backend/server/src/main/kotlin/com/earthrevealed/immaru/routes/api` — REST API route definitions
- `backend/server/src/main/resources/db/migration` — Flyway migrations

Backend changes often need coordinated updates in `shared/core` models and `frontend/composeApp` API consumers.

## Build and runtime notes

- This is a Kotlin Multiplatform/Compose project, so platform source sets matter.
- Shared module changes usually require a build of at least the affected frontend and backend modules.
- The backend expects configuration from `config/immaru.properties` and a running Postgres database.
- `build/`, `backend/server/build/`, `frontend/*/build/`, and other generated directories should generally not be edited
  manually.

## Practical guidance for agents

1. **Start with the module that owns the concept.**
    - Domain model: `shared/core`
    - UI: `frontend/composeApp`
    - API and persistence: `backend/server`
    - Android launcher: `frontend/androidApp`

2. **Trace changes across layers.**
    - A model change in `shared/core` may require backend serialization changes and frontend UI updates.
    - An API change in `backend/server` may require client updates in `frontend/composeApp`.

3. **Prefer the existing package structure.**
    - Package root is `com.earthrevealed.immaru`.
    - Keep new code near related feature folders rather than creating new top-level areas.

4. **Check the domain glossary when unsure.**
    - `CONCEPTS.md` defines project terms such as Asset, Tag, and Group.

## Useful entry points

- Backend: `backend/server/src/main/kotlin/com/earthrevealed/immaru/ImmaruServer.kt`
- JVM frontend: `frontend/composeApp/src/jvmMain/kotlin/main.kt`
- Android frontend: `frontend/androidApp/src/main/kotlin/com/earthrevealed/immaru/MainActivity.kt`
- Shared domain examples: `shared/core/src/commonMain/kotlin/com/earthrevealed/immaru/assets/Asset.kt`

## When in doubt

If a task touches multiple modules, inspect the shared domain first, then the backend API, then the frontend consumer
code.
