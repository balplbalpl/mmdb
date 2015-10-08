package com.mmdb.service.info.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.utils.ProjectInfo;
import com.mmdb.core.utils.SvgUtil;
import com.mmdb.core.utils.SysProperties;
import com.mmdb.model.bean.Page;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.ViewCategory;
import com.mmdb.model.icon.ViewIcon;
import com.mmdb.model.icon.storage.BackgroundStorage;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.model.info.storage.ViewInfoStorage;
import com.mmdb.model.relation.storage.CiViewRelStorage;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.info.IViewInfoService;
import com.mmdb.service.subscription.ISubscriptionService;
import com.mmdb.util.FileManager;

/**
 * 视图数据 服务 - 实现类
 * 
 * @author XIE
 */
@Component("viewInfoService")
public class ViewInfoServiceImpl implements IViewInfoService {

	@Autowired
	private ViewInfoStorage vInfoStorage;
	@Autowired
	private CiViewRelStorage ciViewRelStorage;
	@Autowired
	private BackgroundStorage backgroundStorage;
	@Autowired
	private ISubscriptionService subscription;

	@Override
	public ViewInformation save(ViewInformation info) throws Exception {
		return buildSubscriber(vInfoStorage.save(info));
	}

	@Override
	public ViewInformation update(ViewInformation info) throws Exception {
		return buildSubscriber(vInfoStorage.update(info));
	}

	@Override
	public boolean exist(String cateId, String name) throws Exception {
		return vInfoStorage.exist(cateId, name);
	}

	@Override
	public ViewInformation getById(String id) throws Exception {
		return buildSubscriber(vInfoStorage.getById(id));
	}

	@Override
	public ViewInformation getByName(String cateId, String name)
			throws Exception {
		return buildSubscriber(vInfoStorage.getByName(cateId, name));
	}

	@Override
	public List<ViewInformation> getAll() throws Exception {
		return buildSubscriber(vInfoStorage.getAll());
	}

	@Override
	public List<ViewInformation> getAllPrivateViewByUser(User user)
			throws Exception {
		return buildSubscriber(vInfoStorage.getAllPrivateViewByUser(user
				.getLoginName()));
	}

	@Override
	public List<ViewInformation> getByids(List<String> ids) throws Exception {
		if (ids == null || ids.size() == 0)
			return null;
		StringBuffer sql = new StringBuffer("select * from Views where ");
		for (int i = 0, length = ids.size(); i < length; i++) {
			sql.append(" _id = '");
			sql.append(ids.get(i));
			sql.append("' or");
		}
		sql.delete(sql.length() - 2, sql.length());
		return buildSubscriber(vInfoStorage.query(sql.toString(), null));
	}

	@Override
	public Page<ViewInformation> getSubscriptionByIds(List<String> ids,
			String username, int page, int pageSize, boolean containSelf)
			throws Exception {
		StringBuffer sql = new StringBuffer("select * from Views where ");
		if (!containSelf) {
			sql.append(" `userName` != '").append(username).append("' and( ");
		}

		for (int i = 0, length = ids.size(); i < length; i++) {
			sql.append(" _id = '");
			sql.append(ids.get(i));
			sql.append("' or");
		}
		sql.delete(sql.length() - 2, sql.length());
		sql.append(" ) order by updateTime desc");
		int count = vInfoStorage.getCount(sql.toString());

		sql.append(" limit ").append((page - 1) * pageSize).append(",")
				.append(pageSize);

		Page<ViewInformation> p = new Page<ViewInformation>();
		p.setTotalCount(count);
		p.setDatas(buildSubscriber(vInfoStorage.query(sql.toString(), null)));
		return p;
	}

	// @Override
	// public List<ViewInformation> getAllOpenInfo() throws Exception {
	// return buildSubscriber(vInfoStorage.getAllOpenInfo());
	// }

	@Override
	public List<String> getAllOpenViewAuthor() throws Exception {
		return vInfoStorage.getAllOpenViewAuthor();
	}

