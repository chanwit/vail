@echo off

cd vail-bootstrap
call ../gradlew clean assemble install
cd ..

javac -cp vertx\lib\jars\vert.x-core.jar;vertx\lib\jars\vert.x-platform.jar vertx\mods\vail-web-server\org\vertx\mods\VailWebServer.java

call bin/vail.bat
