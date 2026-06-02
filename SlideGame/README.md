# SlideGame - Android Game POC

A simple ball sliding game built with LibGDX for Android tablets.

## Game Concept

- **Objective**: Guide a ball to the goal using touch/slide gestures
- **Controls**: Touch and drag the ball, then release to flick it with momentum
- **Ball Size**: 1/7 of screen height
- **Orientation**: Landscape mode
- **Lives**: 3 lives per session

### Current POC Features
- Ball with physics (friction, bouncing, momentum)
- Touch/drag/flick controls
- Goal target to reach
- Stage completion screen with auto-advance
- Lives and stage tracking

### Future Features
- Obstacles (walls) that cost lives when hit
- Bluetooth button integration to explode the ball and destroy obstacles
- Multiple stages with increasing difficulty

## Build Requirements

1. **Android SDK**: Install Android Studio or the Android SDK command-line tools
   - Required SDK API Level: 33
   - Minimum SDK API Level: 15 (for Android 4.0+)

2. **Java JDK**: Java 17 or Java 21 (already available on this system)

## Building the APK

### Step 1: Install Android SDK

If you don't have Android Studio:
1. Download Android command-line tools from https://developer.android.com/studio#command-tools
2. Extract to a location like `~/android-sdk`
3. Run: `sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.0"`

### Step 2: Configure SDK Location

Create a file named `local.properties` in this directory with:
```
sdk.dir=/path/to/your/android/sdk
```

Example:
```
sdk.dir=/Users/noam.fattal/Library/Android/sdk
```

### Step 3: Build the APK

Run:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew assembleDebug
```

The APK will be created at:
```
android/build/outputs/apk/debug/android-debug.apk
```

## Deploying to Your Lenovo IdeaTab

### Without USB Connection

Since you don't have a USB connection available:

1. **Email Method**:
   - Email the APK file to yourself
   - Open email on your tablet
   - Download and install the APK

2. **Cloud Storage**:
   - Upload APK to Google Drive, Dropbox, or OneDrive
   - Download from your tablet's browser
   - Install the APK

3. **HTTP Server** (for repeated testing):
   ```bash
   cd android/build/outputs/apk/debug
   python3 -m http.server 8000
   ```
   Then on your tablet's browser, go to:
   `http://<your-mac-ip>:8000/android-debug.apk`

4. Find your Mac's IP:
   ```bash
   ifconfig | grep "inet " | grep -v 127.0.0.1
   ```

### Installing APK on Tablet

1. Enable "Install from Unknown Sources" in Settings > Security
2. Download the APK file
3. Tap the APK file to install
4. Launch "SlideGame"

## Project Structure

```
SlideGame/
├── android/          # Android-specific code and resources
│   ├── src/          # Android launcher
│   ├── res/          # Android resources (icons, strings)
│   └── build.gradle  # Android module build config
├── core/             # Game logic (platform-independent)
│   └── src/
│       └── com/slidegame/game/
│           ├── SlideGame.java          # Main game class
│           ├── GameScreen.java         # Gameplay screen with touch controls
│           ├── Ball.java               # Ball entity with physics
│           ├── Goal.java               # Goal target
│           └── StageCompleteScreen.java # Stage completion screen
├── build.gradle      # Root build configuration
├── settings.gradle   # Gradle settings
└── gradlew          # Gradle wrapper script
```

## Game Physics

The ball uses a simple but fun physics system:
- **Flick-based**: Drag creates a trajectory, release flicks the ball
- **Momentum**: Ball continues moving based on flick strength
- **Friction**: Gradually slows down (0.98 friction coefficient)
- **Bouncing**: Bounces off screen edges with energy loss (0.7 damping)
- **Max Speed**: Capped at 800 units/second for playability

## Controls

- **Touch & Drag**: Start dragging near the ball
- **Release**: Flicks the ball in the opposite direction of your drag
- **Visual Indicator**: Yellow line shows drag direction and strength

## Next Steps for Development

1. **Install Android SDK** (required to build)
2. **Test the POC** on your tablet
3. **Add Obstacles**: Implement wall obstacles
4. **Bluetooth Integration**: Add support for Bluetooth controllers
5. **Explosion Mechanic**: Implement ball explosion to destroy obstacles
6. **Level Design**: Create multiple stages with varying difficulty

## Troubleshooting

- **Build fails**: Make sure `local.properties` has correct SDK path
- **Java version errors**: Use Java 17 with `export JAVA_HOME=$(/usr/libexec/java_home -v 17)`
- **Can't install APK**: Enable "Unknown Sources" in tablet settings
- **App crashes**: Check that your tablet runs Android 4.0 (API 15) or higher

## Technical Details

- **Framework**: LibGDX 1.11.0
- **Language**: Java
- **Target Android SDK**: 33
- **Minimum Android SDK**: 15
- **Screen Resolution**: 800x480 virtual units (landscape)
- **Build System**: Gradle 8.5 with Android Gradle Plugin 8.1.4
