@echo off
echo ========================================
echo JENKINS SETUP - VERIFICATION ET INSTALLATION
echo ========================================
echo.

REM Ajouter Docker au PATH
set PATH=%PATH%;C:\Program Files\Docker\Docker\resources\bin

echo [1/5] Verification de Docker...
docker --version
if %errorlevel% neq 0 (
    echo ERREUR: Docker n'est pas installe ou n'est pas dans le PATH
    echo Veuillez demarrer Docker Desktop
    pause
    exit /b 1
)
echo OK - Docker est disponible
echo.

echo [2/5] Verification du conteneur Jenkins...
docker ps -a | findstr jenkins
if %errorlevel% neq 0 (
    echo Jenkins n'existe pas. Creation en cours...
    docker network create jenkins 2>nul
    docker run -d --name jenkins --restart=on-failure -p 8090:8080 -p 50000:50000 -v jenkins-data:/var/jenkins_home jenkins/jenkins:lts-jdk17
    echo Attente du demarrage de Jenkins (60 secondes)...
    timeout /t 60 /nobreak
) else (
    echo Jenkins existe deja
    docker ps | findstr jenkins
    if %errorlevel% neq 0 (
        echo Demarrage de Jenkins...
        docker start jenkins
        timeout /t 30 /nobreak
    ) else (
        echo Jenkins est deja en cours d'execution
    )
)
echo.

echo [3/5] Verification de l'etat de Jenkins...
docker ps | findstr jenkins
if %errorlevel% neq 0 (
    echo ERREUR: Jenkins ne demarre pas
    echo Logs:
    docker logs jenkins --tail 20
    pause
    exit /b 1
)
echo OK - Jenkins est en cours d'execution
echo.

echo [4/5] Recuperation du mot de passe initial...
echo.
echo ========================================
echo MOT DE PASSE JENKINS:
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
echo ========================================
echo.
echo COPIEZ CE MOT DE PASSE !
echo.

echo [5/5] Ouverture de Jenkins dans le navigateur...
start http://localhost:8090
echo.

echo ========================================
echo JENKINS EST PRET !
echo ========================================
echo.
echo URL: http://localhost:8090
echo.
echo PROCHAINES ETAPES:
echo 1. Collez le mot de passe ci-dessus dans la page web
echo 2. Cliquez sur "Install suggested plugins"
echo 3. Creez votre compte admin
echo 4. Suivez les instructions pour configurer Jenkins
echo.
pause
