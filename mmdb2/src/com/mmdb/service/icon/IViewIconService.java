package com.mmdb.service.icon;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.mmdb.model.bean.Page;
import com.mmdb.model.icon.ViewIcon;

public interface IViewIconService {
	public ViewIcon save(ViewIcon icon) throws IOException;

	public Page<Map<String, Object>> fuzzyQuery(String name, int page,
			int pageSize, String userName) throws Exception;

	public void deleteByName(String name, String userName) throws Exception;

	public ViewIcon getByName(String filename, String userName)
			throws Exception;

	public void deleteAll(String userName) throws Exception;

	public File getOwnIconPath(String userName);

	/**
	 * 更改用户头像.
	 * 
	 * @param userName
	 * @return
	 */
	public ViewIcon saveOrUpdateUserIcon(ViewIcon icon);

	/**
	 * 刷新一个用户的头像,保存到tomcate中
	 * 
	 * @param userName
	 */
	public void refreshUserIcon(String userName);
	/**
	 * 将用户上传的图片保存到tomcate中
	 * @param userName
	 */
	public void refreshOwnIcon(String userName);
	/**
	 * 刷新全部用户的头像,保存到tomcate中
	 */
	public void refreshAllUserIcon();
}
