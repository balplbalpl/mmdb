package com.mmdb.rest.icon;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang.StringUtils;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.utils.MD5;
import com.mmdb.core.utils.ZipUtil;
import com.mmdb.model.bean.Page;
import com.mmdb.model.icon.ViewIcon;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.icon.IImageService;
import com.mmdb.util.FileManager;

/**
 * 这个是专门管理分类使用的图标的,不支持单个图片上次,上次必须是zip格式,zip中的文件夹必须要和定义的组样式(config/demo/demo-
 * global .properties)一样 文件下的图片数量和名字必须是一样的.
 * 
 * @author xj
 * 
 */

public class ImageRest extends BaseRest {
	private IImageService imgService;
	private static List<String> themeName;

	static {
		themeName = new ArrayList<String>();
		ResourceBundle init = ResourceBundle
				.getBundle("config.demo.demo-global");
		String imageTheme = init.getString("imageTheme");

		String[] split = imageTheme.split(",");
		for (int i = 0; i < split.length; i++) {
			themeName.add(split[i]);
		}
	}

	@Override
	public void ioc(ApplicationContext context) {
		imgService = context.getBean(IImageService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if (param1 != null && "".equals(param1)) {
			// return getByName(param1);
		} else if ("export".equals(param1)) {// 导出全部,包含内容
			return exportData();
		} else if ("themes".equals(param1)) {
			return getThemes();
		} else if ("3dpathbytheme".equals(param1)) {
			String param2 = (String) getRequestAttributes().get("param2");
			if (param2 != null) {
				param2 = URLDecoder.decode(param2, "utf-8");
			}
			return getPathByTheme(param2);
		}
		return notFindMethod(null);
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if ("import".equals(param1)) {
			return importData(entity);
		}
		JSONObject params = parseEntity(entity);
		if ("3dpathsbynames".equals(param1)) {
			JSONArray names = params.getJSONArray("names");
			return get3DPathByName(names);
		} else if ("3dpathsbysvg".equals(param1)) {
			JSONArray paths = params.getJSONArray("paths");
			return get3DPathByPath(paths);
		}
		return query(params);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return clear();
		} else {// 删除一个属性
			return deleteByName(param1);
		}
	}

