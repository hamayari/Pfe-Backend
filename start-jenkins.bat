@echo off
echo =========================================
echo STARTING JENKINS WITH DOCKER SUPPORT
echo =========================================
echo.

REM Check if Jenkins container already exists
docker ps -a | findstr jenkins >nul 2>&1
if %errorlevel% equ 0 (
    echo Jenkins container already exists. Removing it...
    docker stop jenkins 2>nul
    docker rm jenkins 2>nul
)

echo Starting Jenkins container...
docker run -d ^
  --name jenkins ^
  -p 8090:8080 ^
  -p 50000:50000 ^
  -v jenkins-data:/var/jenkins_home ^
  -v //var/run/docker.sock:/var/run/docker.sock ^
  --restart unless-stopped ^
  --privileged ^
  jenkins/jenkins:lts-jdk17

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to start Jenkins!
    pause
    exit /b 1
)

echo.
echo Waiting for Jenkins to start (30 seconds)...
timeout /t 30 /nobreak >nul

echo.
echo Installing Docker CLI inside Jenkins container...
docker exec -u root jenkins bash -c "apt-get update && apt-get install -y docker.io"

echo.
echo Getting Jenkins initial admin password...
echo.
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

echo.
echo =========================================
echo JENKINS IS READY!
echo =========================================
echo.
echo URL: http://localhost:8090
echo Username: admin
echo Password: (shown above)
echo.
echo Opening Jenkins in browser...
start http://localhost:8090
echo.
pause
