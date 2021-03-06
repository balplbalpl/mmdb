package com.mmdb.service.ruleEngine;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface IRuleEngineService {
	/**
	 * 获取规则树
	 * 
	 * **/
	public JSONArray getTree() throws Exception;

	/**
	 * 获取规则节点
	 * 
	 * **/
	public JSONObject getTreeNodeById(String id) throws Exception;

	/**
	 * 添加规则节点
	 * 
	 * **/
	public JSONArray addTreeNode(String id, String name, JSONObject rule)
			throws Exception;

	/**
	 * 删除规则节点
	 * 
	 * **/
	public JSONArray delTreeNode(String id) throws Exception;

	/**
	 * 获取CategoryIds
	 * 
	 * **/
	public JSONArray getCategoryIds() throws Exception;

	/**
	 * 获取CI属性
	 * 
	 * **/
	public JSONArray getCiAttrs(String categoryId) throws Exception;

	/**
	 * 获取indis
	 * 
	 * **/
	public JSONArray getIndis() throws Exception;

	/**
	 * 添加indi
	 * 
	 * **/
	public void addIndi(String indi) throws Exception;

	// public List<String[]> exportPerfRule();

	public List<String[]> exportRule(String path);

	public String importPerfRule(List<String[]> rules);

	public String importEventRule(List<String[]> rules);

	/**
	 * 获取全部的rule名称,多层的rule名称会被合并.
	 * 
	 * @param rulePath
	 * @return [0]名称;[1]最后一层的路径;[2]=null没有val-kpi,="ok"正常的,="multiple"多层的.
	 */
	public List<String[]> getRuleDatas(String rulePath);

	/**
	 * 
	 * @param ruleName
	 *            对应的rule名字
	 * @param rulePath
	 *            必须给最深的路径, 一个标准的 /rule/perf/a/b
	 * @return	map<String,String>
	 */
	public JSONObject getRuleByName(String ruleName, String rulePath);
	
	/**
	 * 给定一个path然后删除
	 * @param rulePath
	 * @return
	 */
	public boolean delete(String rulePath);
	
	public boolean insertRule(String rulePath,JSONArray rule);
}
