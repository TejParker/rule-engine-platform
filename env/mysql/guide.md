### Docker快速搭建MySQL开发测试环境

1. 创建配置文件目录（路径可以自定义）
```shell
# 2. 创建MySQL配置文件目录
mkdir -p ~/mysql-config
```

2. 配置文件打开binlog
```shell
# 3. 创建MySQL配置文件开启binlog
cat > ~/mysql-config/my.cnf << 'EOF'
[mysqld]
# 开启binlog
log-bin=mysql-bin
server-id=1
binlog-format=ROW
binlog-expire-logs-seconds=604800
max-binlog-size=100M

# 其他常用配置
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
default-time-zone='+8:00'
EOF
```

3. 拉起容器
```shell
# 4. 重新启动MySQL容器，挂载配置文件
sudo docker run -d \
  --name jeeplus-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=jeeplus-vue-saas-v3 \
  -v ~/mysql-config/my.cnf:/etc/mysql/conf.d/my.cnf \
  mysql:8.0
  ```


4. 验证binlog是否开启
```shell
# 5. 验证binlog是否开启
sudo docker exec -it jeeplus-mysql mysql -uroot -proot -e "SHOW VARIABLES LIKE 'log_bin';"
```



