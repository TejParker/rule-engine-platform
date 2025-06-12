#!/bin/bash

echo "=== HBase环境启动脚本 ==="

# 检查docker-compose是否可用
if ! command -v docker-compose &> /dev/null; then
    echo "错误: docker-compose未安装或不在PATH中"
    exit 1
fi

# 停止现有容器（如果存在）
echo "停止现有的HBase容器..."
docker-compose -f docker-compose-standalone.yml down -v

# 清理卷（可选，取消注释如果需要完全重置）
# echo "清理Docker卷..."
# docker volume prune -f

# 启动服务
echo "启动HBase环境..."
docker-compose -f docker-compose-standalone.yml up -d

# 等待服务启动
echo "等待Hadoop和HBase服务启动..."
sleep 30

# 检查服务状态
echo "检查服务状态..."
docker-compose -f docker-compose-standalone.yml ps

# 等待HBase完全就绪
echo "等待HBase完全就绪（这可能需要几分钟）..."
for i in {1..60}; do
    if curl -s -f http://localhost:16010 > /dev/null 2>&1; then
        echo "HBase Master Web UI可访问"
        break
    fi
    echo "第 $i 次检查，等待HBase Master启动..."
    sleep 10
done

# 检查Thrift服务
echo "检查HBase Thrift服务..."
for i in {1..30}; do
    if netstat -an | grep -q :9090; then
        echo "HBase Thrift服务已启动在端口9090"
        break
    fi
    echo "第 $i 次检查，等待Thrift服务..."
    sleep 5
done

echo "=== 服务地址信息 ==="
echo "Hadoop NameNode Web UI: http://localhost:50070"
echo "HBase Master Web UI: http://localhost:16010"
echo "HBase Thrift服务: localhost:9090"
echo "HBase Thrift信息端口: http://localhost:9095"

echo "=== 启动完成 ==="
echo "现在可以运行Java客户端程序了" 