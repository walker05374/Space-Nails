@echo off
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot"
echo Using JAVA_HOME: %JAVA_HOME%
.\mvnw.cmd spring-boot:run
