@echo off
echo Cleaning workspace...
gradlew clean
echo Setting up decomp workspace...
gradlew setupDecompWorkspace
echo Generating Eclipse project files (this will also fix .project automatically)...
gradlew eclipse

echo.
echo Done! Your workspace should now be properly configured.
echo The build folder will be excluded from Eclipse indexing.
pause