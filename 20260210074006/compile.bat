@echo off
cd /d c:/Users/yysyp/CodeBuddy/20260210074006
mvnw.cmd clean package -DskipTests > compile.log 2>&1
type compile.log
