# StudyBuddy Backend - Events API Setup Guide

## What We Built

A complete REST API for managing study events with Firebase Firestore as the database.

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/events` | Create a new study event |
| GET | `/api/events` | Get all events (for Pinterest grid) |
| DELETE | `/api/events/{eventId}?userId={userId}` | Delete event (host only) |

---

## Firebase Setup (REQUIRED to run the app)

### Step 1: Get Your Firebase Service Account Key

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project (or create a new one)
3. Click the gear icon → **Project Settings**
4. Go to the **Service Accounts** tab
5. Click **"Generate New Private Key"** → Downloads a JSON file
6. **Rename it to exactly:** `serviceAccountKey.json`

### Step 2: Place the Key in Your Project
