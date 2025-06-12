package doe.top.hbase;

import org.apache.hadoop.hbase.thrift.generated.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * HBase Thrift客户端示例
 * 演示如何通过Thrift接口连接和操作HBase
 */

public class HBaseThriftExample {
    
    private static final String HBASE_THRIFT_HOST = "localhost";  // Docker容器端口映射到本地
    private static final int HBASE_THRIFT_PORT = 9090;
    private static final int CONNECTION_TIMEOUT = 30000; // 30秒连接超时
    private static final int SOCKET_TIMEOUT = 60000;     // 60秒socket超时
    private static final int MAX_RETRIES = 3;            // 最大重试次数
    
    private TTransport transport;
    private Hbase.Client client;
    
    /**
     * 连接到HBase Thrift服务
     */
    public void connect() throws TTransportException {
        System.out.println("正在连接到HBase Thrift服务...");
        
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                transport = new TSocket(HBASE_THRIFT_HOST, HBASE_THRIFT_PORT);
                ((TSocket) transport).setTimeout(SOCKET_TIMEOUT);
                TProtocol protocol = new TBinaryProtocol(transport, true, true);
                client = new Hbase.Client(protocol);
                transport.open();
                
                // 测试连接
                client.getTableNames();
                System.out.println("成功连接到HBase Thrift服务");
                return;
                
            } catch (Exception e) {
                System.err.println("第 " + (i + 1) + " 次连接尝试失败: " + e.getMessage());
                if (transport != null && transport.isOpen()) {
                    transport.close();
                }
                
                if (i == MAX_RETRIES - 1) {
                    throw new TTransportException("无法连接到HBase Thrift服务，已重试 " + MAX_RETRIES + " 次");
                }
                
                // 等待后重试
                try {
                    Thread.sleep(5000); // 等待5秒
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new TTransportException("连接被中断");
                }
            }
        }
    }
    
    /**
     * 关闭连接
     */
    public void close() {
        if (transport != null && transport.isOpen()) {
            transport.close();
            System.out.println("已关闭HBase连接");
        }
    }
    
    /**
     * 检查表是否存在（替代isTableEnabled方法）
     */
    public boolean tableExists(String tableName) throws TException {
        try {
            List<ByteBuffer> tableNames = client.getTableNames();
            ByteBuffer tableNameBuffer = ByteBuffer.wrap(tableName.getBytes());
            
            for (ByteBuffer name : tableNames) {
                if (name.equals(tableNameBuffer)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("检查表存在性时出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 创建表
     */
    public void createTable(String tableName, String columnFamily) throws TException {
        // 检查表是否已存在
        if (tableExists(tableName)) {
            System.out.println("表 " + tableName + " 已存在");
            return;
        }
        
        // 创建列族描述符
        ColumnDescriptor col = new ColumnDescriptor();
        col.name = ByteBuffer.wrap(columnFamily.getBytes());
        col.maxVersions = 1;
        col.compression = "NONE";
        col.inMemory = false;
        col.bloomFilterType = "NONE";
        
        List<ColumnDescriptor> columns = new ArrayList<>();
        columns.add(col);
        
        // 创建表
        try {
            client.createTable(ByteBuffer.wrap(tableName.getBytes()), columns);
            System.out.println("成功创建表: " + tableName);
            
            // 等待表创建完成
            Thread.sleep(2000);
            
        } catch (Exception e) {
            System.err.println("创建表失败: " + e.getMessage());
            throw new TException("创建表失败", e);
        }
    }
    
    /**
     * 插入数据
     */
    public void putData(String tableName, String rowKey, String columnFamily, 
                       String column, String value) throws TException {
        
        try {
            List<Mutation> mutations = new ArrayList<>();
            Mutation mutation = new Mutation();
            mutation.column = ByteBuffer.wrap((columnFamily + ":" + column).getBytes());
            mutation.value = ByteBuffer.wrap(value.getBytes());
            mutations.add(mutation);
            
            client.mutateRow(ByteBuffer.wrap(tableName.getBytes()), 
                            ByteBuffer.wrap(rowKey.getBytes()), 
                            mutations, null);
            
            System.out.println("成功插入数据: " + rowKey + " -> " + value);
            
        } catch (Exception e) {
            System.err.println("插入数据失败: " + e.getMessage());
            throw new TException("插入数据失败", e);
        }
    }
    
    /**
     * 读取数据
     */
    public void getData(String tableName, String rowKey) throws TException {
        try {
            List<TRowResult> results = client.getRow(ByteBuffer.wrap(tableName.getBytes()), 
                                                    ByteBuffer.wrap(rowKey.getBytes()), 
                                                    null);
            
            if (results.isEmpty()) {
                System.out.println("未找到数据: " + rowKey);
                return;
            }
            
            TRowResult result = results.get(0);
            System.out.println("读取数据 - RowKey: " + rowKey);
            
            for (ByteBuffer column : result.columns.keySet()) {
                TCell cell = result.columns.get(column);
                String columnName = new String(column.array());
                String value = new String(cell.value.array());
                System.out.println("  " + columnName + " = " + value);
            }
            
        } catch (Exception e) {
            System.err.println("读取数据失败: " + e.getMessage());
            throw new TException("读取数据失败", e);
        }
    }
    
    /**
     * 扫描表
     */
    public void scanTable(String tableName) throws TException {
        int scannerId = -1;
        
        try {
            scannerId = client.scannerOpen(ByteBuffer.wrap(tableName.getBytes()), 
                                          ByteBuffer.wrap("".getBytes()), 
                                          null, null);
            
            System.out.println("扫描表: " + tableName);
            
            List<TRowResult> results;
            while ((results = client.scannerGet(scannerId)) != null && !results.isEmpty()) {
                for (TRowResult result : results) {
                    String rowKey = new String(result.row.array());
                    System.out.println("RowKey: " + rowKey);
                    
                    for (ByteBuffer column : result.columns.keySet()) {
                        TCell cell = result.columns.get(column);
                        String columnName = new String(column.array());
                        String value = new String(cell.value.array());
                        System.out.println("  " + columnName + " = " + value);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("扫描表失败: " + e.getMessage());
            throw new TException("扫描表失败", e);
        } finally {
            if (scannerId != -1) {
                try {
                    client.scannerClose(scannerId);
                } catch (Exception e) {
                    System.err.println("关闭扫描器失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 等待HBase服务就绪
     */
    public void waitForHBaseReady() {
        System.out.println("等待HBase服务完全启动...");
        for (int i = 0; i < 30; i++) { // 最多等待5分钟
            try {
                List<ByteBuffer> tables = client.getTableNames();
                System.out.println("HBase服务已就绪，当前有 " + tables.size() + " 个表");
                return;
            } catch (Exception e) {
                System.out.println("第 " + (i + 1) + " 次检查，HBase还未就绪...");
                try {
                    Thread.sleep(10000); // 等待10秒
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        System.err.println("警告: HBase服务可能还未完全就绪");
    }
    
    /**
     * 主方法 - 演示基本操作
     */
    public static void main(String[] args) {
        HBaseThriftExample example = new HBaseThriftExample();
        
        try {
            // 连接HBase
            example.connect();
            
            // 等待HBase完全就绪
            example.waitForHBaseReady();
            
            // 创建表
            String tableName = "test_table";
            String columnFamily = "cf";
            example.createTable(tableName, columnFamily);
            
            // 插入数据
            example.putData(tableName, "row1", columnFamily, "name", "张三");
            example.putData(tableName, "row1", columnFamily, "age", "25");
            example.putData(tableName, "row2", columnFamily, "name", "李四");
            example.putData(tableName, "row2", columnFamily, "age", "30");
            
            // 读取数据
            example.getData(tableName, "row1");
            example.getData(tableName, "row2");
            
            // 扫描表
            example.scanTable(tableName);
            
        } catch (Exception e) {
            System.err.println("操作失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            example.close();
        }
    }
}  
