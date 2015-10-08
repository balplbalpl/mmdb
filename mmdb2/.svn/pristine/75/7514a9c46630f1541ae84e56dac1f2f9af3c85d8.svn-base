package com.mmdb.model.database.datasource;


/**
 * jdbc:microsoft:sqlserver://<hostname>:<port>;DatabaseName=<dbName>
 * Created by XIE on 2015/3/27.
 */
public class SqlServerSingleDataSource extends SuperDataSourceTemplate {
    /**
     * 驱动名称
     */
    private String driverName = "jdbc:microsoft:sqlserver://";
    private String hostname;
    private int port;
    private String dataBaseName;

    public SqlServerSingleDataSource(String hostname, int port, String dataBaseName, String userName, String password) {
        this.driverClassName = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
        this.url = this.driverName + hostname + ":" + port + ";DatabaseName=" + dataBaseName;
        this.userName = userName;
        this.passWord = password;
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
