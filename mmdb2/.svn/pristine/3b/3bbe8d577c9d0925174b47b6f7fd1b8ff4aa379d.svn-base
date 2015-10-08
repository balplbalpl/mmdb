/**
 * 
 */
package com.mmdb.service.mon.threshold;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.annotation.Resource;
import javax.sql.DataSource;

import com.ccb.iomp.monitoring.eap.common.util.DBUtils;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;

/**
 * 定时清理预警数据表
 * 
 * @author aol_aog@163.com(James Gao)
 * 
 */
//@Component("ProactiveEventCleanupTask")
public class ProactiveEventCleanupTask {
	private Log log = LogFactory.getLogger("ProactiveEventCleanupTask");
	/**
	 * 每隔一天清理一次一天前的预警表数据。
	 */
	private int interval = 86400;

	/**
	 * 数据源。
	 */
	@Resource(name = "eventDS")
	private DataSource ds;

	private String cleanSql = "delete from mon_proactive_event where  last_occur_time < ?";

	private boolean isStart;

	Thread cleanupThread;

	/**
	 * 
	 */
	public ProactiveEventCleanupTask() {
		isStart = false;
	}

	public void start() {
		isStart = true;
		Thread cleanupThread = new Thread() {
			@Override
			public void run() {
				while (isStart) {
					cleanupData();

					try {
						//等待指定时间
						Thread.sleep(interval * 1000L);
					} catch (Exception ex) {
						//ignore this exception.
					}
				}
			}
		};
		cleanupThread.setName("清理预警事件线程");
		cleanupThread.start();

	}

	public void stop() {
		isStart = false;
		if (cleanupThread != null) {
			try {
				cleanupThread.interrupt();
				Thread.yield();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void cleanupData() {

		log.iLog("开始清理一天前预警事件...");

		// 执行创建
		// 建表
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			// 执行查询
			conn = this.ds.getConnection();
			ps = conn.prepareStatement(this.cleanSql);
			ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()
					- interval * 1000L));
			int dc = ps.executeUpdate();
			log.iLog("清理一天前预警事件" + dc + "条。");
		} catch (SQLException e) {
			log.eLog("清理一天前预警事件时失败: " + e.getMessage(), e);

		} finally {
			DBUtils.closeAll(conn, ps, null);
		}

		log.iLog("清理一天前预警事件完毕。");

	}
}
