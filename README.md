# StudyBuddy

A collaborative study platform that connects students to organize and participate in group study sessions. StudyBuddy allows users to create study events, view nearby sessions on an interactive grid, and manage their study calendar—all in one place.

---

## StudyBuddy Setup & Installation Guide

### Prerequisites

- Docker *(Recommended for deployment)*
- Java 21 (JDK)
- Node.js (v22+)
- Maven
- Git
- PostgreSQL *(for stub database option)*

---

## 1. Clone the Repository

```bash
git clone https://github.com/hvpham-yorku/project-group-10_StudyBuddy.git
cd project-group-10_StudyBuddy
```

---

## 2. Backend Configuration

### Firebase Firestore (Default)

1. Obtain your Firebase service account key from [Firebase Console](https://console.firebase.google.com/).
2. Place the file as: `serviceAccountKey.json`
3. **Do not commit this file**; it is confidential and already in `.gitignore`.

### Stub Database (PostgreSQL)

**Install PostgreSQL:**

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**Start PostgreSQL and create a database:**

```sql
sudo service postgresql start
sudo -u postgres psql

CREATE DATABASE studybuddy_stub;
CREATE USER studybuddy_user WITH PASSWORD 'studybuddy_pass';
GRANT ALL PRIVILEGES ON DATABASE studybuddy_stub TO studybuddy_user;
\q
```

**Update `application.properties`:**

```properties
spring.profiles.active=stub
spring.datasource.url=jdbc:postgresql://localhost:5432/studybuddy_stub
spring.datasource.username=studybuddy_user
spring.datasource.password=studybuddy_pass
```

> Place `application.properties` in `resources` — **do not commit; confidential.**

**Populate the stub database** *(run this script after DB setup)*:

```sql
-- populate_stub_db.sql
INSERT INTO users (email, name) VALUES
  ('alex@my.yorku.ca', 'Alex'),
  ('sarah@my.yorku.ca', 'Sarah'),
  ('marcus@my.yorku.ca', 'Marcus');
-- Add more sample data as needed
```

**To run:**

```bash
psql -U studybuddy_user -d studybuddy_stub -f populate_stub_db.sql
```

---

## 3. Build & Run

### Option 1: Docker *(Recommended)*

```bash
docker build -t studybuddy .
docker run -p 8080:8080 studybuddy
```

### Option 2: Manual Setup

**Backend:**

```bash
# Windows
cd StudyBuddy && mvnw.cmd spring-boot:run

# Mac/Linux
cd StudyBuddy && ./mvnw spring-boot:run
```

**Frontend:**

```bash
cd Frontend
npm install
npm run server
```

---

## 4. Access the Application

| Service  | URL                       |
|----------|---------------------------|
| Backend  | http://localhost:8080     |
| Frontend | http://localhost:5173     |

---

## 5. Confidential Files

> ⚠️ **Do not commit** `serviceAccountKey.json` or `application.properties` — both are already in `.gitignore`.
>
> **TAs:** Request these files from the project maintainers if needed.

---

## 6. Switching Between Firestore and Stub DB

Edit `spring.profiles.active` in `application.properties`:

| Value     | Database             |
|-----------|----------------------|
| `firebase` | Firebase Firestore  |
| `stub`     | PostgreSQL Stub DB  |

---

## 7. Stub DB Test Accounts

Login with any of the following accounts using **any password**:

- `alex@my.yorku.ca`
- `sarah@my.yorku.ca`
- `marcus@my.yorku.ca`

---

## 8. Running Tests

**Backend:**

```bash
cd StudyBuddy
./mvnw test        # Linux/Mac
mvnw.cmd test      # Windows
```

**Frontend** *(new terminal)*:

```bash
cd Frontend
npm run test
```

---

## 9. Troubleshooting

See the original README for help with port conflicts, Firebase errors, and node module issues.

---

## 10. Documentation

See the `Documents` folder for guides, sample data, and schema.

---

## 11. Support

For confidential files or setup help, contact the project maintainers.

---

*Happy studying! 🎓*
