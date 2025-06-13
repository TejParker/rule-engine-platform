package other.state_clear;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.streaming.api.operators.AbstractStreamOperator;
import org.apache.flink.streaming.api.operators.StreamingRuntimeContext;
import org.apache.flink.util.Collector;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试数据说明:
 * 
 * 1. 启动两个终端,分别运行:
 *    终端1: nc -lk 9991  # 用于发送事件数据
 *    终端2: nc -lk 9992  # 用于发送广播元数据
 * 
 * 2. 在终端1中输入以下事件数据(每行一个):
 *    a
 *    a  # 此时输出: aa
 *    b  
 *    b  # 此时输出: bb
 *    c
 *    c  # 此时输出: cc
 * 
 * 3. 在终端2中输入以下广播数据:
 *    X   # 输入X会触发状态清理,清理a/b/c的状态
 *    注意: 广播数据会发送给所有的并行算子实例(operator)
 * 
 * 4. 清理后再次在终端1输入:
 *    a   # 此时输出: a (因为之前的状态被清理了)
 *    b   # 此时输出: b
 *    c   # 此时输出: c
 */
@Slf4j
public class TestMain {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000);
        env.getCheckpointConfig().setCheckpointStorage("file:///tmp/operator_ckpt");

        env.setParallelism(3);


        DataStreamSource<String> events = env.socketTextStream("localhost", 9991);

        // 广播流会将数据发送给所有的下游operator实例
        DataStreamSource<String> meta = env.socketTextStream("localhost", 9992);
        BroadcastStream<String> meta_bc = meta.broadcast(new MapStateDescriptor<String, String>("bc_state", String.class, String.class));


        events.keyBy(s -> s)
                .connect(meta_bc)
                .process(new KeyedBroadcastProcessFunction<String, String, String, String>() {
                    ListState<String> st;
                    Object lock ;

                    @Override
                    public void open(Configuration parameters) throws Exception {

                        lock = new Object();
                        st = getRuntimeContext().getListState(new ListStateDescriptor<String>("st", String.class));
                    }

                    @Override
                    public void processElement(String s, KeyedBroadcastProcessFunction<String, String, String, String>.ReadOnlyContext readOnlyContext, Collector<String> collector) throws Exception {
                        log.info("current key: {}, processElement: {}", readOnlyContext.getCurrentKey(), s);
                        synchronized (lock) {
                            st.add(s);

                            StringBuilder sb = new StringBuilder();
                            for (String v : st.get()) {
                                sb.append(v);
                            }

                            collector.collect(sb.toString());
                        }
                    }

                    @Override
                    public void processBroadcastElement(String s, KeyedBroadcastProcessFunction<String, String, String, String>.Context context, Collector<String> collector) throws Exception {

                        // 当前广播所在的operator实例
                        StreamingRuntimeContext runtimeContext = (StreamingRuntimeContext) getRuntimeContext();
                        AbstractStreamOperator<?> operator = runtimeContext.getOperator();
                        log.info("current operator: {}", operator.getCurrentKey());

                        log.info("processBroadcastElement: {}", s);
                        if (operator.getCurrentKey() == null) {
                            return;
                        }
                        // 广播流会发送给所有的并行算子实例(operator)
                        String[] ids = {"a", "b", "c"};
                        if (s.equals("X")) {

                            synchronized (lock) {
                                // StreamingRuntimeContext runtimeContext = (StreamingRuntimeContext) getRuntimeContext();
                                // AbstractStreamOperator<?> operator = runtimeContext.getOperator();

                                Object currentKey = operator.getCurrentKey();

                                // 清理状态
                                for (String id : ids) {
                                    operator.setCurrentKey(id);
                                    st.clear();
                                }

                                // 移除运算机

                                // 恢复此前的key
                                operator.setCurrentKey(currentKey);
                            }
                        }
                    }
                }).print("result");

        env.execute();


    }
}
