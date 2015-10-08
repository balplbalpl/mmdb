package com.mmdb.model.database;

import com.mmdb.model.database.bean.DataBaseConfig;
import com.mmdb.model.database.bean.DataBaseSelf;
import com.mmdb.model.database.bean.Page;
import com.mmdb.model.database.bean.SplitPageResultSetExtractor;
import com.mmdb.model.database.datasource.MySqlSingleDataSource;
import com.mmdb.model.database.datasource.OracleRacDataSource;
import com.mmdb.model.database.datasource.OracleSingleDataSource;
import com.mmdb.model.database.datasource.SuperDataSourceTemplate;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.beans.PropertyVetoException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http://itlab.idcquan.com/Java/Spring/38091.html
 * http://www.blogjava.net/buaacaptain/archive/2006/05/03/44411.html
 * 数据库分发连接
 * Created by XIE on 2015/3/25.
 */
public class RouteDataSource {
    private static Logger logger = Logger.getLogger(RouteDataSource.class.getName());
    private JdbcTemplate jdbcTemplate;
    private DataBaseConfig dc;
    private String querySchemaSql;
    private String queryTableSql;
    private String queryViewSql;

    public RouteDataSource(DataBaseConfig dc) throws Exception {
        this.dc = dc;
        this.switchDataSource();
    }

    /**
     * 装载JdbcTemplate连接
     *
     * @throws PropertyVetoException
     */
    public void switchDataSource() throws PropertyVetoException {
        SuperDataSourceTemplate dataSourceTemplate = null;
        String type = dc.getType();
        if ("mysql".equalsIgnoreCase(type)) {
            this.querySchemaSql = "SHOW DATABASES";
            dataSourceTemplate = new MySqlSingleDataSource(dc.getHostName(), dc.getPort(),
                    dc.getDatabaseName(), dc.getUsername(), dc.getPassword());
        } else if ("oracle".equalsIgnoreCase(type)) {
            this.querySchemaSql = "SELECT USERNAME FROM ALL_USERS";
            if (dc.getRac()) {
                dataSourceTemplate = new OracleRacDataSource(dc.getRacAddress(), dc.getUsername(), dc.getPassword());
            } else {
                dataSourceTemplate = new OracleSingleDataSource(dc.getHostName(), dc.getPort(),
                        dc.getDatabaseName(), dc.getUsername(), dc.getPassword());
            }
        } else if ("sqlserver".equalsIgnoreCase(type)) {
            this.querySchemaSql = "SHOW SCHEMA";

            //TODO
        } else if ("db2".equalsIgnoreCase(type)) {
            this.querySchemaSql = "SELECT SCHEMANAME FROM SYSCAT.SCHEMATA";
            dataSourceTemplate = new OracleSingleDataSource(dc.getHostName(), dc.getPort(),
                    dc.getDatabaseName(), dc.getUsername(), dc.getPassword());
        }
        jdbcTemplate = dataSourceTemplate.getJdbcTemplate();
    }

    public boolean testing() throws SQLException {
        boolean bool = false;
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        if (connection != null) {
            bool = true;
        }
        return bool;
    }


