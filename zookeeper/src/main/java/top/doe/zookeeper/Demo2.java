package top.doe.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.zookeeper.*;

import java.io.*;

public class Demo2 {
    public static void main(String[] args) throws Exception {

        // 创建zookeeper的客户端对象
        ZooKeeper zkCli = new ZooKeeper(
                "172.27.170.34:2181",
                5000,
                new Watcher() {
                    @Override
                    public void process(WatchedEvent watchedEvent) {
                        System.out.println("收到一个通知：" + watchedEvent);

                    }
                });


        // 骚操作，搞一个对象，序列化成字节，放入zookeeper记录起来
        Person person = new Person("爱星星", 20, 28000);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oOut = new ObjectOutputStream(bout);
        oOut.writeObject(person);


        byte[] bytes = bout.toByteArray();

        // 首先创建父节点 /aa（如果不存在）
        try {
            zkCli.create("/aa", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("父节点 /aa 创建成功");
        } catch (KeeperException.NodeExistsException e) {
            System.out.println("父节点 /aa 已存在");
        }

        // 创建子节点 /aa/www
        String s = zkCli.create("/aa/www", bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("节点创建成功：" + s);


        // 读取上面创建的znode的数据
        byte[] data = zkCli.getData("/aa/www", false, null);
        ByteArrayInputStream bIn = new ByteArrayInputStream(data);
        ObjectInputStream oIn = new ObjectInputStream(bIn);
        Person o = (Person) oIn.readObject();
        System.out.println(o);


        zkCli.close();
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Person implements Serializable {
        private String name;
        private int age;
        private double salary;
    }

}
