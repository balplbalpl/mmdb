package com.mmdb.service.icon.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.core.utils.SysProperties;
import com.mmdb.core.utils.ZipUtil;
import com.mmdb.model.bean.Page;
import com.mmdb.model.icon.ViewIcon;
import com.mmdb.model.icon.storage.ImageStorage;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.category.ICiCateService;
import com.mmdb.service.icon.IImageService;
import com.mmdb.util.FileManager;

@Service("imageService")
public class ImageServiceImpl implements IImageService {

	@Autowired
	private ImageStorage imageStorage;
	@Autowired
	private ICiCateService ciCateService;
	private String theme;
	private final String svgBaseDir = "resource/svg/";
	private final String svg3DBaseDir = "resource/3d/";
	private final String defualt3D;

	public ImageServiceImpl() {
		theme = SysProperties.get("svg.base");
		defualt3D = SysProperties.get("svg3d.default");
	}

	@Override
	public List<ViewIcon> getAll() {
		return imageStorage.getAll();
	}

	@Override
	public ViewIcon getById(String id) throws Exception {
		return imageStorage.getById(id);
	}

	@Override
	public ViewIcon getByName(String name) throws Exception {
		return imageStorage.getByName(getTheme(), name);
	}

	@Override
	public String getSvgBaseDir() {
		return "/" + svgBaseDir;
	}

	@Override
	public String get3DBaseDir() {
		return "/" + svg3DBaseDir;
	}

	// @Override
	// public ViewIcon save(ViewIcon img) throws Exception {
	// return imageStorage.save(img);
	// }

