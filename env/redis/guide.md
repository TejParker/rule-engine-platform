### Docker快速搭建Redis开发测试环境

1. 创建redis数据挂载目录
```shell
mkdir -p $HOME/redis-data
```

2. 启动redis
```shell
docker run -d   --name jeeplus_redis   -p 6379:6379   -v $HOME/redis-data:/data   --restart unless-stopped   redis:7.2   redis-server --appendonly yes --requirepass 123456
```
