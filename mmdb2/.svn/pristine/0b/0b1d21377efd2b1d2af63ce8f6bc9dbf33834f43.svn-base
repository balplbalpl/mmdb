/**
 * 
 */
package com.mmdb.common.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.ccb.iomp.monitoring.eap.common.util.DBUtils;

/**
 * 主要是创建与MMDB相关的用户表。
 * 
 * @author aol_aog@163.com(James Gao)
 * 
 */
public class MMDBSynchronizer {

	private DataSource ds;
	
	private String jdbcURL;

	/**
	 * 
	 */
	public void start() throws Exception {
		initDB();

	}

	/**
	 * 初始化数据库，创建表和索引等工作。
	 * 
	 * @throws Exception
	 */
	private void initDB() throws Exception {
		// CREATE TABLE portal_org

//		createTablePerfData();
//		createTableAlarmData();
//		createTableTicketData();
//		createTableThresholdData();
//		createTableViewKPICIUser();
//		createTablemon_proactive_event();

	}

	private void createTablePerfData() throws Exception {
		boolean rv = this.isTableExisted("MON_PERF_DATA");
		if (!rv) {

			String str = "CREATE TABLE MON_PERF_DATA("
					+ "RESOURCE_NAME        VARCHAR(128) NOT NULL ,"
					+ "INSTANCE_NAME        VARCHAR(128) NOT NULL ,"
					+ "METRIC_CLASS         VARCHAR(128) NOT NULL ,"
					+ "METRIC_NAME          VARCHAR(64) NOT NULL ,"
					+ "USERID               VARCHAR(64) NULL ,"
					+ "VIEWID               VARCHAR(64) NULL ,"
					+ "METRIC_TIME          TIMESTAMP NOT NULL ,"
					+ "METRIC_STATUS        integer NOT NULL ,"
					+ "METRIC_VALUE         VARCHAR(128) NOT NULL,"
					+ "RESERVED_FIELD1      VARCHAR(64)  NULL,"
					+ "RESERVED_FIELD2      VARCHAR(64)  NULL,"
					+ "RESERVED_FIELD3      VARCHAR(64)  NULL,"
					+ "RESERVED_FIELD4      VARCHAR(64)  NULL,"
					+ "RESERVED_FIELD5      VARCHAR(64)  NULL )";
			createTable(str);
			str = getCreateIndexClause("METRIC_NAME", "MON_PERF_DATA");
			createTable(str);
			System.out.println("数据库表MON_PERF_DATA自动创建完毕。");
		}
	}

	private void createTableAlarmData() throws Exception {
		boolean rv = this.isTableExisted("mon_eap_event");
		if (!rv) {

			String str = "CREATE TABLE mon_eap_event("
					+ "Serial        VARCHAR(64) NOT NULL ,"
					+ "CIName        VARCHAR(128) NOT NULL ,"
					+ "Severity          VARCHAR(64) NOT NULL ,"
					+ "status          VARCHAR(64) NOT NULL ,"
					+ "LastOccurrence          TIMESTAMP NOT NULL ,"
					+ "KPIAlertKey        VARCHAR(128)  NULL ,"
					+ "KPIDescription        VARCHAR(128)  NULL ,"
					+ "Summary         VARCHAR(512)  NULL )";
			createTable(str);
			str = getCreateIndexClause("LastOccurrence", "mon_eap_event");
			createTable(str);
			System.out.println("数据库表mon_eap_event自动创建完毕。");
		}
	}

	private void createTableTicketData() throws Exception {
		boolean rv = this.isTableExisted("MON_TICKET_DATA");
		if (!rv) {
			String str = "CREATE TABLE MON_TICKET_DATA("
					+ "TICKETID        VARCHAR(64) NOT NULL ,"
					+ "ASSIGNMENT        VARCHAR(128)  NULL ," // 任务
					+ "ASSIGNEE_NAME          VARCHAR(128)  NULL ,"// 受托人
					+ "CINAME          VARCHAR(128) NOT NULL ,"
					+ "SUMMARY         VARCHAR(256) NULL ,"
					+ "OPEN_TIME        TIMESTAMP  NULL)";
			createTable(str);

			System.out.println("数据库表MON_TICKET_DATA自动创建完毕。");
		}
	}

	private void createTableThresholdData() throws Exception {
		boolean rv = this.isTableExisted("MON_THRESHOLD_DATA");
		if (!rv) {
			String str = "CREATE TABLE MON_THRESHOLD_DATA("
					+ "CINAME        VARCHAR(128) NOT NULL ,"
					+ "CLASS_NAME        VARCHAR(128)  NULL ," // 任务
					+ "INSTANCE_NAME          VARCHAR(64)  NULL ,"// 受托人
					+ "PARA_NAME          VARCHAR(128)  NULL ,"
					+ "THRESHOLD_INFORMATION        VARCHAR(128)  NULL ,"
					+ "THRESHOLD_MINOR        VARCHAR(128)  NULL ,"
					+ "THRESHOLD_MAJOR        VARCHAR(128)  NULL ,"
					+ "THRESHOLD_CRITICAL        VARCHAR(128)  NULL)";
			createTable(str);

			System.out.println("数据库表MON_THRESHOLD_DATA自动创建完毕。");
		}
	}

