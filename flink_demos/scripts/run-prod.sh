#!/bin/bash

echo "启动生产环境..."

# 方式1: 使用系统属性指定配置环境
java -Dapp.profile=prod -cp "target/classes:target/lib/*" top.doe.flink.demos.Demo3_ReduceDemo

# 方式2: 使用环境变量指定配置环境 (注释掉的备选方案)
# export APP_PROFILE=prod
# java -cp "target/classes:target/lib/*" top.doe.flink.demos.Demo3_ReduceDemo 