@echo off
echo Iniciando servicios para el Sistema de Análisis de Tráfico...

REM Verifica si MySQL está en ejecución
echo Verificando MySQL...
sc query MySQL > nul
if %ERRORLEVEL% neq 0 (
    echo [ADVERTENCIA] MySQL no parece estar en ejecución. Asegúrate de que el servicio MySQL esté iniciado.
    pause
)

REM Inicia el backend (Spring Boot)
echo Iniciando backend (Spring Boot)...
start cmd /k "cd C:\ProyectoFinalPatrones\projectback && mvn spring-boot:run"

REM Espera a que el backend se inicie
echo Esperando a que el backend se inicie (30 segundos)...
timeout /t 30 /nobreak > nul

REM Inicia el frontend (Next.js)
echo Iniciando frontend (Next.js)...
start cmd /k "cd C:\frontend-patrones && npm run dev"

echo.
echo Sistema de Análisis de Tráfico iniciado correctamente.
echo - Backend: http://localhost:8080
echo - Frontend: http://localhost:3000
echo.
echo Presiona cualquier tecla para cerrar esta ventana...
pause > nul