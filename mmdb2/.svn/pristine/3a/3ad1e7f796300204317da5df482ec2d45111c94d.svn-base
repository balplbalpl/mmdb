package com.mmdb.model.database.datasource;

/**
 * RAC连接
 * Created by XIE on 2015/3/27.
 */
public class OracleRacDataSource extends SuperDataSourceTemplate {
    /**
     * 驱动名称
     */
    private String driverName = "jdbc:oracle:thin:@";
    /**
     * RAC地址
     */
    private String racAddress;

    /**
     * 重写默认构造函数
     *
     * @param racAddress
     * @param userName
     * @param password
     */
    public OracleRacDataSource(String racAddress, String userName, String password) {
        this.driverClassName = "oracle.jdbc.OracleDriver";
        this.url = this.driverName + racAddress;
        if (userName.equals("sys")) {
            userName = "as sysdba";
        }
        this.userName = userName;
        this.passWord = password;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getRacAddress() {
        return racAddress;
    }

    public void setRacAddress(String racAddress) {
        this.racAddress = racAddress;
    }
}
