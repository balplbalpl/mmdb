/**
 * 
 */
package com.mmdb.service.mon.threshold.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author aol_aog@163.com(James Gao)
 * 
 */
public class ThresholdExpressionCache implements Serializable {

	/**
	 * serialUID.
	 */
	private static final long serialVersionUID = 3448947350439945378L;

	/**
	 * 当前缓存的key.
	 */
	private String key;

	/**
	 * 保存下级的阈值定义缓存 。
	 */
	private Map<String, ThresholdExpressionCache> cache;

	/**
	 * 保存当前级的阈值定义。
	 */
	private ThresholdExpression tExp;

	public ThresholdExpressionCache(String key) {
		this.key = key;

		this.cache = Collections
				.synchronizedMap(new LinkedHashMap<String, ThresholdExpressionCache>());
	}

	public String getKey() {
		return key;
	}

	public ThresholdExpression gettExp() {
		return tExp;
	}

	public void settExp(ThresholdExpression tExp) {
		this.tExp = tExp;
	}

	public ThresholdExpressionCache getCache(String key) {
		return cache.get(key);
	}

	public ThresholdExpressionCache[] getCaches() {
		return this.cache.values().toArray(new ThresholdExpressionCache[0]);
	}

	public void setCache(ThresholdExpressionCache _cache) {
		this.cache.put(_cache.getKey(), _cache);
	}

}
