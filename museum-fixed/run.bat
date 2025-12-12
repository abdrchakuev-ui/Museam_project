@echo off
chcp 65001 >nul
title Museum Catalog

echo ==========================================
echo   MUSEUM CATALOG - Version 1.0
echo ==========================================
echo.

:: Change to project directory
cd /d "%~dp0musem"

:: Check Java
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found! Install JDK 17+
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)

:: Check Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%g
echo [OK] Java found: %JAVA_VER%

:: Check Maven
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] Maven not found!
    echo Install Maven: https://maven.apache.org/download.cgi
    pause
    exit /b 1
) else (
    set MVN=mvn
)

:menu
echo.
echo ==========================================
echo   MENU
echo ==========================================
echo   [1] Run application
echo   [2] Build project (compile)
echo   [3] Run tests
echo   [4] Create JAR file
echo   [5] Clean and rebuild
echo   [6] Generate Javadoc
echo   [0] Exit
echo ==========================================
echo.

set /p choice="Select action: "

if "%choice%"=="1" goto run
if "%choice%"=="2" goto compile
if "%choice%"=="3" goto test
if "%choice%"=="4" goto package
if "%choice%"=="5" goto clean
if "%choice%"=="6" goto javadoc
if "%choice%"=="0" exit /b 0
goto menu

:run
echo.
echo [INFO] Running application...
echo.
call %MVN% javafx:run
goto end

:compile
echo.
echo [INFO] Compiling project...
call %MVN% compile
goto menu

:test
echo.
echo [INFO] Running tests...
call %MVN% test
goto menu

:package
echo.
echo [INFO] Creating JAR file...
call %MVN% package -DskipTests
echo.
echo [OK] JAR file created in target/
goto menu

:clean
echo.
echo [INFO] Cleaning and rebuilding...
call %MVN% clean compile
goto menu

:javadoc
echo.
echo [INFO] Generating Javadoc...
call %MVN% javadoc:javadoc
echo.
echo [OK] Documentation created in target/site/apidocs/
if exist target\site\apidocs\index.html start target\site\apidocs\index.html
goto menu

:end
echo.
pause
