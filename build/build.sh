#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "==================================="
echo "  Chess Game Build Script"
echo "==================================="
echo "Project Directory: $PROJECT_DIR"
echo ""

TARGET=swing
for arg in "$@"; do
  case "$arg" in
    --javafx) TARGET=javafx ;;
    --swing)  TARGET=swing  ;;
  esac
done

echo "Target: $TARGET"
echo ""

mkdir -p target/classes
mkdir -p target/test-classes

# Step 1: Main sources (Swing + model)
echo "[1/4] Compiling main sources..."
javac -d target/classes \
  src/main/java/com/chessgame/model/*.java \
  src/main/java/com/chessgame/model/board/*.java \
  src/main/java/com/chessgame/model/piece/*.java \
  src/main/java/com/chessgame/model/move/*.java \
  src/main/java/com/chessgame/rules/*.java \
  src/main/java/com/chessgame/game/*.java \
  src/main/java/com/chessgame/util/*.java \
  src/main/java/com/chessgame/swing/*.java \
  src/main/java/com/chessgame/*.java
echo "  OK"

# Step 2: Demo runners
echo "[2/4] Compiling demo runners..."
javac -cp target/classes -d target/test-classes \
  src/test/java/com/chessgame/TestGame.java \
  src/test/java/com/chessgame/SpecialMovesTest.java
echo "  OK"

# Step 3: JUnit tests (requires JUnit on classpath)
echo "[3/4] Compiling JUnit tests..."
if javac -cp target/classes -d target/test-classes \
  src/test/java/com/chessgame/game/*.java \
  src/test/java/com/chessgame/model/board/*.java \
  src/test/java/com/chessgame/model/move/*.java 2>/dev/null; then
  echo "  OK"
else
  echo "  SKIPPED (run via Maven: ./mvnw test)"
fi

# Step 4: JavaFX sources (requires Maven / JavaFX SDK)
echo "[4/4] Compiling JavaFX sources..."
if [ "$TARGET" = "javafx" ]; then
  if [ -f "$PROJECT_DIR/mvnw" ]; then
    chmod +x "$PROJECT_DIR/mvnw"
    if "$PROJECT_DIR/mvnw" compile -q -DskipTests; then
      echo "  OK"
    else
      echo "  FAILED (check mvnw output above)"
      exit 1
    fi
  else
    echo "  FAILED (mvnw not found)"
    exit 1
  fi
else
  echo "  SKIPPED (pass --javafx to include)"
fi

echo ""
echo "==================================="
echo "  Build Summary"
echo "==================================="
echo "  Main classes:  target/classes"
echo "  Test classes:  target/test-classes"
echo ""
echo "To run GUI (Swing):"
echo "  java -cp target/classes com.chessgame.Main"
echo ""
echo "To run interactive game:"
echo "  java -cp target/classes com.chessgame.InteractiveGame"
echo ""
echo "To run demo runners:"
echo "  java -cp 'target/classes:target/test-classes' com.chessgame.TestGame"
echo "  java -cp 'target/classes:target/test-classes' com.chessgame.SpecialMovesTest"
echo ""
if [ "$TARGET" = "javafx" ]; then
  echo "To run JavaFX GUI:"
  echo "  ./mvnw javafx:run"
else
  echo "To build with JavaFX:"
  echo "  build/build.sh --javafx"
fi
echo ""
