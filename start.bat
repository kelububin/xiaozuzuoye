@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist build\classes mkdir build\classes
javac -encoding UTF-8 -sourcepath src -d build\classes src\com\ecshop\App.java
if errorlevel 1 goto failed
java -Dfile.encoding=UTF-8 -Dstdin.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "build\classes;lib\mysql-connector-java-8.0.28.jar" com.ecshop.App
pause
exit /b
:failed
echo Compilation failed. Please install JDK 17 or newer and check PATH.
pause
exit /b 1

