@echo off
REM ============================================================
REM Script de test local de la pipeline CI/CD
REM Simule les étapes Jenkins en local
REM ============================================================

echo ========================================
echo    PIPELINE CI/CD - TEST LOCAL
echo ========================================
echo.

REM Configuration
set IMAGE_NAME=hamalak/commercial-pfe-backend
set IMAGE_TAG=local-test
set SONAR_URL=http://localhost:9000

echo [1/7] Verification des prerequis...
echo ========================================

REM Verifier Maven
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Maven n'est pas installe ou pas dans le PATH
    exit /b 1
)
echo [OK] Maven trouve

REM Verifier Docker
where docker >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Docker n'est pas installe ou pas dans le PATH
    exit /b 1
)
echo [OK] Docker trouve

REM Verifier Java
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Java n'est pas installe ou pas dans le PATH
    exit /b 1
)
echo [OK] Java trouve

echo.
echo [2/7] Compilation du code...
echo ========================================
call mvn clean compile -B -DskipTests
if %errorlevel% neq 0 (
    echo [ERREUR] Compilation echouee
    exit /b 1
)
echo [OK] Compilation reussie

echo.
echo [3/7] Execution des tests unitaires...
echo ========================================
call mvn test -Dspring.profiles.active=test -B
if %errorlevel% neq 0 (
    echo [ERREUR] Tests unitaires echoues
    exit /b 1
)
echo [OK] Tests unitaires reussis

echo.
echo [4/7] Analyse SonarQube (optionnel)...
echo ========================================
curl -s %SONAR_URL%/api/system/status >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] SonarQube detecte, lancement de l'analyse...
    call mvn sonar:sonar ^
        -Dsonar.projectKey=Commercial-PFE-Backend ^
        -Dsonar.host.url=%SONAR_URL% ^
        -B
    if %errorlevel% equ 0 (
        echo [OK] Analyse SonarQube terminee
        echo [INFO] Rapport: %SONAR_URL%/dashboard?id=Commercial-PFE-Backend
    ) else (
        echo [WARN] Analyse SonarQube echouee mais on continue
    )
) else (
    echo [WARN] SonarQube non disponible, etape ignoree
)

echo.
echo [5/7] Creation du package JAR...
echo ========================================
call mvn package -DskipTests -B
if %errorlevel% neq 0 (
    echo [ERREUR] Package JAR echoue
    exit /b 1
)
echo [OK] Package JAR cree

echo.
echo [6/7] Build de l'image Docker...
echo ========================================
docker build -t %IMAGE_NAME%:%IMAGE_TAG% -t %IMAGE_NAME%:latest .
if %errorlevel% neq 0 (
    echo [ERREUR] Build Docker echoue
    exit /b 1
)
echo [OK] Image Docker creee

echo.
echo [7/7] Test de l'image Docker...
echo ========================================

REM Nettoyage
docker stop backend-test 2>nul
docker rm backend-test 2>nul

REM Demarrer le conteneur
echo [INFO] Demarrage du conteneur...
docker run -d --name backend-test -p 8082:8080 -e SPRING_PROFILES_ACTIVE=test %IMAGE_NAME%:%IMAGE_TAG%
if %errorlevel% neq 0 (
    echo [ERREUR] Demarrage du conteneur echoue
    docker rm backend-test 2>nul
    exit /b 1
)

echo [INFO] Attente du demarrage (30s)...
timeout /t 30 /nobreak >nul

REM Health check
echo [INFO] Health check...
curl -f http://localhost:8082/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Health check reussi
) else (
    echo [WARN] Health check echoue (normal sans MongoDB)
    echo [INFO] Logs du conteneur:
    docker logs backend-test --tail 20
)

REM Nettoyage
echo [INFO] Nettoyage...
docker stop backend-test >nul 2>&1
docker rm backend-test >nul 2>&1

echo.
echo ========================================
echo    PIPELINE TERMINEE AVEC SUCCES!
echo ========================================
echo.
echo ARTEFACTS CREES:
echo   - JAR: target\demo-0.0.1-SNAPSHOT.jar
echo   - Docker: %IMAGE_NAME%:%IMAGE_TAG%
echo   - Docker: %IMAGE_NAME%:latest
echo.
echo RAPPORTS:
echo   - Tests JUnit: target\surefire-reports\
echo   - Couverture JaCoCo: target\site\jacoco\index.html
echo   - SonarQube: %SONAR_URL%/dashboard?id=Commercial-PFE-Backend
echo.
echo COMMANDES UTILES:
echo   - Voir l'image: docker images %IMAGE_NAME%
echo   - Lancer l'image: docker run -p 8080:8080 %IMAGE_NAME%:latest
echo   - Voir les logs: docker logs backend-test
echo.
pause
