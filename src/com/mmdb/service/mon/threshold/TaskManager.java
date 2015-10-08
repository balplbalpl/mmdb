/**
 * 
 */
package com.mmdb.service.mon.threshold;

/**
 * @author aol_aog@163.com(James Gao)
 * 
 */
public class TaskManager {
	/**
	 * 性能数据清理任务.
	 */

	private PerfDataCleanupTask perfDataCleanupTask;

	/**
	 * 预警事件清理任务。
	 */

	private ProactiveEventCleanupTask pecTask;

	/**
	 * 
	 */
	public TaskManager() {

	}

	public void startService() {
		this.pecTask.start();
		this.perfDataCleanupTask.start();
	}

	public void stopService() {
		this.pecTask.stop();
		this.perfDataCleanupTask.stop();
	}

	public PerfDataCleanupTask getPerfDataCleanupTask() {
		return perfDataCleanupTask;
	}

	public void setPerfDataCleanupTask(PerfDataCleanupTask perfDataCleanupTask) {
		this.perfDataCleanupTask = perfDataCleanupTask;
	}

	public ProactiveEventCleanupTask getPecTask() {
		return pecTask;
	}

	public void setPecTask(ProactiveEventCleanupTask pecTask) {
		this.pecTask = pecTask;
	}
}
