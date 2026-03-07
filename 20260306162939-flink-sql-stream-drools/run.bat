@echo off
chcp 65001 >nul
title Flink Transaction Tagging - 运行
echo ========================================
echo Flink Transaction Tagging 应用运行
echo ========================================
echo.

REM 设置 JAVA_HOME
set JAVA_HOME=D:\app\azul-17.0.10
set PATH=%JAVA_HOME%\bin;%PATH%

echo 使用 Java 版本:
java -version
echo.

REM 检查 JAR 文件是否存在
if not exist "target\flink-transaction-tagging-1.0.0.jar" (
    echo JAR 文件不存在，请先运行 build.bat 进行编译！
    echo.
    pause
    exit /b 1
)

echo 请选择运行模式:
echo 1. SQL 模式 (推荐) - 支持DRL和CSV表格两种规则源
echo 2. DataStream 模式
echo 3. Hybrid 模式
echo 4. 生成测试数据
echo.
set /p mode=请输入模式编号 (1-4):

set INPUT_FILE=src\main\resources\data\transactions.csv
set OUTPUT_FILE=output\tagged_result.csv

if "%mode%"=="1" goto sql_mode
if "%mode%"=="2" goto datastream_mode
if "%mode%"=="3" goto hybrid_mode
if "%mode%"=="4" goto generate_mode

echo.
echo 无效的选择！
goto end

:sql_mode
echo.
echo 使用 SQL 模式处理数据...
echo 输入文件: %INPUT_FILE%
echo 输出文件: %OUTPUT_FILE%
echo.
echo 请选择规则源:
echo 1. DRL 规则文件 (默认)
echo 2. CSV 表格规则定义
echo.
set /p rule_source=请输入规则源编号 (1-2, 直接回车使用默认DRL):

if "%rule_source%"=="2" goto table_rules
if "%rule_source%"=="" goto drl_rules
if "%rule_source%"=="1" goto drl_rules
echo 无效的选择，使用默认DRL规则
goto drl_rules

:drl_rules
echo 使用 DRL 规则文件...
REM 创建输出目录
if not exist "output" mkdir output

java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target\flink-transaction-tagging-1.0.0.jar sql %INPUT_FILE% %OUTPUT_FILE%
goto end

:table_rules
echo 使用 CSV 表格规则定义...
echo 规则文件路径 (直接回车使用默认 src/main/resources/rules/table-rules.csv):
set /p table_path=
if "%table_path%"=="" set table_path=src/main/resources/rules/table-rules.csv

REM 创建输出目录
if not exist "output" mkdir output

java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target\flink-transaction-tagging-1.0.0.jar sql %INPUT_FILE% %OUTPUT_FILE% table %table_path%
goto end

:datastream_mode
echo.
echo 使用 DataStream 模式处理数据...
echo 输入文件: %INPUT_FILE%
echo 输出文件: %OUTPUT_FILE%
echo.

REM 创建输出目录
if not exist "output" mkdir output

java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.util.Arrays=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.math=ALL-UNNAMED -jar target\flink-transaction-tagging-1.0.0.jar datastream %INPUT_FILE% %OUTPUT_FILE%
goto end

:hybrid_mode
echo.
echo 使用 Hybrid 模式处理数据...
echo 输入文件: %INPUT_FILE%
echo 输出文件: %OUTPUT_FILE%
echo.

REM 创建输出目录
if not exist "output" mkdir output

java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target\flink-transaction-tagging-1.0.0.jar hybrid %INPUT_FILE% %OUTPUT_FILE%
goto end

:generate_mode
echo.
echo 生成测试数据...
set /p gen_output=请输入输出文件路径 (直接回车使用默认 target\test-data\test.csv):

if "%gen_output%"=="" set gen_output=target\test-data\test.csv

REM 创建输出目录
if not exist "target\test-data" mkdir target\test-data

java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target\flink-transaction-tagging-1.0.0.jar generate %gen_output%
goto end

:end
echo.
echo ========================================
pause
