#!/usr/bin/env bash
# --- ANDROID SDK SETUP ---
export ANDROID_HOME="/opt/sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

mkdir -p "$ANDROID_HOME"

echo "Downloading Android command line tools..."
curl -L "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -o tools.zip

echo "Extracting tools..."
unzip -q tools.zip && rm tools.zip
find . -name cmdline-tools
# Move command line tools into correct structure (latest Google SDK zips have nested 'cmdline-tools')
mkdir -p "$ANDROID_HOME/cmdline-tools/latest"
mv cmdline-tools/* "$ANDROID_HOME/cmdline-tools/latest/"
rm -rf cmdline-tools

# --- SDK LICENSES AND TOOLS ---
echo "Accepting SDK Licenses..."
bash -c "yes | sdkmanager --sdk_root=\"${ANDROID_HOME}\" --licenses 2>&1 | tail -n 1"

echo "Downloading SDK Tools..."
bash -c "yes | sdkmanager --sdk_root=\"${ANDROID_HOME}\" \"platform-tools\" \"build-tools;34.0.0\" \"platforms;android-34\" 2>&1"

# --- GRADLE SETUP ---
if [ -d "/workspace/Supernova" ]; then
    cd /workspace/Supernova

    if ! ./gradlew --version &>/dev/null; then
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
    ./gradlew dependencies
else
    echo "Warning: /workspace/Supernova directory not found, skipping Gradle setup"
fi

echo "Setup complete - ready!"
