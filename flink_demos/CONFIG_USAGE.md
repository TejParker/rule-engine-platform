# 配置文件使用说明

## 配置文件概述

项目支持多环境配置，通过不同的配置文件来管理不同环境的参数。

### 配置文件列表

- `application.properties` - 默认配置文件（开发环境）
- `application-prod.properties` - 生产环境配置文件

## 使用方式

### 1. 使用默认配置（开发环境）

直接运行应用程序，会自动加载 `application.properties`：

```bash
java -cp "target/classes:target/lib/*" top.doe.flink.demos.Demo3_ReduceDemo
```

或使用开发环境脚本：
```bash
./scripts/run-dev.sh
```

### 2. 使用生产环境配置

#### 方式一：通过系统属性指定

```bash
java -Dapp.profile=prod -cp "target/classes:target/lib/*" top.doe.flink.demos.Demo3_ReduceDemo
```

#### 方式二：通过环境变量指定

```bash
export APP_PROFILE=prod
java -cp "target/classes:target/lib/*" top.doe.flink.demos.Demo3_ReduceDemo
```

#### 方式三：使用生产环境脚本

```bash
./scripts/run-prod.sh
```

### 3. 在Cursor/IDE中运行

在 **Run Configuration** 中添加 VM options：
```
-Dapp.profile=prod
```

或在 **Environment Variables** 中添加：
```
APP_PROFILE=prod
```

## 配置项说明

### Socket连接配置
- `socket.host` - Socket服务器地址
- `socket.port` - Socket服务器端口

### 应用程序配置
- `app.parallelism` - Flink并行度
- `app.name` - 应用程序名称
- `app.version` - 应用程序版本

## 配置优先级

系统属性 > 环境变量 > 默认配置

即：`-Dapp.profile=prod` > `APP_PROFILE=prod` > 默认使用 `application.properties`

## 添加新环境

要添加新的环境配置（如测试环境），只需：

1. 创建新的配置文件：`application-test.properties`
2. 使用 `-Dapp.profile=test` 或 `APP_PROFILE=test` 来指定

## 配置验证

应用程序启动时会输出当前使用的配置环境：

```
=== 应用程序启动 ===
当前配置环境: prod
应用名称: Demo3_ReduceDemo_Prod
应用版本: 1.0.0
==================
已加载配置文件: application-prod.properties (profile: prod)
连接到Socket服务: prod-server.example.com:9898
``` 