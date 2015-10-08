package com.mmdb.service.mon.threshold.model;

import java.io.Serializable;

/**
 * 描述数值类型性能指标取值范围的边界值。
 * 
 * @author James Gao
 * @version 1.0 2014-6-6
 */
public class BoundaryValue implements Serializable {

	/**
	 * serialUID.
	 */
	private static final long serialVersionUID = -5476354531776799448L;

	/**
	 * 描述边界值的数值。
	 */
	private float bottomValue, aboveValue;

	/**
	 * 描述取值范围是否包含上边界值。
	 */
	private boolean isIncludedAbove;

	/**
	 * 描述取值范围是否包含下边界值。
	 */
	private boolean isIncludedBottom;
	
	private String threshold;
	
	public BoundaryValue(String threshold){
		this.threshold = threshold;
	}

	
	public boolean isIncludedAbove() {
		return isIncludedAbove;
	}

	public void setIncludedAbove(boolean isIncludedAbove) {
		this.isIncludedAbove = isIncludedAbove;
	}

	public boolean isIncludedBottom() {
		return isIncludedBottom;
	}

	public void setIncludedBottom(boolean isIncludedBottom) {
		this.isIncludedBottom = isIncludedBottom;
	}

	public String getThreshold() {
		return threshold;
	}


	public float getBottomValue() {
		return bottomValue;
	}


	public void setBottomValue(float bottomValue) {
		this.bottomValue = bottomValue;
	}


	public float getAboveValue() {
		return aboveValue;
	}


	public void setAboveValue(float aboveValue) {
		this.aboveValue = aboveValue;
	}

}
