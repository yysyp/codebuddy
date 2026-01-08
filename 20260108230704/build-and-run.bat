@echo off
set MAVEN_HOME=D:\app\ideaIC-2023.2.6.win\plugins\maven\lib\maven3
set PATH=%MAVEN_HOME%\bin;%PATH%
cd /d c:\Users\yysyp\CodeBuddy\20260108230704
call mvn.cmd clean spring-boot:run
