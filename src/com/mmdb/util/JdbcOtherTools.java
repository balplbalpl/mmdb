package com.mmdb.util;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import jdbc.JdbcConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 第三方数据库操操作工具类
 * Created by XIE on 2015/3/31.
 */
public class JdbcOtherTools {
    private static Log log = LogFactory.getLogger("JdbcOtherTools");

    /**
     * 获取Oracle的Connection
     *
     * @param dbInfo
     * @return
     */
    private static Connection getOracleDB2Connection(Map<String, String> dbInfo) {
        dbInfo.put("schema", dbInfo.get("database"));
        return JdbcConnection.getConnection(dbInfo.get("type").toLowerCase(),
                dbInfo.get("url"), dbInfo.get("username"),
                dbInfo.get("password"), dbInfo.get("schema"),
                dbInfo.get("port"), "");
    }

    /**
     * 获取其他数据库的Connection
     *
     * @param dbInfo
     * @return
     */
    private static Connection getOtherConnection(Map<String, String> dbInfo) {
        return JdbcConnection.getConnection(dbInfo.get("type").toLowerCase(),
                dbInfo.get("url"), dbInfo.get("username"),
                dbInfo.get("password"), "", dbInfo.get("port"),
                dbInfo.get("database"));
    }

    /**
     * 获取rac连接
     *
     * @param dbMap
     * @return
     */
    public static Connection getRacConnection(Map<String, String> dbMap) {
        return JdbcConnection.getConnection(dbMap.get("url"), dbMap.get("username"), dbMap.get("password"));
    }

    /**
     * 获取非rac连接
     *
     * @param isRac rac连接
     * @param dbMap 参数信息
     * @return
     */
    public static Connection getConnection(boolean isRac, Map<String, String> dbMap) {
        Connection connection;
        if (isRac) {
            connection = getRacConnection(dbMap);
        } else {
            String type = dbMap.get("type").toLowerCase();
            if ("oracle".equals(type) || "db2".equals(type)) {
                connection = getOracleDB2Connection(dbMap);
            } else {
                connection = getOtherConnection(dbMap);
            }
        }
        return connection;
    }

    public static void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
                if (connection.isClosed()) {
                    System.out.println("connection.isClosed");
                }
            }
        } catch (SQLException e) {
            log.eLog(e);
        }
    }
}
