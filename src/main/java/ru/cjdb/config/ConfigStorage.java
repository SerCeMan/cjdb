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
        InputStream configFile = this.getClass().getResourceAsStream("db.properties");
        try {
            properties.load(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Please provide db.properties file");
        }
    }

    public String getRootPath() {
        return properties.getProperty(Props.PATH);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
