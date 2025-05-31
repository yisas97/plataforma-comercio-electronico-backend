@echo off

echo Ruta de Java HOME...
REM Java 21
set JAVA_HOME=C:\Users\JESUS\.jdks\corretto-17.0.11

echo A%dir_PATH%
set PATH=%JAVA_HOME%\bin;%PATH%

echo Verificacion del JAVA
java -version

echo Generar el war...
REM mvn clean install -DskipTests

echo Genera la evaluación de SONAR
set TOKEN=sqp_a52ac44969f0c8c0b7b69f1d62a4ffb99cceb6f4
call mvn clean verify -DskipTests sonar:sonar -Dsonar.projectKey=comercio-electronico-backend -Dsonar.projectName='comercio-electronico-backend' -Dsonar.host.url=http://localhost:9000 -Dsonar.token=sqp_ca5eed8f85da809f7910b91ca8d8afa4a95f77b7


REM echo Levantando con docker
REM echo Verificacion si esta levantado...
REM docker compose down

REM echo Builder la imagen:
REM docker compose build

REM echo Levantar el contenedor:
REM docker compose up -d