	@Override
	public List<ViewInformation> getOpenViewByUser(String userName)
			throws Exception {
		return buildSubscriber(vInfoStorage.getOpenViewByUser(userName));
	}

	@Override
	public void deleteById(String id) throws Exception {
		vInfoStorage.deleteById(id);
		ciViewRelStorage.deleteByView(id);
		subscription.delete(id, true);
	}

	@Override
	public void deleteByViewCategory(List<ViewCategory> vCates)
			throws Exception {
		if (vCates != null) {
			// 清除相关联的订阅
			for (ViewCategory viewCategory : vCates) {
				List<String> viewIds = vInfoStorage
						.getViewIdsByCategoryId(viewCategory.getId());
				for (String viewId : viewIds) {
					ciViewRelStorage.deleteByView(viewId);
					subscription.delete(viewId, true);
				}
			}
			vInfoStorage.deleteByViewCategory(vCates);
		}
	}

	@Override
	public List<ViewInformation> qureyByAdvanced(ViewCategory category,
			Map<String, String> mustExp, Map<String, String> orExp,
			boolean extend, User user) throws Exception {
		boolean queryAll = true;// 当参数没有值的时候,就查询全部的.
		StringBuffer match = new StringBuffer("select * from Views where ");
		// 用于判断是否出现继承和是否有categroyid这个条件
		List<String> cgIds = new ArrayList<String>();
		if (category != null) {
			cgIds.add(category.getId());
			if (extend) {// ciCate是否继承
				List<ViewCategory> children = category.getAllChildren();
				for (ViewCategory child : children) {
					cgIds.add(child.getId());
				}
			}
		}

		if (cgIds.size() != 0) {
			queryAll = false;
			for (String cgid : cgIds) {
				match.append(" categoryId = '");
				match.append(cgid);
				match.append("' or");
			}
			match.delete(match.length() - 2, match.length());
		}

		// 必要字段
		if (mustExp != null && mustExp.size() > 0) {
			if (!queryAll) {// 出现了categoryid where n.xx =='xx' and
				match.append(" and");
			}
			queryAll = false;
			for (Entry<String, String> entry : mustExp.entrySet()) {
				match.append("`");
				match.append(entry.getKey());
				match.append("` like '");
				match.append(entry.getValue());
				match.append("' and");
			}
			match.delete(match.length() - 3, match.length());// 去掉一个多余的and
		}//

		if (orExp != null && orExp.size() > 0) {
			if (!queryAll) {// 出现了categoryid where n.xx =='xx' and
				if (!"*".equals(orExp.get("*"))) {
					match.append(" and( ");
				} else {
					match.append("  ");
				}
			}
			queryAll = false;
			for (Entry<String, String> entry : orExp.entrySet()) {
				match.append("`");
				match.append(entry.getKey());
				match.append("` like '");
				match.append(entry.getValue());
				match.append("' or");
			}
			match.delete(match.length() - 2, match.length());// 去掉一个多余的or
			match.append(")");
		}

		if (queryAll) {
			match.delete(match.length() - 6, match.length());// 将多出的where 删除掉
		}
		// match.append("return n");
		return buildSubscriber(vInfoStorage.query(match.toString(), null));
	}

