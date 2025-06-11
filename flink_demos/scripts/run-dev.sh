#!/bin/bash

echo "启动开发环境..."

# 使用默认配置 (application.properties)
java -cp "target/classes:target/lib/*" top.doe.flink.demos.Demo3_ReduceDemo 