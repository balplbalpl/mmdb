package com.mmdb.model.database.datasource;

/**
 * jdbc:oracle:thin:@<hostname>:<port>:dbName
 * Created by XIE on 2015/3/27.
 */
public class OracleSingleDataSource extends SuperDataSourceTemplate {
    /**
     * 驱动名称
     */
    private String driverName = "jdbc:oracle:thin:@";
    private String hostname;
    private int port;
    private String dataBaseName;

//    private String schema;

    public OracleSingleDataSource(String hostname, int port, String dataBaseName, String userName, String password) {
        this.driverClassName = "oracle.jdbc.OracleDriver";
        this.url = this.driverName + hostname + ":" + port + ":" + dataBaseName;
        if (userName.equals("sys")) {
            userName = "as sysdba";
        }
        this.userName = userName;
        this.passWord = password;
    }

//    public OracleSingleDataSource(String hostname, int port, String dataBaseName, String schema, String userName, String password) {
//        this(hostname, port, dataBaseName, userName, password);
//        this.schema = schema;
//    }

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

//    public String getSchema() {
//        return schema;
//    }
//
//    public void setSchema(String schema) {
//        this.schema = schema;
//    }
}
