#!/usr/bin/env bash

set -e

# Android SDK paths
export ANDROID_HOME="/opt/sdk"
export ANDROID_SDK_HOME="$ANDROID_HOME/.android"
export ANDROID_AVD_HOME="$ANDROID_SDK_HOME/avd"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

# GitHub secrets for upload/download
R2_USER="${GRADLE_CACHE_USER:?Missing user}"
R2_PASS="${GRADLE_CACHE_PASS:?Missing password}"
EMU_BUCKET="https://aa44b0d9c4503975b23eae50165d0e0f.r2.cloudflarestorage.com/emu"

# Base setup
cd /workspace
mkdir -p "$ANDROID_HOME"
apt-get update && apt-get install -y gh curl unzip zstd

echo "Downloading Android command line tools..."
curl -L "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -o tools.zip

echo "Extracting tools..."
unzip -q tools.zip && rm tools.zip
mkdir -p "$ANDROID_HOME/cmdline-tools/latest"
mv cmdline-tools/* "$ANDROID_HOME/cmdline-tools/latest/"
rm -rf cmdline-tools

echo "Accepting SDK Licenses..."
yes | sdkmanager --sdk_root="$ANDROID_HOME" --licenses | tail -n 1

echo "Downloading SDK Components..."
yes | sdkmanager --sdk_root="$ANDROID_HOME" \
  "platform-tools" \
  "build-tools;34.0.0" \
  "platforms;android-34" \
  "emulator" \
  "system-images;android-34;google_apis;x86_64"

# Try to restore cached emulator files
echo "Restoring emulator cache (if available)..."
curl -u "$R2_USER:$R2_PASS" -fL "$EMU_BUCKET/cache.tar.zst" -o emu-cache.tar.zst || echo "No existing cache"
if [ -f emu-cache.tar.zst ]; then
  tar --zstd -xf emu-cache.tar.zst -C "$ANDROID_HOME"
  echo "Emulator cache restored"
fi

# Create emulator if not exists
AVD_NAME="test_device"
if [ ! -f "$ANDROID_AVD_HOME/$AVD_NAME.avd/config.ini" ]; then
  echo "Creating emulator AVD..."
  echo "no" | avdmanager create avd -n "$AVD_NAME" -k "system-images;android-34;google_apis;x86_64" --device "pixel"
fi

# Pre-boot emulator (optional)
# nohup emulator -avd "$AVD_NAME" -no-snapshot -no-audio -no-window &

# Bootstrap Gradle wrapper
cd /workspace/Supernova
if ! bash ./gradlew --version &>/dev/null; then
  echo "No Gradle wrapper found. Trying to bootstrap..."
  if command -v gradle >/dev/null 2>&1; then
    gradle wrapper --gradle-version 8.14.3
  else
    echo "Error: No Gradle wrapper and no global 'gradle' command found. Please install Gradle or provide the wrapper."
    exit 1
  fi
else
  echo "Gradle wrapper is valid, skipping download"
fi

echo "Resolving Gradle deps..."
bash ./gradlew dependencies --build-cache --no-configuration-cache

# Save emulator cache if under 1GB
echo "Packing emulator cache..."
tar --zstd -cf emu-cache.tar.zst "$ANDROID_HOME/system-images" "$ANDROID_SDK_HOME"
EMU_SIZE=$(du -b emu-cache.tar.zst | cut -f1)
MAX_SIZE=$((1024 * 1024 * 1024))

if [ "$EMU_SIZE" -lt "$MAX_SIZE" ]; then
  echo "Uploading emulator cache..."
  curl -u "$R2_USER:$R2_PASS" -T emu-cache.tar.zst "$EMU_BUCKET/cache.tar.zst"
else
  echo "Cache exceeds 1GB. Skipping upload."
fi

echo "Setup complete - ready!"