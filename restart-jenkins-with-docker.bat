@echo off
echo ========================================
echo RESTART JENKINS WITH DOCKER ACCESS
echo ========================================
echo.

REM Arrêter et supprimer l'ancien Jenkins
echo Arret de Jenkins...
docker stop jenkins 2>nul
docker rm jenkins 2>nul

echo.
echo Demarrage de Jenkins avec acces Docker...

REM Démarrer Jenkins avec accès Docker
docker run -d ^
  --name jenkins ^
  --restart=on-failure ^
  -p 8090:8080 ^
  -p 50000:50000 ^
  -v jenkins-data:/var/jenkins_home ^
  -v /var/run/docker.sock:/var/run/docker.sock ^
  -u root ^
  jenkins/jenkins:lts-jdk17

echo.
echo Attente du demarrage (30 secondes)...
timeout /t 30 /nobreak

echo.
echo Installation de Docker CLI dans Jenkins...
docker exec -u root jenkins bash -c "apt-get update && apt-get install -y docker.io maven"

echo.
echo ========================================
echo JENKINS EST PRET !
echo ========================================
echo.
echo URL: http://localhost:8090
echo Username: admin
echo Password: jenkins
echo.
pause
