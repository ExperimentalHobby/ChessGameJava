@echo off
setlocal

set "MVN_VERSION=4.0.0-rc-5"
set "MVN_DIR=%~dp0.mvn\bin\apache-maven-%MVN_VERSION%"
set "MVN_CMD=%MVN_DIR%\bin\mvn.cmd"
set "MVN_ZIP_URL=https://downloads.apache.org/maven/maven-4/%MVN_VERSION%/binaries/apache-maven-%MVN_VERSION%-bin.zip"
set "MVN_ZIP=%TEMP%\apache-maven-%MVN_VERSION%-bin.zip"

where mvn >nul 2>&1
if %errorlevel% equ 0 set "MVN_CMD=mvn"
if %errorlevel% equ 0 goto :run

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
call %MVN_CMD% %*
endlocal
