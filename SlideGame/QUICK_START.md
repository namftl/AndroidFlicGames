# Quick Start Guide

## What You Have

Your SlideGame POC is complete and ready to build! Here's what's implemented:

### ✅ Completed Features
- 2D ball physics with momentum and friction
- Touch/drag/flick controls
- Ball bounces off screen edges
- Goal target to reach
- Stage completion screen
- Lives system (3 lives)
- Landscape orientation
- Ball size is 1/7 of screen height

### 📋 What You Need to Build

**You need the Android SDK installed** to build the APK. Your code is ready to compile!

## Installation Steps

### Option 1: Install Android Studio (Easiest)
1. Download from: https://developer.android.com/studio
2. Install Android Studio
3. Open Android Studio once to let it install the SDK
4. The SDK will be at: `~/Library/Android/sdk`

### Option 2: Command Line Tools Only
```bash
# Download Android command-line tools from:
# https://developer.android.com/studio#command-tools

# Create SDK directory
mkdir -p ~/android-sdk
cd ~/android-sdk

# Unzip the downloaded tools
# Then install required packages:
./cmdline-tools/bin/sdkmanager --sdk_root=. "platform-tools" "platforms;android-33" "build-tools;33.0.0"
```

## Build the APK

### Step 1: Create local.properties

Create a file named `local.properties` in the SlideGame directory:

```properties
sdk.dir=/Users/noam.fattal/Library/Android/sdk
```

(Replace the path with your actual Android SDK location)

### Step 2: Run the Build Script

```bash
cd /Users/noam.fattal/slideGame/SlideGame
./build-apk.sh
```

The script will:
- Use Java 17 (already on your system)
- Build the debug APK
- Tell you where the APK file is located

### Step 3: Deploy to Your Tablet

**Method 1 - Cloud Storage** (Recommended):
1. Find the APK at: `android/build/outputs/apk/debug/android-debug.apk`
2. Upload to Google Drive or Dropbox
3. Download on your tablet and install

**Method 2 - Email**:
1. Email the APK to yourself
2. Open on tablet and install

**Method 3 - Local Network**:
```bash
cd android/build/outputs/apk/debug
python3 -m http.server 8000
# Find your IP with: ifconfig | grep "inet "
# On tablet browser: http://YOUR_IP:8000/android-debug.apk
```

### Step 4: Install on Tablet
1. On tablet: Settings > Security > Enable "Unknown Sources"
2. Download/open the APK file
3. Tap "Install"
4. Launch SlideGame!

## Game Controls

- **Drag the ball** - Touch near the ball and drag
- **Release** - The ball flicks in the opposite direction
- **Reach the green goal** - Complete the stage
- **Yellow line** - Shows your drag direction/strength

## Testing Checklist

Once installed, test:
- [ ] Ball responds to touch and drag
- [ ] Ball flicks when you release
- [ ] Ball bounces off screen edges
- [ ] Ball slows down naturally (friction)
- [ ] Reaching the goal shows completion screen
- [ ] Can advance to next stage
- [ ] Lives counter displays
- [ ] Game works in landscape mode

## What's Next

After testing the POC, we can add:
1. **Obstacles** - Walls that cost lives when hit
2. **Bluetooth buttons** - Controller support for explosion mechanic
3. **Explosion** - Ball destroys obstacles when button pressed
4. **More stages** - Progressive difficulty
5. **Better graphics** - Replace circles with sprites
6. **Sound effects** - Add audio feedback

## Troubleshooting

**"SDK location not found"**
- Create `local.properties` with correct SDK path

**Build fails with Java errors**
- Make sure to use the build script (it sets Java 17)

**Can't install on tablet**
- Enable "Unknown Sources" in Security settings
- Make sure tablet is Android 4.0 or newer

**App crashes on launch**
- Check Android version (needs 4.0+)
- Try clearing app data and reinstalling

Need help? Check the full README.md for detailed information!
