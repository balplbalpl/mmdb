package com.mmdb.model.database.datasource;

/**
 * jdbc:db2://<hostname>:<port>/dbName
 * Created by XIE on 2015/3/27.
 */
public class DB2SingleDataSource extends SuperDataSourceTemplate {
    /**
     * 驱动名称
     */
    private String driverName = "jdbc:db2://";
    private String hostname;
    private int port;
    private String dataBaseName;

    public DB2SingleDataSource(String hostname, int port, String dataBaseName, String userName, String password) {
        this.driverClassName = "com.ibm.db2.jcc.DB2Driver";
        this.url = this.driverName + hostname + ":" + port + "/" + dataBaseName;
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
