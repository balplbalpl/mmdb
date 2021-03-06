/**
 * 
 */
package com.mmdb.service.mon.threshold.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aol_aog@163.com(James Gao)
 * 
 */
public class ThresholdExpression implements Serializable {

	/**
	 * serialUID.
	 */
	private static final long serialVersionUID = 8190099445610170991L;

	private String id;

	private String kpi;

	private String kpiClass;

	private String ci;

	private String instance;

	private String view;

	private String user;

	private String threshold1, threshold2, threshold3, threshold4, threshold5;

	private Timestamp updateTime;

	private BoundaryValue[] bvs;

	private boolean isCached;
	
	private String enabled ="Y";

	public ThresholdExpression() {
		bvs = new BoundaryValue[4];
		isCached = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKpi() {
		return kpi;
	}

	public void setKpi(String kpi) {
		this.kpi = kpi;
	}

	public String getCi() {
		return ci;
	}

	public void setCi(String ci) {
		this.ci = ci;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getThreshold1() {
		return threshold1;
	}

	public void setThreshold1(String threshold1) {
		if(threshold1 == null || threshold1.trim().equalsIgnoreCase("null"))
			this.threshold1 = "";
		this.threshold1 = threshold1;
	}

	public String getThreshold2() {
		return threshold2;
	}

	public void setThreshold2(String threshold2) {
		if(threshold2 == null || threshold2.trim().equalsIgnoreCase("null"))
			this.threshold2 = "";
		this.threshold2 = threshold2;
	}

	public String getThreshold3() {
		return threshold3;
	}

	public void setThreshold3(String threshold3) {
		if(threshold3 == null || threshold3.trim().equalsIgnoreCase("null"))
			this.threshold3 = "";
		this.threshold3 = threshold3;
	}

	public String getThreshold4() {
		return threshold4;
	}

	public void setThreshold4(String threshold4) {
		if(threshold4 == null || threshold4.trim().equalsIgnoreCase("null"))
			this.threshold4 = "";
		this.threshold4 = threshold4;
	}

	public String getThreshold5() {
		return threshold5;
	}

	public void setThreshold5(String threshold5) {
		if(threshold5 == null || threshold5.trim().equalsIgnoreCase("null"))
			this.threshold5 = "";
		this.threshold5 = threshold5;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getKpiClass() {
		return kpiClass;
	}

	public void setKpiClass(String kpiClass) {
		this.kpiClass = kpiClass;
	}

	public BoundaryValue[] getPortable() {
		if (isCached)
			return bvs;
		this.bvs[0] = this.parse(threshold1);
		this.bvs[1] = this.parse(threshold2);
		this.bvs[2] = this.parse(threshold3);
		this.bvs[3] = this.parse(threshold4);
		isCached = true;
		return bvs;
	}

	private BoundaryValue parse(String threshold) {
		if (threshold != null && !threshold.trim().equals("")) {
			BoundaryValue bv = new BoundaryValue(threshold);
			String[] pair = threshold.split(",");
			String below = pair[0].trim();
			String above = pair[1].trim();

			// 处理下边界值

			// 处理是否包含边界

			char bc = below.charAt(0);
			if (bc == '[') {
				bv.setIncludedBottom(true);
			}
			// 解析边界的值
			String belowValue = below.substring(1);
			bv.setBottomValue(Float.parseFloat(belowValue));

			// 处理上边界值

			// 处理是否包含边界

			char ac = above.charAt(above.length() - 1);
			if (ac == ']') {
				bv.setIncludedAbove(true);
			}
			// 解析边界的值
			String aboveValue = above.substring(0, above.length() - 1).trim();
			bv.setAboveValue(Float.parseFloat(aboveValue));

			return bv;
		} else {
			return null;
		}

	}

	public Map<String, Object> asMap() {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("id", this.id);
		retMap.put("ci", this.ci);
		retMap.put("kpi", this.kpi);
		retMap.put("instance", this.instance);
		retMap.put("kpiClass", this.kpiClass);
		retMap.put("view", this.view);
		retMap.put("threshold1", this.threshold1);
		retMap.put("threshold2", this.threshold2);
		retMap.put("threshold3", this.threshold3);
		retMap.put("threshold4", this.threshold4);
		retMap.put("threshold5", this.threshold5);
		retMap.put("updateTime", this.updateTime);
		retMap.put("enabled", this.enabled);
		return retMap;
	}

	public String getEnabled() {
		if(enabled == null || enabled.trim().equals("")) return "N";
		if(enabled.trim().toUpperCase().equals("Y")) return enabled.trim();
		else return "N";
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled.toUpperCase();
	}

}
