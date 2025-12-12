@echo off
chcp 65001 >nul
cd /d "%~dp0musem"
mvn javafx:run -q
