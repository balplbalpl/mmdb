package com.mmdb.service.icon;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.mmdb.model.bean.Page;
import com.mmdb.model.icon.ViewIcon;

/**
 * 用于图标库管理
 * 
 * @author xj
 * 
 */
public interface IImageService {

	public List<ViewIcon> getAll();

	public ViewIcon getById(String id) throws Exception;

	public ViewIcon getByName(String name) throws Exception;

	public Page<Map<String, Object>> fuzzyQuery(String name, int page,
			int pageSize) throws Exception;

	/**
	 * 保存主题中的图片
	 * 
	 * @param icons
	 */
	public void save(List<ViewIcon> icons);

	/**
	 * 将svg图片从数据库中保存到指定的位置 /mmdb2/resource 中
	 */
	public void copyToDesk();

	/**
	 * 删除全部的image
	 */
	public void clear();

	public boolean checkVersion() throws Exception;

	public String getSvgPath();

	public String getTheme();

	public File exportFile();

	/**
	 * 不包含主题的文件所在路径
	 * 
	 * @return "/resource/svg/"
	 */
	public String getSvgBaseDir();

	public String get3DBaseDir();

	/**
	 * 通过名称删除图片,要保证图片没有被使用
	 * 
	 * @param name
	 * @return
	 */
	public void deleteByName(String name) throws Exception;

	/**
	 * 通过一组存在的svg图片找到对应名字的3d图片位置
	 * 
	 * @param names
	 * @return ['/resource/3d',]
	 * @throws Exception
	 */
	public List<String> get3dPathByNames(List<String> names) throws Exception;

	/**
	 * 通过名称获取一个3D模型,不存在返回null
	 * 
	 * @param name
	 *            没有后缀的文件名
	 * @return
	 */
	public String get3DByName(String theme,String name);

	/**
	 * 获取默认的3d模型位置
	 * 
	 * @return /resource/3d/theme/xxx.xxx
	 */
	public String getDefault3D(String theme);
}
