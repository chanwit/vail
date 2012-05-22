@echo off

for %%? in ("%~dp0..") do set VAIL_HOME=%%~f?
set VERTX_HOME=%VAIL_HOME%\vertx
"%JAVA_HOME%\bin\java" -cp dist\vail-bootstrap.jar;%VERTX_HOME%\lib\jars\js.jar org.vail.bootstrap.cli.Starter %*
