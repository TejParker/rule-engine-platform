package top.doe.flink.demos;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.doe.flink.config.AppConfig;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Properties;

/**
 * @Author: cxw
 * 实时捕获业务系统上的营收表，实时统计各性别的平均营收
 **/

@Slf4j
public class Demo10_MySqlCdcSource_Excersize {
    //private static Logger logger = LoggerFactory.getLogger(Demo10_MySqlCdcSource_Excersize.class);

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000);
        env.getCheckpointConfig().setCheckpointStorage(AppConfig.App.getCheckpointStorage());
        env.setParallelism(AppConfig.App.getParallelism());
        Properties properties = new Properties();
        properties.setProperty("connectionTimeZone", "Asia/Shanghai");
        properties.setProperty("database.characterSetServerName", "utf8mb4");
        properties.setProperty("database.connectionCollation", "utf8mb4_unicode_ci");
        properties.setProperty("database.characterEncoding", "UTF-8");
        properties.setProperty("decimal.handling.mode", "string");
        // properties.setProperty("bigint.unsigned.handling.mode", "precise");

        // 创建source
        MySqlSource<String> source = MySqlSource.<String>builder()
                .username(AppConfig.MySQL.getUsername())
                .password(AppConfig.MySQL.getPassword())
                .hostname(AppConfig.MySQL.getHost())
                .port(AppConfig.MySQL.getPort())
                .databaseList(AppConfig.MySQL.getDatabaseList())
                .tableList(AppConfig.MySQL.getTableList())
                .startupOptions(StartupOptions.initial())
                // .serverTimeZone("Asia/Shanghai")    // 设置服务器时区为Asia/Shanghai
                .debeziumProperties(properties)
                .jdbcProperties(properties)
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();

        DataStreamSource<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "mysql-source");

        // json 解析
        SingleOutputStreamOperator<CdcBean> beanStream = stream.map(new MapFunction<String, CdcBean>() {
            @Override
            public CdcBean map(String cdcJson) throws Exception {
                System.out.println("cdcJson:" + cdcJson);
                try {
                    CdcBean cdcBean = JSON.parseObject(cdcJson, CdcBean.class);
                    // 使用log打印对象,避免println可能的缓冲区问题
                    log.info("解析后的对象: {}", cdcBean);
                    return cdcBean;
                } catch (Exception e) {
                    log.error("JSON解析失败: {}, 错误: {}", cdcJson, e.getMessage(), e);
                    throw e;
                }
            }
        });
        beanStream.print("beanStream>");

//        beanStream.process(new ProcessFunction<CdcBean, String>() {
//            @Override
//            public void processElement(CdcBean value, ProcessFunction<CdcBean, String>.Context ctx, Collector<String> out) throws Exception {
//            }
//        });



        // keyBy
        KeyedStream<CdcBean, String> keyedStream = beanStream.keyBy(new KeySelector<CdcBean, String>() {
            @Override
            public String getKey(CdcBean bean) throws Exception {
                return bean.after != null ? bean.after.gender : bean.before.gender;
            }
        });


        // process算子是一个灵活的算子，没有固定某种处理模型；而是完全交给用户来编写数据处理逻辑
        SingleOutputStreamOperator<String> resultStream = keyedStream.process(new KeyedProcessFunction<String, CdcBean, String>() {

            HashMap<String, Pair> mapState = new HashMap<>();
            JSONObject res = new JSONObject();

            @Override
            public void processElement(CdcBean cdcBean, KeyedProcessFunction<String, CdcBean, String>.Context ctx, Collector<String> out) throws Exception {

                // 统计各性别的 平均营收
                SalaryInfo before = cdcBean.before;
                SalaryInfo after = cdcBean.getAfter();
                String op = cdcBean.op;

                // 方法的ctx上下中，持有了当前数据所属的 key
                String gender = ctx.getCurrentKey();
                //String gender = after != null ? after.gender : before.gender;

                if (op.equals("r") || op.equals("c")) {

                    Pair stateValue = mapState.getOrDefault(gender, new Pair(0, 0.0));

                    stateValue.cnt++;
                    stateValue.sum += parseStringToDouble(after.salary);

                    mapState.put(gender, stateValue);

                } else if (op.equals("u")) {
                    // 计算更新前和更新后的金额的差值
                    double diff = parseStringToDouble(after.salary) - parseStringToDouble(before.salary);

                    Pair stateValue = mapState.get(gender);

                    if (stateValue == null) throw new RuntimeException("有问题有问题，大大的有问题");

                    stateValue.sum += diff;

                } else if (op.equals("d")) {

                    Pair stateValue = mapState.get(gender);
                    if (stateValue == null) throw new RuntimeException("有问题有问题，大大的有问题");

                    stateValue.sum -= parseStringToDouble(before.salary);
                    stateValue.cnt--;

                } else {
                    log.error("有点小问题，不应该有这样的op:{}", op);
                }

                Pair pair = mapState.get(gender);
                if (pair != null && pair.cnt != 0) {
                    res.put("gender", gender);
                    res.put("avg_salary", pair.sum / pair.cnt);

                    out.collect(res.toJSONString());
                }
            }
        });


        resultStream.print();


        env.execute();
    }

    // 添加一个工具方法来解析precise模式下的decimal值
    private static double parseStringToDouble(String salaryStr) {
        try {
            // 尝试直接解析
            return Double.parseDouble(salaryStr);
        } catch (NumberFormatException e) {
            // 处理Debezium precise模式下的特殊编码
            try {
                // 对于Debezium precise模式，需要引入专门的解析库
                // 这里提供一个简单实现，仅用于演示
                // 实际生产环境中应使用org.apache.kafka.connect.data.Decimal类或其他专用库
                
                // 打印日志便于调试
                log.info("尝试解析special decimal format: {}", salaryStr);
                
                // 简单解决方案：修改配置，使用字符串模式而非precise模式
                // 临时方案：使用默认值以便程序可以继续运行
                return 0.0;
            } catch (Exception ex) {
                log.error("解析precise模式下的decimal值失败: {}", salaryStr, ex);
                return 0.0;
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pair implements Serializable {
        private int cnt;
        private double sum;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalaryInfo implements Serializable {
        private int id;
        private String name;
        private String gender;
        private String salary;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CdcBean implements Serializable {
        private SalaryInfo before;
        private SalaryInfo after;
        private String op;

    }


}
