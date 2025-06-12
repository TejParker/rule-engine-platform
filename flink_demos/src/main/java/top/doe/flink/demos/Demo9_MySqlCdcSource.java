package top.doe.flink.demos;

import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.StringDebeziumDeserializationSchema;

import top.doe.flink.config.AppConfig;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Author: cxw
 *    mysql cdc  source 示例
 **/
public class Demo9_MySqlCdcSource {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(AppConfig.App.getParallelism());
        // 开启checkpoint
        env.enableCheckpointing(1000);  // 快照周期
        env.getCheckpointConfig().setCheckpointStorage(AppConfig.App.getCheckpointStorage());  // 设置快照的存储路径

        // 创建mysql cdc source 对象
        MySqlSource<String> source = MySqlSource.<String>builder()
                .username(AppConfig.MySQL.getUsername())
                .password(AppConfig.MySQL.getPassword())
                .hostname(AppConfig.MySQL.getHost())
                .port(AppConfig.MySQL.getPort())
                .databaseList(AppConfig.MySQL.getDatabaseList())
                .tableList(AppConfig.MySQL.getTableList())
                // .serverTimeZone("UTC")  // 设置服务器时区为UTC，与MySQL服务器时区保持一致
                //配置启动模式
                .startupOptions(StartupOptions.initial())
                .deserializer(new StringDebeziumDeserializationSchema())
                .build();

        DataStreamSource<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "mysql-cdc");





        stream.print();


        env.execute();


    }

}
