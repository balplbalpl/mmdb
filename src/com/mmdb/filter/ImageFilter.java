package com.mmdb.filter;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.mortbay.log.Log;

import com.mmdb.common.Global;
import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.model.categroy.ViewCategory;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.icon.IImageService;
import com.mmdb.service.icon.IViewIconService;
import com.mmdb.service.info.IViewInfoService;
import com.mmdb.service.role.IRoleService;
import com.mmdb.service.role.IUserService;
import com.mmdb.util.HexString;
import com.mmdb.util.des.Des;

/**
 * 图片过滤器,所有的图片资源必须放在 /resource文件夹下面
 * 
 * @author Gemu
 * @time 2015年8月21日 下午4:24:59
 */
public class ImageFilter implements Filter {

	private String path;

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		Global.projectPort = request.getLocalPort();
		// 判断请求的资源路径是否在项目中存在
		String url = request.getRequestURL().toString();
		url = URLDecoder.decode(url, "utf-8");
		String name = null;

		// 获取名称
		int param = url.lastIndexOf("?");
		if (param > 0) {
			name = url.substring(url.lastIndexOf("/") + 1, param);
		} else {
			name = url.substring(url.lastIndexOf("/") + 1);
		}
		// System.err.println(name);

		// 获取路径
		int index = url.indexOf("resource");
		int checkfile = name.indexOf(".");
		if (index > 0 && checkfile > 0) {
			// System.out.println(url.substring(index));
			String t = url.substring(index);
			File f = new File(path + t);
			if (f.exists()) {
				chain.doFilter(req, resp);
				return;
			}

			t = t.substring(9);
			String seDir = null;
			int indexOf = t.indexOf("/");
			if (indexOf > 0) {
				seDir = t.substring(0, indexOf);
			}
			String username = null;
			String token = ((HttpServletRequest) req).getHeader("token");
			if (token != null) {
				String userpwd = des.decrypt(token);
				String[] strs = userpwd.split("\\|");
				username = strs[0];
			}

			if (seDir == null) {
				// 指向默认的图片
			} else if (seDir.equals("svg")) {
				if (svgService != null)
					svgService.copyToDesk();
			} else if (seDir.equals("usericon")) {
				if (username == null) {
					username = name.substring(0, name.indexOf("."));
				}
				if (iconService != null)
					iconService.refreshUserIcon(username);
			} else if (seDir.equals("background")) {
				if (username != null) {
					if (vInfoService != null)
						vInfoService.refreshBackground(username);
				}
			} else if (seDir.equals("ownsvg")) {
				if (username != null) {
					if (iconService != null)
						iconService.refreshOwnIcon(username);
				}
			}
			// 刷新后图片还不存在,给默认值
			if (!f.exists()) {
				req.getRequestDispatcher("/resource/404.png")
						.forward(req, resp);
			}

		} else {
			chain.doFilter(req, resp);
		}

	}

	private Des des = new Des();
	private IViewInfoService vInfoService;
	private IUserService userService;
	private IRoleService roleService;
	private IImageService svgService;
	private IViewIconService iconService;

	@Override
	public void init(FilterConfig config) throws ServletException {
		Log.info("初始化图片过滤器!");
		path = Tool.getRealPath();
		vInfoService = (IViewInfoService) SpringContextUtil
				.getBean("viewInfoService");
		userService = (IUserService) SpringContextUtil.getBean("userService");
		roleService = (IRoleService) SpringContextUtil.getBean("roleService");
		svgService = (IImageService) SpringContextUtil.getBean("imageService");
		iconService = (IViewIconService) SpringContextUtil
				.getBean("viewIconService");
	}

	/**
	 * 刷新全部的视图缩略图
	 */
	private void refreshGraphSvg() {
		try {
			List<ViewInformation> all = vInfoService.getAll();
			for (ViewInformation viewInformation : all) {
				String svg = viewInformation.getSvg();
				ViewCategory nc = viewInformation.getCategory();
				String Hexname = HexString.encode(HexString.json2Str(
						nc.getName(), viewInformation.getName()));
				vInfoService.createSvg(Hexname, svg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
