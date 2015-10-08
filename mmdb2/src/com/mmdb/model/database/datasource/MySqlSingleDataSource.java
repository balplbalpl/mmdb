package com.mmdb.model.database.datasource;

/**
 * Mysql单点连接配置
 * jdbc:mysql://<hostname>:<port>/dbName
 * Created by XIE on 2015/3/27.
 */
public class MySqlSingleDataSource extends SuperDataSourceTemplate {

    /**
     * 驱动名称
     */
    private String driverName = "jdbc:mysql://";
    private String hostname;
    private int port;
    private String dataBaseName;

    public MySqlSingleDataSource(String hostname, int port, String dataBaseName, String userName, String passWord) {
        this.driverClassName = "com.mysql.jdbc.Driver";
        this.url = this.driverName + hostname + ":" + port + "/" + dataBaseName;
        this.userName = userName;
        this.passWord = passWord;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }

}
