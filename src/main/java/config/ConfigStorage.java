package config;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.Properties;

import static java.lang.ClassLoader.getSystemResourceAsStream;

/**
 * Хранилище конфигурации
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
@Singleton
public class ConfigStorage {
    private Properties properties = new Properties();

    @Inject
    public ConfigStorage() {
        InputStream configFile = getSystemResourceAsStream("db.properties");
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
