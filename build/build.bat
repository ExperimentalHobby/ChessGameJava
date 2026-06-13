@echo off
setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0..\
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
if not exist target\test-classes mkdir target\test-classes

REM Step 1: Main sources (Swing only, JavaFX uses Maven)
echo [1/4] Compiling main sources...

javac -d target\classes ^
  src\main\java\com\chessgame\model\*.java ^
  src\main\java\com\chessgame\board\model\*.java ^
  src\main\java\com\chessgame\piece\model\*.java ^
  src\main\java\com\chessgame\piece\rules\*.java ^
  src\main\java\com\chessgame\move\model\*.java ^
  src\main\java\com\chessgame\gamestate\model\*.java ^
  src\main\java\com\chessgame\detection\rules\*.java ^
  src\main\java\com\chessgame\game\core\*.java ^
  src\main\java\com\chessgame\game\player\*.java ^
  src\main\java\com\chessgame\game\observer\*.java ^
  src\main\java\com\chessgame\rules\*.java ^
  src\main\java\com\chessgame\swing\ui\*.java ^
  src\main\java\com\chessgame\swing\board\*.java ^
  src\main\java\com\chessgame\swing\asset\*.java ^
  src\main\java\com\chessgame\*.java

if %errorlevel% neq 0 (
    echo Main compilation failed!
    exit /b 1
)
echo   OK

REM Step 2: Demo runners
echo [2/4] Compiling demo runners...
javac -cp target\classes -d target\test-classes ^
  src\test\java\com\chessgame\TestGame.java ^
  src\test\java\com\chessgame\SpecialMovesTest.java
if %errorlevel% neq 0 (
    echo Demo runner compilation failed!
    exit /b 1
)
echo   OK

REM Step 3: JUnit tests (requires JUnit on classpath)
echo [3/4] Compiling JUnit tests...
javac -cp target\classes -d target\test-classes ^
  src\test\java\com\chessgame\board\*.java ^
  src\test\java\com\chessgame\game\core\*.java ^
  src\test\java\com\chessgame\game\player\*.java ^
  src\test\java\com\chessgame\move\*.java ^
  src\test\java\com\chessgame\piece\*.java 2>nul
if %errorlevel% neq 0 (
    echo   SKIPPED ^(run via Maven: mvnw.cmd test^)
) else (
    echo   OK
)

REM Step 4: Package exe
echo [4/4] Packaging exe...
if "%TARGET%"=="javafx" goto :package_javafx

REM --- Swing ---
echo   [4a] Creating JAR...
jar --create --file target\ChessGame.jar --main-class com.chessgame.Main -C target\classes .
if !errorlevel! neq 0 (
    echo   FAILED ^(JAR creation failed^)
    exit /b 1
)
echo   [4b] Packaging exe...
if exist dist\ChessGame rd /s /q dist\ChessGame 2>nul
if exist dist\ChessGame (
    echo   FAILED ^(dist\ChessGame is locked - close ChessGame.exe and retry^)
    exit /b 1
)
jpackage --type app-image --name ChessGame --app-version 1.0 ^
  --input target --main-jar ChessGame.jar ^
  --main-class com.chessgame.Main --dest dist
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
echo   [4a] Maven compile...
call "%PROJECT_DIR%mvnw.cmd" compile -q -DskipTests
if !errorlevel! neq 0 (
    echo   FAILED ^(check mvnw output above^)
    exit /b 1
)
echo   [4b] Collecting JavaFX JARs...
call "%PROJECT_DIR%mvnw.cmd" dependency:copy-dependencies -q -DincludeGroupIds=org.openjfx -DoutputDirectory=target\javafx-libs
if !errorlevel! neq 0 (
    echo   FAILED ^(dependency copy failed^)
    exit /b 1
)
echo   [4c] Packaging exe...
if exist dist\ChessGameFX rd /s /q dist\ChessGameFX 2>nul
if exist dist\ChessGameFX (
    echo   FAILED ^(dist\ChessGameFX is locked - close ChessGameFX.exe and retry^)
    exit /b 1
)
if not exist target\javafx-input mkdir target\javafx-input
copy /y target\javafx-libs\*.jar target\javafx-input\ >nul
jar --create --file target\javafx-input\ChessGameFX.jar -C target\classes .
jpackage --type app-image --name ChessGameFX --app-version 1.0 ^
  --input target\javafx-input ^
  --main-jar ChessGameFX.jar ^
  --main-class com.chessgame.javafx.FXLauncher ^
  --java-options "--module-path $APPDIR --add-modules javafx.controls,javafx.graphics,javafx.base" ^
  --dest dist
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
echo   Test classes:  target\test-classes
echo.
echo To run GUI (Swing):
echo   java -cp target\classes com.chessgame.Main
echo.
echo To run interactive game:
echo   java -cp target\classes com.chessgame.InteractiveGame
echo.
echo To run demo runners:
echo   java -cp "target\classes;target\test-classes" com.chessgame.TestGame
echo   java -cp "target\classes;target\test-classes" com.chessgame.SpecialMovesTest
echo.
if "%TARGET%"=="javafx" (
    echo Executable:  dist\ChessGameFX\ChessGameFX.exe
    echo.
    echo To run JavaFX GUI:
    echo   mvnw.cmd javafx:run
) else (
    echo Executable:  dist\ChessGame\ChessGame.exe
    echo.
    echo To build JavaFX version:
    echo   build\build.bat --javafx
)
echo.

endlocal
