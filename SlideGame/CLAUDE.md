# SlideGame - Android Game Project

## Project Overview

SlideGame is a 2D physics-based puzzle game for Android tablets, specifically designed for the Lenovo IdeaTab. The core mechanic involves flicking a ball across the screen to reach a goal, with the unique ability to explode obstacles using a Flic button controller.

**Target Device:** Lenovo IdeaTab (Android 4.0+, API 15+)  
**Orientation:** Landscape only  
**Controller:** Flic 1 Bluetooth button (physical hardware)

## Technology Stack

- **Framework:** LibGDX 1.11.0 (cross-platform game framework)
- **Language:** Java 8
- **Build System:** Gradle 8.5
- **Android SDK:** Compile SDK 33, Min SDK 15, Target SDK 30
- **Physics:** Custom physics implementation (not Box2D despite dependency)
- **Input:** Touch gestures + Flic Bluetooth button (via broadcast intents)

## Project Structure

```
SlideGame/
├── android/                          # Android-specific code
│   ├── src/com/slidegame/game/
│   │   ├── AndroidLauncher.java      # Main Android activity
│   │   └── FlicButtonReceiver.java   # Flic button broadcast receiver
│   ├── res/                          # Android resources
│   ├── AndroidManifest.xml           # App configuration + intent filters
│   └── build.gradle                  # Android module build config
│
├── core/                             # Platform-independent game logic
│   └── src/com/slidegame/game/
│       ├── SlideGame.java            # Main game class, manages screens/state
│       ├── GameScreen.java           # Main gameplay screen
│       ├── StageCompleteScreen.java  # Stage completion screen
│       ├── Ball.java                 # Ball entity with physics
│       ├── Goal.java                 # Goal target entity
│       └── ButtonListener.java       # Interface for button press events
│
├── gradle/wrapper/                   # Gradle wrapper files
├── build.gradle                      # Root build configuration
├── settings.gradle                   # Gradle project settings
├── gradle.properties                 # Gradle JVM settings
├── gradlew                          # Gradle wrapper script (Unix)
├── local.properties                  # SDK location (not in git)
├── build-apk.sh                     # Build helper script
├── README.md                        # User documentation
├── QUICK_START.md                   # Quick start guide
└── CLAUDE.md                        # This file

```

## Build Instructions

### Prerequisites

1. **Java 17** (installed at `/Library/Java/JavaVirtualMachines/zulu-17.jdk/`)
2. **Android SDK** (installed at `~/android-sdk/`)
3. **Gradle 8.5** (via wrapper, downloads automatically)

### Building the APK

```bash
cd /Users/noam.fattal/slideGame/SlideGame

# Set Java version
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Build debug APK
./gradlew assembleDebug

# APK location:
# android/build/outputs/apk/debug/android-debug.apk
```

### Using the Build Script

```bash
./build-apk.sh
```

This helper script:
- Sets correct Java version
- Builds the APK
- Shows APK location and size
- Provides deployment instructions

### Deployment (No USB Available)

Since USB is not available, use one of these methods:

1. **Cloud Storage:** Upload APK to Google Drive/Dropbox, download on tablet
2. **Email:** Attach APK (must be zipped - Gmail blocks .apk files)
3. **HTTP Server:** `python3 -m http.server 8000` from APK directory
4. **ADB over WiFi:** If enabled on tablet

## Game Architecture

### Screen Flow

```
AndroidLauncher (onCreate)
    ↓
SlideGame.create()
    ↓
GameScreen (main gameplay)
    ↓
[Ball reaches goal]
    ↓
StageCompleteScreen
    ↓
[Auto-advance after 3s or tap]
    ↓
GameScreen (next stage)
```

### Key Components

#### Ball.java - Physics Implementation

**Ball size:** `VIRTUAL_HEIGHT / 7` (~68.5 pixels at 480p)

**Physics constants:**
- `FRICTION = 0.98f` - Applied each frame, slows ball naturally
- `BOUNCE_DAMPING = 0.7f` - Energy loss on wall bounce
- `MIN_VELOCITY = 0.5f` - Threshold for stopping
- `MAX_SPEED = 800f` - Velocity cap for playability

**Explosion mechanics:**
- Duration: 0.5 seconds
- Visual: 3 concentric expanding circles (red core, yellow middle, orange outer)
- Max radius: 200 pixels
- Ball freezes during explosion, then respawns at start position

