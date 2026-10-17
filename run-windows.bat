@echo off
setlocal
cd /d "%~dp0"
title FamilyShield - Digital Scam Prevention

echo ==========================================
echo   FamilyShield - Digital Scam Prevention
echo ==========================================
echo.

where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java was not found. Install JDK 8 or newer and add it to PATH.
    echo Download: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
    echo ERROR: Maven was not found. Install Maven 3.8 or newer and add its bin folder to PATH.
    echo Then close and reopen Command Prompt and try again.
    echo.
    pause
    exit /b 1
)

echo Checking Java version...
java -version
echo.
echo Building FamilyShield. The first build may take a few minutes to download dependencies.
echo.
call mvn -B clean package
if errorlevel 1 (
    echo.
    echo BUILD FAILED. Read the error messages above.
    echo Common causes: no internet for Maven downloads, or a Java/Maven setup problem.
    echo.
    pause
    exit /b 1
)

echo.
echo Build successful. Starting FamilyShield...
echo.
if not exist "target\FamilyShield.jar" (
    echo ERROR: target\FamilyShield.jar was not created by Maven.
    echo Check the build output above.
    pause
    exit /b 1
)
java -jar "target\FamilyShield.jar"
if errorlevel 1 (
    echo.
    echo FamilyShield closed with an error. Check the messages above.
    echo If the message mentions MySQL access denied, update config\db.properties.
    echo.
    pause
    exit /b 1
)
echo.
echo FamilyShield has closed.
pause
endlocal
