# 🎬 Netflix Clone - Full-Stack Streaming Application

A production-ready video streaming application built with modern Android (Kotlin/Jetpack Compose) and Node.js backend, featuring secure authentication, adaptive video streaming, and real-time search functionality.

![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![Backend](https://img.shields.io/badge/Backend-Node.js-green.svg)
![Database](https://img.shields.io/badge/Database-MySQL-blue.svg)

---

## ✨ Features

### 🔐 Authentication & Security
- **Email/Password Authentication** with Bcrypt password hashing
- **OAuth 2.0 Integration** (Google & GitHub sign-in)
- **Session-based Authentication** (no JWT - enhanced security)
- **Real-time Input Validation** (email format, password strength)
- **Secure Video Streaming** with session verification

### 🎥 Video Streaming
- **HLS Adaptive Bitrate Streaming** (auto-adjusts quality based on network)
- **Manual Quality Selection** (1080p, 720p, 480p, Auto)
- **ExoPlayer Integration** with Media3 library
- **Session-protected Streaming** (only authenticated users can watch)

### 🔍 Search & Discovery
- **Real-time Search** on movie titles and descriptions
- **Case-insensitive Search** with instant results
- **Multi-select Tag Filtering** (Action, Comedy, Drama, Romance)
- **Combined Search + Tags** (filters work together)
- **Featured Content Section** (curated picks)

### 🎨 User Experience
- **Modern Material 3 Design** with dark theme
- **Responsive UI** with Jetpack Compose
- **Loading States** with progress indicators
- **Error Handling** with user-friendly messages
- **Empty States** ("No movies found" instead of blank screen)

---

## 🛠️ Tech Stack

### **Frontend (Android)**
| Technology | Purpose |
|-----------|---------|
| **Kotlin** | Programming language |
| **Jetpack Compose** | Modern declarative UI framework |
| **MVVM Architecture** | Separation of concerns, testability |
| **Dagger Hilt** | Dependency injection |
| **Retrofit** | REST API client |
| **ExoPlayer (Media3)** | Video playback with HLS support |
| **Coil** | Image loading library |
| **Coroutines & Flow** | Async programming & reactive streams |
| **Material 3** | Design system |

### **Backend (Node.js)**
| Technology | Purpose |
|-----------|---------|
| **Express.js** | Web framework for REST API |
| **Sequelize** | ORM for MySQL database |
| **Express-Session** | Session management (no JWT) |
| **Passport.js** | OAuth authentication middleware |
| **Bcrypt** | Password hashing (10 rounds) |
| **MySQL** | Relational database |
| **CORS** | Cross-origin resource sharing |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│           ANDROID APP (Kotlin + Compose)            │
│  - MVVM Architecture                                │
│  - Dagger Hilt (Dependency Injection)               │
│  - Retrofit (API Client)                            │
│  - ExoPlayer (Video Playback)                       │
└──────────────────┬──────────────────────────────────┘
                   │
                   │ HTTP/REST API
                   │ Session Cookies
                   │
┌──────────────────▼──────────────────────────────────┐
│         NODE.JS BACKEND (Express.js)                │
│  - Express Session Management                       │
│  - Passport.js (OAuth)                              │
│  - Sequelize ORM                                    │
│  - Bcrypt (Password Hashing)                        │
└──────────────────┬──────────────────────────────────┘
                   │
                   │ SQL Queries
                   │
┌──────────────────▼──────────────────────────────────┐
│              MySQL DATABASE                         │
│  - Users Table (with googleId, githubId)            │
│  - Movies Table                                     │
│  - Tags Table                                       │
│  - Movie_Tags (Many-to-Many Junction)               │
│  - Sessions Table (Express Session Store)           │
└─────────────────────────────────────────────────────┘
```

---

## 📦 Project Structure

```
NetflixClone/
├── app/                                  # Android Application
│   ├── src/main/java/com/netflixclone/
│   │   ├── ui/
│   │   │   ├── screens/                 # Compose UI screens
│   │   │   │   ├── LoginScreen.kt
│   │   │   │   ├── SignupScreen.kt
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   ├── SearchScreen.kt
│   │   │   │   ├── MovieDetailsScreen.kt
│   │   │   │   └── VideoPlayerScreen.kt
│   │   │   └── viewmodels/              # MVVM ViewModels
│   │   │       ├── AuthViewModel.kt
│   │   │       ├── HomeViewModel.kt
│   │   │       └── SearchViewModel.kt
│   │   ├── data/
│   │   │   ├── repository/              # Repository pattern
│   │   │   │   ├── AuthRepository.kt
│   │   │   │   ├── FeedRepository.kt
│   │   │   │   └── SearchRepository.kt
│   │   │   └── models/                  # Data classes
│   │   │       ├── Movie.kt
│   │   │       └── MovieTag.kt
│   │   ├── network/                     # Retrofit API
│   │   │   ├── ApiService.kt
│   │   │   └── NetworkModule.kt
│   │   ├── di/                          # Dagger Hilt modules
│   │   └── navigation/                  # Navigation graph
│   └── build.gradle                     # App dependencies
│
├── js_backend/                           # Node.js Backend
│   ├── server.js                        # Express server
│   ├── package.json                     # Backend dependencies
│   └── .env                             # Environment variables (not committed)
│
├── .gitignore                           # Git ignore rules
└── README.md                            # This file
```

---

## 🚀 Getting Started

### **Prerequisites**

- **Android Studio** (latest version)
- **Node.js** (v14+ recommended)
- **MySQL** (v8.0+)
- **Git**

### **1. Clone the Repository**

```bash
git clone https://github.com/YOUR_USERNAME/netflix-clone.git
cd netflix-clone
```

### **2. Backend Setup**

```bash
# Navigate to backend folder
cd js_backend

# Install dependencies
npm install

# Create .env file
cat > .env << EOL
PORT=3002
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_mysql_password
DB_NAME=netflix_clone

GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

SESSION_SECRET=your_random_secret_key
EOL

# Create database
mysql -u root -p
CREATE DATABASE netflix_clone;
exit

# Start server
node server.js
```

### **3. Android Setup**

```bash
# Create local.properties file in project root
cat > local.properties << EOL
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
base_url=http://YOUR_LOCAL_IP:3002/
EOL

# Open in Android Studio
# File → Open → Select NetflixClone folder

# Update network_security_config.xml
# Add your local IP to the domain-config

# Sync Gradle
# Build → Clean Project
# Build → Rebuild Project

# Run on device or emulator
```

### **4. Database Setup**

Run this SQL in MySQL Workbench:

```sql
USE netflix_clone;

-- Create sample movies
INSERT INTO movies (title, description, video_url, thumbnail_url, category) VALUES
('Big Buck Bunny HQ', 'A large and lovable rabbit deals with three tiny bullies', 
 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Big_buck_bunny_poster.jpg/800px-Big_buck_bunny_poster.jpg',
 'Animation');

-- Create tags
INSERT INTO tags (name) VALUES ('Action'), ('Comedy'), ('Drama'), ('Romance');

-- Link movies to tags
INSERT INTO movie_tags (movieId, tagId) VALUES (1, 2);
```

---

## 🔐 Security Features

| Feature | Implementation |
|---------|---------------|
| **Password Hashing** | Bcrypt with 10 rounds (irreversible) |
| **Session Management** | Server-side storage in MySQL |
| **OAuth 2.0** | Google & GitHub authentication |
| **Secure Cookies** | HttpOnly, Secure flags |
| **Input Validation** | Real-time email/password validation |
| **SQL Injection Prevention** | Sequelize ORM (parameterized queries) |
| **Middleware Protection** | isAuthenticated on all content routes |

---

## 📱 Screenshots

### Login Screen
- Email/password authentication
- Google & GitHub OAuth buttons
- Real-time validation

### Home Screen
- Search bar with instant results
- Tag filters (multi-select)
- Featured content section
- Movie thumbnails

### Video Player
- HLS adaptive streaming
- Quality selection (Auto, 1080p, 720p, 480p)
- Full-screen support
- Play/pause controls

### Search & Filters
- Real-time search by title
- Multi-select tag filtering
- Combined search + tags
- Reset filters button

---

## 📚 What I Learned

### **Android Development**
- ✅ Modern UI with Jetpack Compose
- ✅ MVVM architecture pattern
- ✅ Dependency injection with Hilt
- ✅ Reactive programming with Flow
- ✅ REST API integration with Retrofit
- ✅ Video streaming with ExoPlayer

### **Backend Development**
- ✅ RESTful API design with Express.js
- ✅ Session-based authentication (no JWT)
- ✅ OAuth 2.0 integration (Passport.js)
- ✅ ORM usage (Sequelize)
- ✅ Database relationships (many-to-many)
- ✅ Security best practices (bcrypt, sessions)

### **Full-Stack Integration**
- ✅ Cookie-based session management
- ✅ API authentication patterns
- ✅ Error handling strategies
- ✅ Real-time data synchronization

---

## 🔮 Future Improvements

- [ ] **Unit Tests** (ViewModels, Repositories)
- [ ] **UI Tests** (Espresso)
- [ ] **Offline Support** (Room database caching)
- [ ] **Download for Offline Viewing**
- [ ] **Watchlist / Favorites**
- [ ] **Resume Playback** (save progress)
- [ ] **User Profiles** (multiple profiles per account)
- [ ] **Recommendations** (AI/ML based)
- [ ] **Push Notifications** (Firebase Cloud Messaging)
- [ ] **Analytics** (Firebase Analytics)
- [ ] **CI/CD Pipeline** (GitHub Actions)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 👨‍💻 Author

**Your Name**
- GitHub: [@your-github-username](https://github.com/your-github-username)
- LinkedIn: [Your LinkedIn](https://linkedin.com/in/your-profile)
- Email: your.email@example.com

---

## 🙏 Acknowledgments

- [TMDB API](https://www.themoviedb.org/) for movie data
- [Mux Test Streams](https://docs.mux.com/guides/test-with-mux-video-sample-files) for HLS video samples
- [Material 3](https://m3.material.io/) for design guidelines
- [ExoPlayer](https://exoplayer.dev/) for video playback

---

## 📞 Support

If you have any questions or need help setting up the project, please open an issue or contact me directly.

**⭐ If you found this project helpful, please give it a star!**
