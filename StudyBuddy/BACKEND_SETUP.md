# StudyBuddy Backend Setup Guide

## What This Backend Provides

A Spring Boot REST API backed by Firebase Auth + Firestore.

### Core Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/events` | Create a study event |
| GET | `/api/events` | Fetch all events |
| DELETE | `/api/events/{eventId}?userId={userId}` | Delete event (host only) |

---

## Firebase Credentials (Required)

The backend loads Firebase Admin credentials in this order:

1. `FIREBASE_CREDENTIALS` environment variable (**recommended primary**)
2. `serviceAccountKey.json` from classpath (`src/main/resources`) as fallback

This behavior is implemented in `FirebaseConfig`.

---

## Recommended Setup (Primary)

### Step 1: Download Service Account JSON

1. Open [Firebase Console](https://console.firebase.google.com)
2. Go to your project → **Project Settings**
3. Open **Service Accounts**
4. Click **Generate New Private Key**
5. Save the downloaded JSON file somewhere secure (outside source control)

### Step 2: Export `FIREBASE_CREDENTIALS`

#### Linux / macOS

```bash
export FIREBASE_CREDENTIALS="$(cat /absolute/path/to/serviceAccountKey.json)"
```

#### Windows PowerShell

```powershell
$env:FIREBASE_CREDENTIALS = Get-Content -Raw "C:\absolute\path\serviceAccountKey.json"
```

### Step 3: Start Backend

From `StudyBuddy/`:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
mvnw.cmd spring-boot:run
```

---

## Optional Local Fallback

If you prefer file-based local setup, place your credential at:

`StudyBuddy/src/main/resources/serviceAccountKey.json`

This file is gitignored by default and should never be committed.

---

## Common Credential Issues

### Backend starts but Firebase calls fail
- Ensure `FIREBASE_CREDENTIALS` is exported in the same terminal session where you run Maven.
- Confirm the JSON is valid and belongs to the correct Firebase project.

### Works once, then fails after clean build
- You may have been using a stale `target/classes/serviceAccountKey.json` artifact.
- Re-export `FIREBASE_CREDENTIALS` (recommended) or add local fallback file in `src/main/resources`.

### Verifying env variable is set

Linux/macOS:
```bash
echo "$FIREBASE_CREDENTIALS" | head -c 40
```

PowerShell:
```powershell
$env:FIREBASE_CREDENTIALS.Substring(0,40)
```
