#!/bin/sh
# Maven Wrapper — downloads Apache Maven automatically if not installed

MVN_VERSION="4.0.0-rc-5"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MVN_DIR="$SCRIPT_DIR/.mvn/bin/apache-maven-$MVN_VERSION"
MVN_CMD="$MVN_DIR/bin/mvn"
MVN_URL="https://downloads.apache.org/maven/maven-4/$MVN_VERSION/binaries/apache-maven-$MVN_VERSION-bin.tar.gz"

if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
fi

if [ -x "$MVN_CMD" ]; then
    exec "$MVN_CMD" "$@"
fi

echo "[mvnw] Maven not found. Downloading Apache Maven $MVN_VERSION..."
mkdir -p "$SCRIPT_DIR/.mvn/bin"
TMP="/tmp/apache-maven-$MVN_VERSION-bin.tar.gz"

if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$MVN_URL" -o "$TMP"
elif command -v wget >/dev/null 2>&1; then
    wget -q "$MVN_URL" -O "$TMP"
else
    echo "[mvnw] curl or wget required. Install Maven: https://maven.apache.org/download.cgi"
    exit 1
fi

tar -xzf "$TMP" -C "$SCRIPT_DIR/.mvn/bin"
rm -f "$TMP"
chmod +x "$MVN_CMD"
echo "[mvnw] Maven $MVN_VERSION ready."
exec "$MVN_CMD" "$@"
