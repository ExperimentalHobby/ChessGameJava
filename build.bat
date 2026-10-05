@echo off
setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0
cd /d "%PROJECT_DIR%"

echo ===================================
echo   Chess Game Build Script
echo ===================================
echo.
echo Project Directory: %PROJECT_DIR%
echo.

set TARGET=swing
if /i "%1"=="--javafx" set TARGET=javafx
if /i "%1"=="--swing"  set TARGET=swing

echo Target: %TARGET%
echo.

if not exist target\classes mkdir target\classes

REM Step 1: Main sources (Swing only, JavaFX uses Maven)
echo [1/2] Compiling main sources...

REM Enumerate sources automatically so new packages never need a build.bat change.
REM JavaFX sources are excluded here (they are compiled by Maven with --javafx).
REM Paths are written with forward slashes inside quotes, as javac @argfiles treat backslashes as escapes.
if exist target\sources.txt del target\sources.txt
for /f "delims=" %%F in ('dir /s /b src\main\java\*.java ^| findstr /v /i "\javafx\\"') do (
    set "SRC=%%F"
    echo "!SRC:\=/!">>target\sources.txt
)

REM -serial is disabled: Swing components are never serialized, so serialVersionUID warnings are noise.
REM All other lint warnings fail the build (-Werror).
javac -Xlint:all,-serial -Werror -d target\classes @target\sources.txt
if %errorlevel% neq 0 (
    echo Main compilation failed!
    exit /b 1
)
echo   OK

REM Step 2: Package exe to bin\
echo [2/2] Packaging exe...
if "%TARGET%"=="javafx" goto :package_javafx

REM --- Swing ---
echo   [2a] Creating JAR...
jar --create --file target\ChessGame.jar --main-class com.chessgame.Main -C target\classes .
if !errorlevel! neq 0 (
    echo   FAILED ^(JAR creation failed^)
    exit /b 1
)
echo   [2b] Packaging exe...
if exist bin\ChessGame rd /s /q bin\ChessGame 2>nul
if exist bin\ChessGame (
    echo   FAILED ^(bin\ChessGame is locked - close ChessGame.exe and retry^)
    exit /b 1
)
REM jpackage copies the whole --input folder into app\, so passing target as-is would bundle
REM Maven intermediates (classes\ etc.). Stage only the jar and the Python AI scripts instead.
REM AIPlayer looks for ai\chess_ai.py next to the jar, hence app\ChessGame.jar + app\ai\.
if exist target\package-input rd /s /q target\package-input
mkdir target\package-input\ai
copy /y target\ChessGame.jar target\package-input\ >nul
copy /y ai\*.py target\package-input\ai\ >nul
del /q target\package-input\ai\test_*.py target\package-input\ai\*_stub.py
jpackage --type app-image --name ChessGame --app-version 1.0 ^
  --input target\package-input --main-jar ChessGame.jar ^
  --main-class com.chessgame.Main --dest bin
if !errorlevel! neq 0 (
    echo   FAILED ^(jpackage failed^)
    exit /b 1
)
echo   OK
goto :build_summary

:package_javafx
REM --- JavaFX ---
if not exist "%PROJECT_DIR%mvnw.cmd" (
    echo   FAILED ^(mvnw.cmd not found^)
    exit /b 1
)
echo   [2a] Maven compile...
call "%PROJECT_DIR%mvnw.cmd" compile -q -DskipTests
if !errorlevel! neq 0 (
    echo   FAILED ^(check mvnw output above^)
    exit /b 1
)
echo   [2b] Collecting JavaFX JARs...
call "%PROJECT_DIR%mvnw.cmd" dependency:copy-dependencies -q -DincludeGroupIds=org.openjfx -DoutputDirectory=target\javafx-libs
if !errorlevel! neq 0 (
    echo   FAILED ^(dependency copy failed^)
    exit /b 1
)
echo   [2c] Packaging exe...
if exist bin\ChessGameFX rd /s /q bin\ChessGameFX 2>nul
if exist bin\ChessGameFX (
    echo   FAILED ^(bin\ChessGameFX is locked - close ChessGameFX.exe and retry^)
    exit /b 1
)
if not exist target\javafx-input mkdir target\javafx-input
copy /y target\javafx-libs\*.jar target\javafx-input\ >nul
jar --create --file target\javafx-input\ChessGameFX.jar -C target\classes .
REM AIPlayer looks for ai\chess_ai.py next to the jar (tests and stubs are not bundled)
if exist target\javafx-input\ai rd /s /q target\javafx-input\ai
mkdir target\javafx-input\ai
copy /y ai\*.py target\javafx-input\ai\ >nul
del /q target\javafx-input\ai\test_*.py target\javafx-input\ai\*_stub.py
jpackage --type app-image --name ChessGameFX --app-version 1.0 ^
  --input target\javafx-input ^
  --main-jar ChessGameFX.jar ^
  --main-class com.chessgame.javafx.ui.FXLauncher ^
  --java-options "--module-path $APPDIR --add-modules javafx.controls,javafx.graphics,javafx.base" ^
  --dest bin
if !errorlevel! neq 0 (
    echo   FAILED ^(jpackage failed^)
    exit /b 1
)
echo   OK

echo.
:build_summary
echo ===================================
echo   Build Summary
echo ===================================
echo   Main classes:  target\classes
echo.
echo To run GUI (Swing):
echo   java -cp target\classes com.chessgame.Main
echo.
echo To run interactive game:
echo   java -cp target\classes com.chessgame.InteractiveGame
echo.
if "%TARGET%"=="javafx" (
    echo Executable:  bin\ChessGameFX\ChessGameFX.exe
    echo.
    echo To run JavaFX GUI:
    echo   mvnw.cmd javafx:run
) else (
    echo Executable:  bin\ChessGame\ChessGame.exe
    echo.
    echo To build JavaFX version:
    echo   build.bat --javafx
)
echo.

endlocal