	/**
	 * 获取当前全部的主题
	 * 
	 * @return
	 * @throws MException
	 */
	private Representation getThemes() throws MException {
		JSONObject ret = new JSONObject();
		if (themeName == null || themeName.size() == 0) {
			throw new MException("主题配置异常");
		} else {
			ret.put("message", "获取主题成功");
			String baseDir3d = imgService.get3DBaseDir();
			String svgBaseDir = imgService.getSvgBaseDir();
			Map<String, List<String>> retData = new HashMap<String, List<String>>();
			for (String theme : themeName) {
				List<String> list = new ArrayList<String>();
				list.add(svgBaseDir + theme);
				list.add(baseDir3d + theme);
				retData.put(theme, list);
			}
			ret.put("data", retData);
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过主题获取该主题的所在相对位置
	 * 
	 * @param theme
	 * @return /resource/svg/:theme
	 * @throws MException
	 */
	private Representation getPathByTheme(String theme) throws MException {
		JSONObject ret = new JSONObject();
		boolean flag = false;
		for (String them : themeName) {
			if (them.contains(theme)) {
				flag = true;
			}
		}
		if (flag) {
			String dir = imgService.getSvgBaseDir();
			String dir3d = imgService.get3DBaseDir();
			Map<String, List<String>> retData = new HashMap<String, List<String>>();
			List<String> p = new ArrayList<String>();
			p.add(dir + theme);
			p.add(dir3d + theme);
			retData.put(theme, p);
			ret.put("data", retData);
			ret.put("message", "获取主题成功");
		} else {
			throw new MException("主题[" + theme + "]不存在");
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过3d图片的名称获取图片
	 * 
	 * @param paths
	 * @return
	 */
	private Representation get3DPathByName(List<String> names) {
		JSONObject ret = new JSONObject();
		Map<String, String> retData = new HashMap<String, String>();
		String baseDir = imgService.get3DBaseDir();
		String default3d = imgService.getDefault3D(null);
		String theme = imgService.getTheme();
		for (String name : names) {
			String rName = imgService.get3DByName(theme, name);
			if (rName != null) {
				retData.put(name, baseDir + theme + "/" + rName);
			} else {
				retData.put(name, default3d);
			}
		}
		ret.put("data", retData);
		ret.put("message", "获取3D路径成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 
	 * @param paths
	 * @return
	 */
	private Representation get3DPathByPath(List<String> paths) {
		JSONObject ret = new JSONObject();
		Map<String, Object> retData = new HashMap<String, Object>();

		String baseDir = imgService.get3DBaseDir();
		String default3d = imgService.getDefault3D(null);

		for (String path : paths) {
			// 路径 =...../3d/主题名称/文件名称.后缀
			int sub1 = path.lastIndexOf("/");
			if (sub1 == -1) {
				sub1 = path.lastIndexOf("\\");
			}
			String theme = null;
			String name = null;
			if (sub1 != -1) {
				String a = path.substring(0, sub1);// 包含主题部分
				String b = path.substring(sub1 + 1, path.length());// 包含文件名部分

				sub1 = a.lastIndexOf("/");
				if (sub1 == -1) {
					sub1 = a.lastIndexOf("\\");
				}
				int sub2 = b.lastIndexOf(".");
				// 拆解路径获取到的主题
				theme = a.substring(sub1 + 1, a.length());
				// 拆解路径获取到文件名称(不包含后缀)
				name = b.substring(0, sub2);
			} else {
				int sub2 = path.lastIndexOf(".");
				name = path.substring(0, sub2);
			}

			if (!themeName.contains(theme)) {
				theme = null;
			}

			String rName = imgService.get3DByName(theme, name);
			if (rName != null) {
				String base = baseDir + theme + "/" + rName;
				Map<String, String> st = new HashMap<String, String>();
				st.put("mtl", base + ".mtl");
				st.put("obj", base + ".obj");
				retData.put(path, st);
			} else {
				Map<String, String> st = new HashMap<String, String>();
				st.put("mtl", default3d + ".mtl");
				st.put("obj", default3d + ".obj");
				retData.put(path, st);
			}
		}
		ret.put("data", retData);
		ret.put("message", "获取3D路径成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导入主题,主题必须和配置(config/demo/demo-global.properties)中的数量名称一样,
	 * 每个主题的图片数量和名称必须是一样的.
	 * <p>
	 * 每次导入会删除原有的主题(mongo的也刪除),并将图片保存到
	 * 项目名称\resource\svg文件夹中.图片也保存到mongo数据库中,当图片不存在时由数据库备份到 目录上.
	 * 
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	private Representation importData(Representation entity) throws Exception {
		log.dLog("import icons");
		JSONObject ret = new JSONObject();
		DiskFileItemFactory factory = new DiskFileItemFactory();
		RestletFileUpload upload = new RestletFileUpload(factory);

		List<FileItem> items = null;
		try {
			items = upload.parseRepresentation(entity);
		} catch (FileUploadException e) {
			log.eLog(e);
		}

		String filename = "";
		FileItem fi = null;
		fi = items.get(0);
		String contentType = fi.getContentType();
		filename = fi.getName();

		if (filename == null || "".equals(filename)
				|| !filename.toLowerCase().endsWith("zip")) {
			throw new MException("文件格式有误");
		}
		File file = FileManager.getInstance().createFile(fi.getInputStream(),
				"image", "zip");
		File target = new File(file.getParent() + File.separator
				+ (filename.substring(0, filename.length() - 4))
				+ new Date().getTime());
		target.mkdir();
		ZipUtil.unZip2(file.getPath(), target.getPath());

		// 对与配置中的主题名称进行比对
		File[] list = target.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		boolean flag = true;
		List<String> NE = new ArrayList<String>();
		for (int i = 0; i < list.length; i++) {
			if (!themeName.contains(list[i].getName())) {
				NE.add(list[i].getName());
				flag = false;
			}
		}
		if (!flag) {
			ret.put("message", "主题与配置主题不一致");
			getResponse().setStatus(new Status(600));
			return new JsonRepresentation(ret.toString());
		}
		NE.clear();
		// 将主题的图片全部取出来,并比对每个主题下的图片名称数量是否相同
		// key为主题名称,value是主题文件
		Map<String, List<File>> tmp = new HashMap<String, List<File>>();
		// 用于做比较,取出一个作为标准用与其他主题比较
		String tName = null;
		List<File> tFiles = null;
		for (int i = 0; i < list.length; i++) {
			List<File> data = new ArrayList<File>();
			File c = list[i];
			File[] listFiles = c.listFiles();
			for (File cc : listFiles) {
				if (cc.isFile() && cc.getName().endsWith(".svg")) {
					data.add(cc);
				}
			}
			tName = c.getName();
			tFiles = data;
			tmp.put(tName, tFiles);
		}

		if (StringUtils.isEmpty(tName) || tFiles == null || tFiles.size() == 0) {
			ret.put("message", "未找到有效的主题配置");
			getResponse().setStatus(new Status(600));
			return new JsonRepresentation(ret.toString());
		}

		Set<String> keySet = tmp.keySet();
		for (String key : keySet) {
			if (!key.equals(tName)) {// 模板不需要比较
				List<File> cs = tmp.get(key);
				if (tFiles.size() != cs.size()) {
					flag = false;
				}
				for (File file2 : tFiles) {
					boolean t = false;
					for (File file3 : cs) {
						if (file2.getName().equals(file3.getName())) {
							t = true;
						}
					}
					if (!t) {
						flag = false;
						NE.add(file2.getName());
					}
				}
				if (!flag) {
					ret.put("message", "主题[" + tName + "]配置中" + NE.toString()
							+ "与主题[" + key + "]不一致");
					getResponse().setStatus(new Status(600));
					return new JsonRepresentation(ret.toString());
				}
			}
		}

		imgService.clear();
		save(tmp);
		imgService.copyToDesk();

		FileManager.getInstance().deleteAll(file);
		FileManager.getInstance().deleteAll(target);
		ret.put("message", "保存成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation exportData() {
		JSONObject ret = new JSONObject();
		File file = null;
		try {
			file = imgService.exportFile();
		} catch (Exception e) {
			log.eLog(e);
		}
		ret.put("message", "下载图标数据成功");
		JSONObject retData = new JSONObject();
		retData.put("url", file.getName());
		ret.put("data", retData);
		return new JsonRepresentation(ret.toString());
	}

	private void save(Map<String, List<File>> themes) {

		InputStream is = null;
		List<ViewIcon> icons = null;

		Set<String> keySet = themes.keySet();
		for (String key : keySet) {
			List<File> files = themes.get(key);
			icons = new ArrayList<ViewIcon>(files.size());

			for (File f : files) {
				try {
					is = new FileInputStream(f);
					// String content = BASE64.base64Icon("image/svg+xml", is);
					byte[] b = new byte[is.available()];
					is.read(b);
					String content = new String(b, "utf-8");
					b = null;
					ViewIcon i = new ViewIcon(f.getName(), key,
							"image/svg+xml", content, MD5.md5(content));
					icons.add(i);
				} catch (Exception e) {
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (Exception e2) {
						}
					}
				}
			}

			imgService.save(icons);
		}
	}

	private Representation query(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		int page = data.getInt("page");
		int pageSize = data.getInt("pageSize");
		String name = data.getString("like");
		Page<Map<String, Object>> content = imgService.fuzzyQuery(name, page,
				pageSize);
		List<Map<String, Object>> datas = content.getDatas();
		JSONObject retData = new JSONObject();
		retData.put("page", page);
		retData.put("pageSize", pageSize);
		retData.put("datas", datas);
		retData.put("count", content.getTotalCount());

		ret.put("data", retData);
		ret.put("message", "获取全部数据成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 
	 */
	private Representation clear() {
		JSONObject ret = new JSONObject();
		imgService.clear();
		ret.put("message", "清除成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteByName(String name) throws Exception {
		JSONObject ret = new JSONObject();
		// 前台上没带后缀的图片名称所以这里加个后缀
		if (name.indexOf(".svg") == -1) {
			name = name + ".svg";
		}
		imgService.deleteByName(name);
		return new JsonRepresentation(ret.toString());
	}
}
