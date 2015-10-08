/**
 * 
 */
package com.mmdb.service.mon.threshold;

import java.io.Serializable;

import com.mmdb.service.mon.threshold.model.BoundaryValue;
import com.mmdb.service.mon.threshold.model.PerfData;
import com.mmdb.service.mon.threshold.model.ThresholdExpression;

/**
 * @author aol_aog@163.com(James Gao)
 * 
 */
public class ThresholdHandler implements Serializable {

	/**
	 * serialUID.
	 */
	private static final long serialVersionUID = -2384943161072142093L;

	/**
	 * 
	 */
	public ThresholdHandler() {

	}

	/**
	 * 处理指定的ci,instance,kpi其值是否超过预定义的阈值。
	 * 
	 * @param te
	 * @param value
	 * @return 返回阈值产生的告警级别，6表示其值为正常，其它的分别对应为critical 1： threshold1,
	 *         major,2:threshold2, minor,3:threshold1, info,4: threshold4
	 */
	public int handler(ThresholdExpression te, PerfData pd) {
		if (pd.value == null || pd.value.trim().equals(""))
			return 6;
		// 不是数值，也返回-1;
		try {
			float v = Float.parseFloat(pd.value);
			BoundaryValue[] bvs = te.getPortable();
			// 从最严重级别依次开始判断。
			int status = -1;
			for (int i = 0; i < bvs.length; i++) {
				BoundaryValue bv = bvs[i];
				if (bv != null) {
					status = p(bv, v);
					if (status > 0) {
						pd.threshold = bv.getThreshold();
						return i + 1;
					}
				}

			}
		} catch (Exception ex) {
			return 6;
		}

		return 6;
	}

	/**
	 * 
	 * @param bv
	 * @param value
	 * @return
	 */
	private int p(BoundaryValue bv, float value) {
		int result = Float.compare(bv.getAboveValue(), value);
		if (bv.isIncludedAbove()) {
			if (result == 0) //上边界闭区间
				return 1;
		}

		if (result == 1) {
			// 判断是否大于下边界
			result = Float.compare(bv.getBottomValue(), value);
			if (bv.isIncludedBottom()) {
				if (result <= 0)
					return 1;					
			} else {
				if (result < 0)
					return 1;
			}
		}

		return -1;
	}
}
