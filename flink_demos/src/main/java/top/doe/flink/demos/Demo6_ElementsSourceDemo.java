package top.doe.flink.demos;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Author: cxw
 *  测试用的source
 **/
public class Demo6_ElementsSourceDemo {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Integer> stream = env.fromElements(1, 2, 3, 4, 5, 6, 7, 8);

        SingleOutputStreamOperator<Integer> filtered = stream.filter(e -> e % 2 == 1);

        filtered.print();

        env.execute();


    }
}
