@echo off
setlocal

set "MVN_VERSION=3.9.9"
set "MVN_DIR=%~dp0.mvn\bin\apache-maven-%MVN_VERSION%"
set "MVN_CMD=%MVN_DIR%\bin\mvn.cmd"
set "MVN_ZIP_URL=https://archive.apache.org/dist/maven/maven-3/%MVN_VERSION%/binaries/apache-maven-%MVN_VERSION%-bin.zip"
set "MVN_ZIP=%TEMP%\apache-maven-%MVN_VERSION%-bin.zip"

where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set "MVN_CMD=mvn"
    goto :run
)

if exist "%MVN_CMD%" goto :run

echo [mvnw] Maven not found. Downloading Apache Maven %MVN_VERSION%...
if not exist "%~dp0.mvn\bin" mkdir "%~dp0.mvn\bin"
powershell -NoProfile -Command "Invoke-WebRequest -Uri '%MVN_ZIP_URL%' -OutFile '%MVN_ZIP%'; Expand-Archive -Path '%MVN_ZIP%' -DestinationPath '%~dp0.mvn\bin' -Force; Remove-Item '%MVN_ZIP%'"
if %errorlevel% neq 0 (
    echo [mvnw] Download failed. Install Maven manually: https://maven.apache.org/download.cgi
    exit /b 1
)
echo [mvnw] Maven %MVN_VERSION% ready.

:run
"%MVN_CMD%" %*
endlocal
