package top.doe.redis;

import redis.clients.jedis.Jedis;

public class Demo1 {
    public static void main(String[] args) {

        // 创建客户端（或建立连接）
        Jedis jedis = new Jedis("172.27.170.34", 6379);
        
        // 进行认证（请替换为您的实际Redis密码）
        jedis.auth("123456");

        // 调方法操作数据
        jedis.set("zhangSan","是一个漂亮的小伙");
        jedis.set("lisi","是一个帅气的小伙");
        jedis.set("wangWu","是一个英俊的小姐姐");


        jedis.close();


    }
}
