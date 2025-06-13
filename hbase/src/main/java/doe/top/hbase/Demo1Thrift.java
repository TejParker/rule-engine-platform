package doe.top.hbase;

import org.apache.hadoop.hbase.thrift.generated.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: cxw
 *   hbase thrift客户端编程示例
 **/
@Slf4j
public class Demo1Thrift {

    private static final String HBASE_THRIFT_HOST = "172.27.170.34";
    private static final int HBASE_THRIFT_PORT = 9090;

    public static void main(String[] args) {
        TTransport transport = null;
        try {
            // 创建thrift连接
            transport = new TSocket(HBASE_THRIFT_HOST, HBASE_THRIFT_PORT);
            TProtocol protocol = new TBinaryProtocol(transport, true, true);
            Hbase.Client client = new Hbase.Client(protocol);
            transport.open();

            // 查询数据
            String tableName = "boys";
            String rowKey = "boy001";
            String family = "f1";
            
            // 检查表是否存在
            if (!tableExists(client, tableName)) {
                // 创建表
                createTable(client, tableName, family);
                
                // 插入模拟数据
                putData(client, tableName, "boy001", family, "name", "张三");
                putData(client, tableName, "boy001", family, "age", "18");
                putData(client, tableName, "boy002", family, "name", "李四");
                putData(client, tableName, "boy002", family, "age", "20");
            }
            
            // 构造get请求
            ByteBuffer table = ByteBuffer.wrap(tableName.getBytes());
            ByteBuffer row = ByteBuffer.wrap(rowKey.getBytes());
            List<ByteBuffer> columns = Arrays.asList(
                ByteBuffer.wrap((family + ":name").getBytes()),
                ByteBuffer.wrap((family + ":age").getBytes())
            );

            // 执行get查询
            List<TRowResult> results = client.getRowWithColumns(table, row, columns, null);

            // 打印结果
            if (!results.isEmpty()) {
                TRowResult result = results.get(0);
                for (ByteBuffer column : result.columns.keySet()) {
                    TCell cell = result.columns.get(column);
                    String columnName = new String(column.array());
                    String value = new String(cell.value.array());
                    log.info("{} = {}", columnName, value);
                }
            }

            // 扫描整个表
            int scannerId = client.scannerOpen(table, ByteBuffer.wrap("".getBytes()), null, null);
            List<TRowResult> scanResults;
            
            while (!(scanResults = client.scannerGet(scannerId)).isEmpty()) {
                for (TRowResult rowResult : scanResults) {
                    String currentRowKey = new String(rowResult.row.array());
                    log.info("\nRow: {}", currentRowKey);
                    
                    for (ByteBuffer column : rowResult.columns.keySet()) {
                        TCell cell = rowResult.columns.get(column);
                        String columnName = new String(column.array());
                        String value = new String(cell.value.array());
                        log.info("列族:限定符={}, 值={}", columnName, value);
                    }
                }
            }
            
            client.scannerClose(scannerId);

        } catch (TException e) {
            log.error("Thrift异常: {}", e.getMessage());
            e.printStackTrace();
        } finally {
            if (transport != null && transport.isOpen()) {
                transport.close();
            }
        }
    }

    private static boolean tableExists(Hbase.Client client, String tableName) throws TException {
        List<ByteBuffer> tables = client.getTableNames();
        ByteBuffer target = ByteBuffer.wrap(tableName.getBytes());
        for (ByteBuffer table : tables) {
            if (Arrays.equals(table.array(), target.array())) {
                return true;
            }
        }
        return false;
    }

    private static void createTable(Hbase.Client client, String tableName, String family) throws TException {
        ByteBuffer table = ByteBuffer.wrap(tableName.getBytes());
        ColumnDescriptor columnDescriptor = new ColumnDescriptor();
        columnDescriptor.name = ByteBuffer.wrap((family + ":").getBytes());
        client.createTable(table, Arrays.asList(columnDescriptor));
    }

    private static void putData(Hbase.Client client, String tableName, String rowKey, String family, String qualifier, String value) throws TException {
        ByteBuffer table = ByteBuffer.wrap(tableName.getBytes());
        ByteBuffer row = ByteBuffer.wrap(rowKey.getBytes());
        ByteBuffer column = ByteBuffer.wrap((family + ":" + qualifier).getBytes());
        ByteBuffer val = ByteBuffer.wrap(value.getBytes());
        Mutation mutation = new Mutation(false, column, val, false);
        client.mutateRow(table, row, Arrays.asList(mutation), null);
    }
}
