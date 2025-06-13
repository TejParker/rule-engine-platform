#!/bin/bash

# 创建日志目录
mkdir -p /tmp/flink-logs

# 设置环境变量
export log.file=/tmp/flink-logs/flink.log

# 设置JVM参数，指定日志文件位置
export FLINK_CONF_DIR=""
export JVM_ARGS="-Dlog.file=/tmp/flink-logs/flink.log -Dweb.log.path=/tmp/flink-logs"

echo "启动Flink应用程序..."
echo "日志文件位置: /tmp/flink-logs/flink.log"
echo "Web Dashboard将能够显示日志文件"

# 运行Java程序
java -cp target/classes:target/dependency/* $JVM_ARGS other.state_clear.TestMain 