@echo off
chcp 65001 >nul
title Museum Catalog
cd /d "%~dp0musem"
echo.
echo ══════════════════════════════════════
echo   Запуск Museum Catalog...
echo ══════════════════════════════════════
echo.
mvn javafx:run
if %errorlevel% neq 0 (
    echo.
    echo [ОШИБКА] Не удалось запустить приложение
    echo Проверьте что установлены Java 17+ и Maven
    pause
)
