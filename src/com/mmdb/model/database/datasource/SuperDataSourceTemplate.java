package com.mmdb.model.database.datasource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.PropertyVetoException;

/**
 * jdbc连接元数据模板
 * Created by XIE on 2015/3/27.
 */
public class SuperDataSourceTemplate {
    /**
     * 驱动
     */
    public String driverClassName;
    /**
     * 访问地址 拼接出来的
     */
    public String url;
    /**
     * 用户名
     */
    public String userName;
    /**
     * 密码
     */
    public String passWord;

    public JdbcTemplate getJdbcTemplate() throws PropertyVetoException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(this.driverClassName);
        dataSource.setJdbcUrl(this.url);
        dataSource.setUser(this.userName);
        dataSource.setPassword(this.passWord);
        dataSource.setMaxIdleTime(5); //最大空闲时间,30秒内未使用则连接被丢弃。若为0则永不丢弃。Default: 0
        return new JdbcTemplate(dataSource);
    }


    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SuperDataSourceTemplate that = (SuperDataSourceTemplate) o;

        if (driverClassName != null ? !driverClassName.equals(that.driverClassName) : that.driverClassName != null)
            return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        return !(passWord != null ? !passWord.equals(that.passWord) : that.passWord != null);

    }

    @Override
    public int hashCode() {
        int result = driverClassName != null ? driverClassName.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (passWord != null ? passWord.hashCode() : 0);
        return result;
    }
}
