@echo off
echo Starting Transaction Labeling Application...
cd /d %~dp0
mvn spring-boot:run -DskipTests