    public List<String> getSchemas() throws SQLException {
        return jdbcTemplate.execute(querySchemaSql, new PreparedStatementCallback<List<String>>() {
            @Override
            public List<String> doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                List<String> schemas = new ArrayList<String>();
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    schemas.add(resultSet.getString(1));
                }
                return schemas;
            }
        });
    }

    private void appendQuerySqlInSchema(String schema) {
        String type = dc.getType();
        if ("mysql".equalsIgnoreCase(type)) {
            if (schema == null || schema.equals("")) {
                this.queryViewSql = "SELECT VIEW_NAME FROM INFORMATION_SCHEMA.VIEWS";
                this.queryTableSql = "SHOW TABLES";
            } else {
                this.queryTableSql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + schema + "'";
                this.queryViewSql = "SELECT VIEW_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = '" + schema + "'";
            }
        } else if ("oracle".equalsIgnoreCase(type)) {
            if (schema == null || schema.equals("")) {
                this.queryTableSql = "SELECT TABLE_NAME FROM ALL_TABLES";
                this.queryViewSql = "SELECT VIEW_NAME FROM ALL_VIEWS";
            } else {
                this.queryTableSql = "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = '" + schema.toUpperCase() + "'";
                this.queryViewSql = "SELECT VIEW_NAME FROM ALL_VIEWS WHERE OWNER = '" + schema.toUpperCase() + "'";
            }
        } else if ("sqlserver".equalsIgnoreCase(type)) {
            this.queryViewSql = "select name from sysdatabases";
            this.queryTableSql = "";
            //TODO
        } else if ("db2".equalsIgnoreCase(type)) {
            this.queryTableSql = "SELECT TABNAME FROM SYSCAT.TABLES";
            this.queryViewSql = "SELECT VIEWNAME FROM SYSCAT.VIEWS";
        }
        System.out.println("queryTableSql: " + queryTableSql);
        System.out.println("queryViewSql: " + queryViewSql);
    }

    public Map<String, List<String>> getTables(String schema) {
        this.appendQuerySqlInSchema(schema);
        Map<String, List<String>> retMap = new HashMap<String, List<String>>();
        List<String> viewNames = jdbcTemplate.execute(this.queryViewSql, new PreparedStatementCallback<List<String>>() {
            @Override
            public List<String> doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                List<String> viewNames = new ArrayList<String>();
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    viewNames.add(resultSet.getString(1));
                }
                return viewNames;
            }
        });
        retMap.put("viewNames", viewNames);
        List<String> tableNames = jdbcTemplate.execute(this.queryTableSql, new PreparedStatementCallback<List<String>>() {
            @Override
            public List<String> doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                List<String> tableNames = new ArrayList<String>();
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    tableNames.add(resultSet.getString(1));
                }
                return tableNames;
            }
        });
        retMap.put("tableNames", tableNames);
        return retMap;
    }

    private String appendQuerySqlInTable(DataBaseSelf ds) {
        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("SELECT * FROM ");
        if (ds.isSelf()) {
            sqlQuery.append("( ").append(ds.getCustomSql()).append(" )");
        } else {
            String schema = ds.getSchema(), table = ds.getTable();
            if (schema == null || schema.equals("")) {
                sqlQuery.append(table);
            } else {
                sqlQuery.append(schema).append(".").append(table);
            }
        }
        System.out.println(sqlQuery.toString());
        return sqlQuery.toString();
    }

    public List<Map<String, String>> getFields(DataBaseSelf ds) {
        List<Map<String, String>> datas = new ArrayList<Map<String, String>>();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(this.appendQuerySqlInTable(ds));
        SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            Map<String, String> data = new HashMap<String, String>();
            data.put("字段", metaData.getColumnName(i + 1));
            data.put("类型", String.valueOf(metaData.getColumnType(i + 1)));
            data.put("空值", metaData.getScale(i + 1) == 1 ? "true" : "false");
            datas.add(data);
        }
        return datas;
    }

    public int getCount(DataBaseSelf ds) {
        RowCountCallbackHandler countCallback = new RowCountCallbackHandler();  // not reusable
        jdbcTemplate.query(this.appendQuerySqlInTable(ds), countCallback);
        return countCallback.getRowCount();
    }

    public List<Map<String, Object>> getMetaDatas(DataBaseSelf ds, final int startNumber, final int lenNumber) {
        jdbcTemplate.query(this.appendQuerySqlInTable(ds), new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int i) throws SQLException {
                List result = new ArrayList();
                int rowNum = 0;
                int end = startNumber + lenNumber;
                point:
                while (rs.next()) {
                    ++rowNum;
                    if (rowNum < startNumber) {
                        continue point;
                    } else if (rowNum >= end) {
                        break point;
                    } else {
                        result.add(this.mapRow(rs, rowNum));
                    }
                }
                System.out.println(result);
                return result;
            }
        });

        return null;
    }

    public List<Map<String, Object>> getMetaDatasByPage(DataBaseSelf ds, int pageNumber, int pageSize) throws Exception {
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Connection con = jdbcTemplate.getDataSource().getConnection();
        if (con == null) {
            throw new Exception("建立数据库连接失败");
        }
        String customSql = this.appendQuerySqlInTable(ds);
        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(customSql);
        resultSet.next();
        int totalRow = resultSet.getRow();
        resultSet.close();
        stmt.close();
        Page page = new Page(totalRow, pageNumber, pageSize);
        PreparedStatement pst = con.prepareStatement(customSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        resultSet = pst.executeQuery();
        pst.setMaxRows(page.getEndIndex());
        if (page.getBeginIndex() > 0) {
            resultSet.absolute(page.getBeginIndex());
        }
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        while (resultSet.next()) {
            Map<String, Object> metaData = new HashMap<String, Object>();
            for (int i = 1; i <= columnCount; i++) {
                String label = resultSetMetaData.getColumnLabel(i).toLowerCase();
//                if (fieldNames.contains(label)) {
                String value = resultSet.getString(label);
                metaData.put(label, value);
//                }
            }
            datas.add(metaData);
        }
        resultSet.close();
        pst.close();
        System.out.println(datas);
        return datas;
    }

    public void close() {
        Connection connection = null;
        try {
            connection = this.jdbcTemplate.getDataSource().getConnection();
            connection.close();
        } catch (Exception e) {
            logger.error("关闭数据库链接失败", e);
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                logger.error("关闭数据库链接失败", e);
            }
        }

    }

    /**
     * 普通分页查询<br>
     * <b>如果结果结合比较大应该调用setFetchsize() 和setMaxRow两个方法来控制一下，否则会内存溢出</b>
     *
     * @param sql       查询的sql语句
     * @param startRow  起始行
     * @param rowsCount 获取的行数
     * @return
     * @throws DataAccessException
     */
    @SuppressWarnings("unchecked")
    public List<Map> querySP(String sql, int startRow, int rowsCount) throws DataAccessException {
        return querySP(sql, startRow, rowsCount, null);
    }

    /**
     * 自定义行包装器查询<br>
     * <b>如果结果结合比较大应该调用setFetchsize() 和setMaxRow两个方法来控制一下，否则会内存溢出</b>
     *
     * @param sql       查询的sql语句
     * @param startRow  起始行
     * @param rowsCount 获取的行数
     * @param rowMapper 行包装器
     * @return
     * @throws DataAccessException
     */
    @SuppressWarnings("unchecked")
    public List<Map> querySP(String sql, int startRow, int rowsCount, RowMapper rowMapper)
            throws DataAccessException {
        return (List) jdbcTemplate.query(sql, new SplitPageResultSetExtractor(rowMapper, startRow,
                rowsCount));
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> dbmap = new HashMap<String, String>();
        //{type:'mysql/sqlserver/oracle/db2',url:'',port:'',schema:'',database:'',username:'',password:''}
        dbmap.put("type", "oracle");
        dbmap.put("url", "192.168.1.100");
        dbmap.put("port", "1521");
        dbmap.put("database", "orcl");
        dbmap.put("username", "em4");
        dbmap.put("password", "uinnova");
        DataBaseConfig dc = new DataBaseConfig(false, dbmap);

        RouteDataSource rds = new RouteDataSource(dc);
//        System.out.println(rds.getSchemas());

//        System.out.println(rds.getTables("EM4"));
//{schema:'',table:'',customSql:''}
        Map<String, String> selfMap = new HashMap();
        selfMap.put("schema", "EM4");
        selfMap.put("table", "MONITOR_NETLINK");
        selfMap.put("customSql", "");
        DataBaseSelf ds = new DataBaseSelf(false, selfMap, null);

//        System.out.println(rds.getCount(ds));
        rds.getFields(ds);
//        rds.getMetaDatas(ds, 1, 10);
    }

}