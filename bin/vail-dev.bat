@echo off

cd vail-bootstrap
call ../gradlew clean assemble install
cd ..
call bin/vail.bat
