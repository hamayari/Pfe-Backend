@echo off
echo ========================================
echo   DEMARRAGE DES CONTENEURS
echo ========================================
echo.

echo [1/3] Demarrage MongoDB...
docker run -d ^
  --name commercial-mongodb ^
  -p 27017:27017 ^
  -e MONGO_INITDB_ROOT_USERNAME=admin ^
  -e MONGO_INITDB_ROOT_PASSWORD=admin123 ^
  --restart unless-stopped ^
  mongo:7.0

if %errorlevel% equ 0 (
    echo [OK] MongoDB demarre sur port 27017
) else (
    echo [INFO] MongoDB deja en cours d'execution
    docker start commercial-mongodb 2>nul
)

echo.
echo [2/3] Demarrage SonarQube...
docker run -d ^
  --name sonarqube ^
  -p 9000:9000 ^
  -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true ^
  --restart unless-stopped ^
  sonarqube:community

if %errorlevel% equ 0 (
    echo [OK] SonarQube demarre sur port 9000
    echo [INFO] Attendre 2-3 minutes pour l'initialisation
) else (
    echo [INFO] SonarQube deja en cours d'execution
    docker start sonarqube 2>nul
)

echo.
echo [3/3] Demarrage Backend (optionnel)...
docker ps | findstr commercial-backend >nul
if %errorlevel% neq 0 (
    echo [INFO] Backend non demarre (normal)
    echo [INFO] Le backend sera build par la pipeline Jenkins
) else (
    echo [OK] Backend deja en cours d'execution
)

echo.
echo ========================================
echo   VERIFICATION DES CONTENEURS
echo ========================================
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo ========================================
echo   ACCES AUX SERVICES
echo ========================================
echo MongoDB:    mongodb://localhost:27017
echo             Username: admin
echo             Password: admin123
echo.
echo SonarQube:  http://localhost:9000
echo             Username: admin
echo             Password: admin (premier login)
echo.
echo ========================================
echo   COMMANDES UTILES
echo ========================================
echo Voir les logs MongoDB:   docker logs commercial-mongodb
echo Voir les logs SonarQube: docker logs sonarqube -f
echo Arreter tout:            docker stop commercial-mongodb sonarqube
echo Redemarrer tout:         docker start commercial-mongodb sonarqube
echo.
pause