**Controls:**
- Drag to create flick trajectory (yellow line indicator)
- Release to flick ball in opposite direction
- Flick strength multiplier: 3.0x

#### GameScreen.java - Main Gameplay

**Virtual resolution:** 800×480 (scales to device via FitViewport)

**Input handling:**
- `GestureDetector` for touch/drag/fling gestures
- `InputMultiplexer` combines gesture + keyboard + button inputs
- Custom `touchDown` handler for on-screen EXPLODE button

**On-screen elements:**
- Ball (blue circle, center start position)
- Goal (green circle with white inner ring, right side)
- Red EXPLODE button (top-right corner, 150×80 pixels)
- Stage/Lives HUD (top-left)
- Debug messages (yellow text, 3-second duration)

**Explosion trigger sources:**
1. Flic button press (via SlideGame.triggerButtonPress())
2. On-screen red EXPLODE button tap
3. Double-tap anywhere on screen
4. Any keyboard key press (for testing)

#### Flic Button Integration

**Hardware:** Flic 1 Bluetooth button (paired via Flic app)

**Integration method:** Android Broadcast Intents

**Configuration in Flic app:**
- Action Type: "Send Intent"
- Target: "Broadcast"
- Action: `com.slidegame.game.FLIC_BUTTON_PRESSED`
- Package: `com.slidegame.game`
- Class: `com.slidegame.game.FlicButtonReceiver`

**Code flow:**
```
[Flic button press]
    ↓
FlicButtonReceiver.onReceive() [Broadcast Receiver]
    ↓
AndroidLauncher.onFlicButtonPressed() [Listener callback]
    ↓
SlideGame.triggerButtonPress() [Game method]
    ↓
GameScreen.onButtonPressed() [ButtonListener interface]
    ↓
Ball.explode() [Trigger explosion]
```

**Alternative path (Activity intent):**
```
[Flic button press]
    ↓
AndroidLauncher.onNewIntent() [Activity receives intent]
    ↓
checkIntent() detects action
    ↓
onFlicButtonPressed()
    ↓
[same as above]
```

## Important Implementation Details