	@Override
	public List<ViewInformation> qureyFuzzy(String keyWord, User user)
			throws Exception {
		List<ViewInformation> views = null;
		if (keyWord == null || keyWord.equals("") || keyWord.equals("*")) {
			if (user == null) {
				views = vInfoStorage.getAll();
			} else {
				List<Object> params = new ArrayList<Object>();
				params.add(true);
				params.add(false);
				params.add(user.getLoginName());
				String CQL = "select * from Views where isOpen = ? or isOpen = ? and userName = ? and status = 'NORMAL'";
				views = vInfoStorage.getViewsBySql(CQL, params, null);
			}
		} else {
			String[] attrs = { "name" };
			String trem = "";
			List<Object> params = new ArrayList<Object>();
			params.add(true);
			params.add(false);
			params.add(user.getLoginName());

			if (keyWord.startsWith("*") && keyWord.endsWith("*")) {
				keyWord = keyWord.replace("*", "%");
			}

			if (!keyWord.startsWith("%") && !keyWord.endsWith("%")) {
				keyWord = "%" + keyWord + "%";
			}

			for (int i = 0; i < attrs.length; i++) {
				if (i == attrs.length - 1) {
					trem += attrs[i] + " like ? ";
				} else {
					trem += attrs[i] + " like ? or ";
				}
				params.add(keyWord);
			}
			String CQL = "select * from Views where  status = 'NORMAL' and (isOpen = ? or isOpen = ? and userName = ? ) and ";
			CQL += trem;
			views = vInfoStorage.getViewsBySql(CQL, params, null);
		}
		return buildSubscriber(views);
	}

	@Override
	public String createSvg(String Hexname, String svg) throws IOException {
		String url = SysProperties.get("svg.url");
		String path;
		if (url.equals("resource")) {
			url = "/" + ProjectInfo.getProjectRealPathConvert();
			svg = this.regex(url, svg);
			path = "resource/thumbnail";
			url += path;
			path = "/" + path;
		} else {
			svg = this.regex(url, svg);
			path = "/thumbnail";
			url += path;
		}
		File file = new File(url);
		if (!file.exists() && !file.isDirectory()) {
			file.mkdir();
		}
		// String Hexname = HexString.encode(HexString.json2Str(nc.getName(),
		// name));
		url += "/" + Hexname + ".png";
		path = "/" + path + "/" + Hexname + ".png";
		if (path.indexOf("//") == 0) {
			path = path.substring(1);
		}
		InputStream ins = new ByteArrayInputStream(svg.getBytes("utf-8"));
		url = url.replace("resourceresource", "resource");
		if (path.indexOf("resource") == -1) {
			path = path.replace("thumbnail", "resource/thumbnail");
		}
		File destFile = new File(url);
		destFile.createNewFile();
		try {
			SvgUtil.convertSvgFile2Png(ins, destFile, 400, 300);
		} catch (Exception e) {
		}
		return path;
	}

	// /---------------------------------------------------------------------------------//
	// /---------------------------------------------------------------------------------//
	// /--------------------------------背景图片管理----------------------------------------//
	// /---------------------------------------------------------------------------------//

	@Override
	public ViewIcon saveBackground(ViewIcon icon) throws IOException {
		File ownIconPath = getBackgroundPath(icon.getUsername());
		File file = new File(ownIconPath, icon.getName());
		FileManager.getInstance().copyFile(file, (byte[]) icon.getContent());
		return backgroundStorage.save(icon);
	}

	@Override
	public File getBackgroundPath(String userName) {
		String realPath = Tool.getRealPath();
		realPath = realPath + "resource/background/" + userName + "/";
		File file = new File(realPath);
		if (!file.exists())
			file.mkdirs();
		return file;
	}

	@Override
	public void deleteBackgroundByName(String name, String userName)
			throws Exception {
		File ownIconPath = getBackgroundPath(userName);
		File file = new File(ownIconPath, name);
		file.delete();
		backgroundStorage.delete(name, userName);
	}

	@Override
	public ViewIcon getBackgroundByName(String filename, String loginName)
			throws Exception {
		return backgroundStorage.getByName(filename, loginName);
	}

