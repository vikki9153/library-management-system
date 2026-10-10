@echo off
setlocal
cd /d "%~dp0"
set "JDK_BIN="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JDK_BIN=%JAVA_HOME%\bin"
if not defined JDK_BIN for /d %%E in ("%USERPROFILE%\.vscode\extensions\redhat.java-*\jre\*") do if exist "%%~E\bin\javac.exe" set "JDK_BIN=%%~E\bin"
if not defined JDK_BIN for %%J in (javac.exe) do if not "%%~$PATH:J"=="" set "JDK_BIN=%%~dp$PATH:J"
if not defined JDK_BIN (
  echo Java JDK not found. Install a JDK or set JAVA_HOME, then run this file again.
  pause
  exit /b 1
)
if not exist build mkdir build
"%JDK_BIN%\javac.exe" --add-modules jdk.httpserver -d build backend\LibraryManagementSystem.java
if errorlevel 1 (
  echo Java compilation failed.
  pause
  exit /b 1
)
echo Open http://127.0.0.1:8080 in your browser. Press Ctrl+C to stop.
"%JDK_BIN%\java.exe" --add-modules jdk.httpserver -cp build LibraryManagementSystem
pause