	@Override
	public Page<Map<String, Object>> fuzzyQuery(String name, int page,
			int pageSize) throws Exception {
		name = name.trim();
		boolean checkVersion = checkVersion();
		if (checkVersion) {
			String svgPath = getSvgPath();
			File themeFile = new File(svgPath + getTheme());
			File[] list = themeFile.listFiles();
			List<Map<String, Object>> svgs = new ArrayList<Map<String, Object>>(
					list.length);
			List<String> useIcon = ciCateService.getUseImages();
			for (File file : list) {
				String fileName = file.getName();
				String lowerName = fileName.toLowerCase();

				if (lowerName.indexOf(name.toLowerCase()) != -1
						|| "".equals(name)) {
					Map<String, Object> data = new HashMap<String, Object>();
					// String prefix = Tool.findPath("admin", "resource");
					data.put("url", svgBaseDir + getTheme() + "/"
							+ fileName);
					data.put("isUse", false);
					if (useIcon.contains(fileName)) {
						data.put("isUse", true);
					}
					svgs.add(data);
				}

			}
			Collections.sort(svgs, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> o1,
						Map<String, Object> o2) {
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
		return null;
	}

	@Override
	public String getTheme() {
		return theme;
	}

	@Override
	public void save(List<ViewIcon> icons) {
		imageStorage.save(icons);
	}

	@Override
	public void clear() {
		String path = getSvgPath();
		// 清理tomcat中的数据
		File svgFile = new File(path);
		if (svgFile.isDirectory()) {
			FileManager.getInstance().deleteAll(svgFile);
		}
		// 清理数据库
		imageStorage.clear();
	}

	@Override
	public void copyToDesk() {
		String svgPath = getSvgPath();
		File svgFile = new File(svgPath);
		if (!svgFile.exists()) {
			svgFile.mkdirs();
		}

		String version = imageStorage.getVersion();

		File verFile = new File(svgFile, "version");
		try {
			copyFile(verFile, version.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		List<ViewIcon> icons = getAll();
		Map<String, List<ViewIcon>> datas = new HashMap<String, List<ViewIcon>>();
		for (ViewIcon viewIcon : icons) {
			String theme = viewIcon.getUsername();
			List<ViewIcon> is = datas.get(theme);
			if (is == null) {
				is = new ArrayList<ViewIcon>();
				datas.put(theme, is);
			}
			is.add(viewIcon);
		}
		Set<String> themeSet = datas.keySet();
		for (String theme : themeSet) {
			List<ViewIcon> list = datas.get(theme);
			File themeFile = new File(svgFile, theme);
			if (!themeFile.exists()) {
				themeFile.mkdirs();
			}
			for (ViewIcon viewIcon : list) {
				String name = viewIcon.getName();
				byte[] data = null;
				try {
					data = viewIcon.getContent().toString().getBytes("utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				File file = new File(themeFile, name);
				copyFile(file, data);
			}
		}
	}

	/**
	 * 返回svg文件的本地位置 例如 e:/xxx/resource/svg
	 * 
	 * @return
	 */
	@Override
	public String getSvgPath() {
		return Tool.getRealPath() + svgBaseDir;
	}

	/**
	 * 返回3D文件的本地位置 例如 e:/xxx/resource/3d
	 * 
	 * @return
	 */
	public String get3DPath() {
		return Tool.getRealPath() + svg3DBaseDir;
	}

	@Override
	public boolean checkVersion() throws Exception {
		String version = imageStorage.getVersion();
		if (version == null) {
			throw new Exception("版本信息不存在,请重新上传图标");
		}
		String svgPath = getSvgPath();
		File verFile = new File(svgPath + "version");
		if (!verFile.exists()) {
			// throw new Exception("版本信息不存在,请重新上传图标");
			FileManager.getInstance().deleteAll(new File(svgPath));
			copyToDesk();
			return true;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(verFile);
			byte[] b = new byte[is.available()];
			is.read(b);
			String v1 = new String(b, "utf-8");
			if (version.equals(v1)) {
				return true;
			}
		} catch (Exception e) {
			throw new Exception("版本信息不存在,请重新上传图标");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return true;
	}

	//
	private void copyFile(File target, byte[] data) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(target);
			fos.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void deleteByName(String name) throws Exception {

		List<String> useImages = ciCateService.getUseImages();
		if (useImages.contains(name))
			throw new Exception("图标[" + name + "]正在使用");

		String svgPath = getSvgPath();
		File svgFile = new File(svgPath);
		File[] themeFiles = svgFile.listFiles();
		for (File file : themeFiles) {
			file = new File(file, name);
			file.delete();
		}
		imageStorage.deleteByName(name);
	}

	@Override
	public File exportFile() {
		String svgPath = getSvgPath();
		File retFile = FileManager.getInstance().createFile("图标", "zip");
		try {
			ZipUtil.zip(retFile.getPath(), new File(svgPath), false);
		} catch (IOException e) {
		}
		return retFile;
	}

	@Override
	public List<String> get3dPathByNames(List<String> names) throws Exception {
		List<String> name3D = new ArrayList<String>();
		List<String> ret = new ArrayList<String>();

		// 判断3d模型是否存在,不存在就默认
		File file = new File(get3DPath() + theme);
		File[] listFiles = file.listFiles();

		// 默认的3d模型,当图形不存在时使用默认的.
		String def = get3DBaseDir() + listFiles[0].getName();

		// 过滤掉文件夹
		for (File file2 : listFiles) {
			if (file2.isFile()) {
				name3D.add(file2.getName());
			}
		}

		file = null;
		listFiles = null;
		// 进行比较
		for (String name : names) {
			boolean exist = false;
			for (String name3d : name3D) {
				int end = name3d.lastIndexOf(".");
				if (end == -1)
					continue;
				String t = name3d.substring(0, end);
				if (t.equals(name)) {
					exist = true;
					ret.add(get3DBaseDir() + name3d);
					break;
				}
			}
			if (!exist) {
				ret.add(def);
			}
		}
		return ret;
	}

	@Override
	public String get3DByName(String theme, String name) {
		boolean mtl = false, obj = false;
		if (theme == null)
			theme = this.theme;
		name = name.toLowerCase();
		File f = new File(get3DPath() + theme);
		File[] listFiles = f.listFiles();
		for (File file : listFiles) {
			if (file.isFile()) {
				String fileName = file.getName();
				String eName = fileName.substring(0, fileName.lastIndexOf("."));
				if (name.equals(eName.toLowerCase())) {
					if (fileName.endsWith("mtl"))
						mtl = true;
					if (fileName.endsWith("obj"))
						obj = true;
					if (mtl && obj)
						return eName;
				}
			}
		}
		return null;
	}

	@Override
	public String getDefault3D(String theme) {
		if (theme == null)
			theme = this.theme;
		return get3DBaseDir() + theme + "/" + defualt3D;
	}

	public static void main(String[] args) {
		File f = new File("D:\\Program Files");
		String[] list = f.list();
		System.out.println(list[0]);
		String name = "aaa.tml";
		name = name.substring(0, name.lastIndexOf("."));
		System.out.println(name);
	}
}
