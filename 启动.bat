@echo off
cd /d "%~dp0"

echo ============================================
echo   Library Management System
echo ============================================
echo.

echo [1/2] Compiling...
javac -encoding UTF-8 -d bin src/main/java/com/library/model/*.java src/main/java/com/library/util/*.java src/main/java/com/library/dao/*.java src/main/java/com/library/service/*.java src/main/java/com/library/controller/*.java src/main/java/com/library/App.java
if errorlevel 1 (
    echo Compile failed!
    pause
    exit /b
)
echo Done.
echo.

echo [2/2] Starting...
echo.
echo     http://localhost:8888
echo     admin / admin123
echo     2024001 / 123456
echo     Ctrl+C to stop
echo ============================================

start http://localhost:8888
java -cp bin -Dapp.dir=. -Dapp.port=8888 com.library.App

pause
