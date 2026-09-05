# RegiBot - Autonomous Companion & Accessibility Automation for Android

**RegiBot** is an open-source Android automation bot and accessibility companion inspired by [Juancavr6/RegiBot](https://github.com/Juancavr6/RegiBot). It features computer vision simulation, Bezier curveball trajectory kinematics, touch gesture dispatching, a floating overlay HUD, and local session analytics.

---

## 📱 How to Download & Install the APK on Your Phone

You can install this app directly onto your Android device using either of the following two methods:

### Option 1: Direct Download from Google AI Studio (Easiest)
1. In the top-right header of Google AI Studio, open the **Project Settings / Options** menu (three dots or gear icon).
2. Click **Generate APK** (or **Export APK / Download APK**).
3. Scan the generated QR code or download the `.apk` file directly to your Android device.
4. On your Android phone, tap the downloaded `.apk` file and tap **Install** (enable *"Allow from this source"* if prompted by Android).

### Option 2: Push to GitHub & Download from GitHub Actions
1. In Google AI Studio, click the **Settings / Export** menu and select **Push to GitHub** (or connect your GitHub account).
2. Once pushed to your GitHub repository, navigate to the repository on GitHub.
3. Click on the **Actions** tab.
4. You will see the **Build Android APK** workflow automatically building the latest version.
5. Once the build finishes with a green checkmark, click on the workflow run, scroll to the **Artifacts** section at the bottom, and download **`RegiBot-Debug-APK`**.
6. Unzip the downloaded archive to get `app-debug.apk` and transfer/install it on your phone.

---

## ⚙️ Device Permissions Setup

After installing the APK on your device:

1. **Accessibility Service**:
   - Go to Android **Settings > Accessibility**.
   - Find and tap **RegiBot Automation Service**.
   - Toggle **Use RegiBot Automation Service** to **ON** and tap **Allow**.
   - *This allows the app to dispatch automated screen taps and curveball gestures.*

2. **Display Over Other Apps (Overlay)**:
   - When enabling the Floating HUD in the app, grant the **Appear on top / Draw over other apps** permission.
   - *This allows the floating pill controller to remain visible while playing.*

---

## 🚀 Key Features

- **Kinematics Curveball Engine**: Simulates realistic curveball swipes using parameterized cubic Bezier curves with customizable power boost (50%–150%) and spin direction (left, right, straight).
- **Fast Catch Skip**: Simulates the ball tray drawer pull to immediately exit encounters and skip the 15-second capture animation.
- **Floating Overlay HUD**: Draggable system overlay pill with live status LED, catch/spin counters, and one-tap play/pause controls.
- **Anti-Detection Humanization**: Configurable random delay jitter (`±50ms` to `±500ms`) and touch dispersion radius (`±4px` to `±32px`) to eliminate rigid mechanical patterns.
- **Offline Persistence**: Uses Room SQLite database to save historical session records and automation profiles locally.

---

## 🛠️ Building Locally via Android Studio or Terminal

```bash
# Clone the repository
git clone https://github.com/<your-username>/RegiBot.git
cd RegiBot

# Build the debug APK using Gradle
gradle :app:assembleDebug

# The APK will be generated at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚠️ Disclaimer
*This application is built for research, accessibility testing, and educational study of Android touch injection and gesture kinematics. Use responsibly and in accordance with relevant platform terms of service.*
