package com.mmdb.model.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库动态连接
 * Created by XIE on 2015/3/4.
 */
@Service
public class DbServer {
    private static Logger logger = Logger.getLogger(DbServer.class.getName());
    private JdbcTemplate jdbcTemplate;

    public void createDataSource() throws Exception {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass("oracle.jdbc.OracleDriver");
        dataSource.setJdbcUrl("jdbc:oracle:thin:@192.168.1.100:1521:ORCL");
        dataSource.setUser("em4");
        dataSource.setPassword("uinnova");
        dataSource.setMaxIdleTime(5);
        jdbcTemplate = new JdbcTemplate(dataSource);
//        System.out.println(this.getDataBaseSchema());
//        System.out.println(getDataBasePreparedStatement());
        System.out.println(this.getFieldsMetaData());
//        this.getTableMetaData();
    }

    public List<String> getDataBaseSchema() throws SQLException {
        return jdbcTemplate.execute("SELECT USERNAME FROM ALL_USERS", new PreparedStatementCallback<List<String>>() {
            @Override
            public List<String> doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                List<String> schemas = new ArrayList<String>();
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    schemas.add(resultSet.getString(1));
                }
                resultSet.close();
                ps.close();
                return schemas;
            }
        });
    }


    public Map<String, List<String>> getDataBasePreparedStatement() {
        Map<String, List<String>> retMap = new HashMap<String, List<String>>();
        List<String> viewNames = jdbcTemplate.execute("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS", new PreparedStatementCallback<List<String>>() {
            @Override
            public List<String> doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                List<String> viewNames = new ArrayList<String>();
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    viewNames.add(resultSet.getString(1));
                }
                resultSet.close();
                ps.close();
                return viewNames;
            }
        });
        retMap.put("viewNames", viewNames);
        List<String> tableNames = jdbcTemplate.execute("SHOW TABLES", new PreparedStatementCallback<List<String>>() {
            @Override
            public List<String> doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                List<String> tableNames = new ArrayList<String>();
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    tableNames.add(resultSet.getString(1));
                }
                resultSet.close();
                ps.close();
                return tableNames;
            }
        });
        retMap.put("tableNames", tableNames);
        return retMap;
    }

    public List<Map<String, String>> getFieldsMetaData() {
        return jdbcTemplate.execute("SELECT * FROM productlibrary.store_products", new PreparedStatementCallback<List<Map<String, String>>>() {
            @Override
            public List<Map<String, String>> doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                List<Map<String, String>> datas = new ArrayList<Map<String, String>>();
                ParameterMetaData parameterMetaData = ps.getParameterMetaData();
                System.out.println(parameterMetaData.getParameterCount());
                ResultSet resultSet = ps.executeQuery();
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                for (int i = 0, columnCount = resultSetMetaData.getColumnCount(); i < columnCount; i++) {
                    Map<String, String> MetaData = new HashMap<String, String>();
                    MetaData.put("字段", resultSetMetaData.getColumnName(i + 1).toLowerCase());
                    MetaData.put("空值", resultSetMetaData.isNullable(i + 1) == 1 ? "true" : "false");
                    MetaData.put("类型", resultSetMetaData.getColumnTypeName(i + 1));
                    MetaData.put("长度", resultSetMetaData.getColumnDisplaySize(i + 1) + "");
                    MetaData.put("schema", resultSetMetaData.getSchemaName(i + 1));
                    MetaData.put("getPrecision", resultSetMetaData.getPrecision(i + 1) + "");
                    datas.add(MetaData);
                }
                resultSet.close();
                ps.close();
                return datas;
            }
        });
    }

    public List<Map<String, Object>> getTableMetaData() {
        return jdbcTemplate.execute("SELECT * FROM productlibrary.store_products", new PreparedStatementCallback<List<Map<String, Object>>>() {
                    @Override
                    public List<Map<String, Object>> doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
                        ResultSet resultSet = ps.executeQuery();
                        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                        int columnCount = resultSetMetaData.getColumnCount();
                        while (resultSet.next()) {
                            Map<String, Object> metaData = new HashMap<String, Object>();
                            for (int i = 1; i <= columnCount; i++) {
                                String label = resultSetMetaData.getColumnLabel(i).toLowerCase();
                                String value = resultSet.getString(label);
                                metaData.put(label, value);
                            }
                            datas.add(metaData);
                        }
                        resultSet.close();
                        ps.close();
                        return datas;
                    }
                }
        );
    }
}
