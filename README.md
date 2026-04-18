## Gas Prices Near Me ⛽️
Gas Prices Near Me is a mobile tracking app that provides real-time price comparison for gas prices in a given area. Built for thrifty drivers, this crowdsourced application leverages location-aware geofencing to prompt users to report prices while they are at the pump, ensuring the community always has access to the most accurate and up-to-date fuel costs.

Submitted to the Department of Computer Science and Engineering at California State University, Fullerton.
Project Advisor: Lidia Morrison

---

## 📖 Table of Contents

- [About the Project](#-about-the-project)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Screenshots](#-screenshots)
- [Installation and Setup](#-installation-and-setup)
- [The Team](#-the-team)
- [Future Scope and Known Issues](#-future-scope-and-known-issues)

---

## 🎯 About the Project
Gasoline expenditures represent a significant portion of the average consumer's budget. While technology has made price comparison seamless for almost every other retail sector, gas stations do not typically publish their real-time prices on digital platforms. This creates a paradox: to find the best deal, one must physically drive to different locations, wasting the very fuel they are trying to save.

Gas Prices Near Me solves this by providing a streamlined, location-aware map and list view of nearby stations. Rather than relying on stale third-party APIs, the app uses Google's Geofencing API to detect when a user is lingering at a gas station and sends a local push notification prompting them to update the prices.

---

## ✨ Key Features

- **Interactive GLMap**: Custom-rendered map with SVG map pins (pin.svg) and user location tracking (arrow.svg, circle.svg).
- **Smart Geofencing**: Uses background location services to draw 50-meter zones around stations. Dwelling at a station for 2 minutes triggers a local push notification.
- **Auto-Filled Reporting**: Tapping the geofence notification routes the user directly to the Report Form with the station address already populated.
- **Secure Authentication**: Firebase Phone Authentication (OTP) and Google OAuth integration.
- **Local Caching**: Utilizes a local Room SQLite database for fast, offline-capable reads, synced seamlessly with Firebase Firestore.
- **Customizable Settings**: Dynamic Dark Mode UI and adjustable search radius sliders.

---

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Backend / Database**: Firebase Firestore
- **Authentication**: Firebase Auth (Phone + GoogleIdTokenCredential)
- **Local Database**: Room (SQLite)
- **Mapping**: GLMap by Globus
- **Location Services**: Google Play Services Location (FusedLocationProvider & Geofencing API)
- **Architecture**: MVVM concepts with Coroutines and Dispatchers.IO for background tasks.

---

## 📱 Screenshots
### **Main Screen**
<img width="511" height="820" alt="image" src="https://github.com/user-attachments/assets/eff2384c-d319-4b47-a722-ad62ab069bd6" />

### **Settings Page**
<img width="510" height="820" alt="image" src="https://github.com/user-attachments/assets/a86a6f22-3b61-4e58-a406-3cc79a8e7ffc" />

### **Report Form Page**
<img width="511" height="820" alt="image" src="https://github.com/user-attachments/assets/dddac1df-f674-4013-9225-19fe514af810" />

### **List View**
<img width="509" height="820" alt="image" src="https://github.com/user-attachments/assets/25e82c47-e434-4e52-a58f-4124ca72be57" />

### **Detailed View**
<img width="508" height="819" alt="image" src="https://github.com/user-attachments/assets/b70ca5a1-4438-4efe-b020-c7aa28c20e5c" />

### **Dark Mode**
<img width="509" height="820" alt="image" src="https://github.com/user-attachments/assets/ba45ce23-f58e-414d-906c-10b7c9e39ca6" />


---

## ⚙️ Installation and Setup

1. Clone the Repository
2. Firebase Setup
   - This app relies on Firebase for Authentication and Firestore.
   - Ensure the google-services.json file is present in the app/ directory. (Note: If pulling from a public repo, you may need to supply your own Firebase project configuration).
3. GLMap Configuration:
   - The application uses GLMap for rendering.
   - Although there is no GLMAP_KEY, you do need to add this api key to local.properties, in which the api key is **api_key = "b4da2cd3-a5ae-41ba-8f3b-4d963c49e8fe"**
4. Build and Run
   - Sync Gradle files and run the app on an emulator or physical device running Android SDK 24 (Nougat) or higher.
   - Note: To test the Geofencing feature on an emulator, use the "Extended Controls -> Location" panel to spoof the device's coordinates and wait 2 minutes.

---

## 👨‍💻 The Team

| Members | Roles |
|---|---|
| Joe Bryant | Database, Report Form Page |
| Daniel Jochum | Map and List View Pages, Google Maps Interfacing |
| Azaan Mavandadipur | Sign In and Sign Up Pages, Notification Logic|
| James Nguyen | Navigation Bar and Settings Page, Map Functions |

---

## 🚀 Future Scope and Known Issues

- Directions Intent: The "Directions" button on the Details view is currently a UI placeholder. Future updates will implement a geo: intent to punt coordinates directly to the Google Maps application.
- Dynamic Distance Calculation: Currently, the distance strings in the List View calculate static strings. Future iterations will cross-reference the CurLocationHelper with the Room Database coordinates to sort stations dynamically by real-time distance.
