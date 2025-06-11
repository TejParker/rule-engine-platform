package top.doe.jedis;

import redis.clients.jedis.Jedis;

public class Demo {
    public static void main(String[] args) {


        Jedis jedis = new Jedis("172.27.170.34", 6379);
        jedis.auth("123456");
        jedis.hset("hs","k1","f1");

        jedis.close();


    }
}
