package com.mmdb.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.model.bean.VitiationToken;
import com.mmdb.service.role.ITokenService;
import com.mmdb.service.role.IUserService;
import com.mmdb.service.role.impl.TokenService;
import com.mmdb.service.role.impl.UserService;
import com.mmdb.util.des.Des;

public class RestFilter implements Filter {

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
		boolean flag = true;
		String message = "请先登录!";
		String path = req.getServletPath() + req.getPathInfo();
		if (!isAuth) {
			chain.doFilter(request, response);
		} else {
			//下载文件时浏览器无法给token.所以下载不拦截
			if (path.equals("/rest/login")||path.indexOf("/rest/download/")!=-1) {
				chain.doFilter(request, response);
			} else {
				String token = req.getHeader("token");
				if (token == null) {
					flag = false;
				} else {
					VitiationToken vitiationToken = tokenService
							.getVitiationToken(token);
					if (vitiationToken == null) {// 判断是否是注销的Token
						String userpwd = des.decrypt(token);
						String[] strs = userpwd.split("\\|");
						String user = strs[0];
						String pwd = strs[1];
						long times = Long.parseLong(strs[2]);
						if (service.getUserByUserAndPwd(user, pwd) == null) {
							flag = false;
							message = "请输入正确的用户名和密码!";
						}

						long curTimes = System.currentTimeMillis();
						if (curTimes > times + timeOut * 60 * 1000) {
							flag = false;
							message = "已超时,请重新登录!";
						}

					} else {
						// 已经注销的token
						flag = false;
						message = "此凭证已经被注销,请重新登录!";
					}
				}

				if (flag) {
					chain.doFilter(request, response);
				} else {
					res.setStatus(401);
					res.setContentType("application/json;charset=UTF-8");
					res.getWriter().print(
							"{\"success\":" + flag + ",\"message\":\""
									+ message + "\"}");
				}
			}
		}
	}

	private Integer timeOut;
	private Boolean isAuth;
	private Des des = new Des();
	private IUserService service = new UserService();
	private ITokenService tokenService = new TokenService();

	@Override
	public void init(FilterConfig cfg) throws ServletException {
		timeOut = Integer.parseInt(cfg.getInitParameter("timeOut"));
		isAuth = Boolean.valueOf(cfg.getInitParameter("isAuth"));
		service = (IUserService) SpringContextUtil.getApplicationContext()
				.getBean("userService");

		tokenService = (ITokenService) SpringContextUtil
				.getApplicationContext().getBean("tokenService");
	}
}
