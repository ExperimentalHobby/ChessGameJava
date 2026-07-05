#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
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

# Step 1: Main sources (Swing only, JavaFX uses Maven)
echo "[1/3] Compiling main sources..."
javac -d target/classes \
  src/main/java/com/chessgame/model/*.java \
  src/main/java/com/chessgame/board/model/*.java \
  src/main/java/com/chessgame/piece/model/*.java \
  src/main/java/com/chessgame/piece/rules/*.java \
  src/main/java/com/chessgame/move/model/*.java \
  src/main/java/com/chessgame/gamestate/model/*.java \
  src/main/java/com/chessgame/detection/rules/*.java \
  src/main/java/com/chessgame/notation/rules/*.java \
  src/main/java/com/chessgame/game/core/*.java \
  src/main/java/com/chessgame/game/player/*.java \
  src/main/java/com/chessgame/game/observer/*.java \
  src/main/java/com/chessgame/rules/*.java \
  src/main/java/com/chessgame/swing/ui/dialog/*.java \
  src/main/java/com/chessgame/swing/ui/panel/*.java \
  src/main/java/com/chessgame/swing/ui/*.java \
  src/main/java/com/chessgame/swing/board/*.java \
  src/main/java/com/chessgame/swing/asset/*.java \
  src/main/java/com/chessgame/*.java
echo "  OK"

# Step 2: JUnit tests (requires JUnit on classpath)
echo "[2/3] Compiling JUnit tests..."
if javac -cp target/classes -d target/test-classes \
  src/test/java/com/chessgame/board/*.java \
  src/test/java/com/chessgame/game/core/*.java \
  src/test/java/com/chessgame/game/player/*.java \
  src/test/java/com/chessgame/move/*.java \
  src/test/java/com/chessgame/piece/*.java 2>/dev/null; then
  echo "  OK"
else
  echo "  SKIPPED (run via Maven: ./mvnw test)"
fi

# Step 3: JavaFX sources (requires Maven / JavaFX SDK)
echo "[3/3] Compiling JavaFX sources..."
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
if [ "$TARGET" = "javafx" ]; then
  echo "To run JavaFX GUI:"
  echo "  ./mvnw javafx:run"
else
  echo "To build with JavaFX:"
  echo "  ./build.sh --javafx"
fi
echo ""
