# StudyBuddy

A collaborative study platform that connects students to organize and participate in group study sessions. StudyBuddy allows users to create study events, view nearby sessions on an interactive grid, and manage their study calendar—all in one place.

## Quick Start

Choose your preferred deployment method below:

---

## Option 1: Docker Deployment (Recommended)

### Prerequisites
- **Docker** ([Install Docker](https://www.docker.com/products/docker-desktop))
- **Git**

### Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/hvpham-yorku/project-group-10_StudyBuddy.git
   cd project-group-10_StudyBuddy
   ```

2. **Configure Firebase Firestore (Required)**
   - Get your Firebase service account key (Firestore credentials) from [Firebase Console](https://console.firebase.google.com)
   - Create a Firebase project and Firestore database if you haven't already
   - Download the service account key JSON file
   - Place it as `StudyBuddy/src/main/resources/serviceAccountKey.json`

3. **Build and Run**
   ```bash
   docker build -t studybuddy .
   docker run -p 8080:8080 studybuddy
   ```

4. **Access the Application**
   - Open your browser and go to: **[http://localhost:8080](http://localhost:8080)**

---

## Option 2: Manual Setup (Windows & Mac)

### Prerequisites
- **Java 21** ([Download JDK 21](https://adoptium.net/))
- **Node.js** (v22 or higher) ([Download Node.js](https://nodejs.org/))
- **Maven** (included with Spring Boot, or [download Maven](https://maven.apache.org/download.cgi))
- **Git**

### Setup Instructions

#### Step 1: Clone the Repository
```bash
git clone https://github.com/hvpham-yorku/project-group-10_StudyBuddy.git
cd project-group-10_StudyBuddy
```

#### Step 2: Configure Firebase Firestore (Required)
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new Firebase project (if you don't have one)
3. Create a Firestore database in your project
4. Navigate to **Project Settings** → **Service Accounts**
5. Click **"Generate New Private Key"** to download the service account JSON file
6. Save the downloaded file as: `StudyBuddy/src/main/resources/serviceAccountKey.json`

This JSON file contains your Firestore database credentials and is required for the app to authenticate with Firestore.

#### Step 3: Start the Backend (Terminal 1)

**Windows:**
```bash
cd StudyBuddy
mvnw.cmd spring-boot:run
```

**Mac/Linux:**
```bash
cd StudyBuddy
./mvnw spring-boot:run
```

The backend will start at [http://localhost:8080](http://localhost:8080)

#### Step 4: Start the Frontend (Terminal 2)

```bash
cd Frontend
npm install
npm run server
```

The frontend will start at [http://localhost:5173](http://localhost:5173)

---

## Project Structure

```
project-group-10_StudyBuddy/
├── StudyBuddy/              # Backend (Spring Boot)
│   ├── src/main/java/       # Java source code
│   ├── src/main/resources/  # Configuration & Firebase key
│   ├── pom.xml              # Maven dependencies
│   └── mvnw / mvnw.cmd      # Maven wrapper (Windows/Mac/Linux)
│
├── Frontend/                # Frontend (React + Vite)
│   ├── src/                 # React components & pages
│   ├── package.json         # Node dependencies
│   └── vite.config.js       # Vite configuration
│
├── Dockerfile               # Docker configuration
└── README.md                # This file
```

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React 19, Vite, React Router |
| **Backend** | Spring Boot 3.4, Java 21, Maven |
| **Database** | Firebase Firestore |
| **Authentication** | Firebase Auth |
| **Deployment** | Docker |

---

## API Endpoints

The backend provides the following REST endpoints:

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/events` | Create a new study event |
| `GET` | `/api/events` | Get all events |
| `DELETE` | `/api/events/{eventId}?userId={userId}` | Delete event (host only) |

For detailed API documentation, see [StudyBuddy/BACKEND_SETUP.md](StudyBuddy/BACKEND_SETUP.md)

---

## Important Notes

### Firebase Firestore Configuration
**Firebase Firestore is required** to run this application. The `serviceAccountKey.json` file contains your Firestore database credentials and is essential for authentication and event storage.

**Setup:**
- Create a Firebase project with Firestore database at [Firebase Console](https://console.firebase.google.com)
- Download the service account key from **Project Settings** → **Service Accounts** → **Generate New Private Key**
- Place the key file in: `StudyBuddy/src/main/resources/serviceAccountKey.json`
- Do not commit this file to version control (already in `.gitignore`)

### Email Service
The app uses Gmail SMTP for email notifications. Email credentials are configured in `StudyBuddy/src/main/resources/application.properties`

### Ports
- **Backend**: Port 8080
- **Frontend**: Port 5173 (development) or served by backend (production)

---

## Running Tests

### Backend Tests
```bash
cd StudyBuddy
./mvnw test              # Linux/Mac
mvnw.cmd test            # Windows
```

### Frontend Tests
```bash
cd Frontend
npm run test
```

---

## Troubleshooting

### Port 8080 Already in Use
```bash
# Find and kill the process using port 8080
# Mac/Linux:
lsof -ti:8080 | xargs kill -9

# Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Firebase Key Error
- Ensure `serviceAccountKey.json` exists in `StudyBuddy/src/main/resources/`
- Verify the JSON file is valid (use a JSON validator)
- Ensure you have the correct Firebase project selected

### Node Modules Issues
```bash
cd Frontend
rm -rf node_modules package-lock.json
npm install
```

---

## Documentation

- [Frontend Tasks](VAUGHN_FRONTEND_TASKS.md)
- [Backend Setup Guide](StudyBuddy/BACKEND_SETUP.md)
- [Event Schema](StudyBuddy/EVENT_SCHEMA.md)
- [Sample Test Data](SAMPLE_TEST_DATA.md)

---

## Contributing

1. Clone the repository
2. Create a feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes (`git commit -m 'Add YourFeature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

---

## License

This project is part of York University's EECS 2311 Software Development Project.

---

## Support

For issues or questions, please refer to the documentation in the `Documents/` folder or contact the project maintainers.

**Happy studying :)**
