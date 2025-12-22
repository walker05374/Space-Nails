@echo off
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ---------------------------------------------------
echo [DEBUG] Verificando versão do Java:
echo JAVA_HOME: %JAVA_HOME%
java -version
echo ---------------------------------------------------

.\mvnw.cmd spring-boot:run
