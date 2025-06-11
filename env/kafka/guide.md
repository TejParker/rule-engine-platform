### Docker快速搭建Kafka开发测试环境

1. 创建Docker Compose文件

2. 启动Kafka集群
```shell
docker compose up -d
```

3. 验证服务状态
```shell
docker compose ps
```

4. 创建测试主题
```shell
docker exec -it kafka kafka-topics --create --topic test-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

5. 验证主题创建
```shell
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

6. 生产者测试
```shell
docker exec -it kafka kafka-console-producer --topic test-topic --bootstrap-server localhost:9092
```

7. 消费者测试（新开一个终端）
```shell
docker exec -it kafka kafka-console-consumer --topic test-topic --from-beginning --bootstrap-server localhost:9092
```

##### 常用管理命令

查看所有topic
```shell
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

查看主题详情
```shell
docker exec -it kafka kafka-topics --describe --topic test-topic --bootstrap-server localhost:9092
```

#### 停止服务
```shell
# Docker Compose方式
docker-compose down

# 单独命令方式
docker stop kafka zookeeper
docker rm kafka zookeeper
```

#### 重启服务
```shell
# Docker Compose方式
docker-compose restart

# 单独命令方式
docker restart zookeeper kafka
```
