package com.mmdb.service.info;

import java.util.List;

import com.mmdb.model.bean.User;
import com.mmdb.model.info.ViewPortfolio;

/**
 * 组合视图服务层
 * 
 * @author XIE
 */
public interface IViewPortfolioService {
	/**
	 * 保存组合视图
	 * 
	 * @param view
	 *            视图数据对象
	 * @return
	 * @throws Exception
	 */

	public ViewPortfolio save(ViewPortfolio view) throws Exception;

	/**
	 * 更新组合视图
	 * 
	 * @param view
	 *            需要更新的视图
	 * @return
	 * @throws Exception
	 */

	public ViewPortfolio update(ViewPortfolio view) throws Exception;

	/**
	 * 查询组合视图
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public ViewPortfolio getById(String id) throws Exception;

	/**
	 * 获取所有视图
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ViewPortfolio> getAll() throws Exception;

	/**
	 * 删除视图
	 * 
	 * @param view
	 *            视图
	 * @throws Exception
	 */
	public void deleteById(String id) throws Exception;

	/**
	 * 删除组合视图
	 * 
	 * @return
	 * @throws Exception
	 */
	public void deleteAll() throws Exception;

	public void deleteAllOpenViewPort() throws Exception;

	public void deleteAllByUser(User user) throws Exception;

	public List<ViewPortfolio> getAllOpenViewPort() throws Exception;

	public List<ViewPortfolio> getAllByUser(User user) throws Exception;

	public boolean exist(boolean open, String name, String userName);

	public List<ViewPortfolio> getAllOpenViewPortByUser(String loginName) throws Exception;

	public List<String> getAllOpenViewPorAuthor() throws Exception;
}