### Permissions Required

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:maxSdkVersion="30" />
```

### Manifest Configuration

**Activity launch mode:** `singleTop` - Prevents app restart when Flic button is pressed while app is running

**Intent filters:**
- `MAIN`/`LAUNCHER` - App launcher
- `com.slidegame.game.FLIC_BUTTON_PRESSED` - Flic button action

**Broadcast receiver:** `FlicButtonReceiver` marked as `exported="true"` to receive intents from Flic app

### Gradle Configuration Notes

**Java version:** Must use Java 17 (not 21/25) due to Gradle 8.5 compatibility

**Deprecated warning:** `XX:MaxPermSize` removed from gradle.properties (not supported in Java 17+)

**LibGDX dependencies:** Core and Box2D included, though Box2D is not currently used

**Native libraries:** All architectures included (armeabi-v7a, arm64-v8a, x86, x86_64)

### Debug Features

**On-screen debug messages:**
- Shows input source when detected (key press, touch, Flic button)
- Displays for 3 seconds in yellow text
- Location: below Lives counter

**Logging tag:** `"SlideGame"` - use `adb logcat | grep SlideGame` to filter logs

**Test features:**
- Red EXPLODE button (always visible, top-right)
- Double-tap to explode
- Any keyboard key triggers explosion

## Current Game State

### Implemented Features

✅ Ball physics (momentum, friction, bouncing)  
✅ Touch/drag/flick controls with visual indicator  
✅ Goal collision detection  
✅ Explosion animation with screen flash  
✅ Ball respawn after explosion  
✅ Stage progression system  
✅ Lives counter (3 lives, not yet decremented)  
✅ Stage completion screen with auto-advance  
✅ Flic button integration (working)  
✅ Landscape-only orientation  
✅ Ball size = 1/7 screen height (as specified)  

### Not Yet Implemented

❌ Obstacles/walls to destroy  
❌ Collision with obstacles → lose life  
❌ Explosion destroys obstacles  
❌ Multiple stages with different layouts  
❌ Game over screen (when lives = 0)  
❌ Sound effects  
❌ Particle effects  
❌ Proper app icon (currently placeholder 1×1 blue pixel)  
❌ Obstacle introduction tutorial  
❌ Limited explosions / cooldown system  

## Future Development Roadmap

### Phase 1: Core Gameplay (Next Priority)

1. **Add obstacle system:**
   - Create `Obstacle.java` class (rectangular walls)
   - Collision detection between ball and obstacles
   - Lose life on collision → respawn ball
   - Visual distinction (red/brown walls)

2. **Explosion mechanics:**
   - Detect obstacles within explosion radius
   - Destroy obstacles when exploded nearby
   - Visual/audio feedback for destruction

3. **Game over handling:**
   - Game over screen when lives = 0
   - Restart level option
   - Return to stage 1 option

### Phase 2: Level Design

1. **Stage variety:**
   - Create level data structure (obstacle positions per stage)
   - Progressive difficulty (more/harder obstacles)
   - Different goal positions per stage

2. **Stage progression:**
   - Save current stage progress
   - Display stage number prominently
   - Stage select screen (optional)

### Phase 3: Polish

1. **Visual improvements:**
   - Replace circles with sprite graphics
   - Proper app icon design
   - Particle effects for explosion
   - Trail effect for moving ball
   - Better UI design (custom fonts, borders)

2. **Audio:**
   - Explosion sound effect
   - Ball bounce sounds
   - Goal reached jingle
   - Background music (optional)
   - Flic button feedback sound (in-game, not system)

3. **UI/UX:**
   - Remove debug features (EXPLODE button, yellow text)
   - Settings screen (sound on/off, reset progress)
   - Tutorial/help screen
   - Better stage complete animation

### Phase 4: Advanced Features

1. **Power-ups:**
   - Bigger explosion radius
   - Extra life
   - Slow motion
   - Magnetic goal

2. **Obstacle types:**
   - Breakable walls (1-hit)
   - Unbreakable walls (must navigate around)
   - Moving obstacles
   - Rotating obstacles

3. **Additional mechanics:**
   - Limited explosions per level
   - Explosion cooldown timer
   - Multiple Flic buttons for different powers
   - Time-based challenges

## Known Issues & Limitations

### Build System

- **Gradle warning:** Package attribute in AndroidManifest.xml deprecated (can be removed)
- **Flic SDK:** Official Maven repository not accessible; using broadcast intents instead

### Performance

- No known performance issues on target device
- LibGDX handles 60 FPS easily with current game complexity

### Flic Button

- **Sound issue:** Flic app plays system sound on button press (disable in Flic app settings)
- **Latency:** ~50-100ms delay from button press to explosion (acceptable)
- **Configuration:** Requires manual setup in Flic app (not automatic)
- **Flic 2 not tested:** Code supports both Flic 1 and Flic 2, but only Flic 1 verified

### Deployment

- **No USB:** Must use cloud storage/email/HTTP server for APK transfer
- **Gmail blocks APK:** Must zip file before emailing
- **Installation:** Requires "Unknown Sources" enabled on tablet

## Development Workflow

### Making Changes

1. Edit code in `/core/src/` for game logic (platform-independent)
2. Edit code in `/android/src/` for Android-specific features
3. Build: `./build-apk.sh`
4. Transfer APK to tablet
5. Install and test

### Testing Without Device

- LibGDX supports desktop testing, but not configured in this project
- Can add desktop module if needed for faster iteration

### Adding New Screens

1. Create new class extending `Screen` in `/core/src/com/slidegame/game/`
2. Implement required methods: `show()`, `render()`, `hide()`, `dispose()`
3. Set screen via `game.setScreen(new YourScreen(game))`

### Adding New Entities

1. Create class with `render(ShapeRenderer)` and `update(float delta)` methods
2. Add instance to `GameScreen`
3. Call `update()` and `render()` in game loop

## Code Style & Conventions

- **Package:** `com.slidegame.game`
- **Naming:** PascalCase for classes, camelCase for methods/variables
- **Screen size:** Use `SlideGame.VIRTUAL_WIDTH` and `VIRTUAL_HEIGHT` constants
- **Logging:** Use `Gdx.app.log(TAG, message)` for game logs, `Log.d(TAG, message)` for Android logs
- **Comments:** Minimal comments; code should be self-documenting

## Contact & Support

**Original requirements:**
- Ball size: 1/7 of screen height ✅
- Landscape orientation ✅
- Touch/slide controls ✅
- Bluetooth Flic button for explosion ✅
- Obstacles (walls) - future feature
- Lose life on wall collision - future feature
- Explode to destroy obstacles - future feature

**Development notes:**
- All core POC features complete
- Flic integration working with broadcast intents
- Ready for obstacle implementation

---

*Last updated: 2026-05-30*  
*Version: 1.0-POC*