	@Override
	public Page<Map<String, Object>> fuzzyQueryBackground(String name,
			int page, int pageSize, String userName) throws Exception {
		name = name.trim().toLowerCase();
		File ownFile = getBackgroundPath(userName);

		File[] list = ownFile.listFiles();
		if (list.length == 0
				|| list.length < backgroundStorage.getCount(userName)) {
			refreshBackground(userName);
			list = ownFile.listFiles();
		}

		List<Map<String, Object>> svgs = new ArrayList<Map<String, Object>>(
				list.length);
		for (File file : list) {
			String fileName = file.getName();
			String lowerName = fileName.toLowerCase();

			if (lowerName.indexOf(name.toLowerCase()) != -1 || "".equals(name)) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("url", "/resource/background/" + userName + "/"
						+ fileName);
				int[] widthAndHeight = backgroundStorage.getWidthAndHeight(
						fileName, userName);
				data.put("width", widthAndHeight[0]);
				data.put("heigth", widthAndHeight[1]);
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
	public List<String> getViewIdsByCate(String id) throws Exception {
		return vInfoStorage.getViewIdsByCategoryId(id);
	}

	@Override
	public void refreshBackground(String userName) {
		try {
			List<ViewIcon> byUser = backgroundStorage.getByUser(userName);
			File backgroundPath = getBackgroundPath(userName);
			FileManager.getInstance().deleteAll(backgroundPath);
			backgroundPath.mkdirs();
			for (ViewIcon viewIcon : byUser) {
				File file = new File(backgroundPath, viewIcon.getName());
				FileManager.getInstance().copyFile(file,
						(byte[]) viewIcon.getContent());
			}
		} catch (Exception e) {
		}
	}

	@Override
	public int getViewCountByCategory(String cateId) {
		return vInfoStorage.getCountByCategory(cateId);
	}

	/**
	 * 正则去替换svg的路径
	 * 
	 * @param str
	 * @return
	 */
	private String regex(String url, String str) throws IOException {
		String gifUrl = url;
		url = "file:///" + url;
		Pattern pattern = Pattern.compile("xlink:href=\"(.*?)\"");
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String st = matcher.group(1);
			if (st.indexOf("resource") != -1) {
				if (st.indexOf("resource/svg/") != -1) {
					st = st.substring(st.indexOf("resource/svg/"));
				} else if (st.indexOf("resource/ownsvg/") != -1) {
					st = st.substring(st.indexOf("resource/ownsvg/"));
				} else if (st.indexOf("resource/background/") != -1) {
					st = st.substring(st.indexOf("resource/background/"));
				}
			} else if (st.indexOf("mxGraph") != -1) {
				st = st.substring(st.indexOf("graph/plugins/"));
			} else if (st.indexOf("graph/images") != -1) {
				st = st.substring(st.indexOf("graph/"));
			}
			String fileurl;
			if (st.substring(st.length() - 3).toLowerCase().equals("gif")) {
				String filePath = ProjectInfo.getProjectRealPathConvert()
						+ "resource/thumbnail/" + System.currentTimeMillis()
						+ ".png";
				ImageIO.write(ImageIO.read(new File(gifUrl + st)), "png",
						new File(filePath));
				fileurl = "file:////" + filePath;
			} else {
				fileurl = url + st;
			}
			fileurl = fileurl.replace("resourceresource", "resource");
			matcher.appendReplacement(sb, "xlink:href=\"" + fileurl + "\"");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private List<ViewInformation> buildSubscriber(List<ViewInformation> infos)
			throws Exception {
		for (ViewInformation info : infos) {
			buildSubscriber(info);
		}
		return infos;
	}

	private ViewInformation buildSubscriber(ViewInformation info)
			throws Exception {
		List<String> subscribers = subscription.getSubscriberByView(info
				.getId());
		info.setSubscripers(subscribers);
		return info;
	}

	@Override
	public List<ViewInformation> getSoftDeleteByUser(String userName, int page,
			int pageSize) throws Exception {
		List<ViewInformation> softDeleteViewByUser = vInfoStorage
				.getSoftDeleteViewByUser(userName, (page - 1) * pageSize,
						pageSize);
		return buildSubscriber(softDeleteViewByUser);
	}

	@Override
	public int getSoftDeleteCountByUser(String userName) {
		return vInfoStorage.getSoftDeleteViewCountByUser(userName);
	}
	
	@Override
	public void softDelete(String viewId) throws Exception {
		vInfoStorage.softDelete(viewId);
		subscription.delete(viewId, true);
	}
}