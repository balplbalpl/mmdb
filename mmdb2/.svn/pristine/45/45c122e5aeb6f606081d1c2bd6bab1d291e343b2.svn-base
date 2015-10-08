/**
 * 
 */
package com.mmdb.common;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;

/**
 * @author aol_aog@163.com(James Gao)
 * 
 */

public class DistributeTaskService {
	private Log log = LogFactory.getLogger("DistributeTaskService");

	/**
	 * 分布式任务集。
	 */
	private DistributeTask[] tasks;

	private DistributeEnv distributeEnv;

	/**
	 * 
	 * @param tasks
	 */

	public DistributeTaskService(DistributeTask[] tasks) {
		this.tasks = tasks;
		this.distributeEnv = new DistributeEnv();

	}

	public void startService() {
		log.iLog("启动分布式任务调度服务...");
		if (tasks != null) {
			for (DistributeTask task : tasks) {
				task.init();
				this.distributeEnv.submitTask(task);
			}
		}
		log.iLog("分布式任务调度服务启动完毕。");
	}

	public void stopService() {
		log.iLog("停止分布式任务调度服务...");
		
		log.iLog("分布式任务调度服务停止完毕。");
	}

	public DistributeEnv getDistributeEnv() {
		return distributeEnv;
	}

	public void setDistributeEnv(DistributeEnv distributeEnv) {
		this.distributeEnv = distributeEnv;
	}

}
