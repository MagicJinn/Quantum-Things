@echo off
echo Cleaning Eclipse metadata files...
if exist .classpath del /f /q .classpath
if exist .project del /f /q .project
if exist .settings rmdir /s /q .settings
echo Eclipse metadata cleaned.
echo.
echo Running Gradle tasks...
call gradlew clean setupDecompWorkspace eclipse build --refresh-dependencies
echo.
echo Workspace reset complete!
pause