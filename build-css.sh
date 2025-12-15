#!/bin/bash

# Configuration
TAILWIND_VERSION="v3.4.1"
OS=$(uname -s | tr '[:upper:]' '[:lower:]')
ARCH=$(uname -m)
BINARY_NAME="tailwindcss"
DOWNLOAD_URL=""

# Determine Architecture
if [[ "$ARCH" == "x86_64" ]]; then
  ARCH_SUFFIX="x64"
elif [[ "$ARCH" == "arm64" ]] || [[ "$ARCH" == "aarch64" ]]; then
  ARCH_SUFFIX="arm64"
else
  echo "Unsupported architecture: $ARCH"
  exit 1
fi

# Define Download URL
# For macOS (darwin) and Linux
if [[ "$OS" == "darwin" ]]; then
    DOWNLOAD_URL="https://github.com/tailwindlabs/tailwindcss/releases/download/${TAILWIND_VERSION}/tailwindcss-macos-${ARCH_SUFFIX}"
elif [[ "$OS" == "linux" ]]; then
    DOWNLOAD_URL="https://github.com/tailwindlabs/tailwindcss/releases/download/${TAILWIND_VERSION}/tailwindcss-linux-${ARCH_SUFFIX}"
else
    echo "Unsupported OS: $OS"
    exit 1
fi

# Ensure bin directory exists
tmpBin="tmp/bin"
mkdir -p $tmpBin

# Download Binary if not present
if [[ ! -f "$tmpBin/$BINARY_NAME" ]]; then
    echo "Downloading Tailwind CSS Standalone CLI (${OS}-${ARCH_SUFFIX})..."
    curl -sL "$DOWNLOAD_URL" -o "$tmpBin/$BINARY_NAME"
    chmod +x "$tmpBin/$BINARY_NAME"
    echo "Download complete."
else
    echo "Tailwind CSS binary found."
fi

# Input/Output paths
INPUT_CSS="./src/main/resources/styles/input.css"
CONFIG_FILE="./src/main/resources/styles/tailwind.config.js"
OUTPUT_CSS="./src/main/resources/static/css/style.css"

# Ensure output directory exists
mkdir -p "$(dirname "$OUTPUT_CSS")"

echo "Building CSS..."
echo "Input: $INPUT_CSS"
echo "Output: $OUTPUT_CSS"
echo "Config: $CONFIG_FILE"

# Run Build
./$tmpBin/$BINARY_NAME -i "$INPUT_CSS" -o "$OUTPUT_CSS" --config "$CONFIG_FILE" $@

echo "CSS Build Successful!"
