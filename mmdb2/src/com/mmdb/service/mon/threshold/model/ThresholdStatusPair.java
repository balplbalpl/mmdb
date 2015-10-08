/**
 * 
 */
package com.mmdb.service.mon.threshold.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author aol_aog@163.com(James Gao)
 *
 */
public class ThresholdStatusPair implements Serializable {

	/**
	 * serialUID.
	 */
	private static final long serialVersionUID = -2931278278740341590L;

	/**
	 * 阈值key
	 */
	public String key ;
	
	/**
	 * ci ,instance ,kpi,kpiClass
	 */
	public String ci ,instance, kpi,kpiClass;
	
	/**
	 * 当前状态,阈值产生的告警级别，6表示其值为正常，其它的分别对应为 1： threshold1, 2:threshold2, 3:threshold1, 4: threshold4
	 */
	public int currentStatus =6;
	
	/**
	 * 当前值。
	 */
	public String currentValue;
	
	/**
	 * 上次状态。阈值产生的告警级别，6表示其值为正常，其它的分别对应为 1： threshold1, 2:threshold2, 3:threshold1, 4: threshold4
	 */
	public int lastStatus =6;
	
	/**
	 * 上次值。
	 */
	public String lastValue;
	
	/**
	 * 其适用的阈值定义。
	 */
	public String threshold;
	
	/**
	 * 数据采集的时间,用于判断当前是数据是否与前一条数据是否相同.
	 */
	public Timestamp  currentTime;
	
	/**
	 * 预留字段所用。
	 */
	public String reservedField1,reservedField2,reservedField3,reservedField4,reservedField5;
}
