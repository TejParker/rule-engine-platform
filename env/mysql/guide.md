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

5. 创建数据库
```shell
create database test;
-- 设置字符串（如果字符集有问题的话）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
```

创建表
```shell
-- 创建表
CREATE TABLE t_person (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    salary DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

插入数据
```shell
INSERT INTO t_person (name, gender, salary) VALUES 
('张三', '男', 8500.00),
('李四', '女', 9200.00),
('王五', '男', 7800.00),
('赵六', '女', 10500.00),
('孙七', '男', 6900.00),
('周八', '女', 11200.00),
('吴九', '男', 8800.00),
('郑十', '女', 9600.00),
('陈十一', '男', 7200.00),
('刘十二', '女', 10800.00);
```


