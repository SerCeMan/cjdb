package ru.cjdb.config;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.Properties;


/**
 * Хранилище конфигурации
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
@Singleton
public class ConfigStorage {
    private final Properties properties = new Properties();

    @Inject
    public ConfigStorage() {
        InputStream configFile = getConfigFile();
        try {
            properties.load(configFile);
        } catch (Exception e) {
            throw new RuntimeException("Please provide db.properties file", e);
        }
    }

    /**
     * Двойная проверка из-за проблем с тестами в CI
     */
    private InputStream getConfigFile() {
        InputStream configFile = this.getClass().getResourceAsStream("db.properties");
        if (configFile == null) {
            configFile = ClassLoader.getSystemResourceAsStream("db.properties");
        }
        if (configFile == null) {
            configFile = this.getClass().getResourceAsStream("/db.properties");
        }
        if (configFile == null) {
            configFile = ClassLoader.getSystemResourceAsStream("/db.properties");
        }
        return configFile;
    }

    public String getRootPath() {
        String path = properties.getProperty(Props.PATH);
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
