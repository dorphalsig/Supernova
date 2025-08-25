#!/usr/bin/env bash
export ANDROID_HOME="/opt/sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
mkdir -p "$ANDROID_HOME"
# shellcheck disable=SC2164
cd $ANDROID_HOME
apt update && apt install -y gh curl unzip 7zip
echo 'Downloading Android SDK & Friends'
wget https://pub-1a4579a1bdf24937913389c14b16471a.r2.dev/android.zip
7zz x android.zip && rm -f android.zip
# shellcheck disable=SC2164
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

echo "Configuring gradle..."
bash ./gradlew dependencies --build-cache
echo "Setup complete - ready!"