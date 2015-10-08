package com.mmdb.service.category;

import java.util.List;

import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.ViewCategory;

/**
 * 视图分类 - 服务层
 * 
 * @author XIE
 */
public interface IViewCateService {

	/**
	 * 添加视图分类
	 * 
	 * @param category
	 *            视图分类
	 * @return
	 */

	public ViewCategory save(ViewCategory category) throws Exception;

	/**
	 * 判断一个视图分类是否存在
	 * 
	 * @param open
	 *            是否是共有视图
	 * @param name
	 *            分类的名称
	 * @param parentId
	 *            分类的父类id
	 * @return true 存在
	 */
	public boolean exist(boolean open, String username,String name, String parentId);

	/**
	 * 更新视图分类
	 * 
	 * @param category
	 * @throws Exception
	 */

	public ViewCategory update(ViewCategory category) throws Exception;

	public ViewCategory getById(String id) throws Exception;

	/**
	 * 
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public List<ViewCategory> getAllByUser(User user) throws Exception;

	public List<ViewCategory> getAllOpenViewCate() throws Exception;

	public List<ViewCategory> getAll() throws Exception;

//	public void deleteAllByUser(User user) throws Exception;

//	public void deleteAllOpenViewCate() throws Exception;

//	public void deleteAll() throws Exception;

	public void deleteById(String id) throws Exception;
}