	private void createTableViewKPICIUser() throws Exception {
		boolean rv = this.isTableExisted("mon_viewkpiciuser_rel");
		if (!rv) {

			String str = "CREATE TABLE mon_viewkpiciuser_rel("
					+ "id            VARCHAR(128) NOT NULL ,"
					+ "kpi_class     VARCHAR(128) NOT NULL ,"
					+ "kpi           VARCHAR(128) NOT NULL ,"
					+ "ci            VARCHAR(128)  NULL ,"
					+ "instance      VARCHAR(128)  NULL ,"
					+ "viewid          VARCHAR(64)  NULL ,"
					+ "userid          VARCHAR(64)  NULL ,"
					+ "threshold1    VARCHAR(64)  NULL ,"
					+ "threshold2    VARCHAR(64)  NULL ,"
					+ "threshold3    VARCHAR(64)  NULL ,"
					+ "threshold4    VARCHAR(64)  NULL ,"
					+ "threshold5    VARCHAR(64)  NULL ,"
					+ "enabled       CHAR(1)      NOT NULL ,"
					+ "update_time   TIMESTAMP  NOT NULL )";
			createTable(str);

			System.out.println("数据库表mon_viewkpiciuser_rel自动创建完毕。");
		}
	}

	/**
	 * 预警事件表。
	 * 
	 * @throws Exception
	 */
	private void createTablemon_proactive_event() throws Exception {
		boolean rv = this.isTableExisted("mon_proactive_event");
		if (!rv) {

			String str = "CREATE TABLE mon_proactive_event("
					+ "id            VARCHAR(64) NOT NULL ,"
					+ "kpi           VARCHAR(128) NOT NULL ,"
					+ "kpi_class     VARCHAR(128) NOT NULL ,"
					+ "severity      integer      NOT NULL ,"
					+ "ci            VARCHAR(128)  NULL ,"
					+ "instance      VARCHAR(128)  NULL ,"
					+ "viewID          VARCHAR(64)  NULL ,"
					+ "userID          VARCHAR(64)  NULL ,"
					+ "threshold     VARCHAR(64)  NULL ,"
					+ "tally         integer      NOT NULL ,"
					+ "summary       VARCHAR(2048)  NULL ,"
					+ "kpi_value     VARCHAR(256)  NULL ,"
					+ "kpi_last_value   VARCHAR(256)  NULL ,"
					+ "first_occur_time   TIMESTAMP  NOT NULL ,"
					+ "last_occur_time    TIMESTAMP  NOT NULL )";
			createTable(str);

			str = getCreateIndexClause("last_occur_time", "mon_proactive_event");
			createTable(str);

			System.out.println("数据库表mon_proactive_event自动创建完毕。");
		}
	}

	/**
	 * 
	 * @param model
	 * @param executor
	 */
	private void createTable(String createSql) {

		try {
			createSql = createSql.toUpperCase();
			
			//oracle是varchar2
			if(this.jdbcURL.toLowerCase().indexOf("oracle") !=-1){
				createSql = createSql.replaceAll("VARCHAR", "VARCHAR2");
			}else{
				createSql = createSql.replaceAll("VARCHAR2", "VARCHAR");
			}

			// 执行创建
			// 建表
			Statement ps = null;
			Connection conn = null;
			try {
				// 执行查询
				conn = ds.getConnection();
				ps = conn.createStatement();
				ps.executeUpdate(createSql);

			} finally {
				DBUtils.closeAll(conn, ps, null);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("创建数据库表时失败: " + e.getMessage());
		}
	}

	/**
	 * 根据事件模型, 验证表是否存在, 存在的话检查表结构是否一致
	 * 
	 * @param model
	 * @return 表存在返回true; 否则返回false
	 * @throws CreateTableException
	 */
	protected boolean isTableExisted(String table) throws Exception {
		boolean retVal = false;

		String sql = "select * from " + table + " where 1>1";
		try {

			Statement ps = null;
			ResultSet rs = null;
			Connection conn = null;
			try {
				// 执行查询
				conn = ds.getConnection();
				ps = conn.createStatement();
				rs = ps.executeQuery(sql);

			} finally {
				DBUtils.closeAll(null, ps, rs);
			}

			retVal = true;
		} catch (SQLException e) {
			// 忽略表不存在的异常
		}

		return retVal;
	}

	private String getCreateIndexClause(String column, String table) {
		String sql = "CREATE INDEX index_" + column + " on " + table + "  ("
				+ column + " )";
		return sql.toUpperCase();
	}

	public DataSource getDs() {
		return ds;
	}

	public void setDs(DataSource ds) {
		this.ds = ds;
	}

	public String getJdbcURL() {
		return jdbcURL;
	}

	public void setJdbcURL(String jdbcURL) {
		this.jdbcURL = jdbcURL;
	}
}