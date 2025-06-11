package top.doe.flink.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 应用程序配置管理类
 * 用于读取和管理应用程序的配置信息
 * 
 * @Author: 深似海
 * @Site: <a href="www.51doit.com">多易教育</a>
 * @Date: 2024/10/10
 * @Desc: 统一配置管理
 */
public class AppConfig {
    
    private static final String DEFAULT_CONFIG_FILE = "application.properties";
    private static final String ENV_PROFILE_KEY = "APP_PROFILE";
    private static final String SYSTEM_PROFILE_KEY = "app.profile";
    
    private static Properties properties;
    private static String currentProfile;
    
    static {
        loadConfig();
    }
    
    /**
     * 加载配置文件
     * 优先级：系统属性 > 环境变量 > 默认配置
     */
    private static void loadConfig() {
        // 获取环境配置
        String profile = System.getProperty(SYSTEM_PROFILE_KEY);
        if (profile == null || profile.trim().isEmpty()) {
            profile = System.getenv(ENV_PROFILE_KEY);
        }
        
        // 确定配置文件名
        String configFile;
        if (profile != null && !profile.trim().isEmpty()) {
            configFile = "application-" + profile.trim() + ".properties";
            currentProfile = profile.trim();
        } else {
            configFile = DEFAULT_CONFIG_FILE;
            currentProfile = "default";
        }
        
        properties = new Properties();
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(configFile)) {
            if (inputStream == null) {
                if (!configFile.equals(DEFAULT_CONFIG_FILE)) {
                    // 如果指定的配置文件不存在，回退到默认配置
                    System.err.println("警告: 找不到配置文件 " + configFile + "，使用默认配置 " + DEFAULT_CONFIG_FILE);
                    loadDefaultConfig();
                    return;
                }
                throw new RuntimeException("无法找到配置文件: " + configFile);
            }
            properties.load(inputStream);
            System.out.println("已加载配置文件: " + configFile + " (profile: " + currentProfile + ")");
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件失败: " + configFile, e);
        }
    }
    
    /**
     * 加载默认配置文件
     */
    private static void loadDefaultConfig() {
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException("无法找到默认配置文件: " + DEFAULT_CONFIG_FILE);
            }
            properties.load(inputStream);
            currentProfile = "default";
            System.out.println("已加载默认配置文件: " + DEFAULT_CONFIG_FILE);
        } catch (IOException e) {
            throw new RuntimeException("读取默认配置文件失败: " + DEFAULT_CONFIG_FILE, e);
        }
    }
    
    /**
     * 获取当前使用的配置环境
     * @return 当前配置环境名称
     */
    public static String getCurrentProfile() {
        return currentProfile;
    }
    
    /**
     * 重新加载配置文件
     */
    public static void reload() {
        loadConfig();
    }
    
    /**
     * 获取字符串配置值
     * @param key 配置键
     * @return 配置值
     */
    public static String getString(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * 获取字符串配置值，带默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * 获取整数配置值
     * @param key 配置键
     * @return 配置值
     */
    public static int getInt(String key) {
        String value = getString(key);
        if (value == null) {
            throw new RuntimeException("配置项不存在: " + key);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("配置项格式错误: " + key + " = " + value, e);
        }
    }
    
    /**
     * 获取整数配置值，带默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Socket连接配置
     */
    public static class Socket {
        public static String getHost() {
            return getString("socket.host");
        }
        
        public static int getPort() {
            return getInt("socket.port");
        }
    }
    
    /**
     * 应用程序配置
     */
    public static class App {
        public static int getParallelism() {
            return getInt("app.parallelism", 1);
        }
        
        public static String getName() {
            return getString("app.name", "FlinkDemo");
        }
        
        public static String getVersion() {
            return getString("app.version", "1.0.0");
        }
    }
} 