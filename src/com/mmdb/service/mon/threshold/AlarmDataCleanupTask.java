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
 * 定时清理mon_eap_event数据表，此数据表主要是存放在内存中。
 * 每4小时清理一下4小时之前的数据.
 * 
 * @author aol_aog@163.com(James Gao)
 * 
 */
//@Component("AlarmDataCleanupTask")
public class AlarmDataCleanupTask {
	private Log log = LogFactory.getLogger("AlarmDataCleanupTask");
	/**
	 * 每4小时清理一下4小时之前的数据。
	 */
	private int interval = 14400;

	/**
	 * 数据源。
	 */
	@Resource(name = "eventDS")
	private DataSource ds;

	private String cleanSql = "delete from mon_eap_event where  LastOccurrence < ?";

	private boolean isStart;

	Thread cleanupThread;

	/**
	 * 
	 */
	public AlarmDataCleanupTask() {
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
		cleanupThread.setName("清理内存告警数据线程");
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

		log.iLog("开始清理4小时前内存告警数据...");

		
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			// 执行查询
			conn = this.ds.getConnection();
			ps = conn.prepareStatement(this.cleanSql);
			ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()
					- interval * 1000L));
			int dc = ps.executeUpdate();
			log.iLog("清理4小时前内存告警数据" + dc + "条。");
		} catch (SQLException e) {
			log.eLog("清理4小时前内存告警数据时失败: " + e.getMessage(), e);

		} finally {
			DBUtils.closeAll(conn, ps, null);
		}

		log.iLog("清理4小时前内存告警数据完毕。");

	}
}
