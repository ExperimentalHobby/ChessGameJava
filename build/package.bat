@echo off
setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0..\
cd /d "%PROJECT_DIR%"

echo ===================================
echo   Chess Game Packaging Script
echo ===================================
echo.

set TARGET=swing
if /i "%1"=="--javafx" set TARGET=javafx
if /i "%1"=="--swing"  set TARGET=swing

echo Target: %TARGET%
echo.

if "%TARGET%"=="javafx" goto :build_javafx

REM ============================================================
REM Swing exe (default) - no external SDK required
REM ============================================================
:build_swing
echo [1/3] Compiling main sources...
if not exist target\classes mkdir target\classes
javac -d target\classes ^
  src\main\java\com\chessgame\model\*.java ^
  src\main\java\com\chessgame\model\board\*.java ^
  src\main\java\com\chessgame\model\piece\*.java ^
  src\main\java\com\chessgame\model\move\*.java ^
  src\main\java\com\chessgame\rules\*.java ^
  src\main\java\com\chessgame\game\*.java ^
  src\main\java\com\chessgame\util\*.java ^
  src\main\java\com\chessgame\swing\*.java ^
  src\main\java\com\chessgame\*.java
if %errorlevel% neq 0 ( echo Compilation failed! & exit /b 1 )
echo   OK

echo [2/3] Creating JAR...
jar --create --file target\ChessGame.jar --main-class com.chessgame.Main -C target\classes .
if %errorlevel% neq 0 ( echo JAR creation failed! & exit /b 1 )
echo   OK

echo [3/3] Packaging exe (Swing)...
if exist dist\ChessGame powershell -NoProfile -Command "Remove-Item -Recurse -Force 'dist\ChessGame' -ErrorAction SilentlyContinue"
jpackage --type app-image --name ChessGame --app-version 1.0 ^
  --input target --main-jar ChessGame.jar ^
  --main-class com.chessgame.Main --dest dist
if %errorlevel% neq 0 ( echo Packaging failed! & exit /b 1 )

echo.
echo ===================================
echo   Done (Swing)
echo ===================================
echo   Executable: dist\ChessGame\ChessGame.exe
echo   Distribute the dist\ChessGame folder (JRE bundled).
echo.
goto :end

REM ============================================================
REM JavaFX exe - uses Maven Wrapper to resolve JavaFX SDK
REM ============================================================
:build_javafx
if not exist "%PROJECT_DIR%mvnw.cmd" (
    echo mvnw.cmd not found. Run this script from the project root.
    exit /b 1
)

echo [1/3] Compiling with Maven (downloads JavaFX if needed)...
call "%PROJECT_DIR%mvnw.cmd" compile -q -DskipTests
if %errorlevel% neq 0 ( echo Maven compile failed! & exit /b 1 )
echo   OK

echo [2/3] Collecting JavaFX JARs...
call "%PROJECT_DIR%mvnw.cmd" dependency:copy-dependencies -q ^
  -DincludeGroupIds=org.openjfx ^
  -DoutputDirectory=target\javafx-libs
if %errorlevel% neq 0 ( echo Dependency copy failed! & exit /b 1 )
echo   OK

echo [3/3] Packaging exe (JavaFX)...
if exist dist\ChessGameFX powershell -NoProfile -Command "Remove-Item -Recurse -Force 'dist\ChessGameFX' -ErrorAction SilentlyContinue"
if not exist target\javafx-input mkdir target\javafx-input

REM JavaFX JARs とアプリ JAR を同じ input フォルダに集める
copy /y target\javafx-libs\*.jar target\javafx-input\ >nul

REM アプリ JAR を作成（main-class は launcher で指定するためマニフェスト不要）
jar --create --file target\javafx-input\ChessGameFX.jar ^
  -C target\classes .

REM jpackage の --module-path / --add-modules をトップレベルで指定する
REM ビルド時のパスが埋め込まれると exe 実行時に JavaFX が見つからず異常終了する問題を回避。
REM --java-options で $APPDIR を使うことで、実行時に bundled app ディレクトリを
REM モジュールパスとして渡し、そこから JavaFX を解決できる。
jpackage --type app-image --name ChessGameFX --app-version 1.0 ^
  --input target\javafx-input ^
  --main-jar ChessGameFX.jar ^
  --main-class com.chessgame.javafx.FXLauncher ^
  --java-options "--module-path $APPDIR --add-modules javafx.controls,javafx.graphics,javafx.base" ^
  --dest dist
if %errorlevel% neq 0 ( echo Packaging failed! & exit /b 1 )

echo.
echo ===================================
echo   Done (JavaFX)
echo ===================================
echo   Executable: dist\ChessGameFX\ChessGameFX.exe
echo   Distribute the dist\ChessGameFX folder (JRE bundled).
echo.

:end
endlocal
