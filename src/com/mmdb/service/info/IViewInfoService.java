package com.mmdb.service.info;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.mmdb.model.bean.Page;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.ViewCategory;
import com.mmdb.model.icon.ViewIcon;
import com.mmdb.model.info.ViewInformation;

/**
 * 视图 视图 - 服务层
 * 
 * @author XIE
 */
public interface IViewInfoService {
	/**
	 * 添加视图
	 * 
	 * @param info
	 *            视图视图对象
	 * @return
	 * @throws Exception
	 */

	public ViewInformation save(ViewInformation info) throws Exception;

	public ViewInformation getByName(String cateId, String name)
			throws Exception;

	public boolean exist(String cateId, String name) throws Exception;

	public ViewInformation getById(String id) throws Exception;

	/**
	 * 获取一个用户的全部视图(包含共有和私有的.)
	 * 
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public List<ViewInformation> getAllPrivateViewByUser(User user)
			throws Exception;

	/**
	 * 获取全部视图
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ViewInformation> getAll() throws Exception;

	public List<ViewInformation> getByids(List<String> ids) throws Exception;

	/**
	 * 获取一个用户的共有全部视图
	 * 
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public List<ViewInformation> getOpenViewByUser(String userName)
			throws Exception;

	/**
	 * 获取全部的共有视图的作者
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getAllOpenViewAuthor() throws Exception;

	public void deleteById(String id) throws Exception;

	/**
	 * 通过分类删除视图
	 * 
	 * @param vCates
	 * @throws Exception
	 */
	public void deleteByViewCategory(List<ViewCategory> vCates)
			throws Exception;

	// -----------------------------------------------------------//
	public ViewIcon saveBackground(ViewIcon icon) throws Exception;

	public void deleteBackgroundByName(String name, String userName)
			throws Exception;

	public Page<Map<String, Object>> fuzzyQueryBackground(String name,
			int page, int pageSize, String userName) throws Exception;

	public File getBackgroundPath(String userName);

	/**
	 * 高级查询
	 * 
	 * @param category
	 *            分类 null时获取所有
	 * @param mustExp
	 *            必备查询条件
	 * @param orExp
	 *            模糊查询条件
	 * @param extend
	 *            是否继承
	 * @return
	 * @throws Exception
	 */
	public List<ViewInformation> qureyByAdvanced(ViewCategory category,
			Map<String, String> mustExp, Map<String, String> orExp,
			boolean extend, User user) throws Exception;

	/**
	 * 模糊搜索,会查询所有的共有视图,和用户增加的私有视图
	 * 
	 * @param keyWord
	 *            关键字
	 * @return
	 */
	public List<ViewInformation> qureyFuzzy(String keyWord, User user)
			throws Exception;

	/**
	 * 刷新用户的背景图片
	 * 
	 * @param userName
	 */
	public void refreshBackground(String userName);

	public ViewInformation update(ViewInformation info) throws Exception;

	public String createSvg(String Hexname, String svg) throws Exception;

	public ViewIcon getBackgroundByName(String filename, String loginName)
			throws Exception;

	public List<String> getViewIdsByCate(String id) throws Exception;

	public Page<ViewInformation> getSubscriptionByIds(List<String> views,
			String username, int page, int pageSize, boolean containSelf)
			throws Exception;

	public int getViewCountByCategory(String cateId);

	public int getSoftDeleteCountByUser(String userName);

	public List<ViewInformation> getSoftDeleteByUser(String userName, int page,
			int pageSize) throws Exception;
	
	public void softDelete(String viewId) throws Exception;
}