@echo off
echo ========================================
echo Execution des tests unitaires
echo ========================================
echo.

REM Nettoyer les anciens rapports
if exist target\surefire-reports rmdir /s /q target\surefire-reports

REM Executer les tests
call mvn clean test -Dmaven.test.failure.ignore=true

echo.
echo ========================================
echo Résumé des résultats
echo ========================================

REM Afficher le résumé
findstr /C:"Tests run" /C:"Failures" /C:"Errors" target\surefire-reports\*.txt

echo.
echo Rapports détaillés disponibles dans: target\surefire-reports\
echo.
pause
