package top.doe.flink.demos;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import scala.Serializable;
import top.doe.flink.config.AppConfig;

import java.util.List;

/**
 * @Author: cxw
 * <p>
 * 从控制台向nc服务输入数据：
 * {"order_id":1,"order_type":"团购","items":[{"pid":"p01","category":"c01","price":10.8,"quantity":2},{"pid":"p03","category":"c02","price":200,"quantity":1}]}
 * {"order_id":2,"order_type":"团购","items":[{"pid":"p05","category":"c02","price":280,"quantity":2},{"pid":"p04","category":"c01","price":20,"quantity":4}]}
 * {"order_id":3,"order_type":"秒杀","items":[{"pid":"p04","category":"c01","price":20,"quantity":2}]}
 * {"order_id":4,"order_type":"秒杀","items":[{"pid":"p01","category":"c01","price":10.8,"quantity":2},{"pid":"p04","category":"c01","price":20,"quantity":6}]}
 * <p>
 * 求所有秒杀订单中，各商品的订单数、销售总额、平均每单销售额
 *
 * <p>
 * 用 flink实时统计：当前的每种类型的订单总金额
 **/
public class Demo4_ReduceExec {

    public static void main(String[] args) throws Exception {
        // 显示当前配置环境信息
        System.out.println("=== 应用程序启动 ===");
        System.out.println("当前配置环境: " + AppConfig.getCurrentProfile());
        System.out.println("应用名称: " + AppConfig.App.getName());
        System.out.println("应用版本: " + AppConfig.App.getVersion());
        System.out.println("==================");
        
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 从配置文件读取并行度设置
        env.setParallelism(AppConfig.App.getParallelism());

        // 1.用source算子加载数据 - 从配置文件读取socket连接信息
        String socketHost = AppConfig.Socket.getHost();
        int socketPort = AppConfig.Socket.getPort();
        System.out.println("连接到Socket服务: " + socketHost + ":" + socketPort);
        
        DataStreamSource<String> stream = env.socketTextStream(socketHost, socketPort);

        // 2.解析json
        SingleOutputStreamOperator<Order> orderStream = stream.map(new MapFunction<String, Order>() {
            @Override
            public Order map(String value) throws Exception {
                return JSON.parseObject(value, Order.class);
            }
        });

        // 3.过滤
        SingleOutputStreamOperator<Order> filtered = orderStream.filter(new FilterFunction<Order>() {
            @Override
            public boolean filter(Order value) throws Exception {
                return value.order_type.equals("秒杀");
            }
        });


        // 4.把商品信息压平
        SingleOutputStreamOperator<Item> itemStream = filtered.flatMap(new FlatMapFunction<Order, Item>() {
            @Override
            public void flatMap(Order od, Collector<Item> out) throws Exception {
                List<Item> items = od.getItems();
                for (Item item : items) {
                    out.collect(item);
                }
            }
        });


        // 5.把item信息，转成 ItemAgg信息
        SingleOutputStreamOperator<ItemAgg> itemAggStream = itemStream.map(new MapFunction<Item, ItemAgg>() {
            @Override
            public ItemAgg map(Item item) throws Exception {
                ItemAgg itemAgg = new ItemAgg();
                itemAgg.pid = item.pid;
                itemAgg.amt = item.quantity * item.price;
                itemAgg.odCnt = 1;

                return itemAgg;
            }
        });


        // keyBy分组
        KeyedStream<ItemAgg, String> keyedStream = itemAggStream.keyBy(it -> it.pid);


        // 聚合 :各商品的订单数、销售总额、平均每单销售额
        SingleOutputStreamOperator<ItemAgg> tmpResult = keyedStream.reduce(new ReduceFunction<ItemAgg>() {
            @Override
            public ItemAgg reduce(ItemAgg agg, ItemAgg item) throws Exception {

                agg.odCnt += item.odCnt;
                agg.amt += item.amt;

                return agg;
            }
        });


        SingleOutputStreamOperator<ItemAgg> resultStream = tmpResult.map(od -> {
            od.setAvgAmt(od.amt / od.odCnt);
            return od;
        });



        // 调用sink算子输出结果
        resultStream.print();

        // 触发job执行
        env.execute();


    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item implements Serializable {
        // "pid":"p01","category":"c01","price":10.8,"quantity":2
        private String pid;
        private String category;
        private double price;
        private int quantity;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order implements Serializable {
        private int order_id;
        private String order_type;
        private List<Item> items;

    }


    // 聚合 :各商品的订单数、销售总额、平均每单销售额
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemAgg implements Serializable {
        private String pid;
        private double amt;
        private int odCnt;
        private double avgAmt;
    }


}
