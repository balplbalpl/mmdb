/**
 * 
 */
package com.mmdb.service.mon.threshold.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 代表一个性能数据。
 * 
 * @author aol_aog@163.com(James Gao)
 * 
 */
public class PerfData implements Serializable {

	/**
	 * serialUID.
	 */
	private static final long serialVersionUID = 3170686071081402137L;

	public String ci;

	public String instance;

	public String kpi;
	
	public String kpiClass;

	public String value;

	/**
	 * 当前值的状态，阈值产生的告警级别，<=0表示其值为正常，其它的分别对应为 1： threshold1, 2:threshold2,
	 * 3:threshold1, 4: threshold4
	 */
	public int status;

	/**
	 * 上一次值的状态。
	 */
	public int lastStatus;
	
	/**
	 * 上次值。
	 */
	public String lastValue;

	/**
	 * 当前指标的时间
	 */
	public Timestamp curTime;

	/**
	 * 如果产生事件，其阈值定义
	 */
	public String threshold;
	
	/**
	 * 预留字段所用。
	 */
	public String reservedField1,reservedField2,reservedField3,reservedField4,reservedField5;

	public PerfData() {
		status = 6;
		lastStatus = 6;
	}
	
	public String getKey(){
		return ci+instance+kpi;
	}
}
