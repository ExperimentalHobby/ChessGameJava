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

# Step 1: Main sources (Swing only, JavaFX uses Maven)
echo "[1/2] Compiling main sources..."
# ソースは自動列挙する（新しいパッケージを追加しても build.sh の変更は不要）。
# JavaFX は --javafx 時に Maven でコンパイルするためここでは除外する。
find src/main/java -name '*.java' -not -path '*/javafx/*' > target/sources.txt
# -serial は無効化: Swing コンポーネントは直列化しないため serialVersionUID の警告はノイズ。
# それ以外の lint 警告は -Werror でビルド失敗にする。
javac -Xlint:all,-serial -Werror -d target/classes @target/sources.txt
echo "  OK"

# Step 2: JavaFX sources (requires Maven / JavaFX SDK)
echo "[2/2] Compiling JavaFX sources..."
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
