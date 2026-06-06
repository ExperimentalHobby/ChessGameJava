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

REM Step 1: Main sources (Swing + model)
echo [1/4] Compiling main sources...

javac -d target\classes ^
  src\main\java\com\chessgame\model\*.java ^
  src\main\java\com\chessgame\model\board\*.java ^
  src\main\java\com\chessgame\model\piece\*.java ^
  src\main\java\com\chessgame\model\move\*.java ^
  src\main\java\com\chessgame\rules\*.java ^
  src\main\java\com\chessgame\game\*.java ^
  src\main\java\com\chessgame\swing\*.java ^
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
  src\test\java\com\chessgame\game\*.java ^
  src\test\java\com\chessgame\model\board\*.java ^
  src\test\java\com\chessgame\model\move\*.java 2>nul
if %errorlevel% neq 0 (
    echo   SKIPPED (run via Maven: mvnw.cmd test)
) else (
    echo   OK
)

REM Step 4: JavaFX sources (requires Maven / JavaFX SDK)
echo [4/4] Compiling JavaFX sources...
if "%TARGET%"=="javafx" (
    if exist "%PROJECT_DIR%mvnw.cmd" (
        call "%PROJECT_DIR%mvnw.cmd" compile -q -DskipTests
        if %errorlevel% neq 0 (
            echo   FAILED (check mvnw output above)
            exit /b 1
        ) else (
            echo   OK
        )
    ) else (
        echo   FAILED (mvnw.cmd not found)
        exit /b 1
    )
) else (
    echo   SKIPPED (pass --javafx to include)
)

echo.
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
    echo To run JavaFX GUI:
    echo   mvnw.cmd javafx:run
) else (
    echo To build with JavaFX:
    echo   build\build.bat --javafx
)
echo.

endlocal
