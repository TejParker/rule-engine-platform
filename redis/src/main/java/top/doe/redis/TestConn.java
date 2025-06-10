package top.doe.redis;

import redis.clients.jedis.Jedis;

public class TestConn {
    public static void main(String[] args) {


        Jedis jedis = new Jedis("172.27.170.34", 6379);
        jedis.auth("123456");
        String pong = jedis.ping();
        System.out.println(pong);

        jedis.close();


    }
}
