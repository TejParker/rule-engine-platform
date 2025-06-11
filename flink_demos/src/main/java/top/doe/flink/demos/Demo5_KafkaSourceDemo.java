package top.doe.flink.demos;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import top.doe.flink.config.AppConfig;

import java.util.regex.Pattern;

/**
 * @Author: 深似海
 * @Site: <a href="www.51doit.com">多易教育</a>
 * @QQ: 657270652
 * @Date: 2024/10/10
 * @Desc: 学大数据，上多易教育
 * kafkaSource的使用示例
 **/
public class Demo5_KafkaSourceDemo {
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

        // 构建一个kafkaSource对象
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
                .setGroupId(AppConfig.Kafka.getGroupId())
                .setClientIdPrefix(AppConfig.Kafka.getClientIdPrefix())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setBootstrapServers(AppConfig.Kafka.getBootstrapServers())
                .setTopics(AppConfig.Kafka.getTopics())
                .build();


        // 用env使用该source获取流
        DataStreamSource<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "kafkaSource");

        // {"order_id":1,"order_amt":38.8,"order_type":"团购"}
        // {"order_id":1,"order_amt":40.8,"order_type":"团购"}

        // json解析
        SingleOutputStreamOperator<Order> orderStream
                = stream.map(json -> JSON.parseObject(json, Order.class));

        // keyBy
        KeyedStream<Order, String> keyedStream = orderStream.keyBy(od -> od.order_type);

        // 聚合
        SingleOutputStreamOperator<Order> resultStream = keyedStream.sum("order_amt");


        // 输出 sink
        resultStream.print();


        // 触发job
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

}
