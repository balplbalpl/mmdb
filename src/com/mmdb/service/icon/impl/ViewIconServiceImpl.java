package com.mmdb.service.icon.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.model.bean.Page;
import com.mmdb.model.icon.ViewIcon;
import com.mmdb.model.icon.storage.UserIconStorage;
import com.mmdb.model.icon.storage.ViewIconStorage;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.icon.IViewIconService;
import com.mmdb.util.FileManager;

@Service("viewIconService")
public class ViewIconServiceImpl implements IViewIconService {

	@Autowired
	private ViewIconStorage iconStorage;
	@Autowired
	private UserIconStorage userIconStorage;

	@Override
	public ViewIcon save(ViewIcon icon) throws IOException {
		File ownIconPath = getOwnIconPath(icon.getUsername());
		File file = new File(ownIconPath, icon.getName());
		FileManager.getInstance().copyFile(file, (byte[]) icon.getContent());
		return iconStorage.save(icon);
	}

	@Override
	public void deleteByName(String name, String userName) throws Exception {
		File ownIconPath = getOwnIconPath(userName);
		File file = new File(ownIconPath, name);
		file.delete();
		iconStorage.delete(name, userName);
	}

	@Override
	public void deleteAll(String userName) throws Exception {
		File file = getOwnIconPath(userName);
		FileManager.getInstance().deleteAll(file);
		iconStorage.deleteByUser(userName);
	}

	@Override
	public ViewIcon getByName(String filename, String userName)
			throws Exception {
		return iconStorage.getByName(filename, userName);
	}

	@Override
	public Page<Map<String, Object>> fuzzyQuery(String name, int page,
			int pageSize, String userName) throws Exception {
		name = name.trim().toLowerCase();
		File ownFile = getOwnIconPath(userName);

		File[] list = ownFile.listFiles();
		if (list.length == 0 || list.length < iconStorage.getCount(userName)) {
			refreshOwnIcon(userName);
			list = ownFile.listFiles();
		}

		List<Map<String, Object>> svgs = new ArrayList<Map<String, Object>>(
				list.length);
		for (File file : list) {
			String fileName = file.getName();
			String lowerName = fileName.toLowerCase();

			if (lowerName.indexOf(name.toLowerCase()) != -1 || "".equals(name)) {
				Map<String, Object> data = new HashMap<String, Object>();
				// String prefix = Tool.findPath("admin", "resource");
				data.put("url", "/resource/ownsvg/" + userName + "/" + fileName);
				// data.put("isUse", false);
				// if (useIcon.contains(fileName)) {
				// data.put("isUse", true);
				// }
				svgs.add(data);
			}
		}
		Collections.sort(svgs, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				String name1 = (String) o1.get("url");
				String name2 = (String) o2.get("url");
				return name1.compareTo(name2);
			};
		});
		int count = svgs.size();
		int start = (page - 1) * pageSize;
		start = start < 0 ? 0 : start;
		int end = page * pageSize;
		start = start > count ? count : start;
		end = end > count ? count : end;
		svgs = svgs.subList(start, end);

		Page<Map<String, Object>> ret = new Page<Map<String, Object>>();
		ret.setCount(svgs.size());
		ret.setDatas(svgs);
		ret.setPageSize(pageSize);
		ret.setStart(page);
		ret.setTotalCount(count);
		return ret;
	}

	@Override
	public File getOwnIconPath(String userName) {
		String realPath = Tool.getRealPath();
		realPath = realPath + "resource/ownsvg/" + userName + "/";
		File file = new File(realPath);
		if (!file.exists())
			file.mkdirs();
		return file;
	}

	@Override
	public void refreshOwnIcon(String userName) {
		try {
			List<ViewIcon> byUser = iconStorage.getByUser(userName);
			File ownIconPath = getOwnIconPath(userName);
			FileManager.getInstance().deleteAll(ownIconPath);
			ownIconPath.mkdirs();
			for (ViewIcon viewIcon : byUser) {
				File file = new File(ownIconPath, viewIcon.getName());
				FileManager.getInstance().copyFile(file,
						(byte[]) viewIcon.getContent());
			}

		} catch (Exception e) {
		}
	}

	@Override
	public ViewIcon saveOrUpdateUserIcon(ViewIcon icon) {
		return userIconStorage.update(icon);
	}

	@Override
	public void refreshUserIcon(String userName) {
		try {
			ViewIcon icon = userIconStorage.getByName(userName);
			String realPath = Tool.getRealPath();
			realPath = realPath + "resource/usericon/";
			File parent = new File(realPath);
			if (!parent.exists()) {
				parent.mkdirs();
			}
			File file = new File(parent, icon.getName());
			FileManager.getInstance()
					.copyFile(file, (byte[]) icon.getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void refreshAllUserIcon() {
		try {
			List<ViewIcon> all = userIconStorage.getAll();
			for (ViewIcon icon : all) {
				File file = new File("resource/usericon/", icon.getName());
				FileManager.getInstance().copyFile(file,
						(byte[]) icon.getContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
