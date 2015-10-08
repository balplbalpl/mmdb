package com.mmdb.service.role;

import java.util.List;

import com.mmdb.model.bean.Module;

public interface IModuleService {
	
	/**
	 * 添加功能模块
	 * @param module
	 * @return
	 */
	public boolean addModule(Module module);
	
	/**
	 * 修改功能模块
	 * @param module
	 * @return
	 */
	public boolean updateModule(Module module);
	
	/**
	 * 根据功能模块名称,删除功能模块
	 * @param name
	 * @return
	 */
	public boolean deleteModule(String name);
	
	/**
	 * 删除父节点下的所有模块
	 * @param parentName
	 * @return
	 */
	public boolean deleteModuleByParent(String parentName);
	
	/**
	 * 根据模块名获取功能模块
	 * @param name
	 * @return
	 */
	public Module getModuleByName(String name);
	
	/**
	 * 获取父节点下的所有模块
	 * @param parentName
	 * @return
	 */
	public List<Module> getModuleByParent(String parentName);
	
	/**
	 * 获取所有模块
	 * @return
	 */
	public List<Module> getAllModule();
	
	/**
	 * 统计功能模块
	 * @return
	 */
	public int getCountModule();

	/**
	 * 获取父节点下的所有模块目录
	 * @param parentName
	 * @return
	 */
	List<Module> getFolderByParent(String parentName);

	/**
	 * 获取所有模块（分页）
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Module> getAllModule(int start, int limit);

	/**
	 * 获取父节点下所有模块（分页）
	 * @param name
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Module> getModuleByParent(String name, int start, int limit);

	/**
	 * 统计父节点下所有模块
	 * @param parentName
	 * @return
	 */
	int getCountModuleByParentName(String parentName);
	
}
