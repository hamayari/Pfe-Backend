@echo off
echo ========================================
echo PUSH BACKEND TO GITHUB
echo ========================================
echo.

REM Aller dans le dossier backend
cd /d "%~dp0"

REM Vérifier si Git est initialisé
if not exist .git (
    echo Initialisation de Git...
    git init
    git remote add origin https://github.com/hamayari/Pfe-Backend.git
)

REM Vérifier le remote
echo Remote actuel:
git remote -v
echo.

REM Ajouter tous les fichiers (sauf ceux dans .gitignore)
echo Ajout des fichiers...
git add .

REM Afficher le statut
echo.
echo Statut:
git status
echo.

REM Demander le message de commit
set /p commit_message="Message de commit: "

REM Commit
echo.
echo Commit en cours...
git commit -m "%commit_message%"

REM Push
echo.
echo Push vers GitHub...
git branch -M main
git push -u origin main

echo.
echo ========================================
echo PUSH TERMINE !
echo ========================================
echo.
echo Verifiez sur: https://github.com/hamayari/Pfe-Backend
echo.
pause
