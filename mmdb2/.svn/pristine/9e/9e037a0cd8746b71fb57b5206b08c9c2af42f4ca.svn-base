/**
 * 
 */
package com.mmdb.common;

import java.io.Serializable;

/**
 * 定义分布式任务接口，实现分布式调度。
 * 
 * @author aol_aog@163.com(James Gao)
 * 
 */
public abstract class DistributeTask implements Serializable {

	/**
	 * serialUID.
	 */
	private static final long serialVersionUID = -389672659222722473L;
	
	/**
	 * 任务执行的间隔,单位：秒。
	 */
	private int interval =30; //

	/**
	 * 此处实现任务的执行逻辑。
	 */
	public abstract void execute();
	
	/**
	 * 得到分布式任务的名称。
	 * @return
	 */
	public abstract String getTaskName();
	
	/**
	 * 得到分面式任务组名称。
	 * @return
	 */
	public abstract String getTaskGroup();
	
	/**
	 * 得到此任务所使用的信用量，用于在任务调度时使用。
	 * @return
	 */
	public abstract String getOwnSemaphore();
	
	/**
	 * 任务初始化,可以在此处做些资源申请的工作.
	 */
	public abstract void init();

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) throws Exception {
		if(interval <=0) throw new Exception ("任务执行间隔时间不能小于1秒，设置值为："+interval+"秒。");
		this.interval = interval;
	}

}