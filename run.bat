@echo off
set JAVA_HOME=C:\Users\MarkOlsen\jdk-25_windows-x64_bin\jdk-25.0.3
set PATH=C:\Users\MarkOlsen\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin;%JAVA_HOME%\bin;%PATH%
cd /d %~dp0

if "%1"=="" goto run
if /i "%1"=="run"     goto run
if /i "%1"=="compile" goto compile
if /i "%1"=="test"    goto test
if /i "%1"=="build"   goto build
if /i "%1"=="clean"   goto clean
echo Unknown command: %1
echo Usage: run [run^|compile^|test^|build^|clean]
goto end

:run
echo Starting MolBioLearner... open http://localhost:8080 ^(Ctrl+C to stop^)
mvn spring-boot:run
goto end

:compile
echo Compiling...
mvn compile
goto end

:test
echo Running tests...
mvn test
goto end

:build
echo Building JAR...
mvn clean package -DskipTests
goto end

:clean
echo Cleaning...
mvn clean
goto end

:end
