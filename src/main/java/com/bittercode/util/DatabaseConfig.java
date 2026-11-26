package com.bittercode.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;
class DatabaseConfig {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());


    static Properties prop = new Properties();
    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream input = classLoader.getResourceAsStream("application.properties")) {
            if (input != null) {
                prop.load(input);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public final static String DRIVER_NAME = prop.getProperty("db.driver");
    public final static String DB_HOST = prop.getProperty("db.host");
    public final static String DB_PORT = prop.getProperty("db.port");
    public final static String DB_NAME = prop.getProperty("db.name");
    public final static String DB_USER_NAME = prop.getProperty("db.username");
    public final static String DB_PASSWORD = prop.getProperty("db.password");
    public final static String CONNECTION_STRING = DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

}
