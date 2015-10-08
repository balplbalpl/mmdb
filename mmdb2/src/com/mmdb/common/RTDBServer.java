/**
 *
 */
package com.mmdb.common;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;

import com.ccb.iomp.monitoring.eap.common.util.DBUtils;

/**
 * 内置的实时数据库.
 *
 * @author aol_aog@163.com(James Gao)
 */
public class RTDBServer {

    private String driverClassName, url, username, password;

    private String dataPath, dbName;

    private String port;

    /**
     *
     */
    public RTDBServer() {

    }

    public void startDBServer() throws Exception {

        Class.forName("org.hsqldb.jdbcDriver");
        HsqlProperties p = new HsqlProperties();
        p.setProperty("server.port", port);

        p.setProperty("server.database.0", dataPath);
        p.setProperty("server.dbname.0", dbName);
        p.setProperty("ifexists", "false");
        p.setProperty("server.silent", "true");

        // set up the rest of properties

        // alternative to the above is
        Server server = new Server();
        server.setProperties(p);

        PrintWriter pw = new PrintWriter(System.out);

        server.setLogWriter(pw); // can use custom
        // writer
        server.setErrWriter(pw); // can use custom

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
			public void run() {
                stopServer();
            }
        });
    }

    public void stopServer() {
        Connection conn = null;
        Statement ps = null;
        try {
            // 执行查询
            conn = DriverManager.getConnection(url, "sa", "");
            ps = conn.createStatement();
            ps.executeUpdate("SHUTDOWN");

        } catch (SQLException e) {
            // 忽略表不存在的异常
        } finally {
            DBUtils.closeAll(conn, ps, null);
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
