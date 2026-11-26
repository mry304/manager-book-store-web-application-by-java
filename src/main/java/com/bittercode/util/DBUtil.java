package com.bittercode.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.bittercode.constant.ResponseCode;
import com.bittercode.model.StoreException;

import java.util.logging.Level;
import java.util.logging.Logger;
public class DBUtil {
    private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());


    static {
        try {
            Class.forName(DatabaseConfig.DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws StoreException {
        try {
            return DriverManager.getConnection(
                    DatabaseConfig.CONNECTION_STRING,
                    DatabaseConfig.DB_USER_NAME,
                    DatabaseConfig.DB_PASSWORD);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new StoreException(ResponseCode.DATABASE_CONNECTION_FAILURE);
        }
    }

}
