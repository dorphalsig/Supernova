#!/usr/bin/env bash
#check heck
export ANDROID_HOME="/opt/sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
# shellcheck disable=SC2164
cd /workspace
mkdir -p "$ANDROID_HOME"
apt-get update && apt-get install -y gh curl unzip
echo "Downloading Android command line tools..."
curl -L "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -o tools.zip

echo "Extracting tools..."
unzip -q tools.zip && rm tools.zip
find . -name cmdline-tools


mkdir -p "$ANDROID_HOME/cmdline-tools/latest"
mv cmdline-tools/* "$ANDROID_HOME/cmdline-tools/latest/"
rm -rf cmdline-tools

echo "Accepting SDK Licenses..."
bash -c "yes | sdkmanager --sdk_root=\"${ANDROID_HOME}\" --licenses 2>&1 | tail -n 1"

echo "Downloading SDK Tools..."
bash -c "yes | sdkmanager --sdk_root=\"${ANDROID_HOME}\" \"platform-tools\" \"build-tools;34.0.0\" \"platforms;android-34\" 2>&1"

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
bash ./gradlew dependencies --build-cache --no-configuration-cache
echo "Setup complete - ready!"
