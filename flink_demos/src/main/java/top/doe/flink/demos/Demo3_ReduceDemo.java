package top.doe.flink.demos;

import com.alibaba.fastjson.JSON;
import lombok.*;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import top.doe.flink.config.AppConfig;

/**
 * @Author: cxw
 * <p>
 * 从控制台向nc服务输入数据：
 * {"order_id":1,"order_amt":38.8,"order_type":"团购"}
 * {"order_id":2,"order_amt":38.2,"order_type":"普通"}
 * {"order_id":3,"order_amt":40.0,"order_type":"普通"}
 * {"order_id":4,"order_amt":25.8,"order_type":"秒杀"}
 * {"order_id":5,"order_amt":52.4,"order_type":"团购"}
 * {"order_id":6,"order_amt":24.0,"order_type":"秒杀"}
 * <p>
 * 用 flink实时统计：当前的每种类型的订单总金额
 **/
public class Demo3_ReduceDemo {

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


        // keyBy分组： 按订单类型
        /* KeyedStream<Order, String> keyedStream = orderStream.keyBy(od -> od.order_type);

        // 在 KeyedStream 上调用简单聚合算子：reduce
        // 各类订单的 平均订单额，最大订单金额，最小订单金额
        // 如下直接对order数据进行reduce，行不通，因为reduce的function的输入数据类型和累加器类型和结果类型都一致
        keyedStream.reduce(new ReduceFunction<Order>() {
            @Override
            public Order reduce(Order od1, Order od2) throws Exception {
                return null;
            }
        }); */


        // 3.把输入数据order，转换成 符合需求的 累加器结构
        SingleOutputStreamOperator<OrderAgg> aggBeanStream = orderStream.map(new MapFunction<Order, OrderAgg>() {
            @Override
            public OrderAgg map(Order od) throws Exception {
                OrderAgg orderAgg = new OrderAgg();

                orderAgg.setOrder_type(od.order_type);
                orderAgg.setOrder_cnt(1);
                orderAgg.setSum(od.order_amt);
                orderAgg.setMinAmt(od.order_amt);
                orderAgg.setMaxAmt(od.order_amt);

                return orderAgg;
            }
        });

        // keyBy分组
        KeyedStream<OrderAgg, String> keyedStream = aggBeanStream.keyBy(agg -> agg.order_type);


        // 调用 reduce算子 聚合
        SingleOutputStreamOperator<OrderAgg> resultStream = keyedStream.reduce(new ReduceFunction<OrderAgg>() {
            @Override
            public OrderAgg reduce(OrderAgg agg, OrderAgg newData) throws Exception {

                // 订单数递增
                agg.order_cnt += newData.order_cnt;

                // 总额递增
                agg.sum += newData.sum;

                // 最大订单额更新
                agg.maxAmt = Math.max(newData.maxAmt, agg.maxAmt);

                // 最小订单额更新
                agg.minAmt = Math.min(agg.minAmt, newData.minAmt);

                // 平均订单额更新
                agg.avgAmt = agg.sum / agg.order_cnt;

                return agg;
            }
        });


        // 调用sink算子输出结果
        resultStream.print();

        // 触发job执行 - 使用配置的应用名称
        env.execute(AppConfig.App.getName());


    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        private int order_id;
        private double order_amt;
        private String order_type;

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderAgg {
        private String order_type;
        private double sum;  // 总金额
        private int order_cnt;  // 总订单数
        private double maxAmt;
        private double minAmt;

        private Double avgAmt;

    }

}
