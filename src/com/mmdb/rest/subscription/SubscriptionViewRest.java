package com.mmdb.rest.subscription;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.model.bean.User;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.info.IViewInfoService;
import com.mmdb.service.relation.ICiViewRelService;
import com.mmdb.service.relation.IViewViewProtfolioRelService;
import com.mmdb.service.subscription.ISubscriptionService;
import com.mmdb.service.subscription.ISubscriptionViewPortfolioService;

/**
 * 用于管理订阅,可以订阅视图和组合视图
 * 
 * @author xiongjian
 * @path /subscription/view
 */
public class SubscriptionViewRest extends BaseRest {
	private IViewInfoService vInfoService;
	private ISubscriptionService subService;
	private IViewViewProtfolioRelService viewRelToProtService;
	private ISubscriptionViewPortfolioService subViewPortService;
	private ICiViewRelService ciViewRelService;

	@Override
	public void ioc(ApplicationContext context) {
		vInfoService = context.getBean(IViewInfoService.class);
		subService = context.getBean(ISubscriptionService.class);
		viewRelToProtService = context
				.getBean(IViewViewProtfolioRelService.class);
		subViewPortService = context
				.getBean(ISubscriptionViewPortfolioService.class);
		ciViewRelService = context.getBean(ICiViewRelService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if ("mysubscription".equals(param1)) {
			return getSubViewIds();
		}
		// 订阅视图
		return subscriptionView(param1);
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		return subscriptionView(param1);
	}
	
	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		return unsubscriptionView(param1);
	}
	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(entity); 
	}
	/**
	 * 获取当用户
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getSubViewIds() throws Exception {
		JSONObject ret = new JSONObject();
		String loginName = getUsername();
		List<String> viewIds = subService.getViewBySubscriber(loginName);
		List<String> retData = new ArrayList<String>();
		List<ViewInformation> byids = vInfoService.getByids(viewIds);
		for (ViewInformation viewInformation : byids) {
			if (!viewInformation.getUserName().equals(loginName)) {
				retData.add(viewInformation.getId());
			}
		}
		ret.put("data", retData);
		ret.put("message", "获取成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 订阅视图
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation subscriptionView(String viewId) throws Exception {
		JSONObject ret = new JSONObject();
		String username = getUser().getLoginName();
		List<String> viewids = subService.getViewBySubscriber(username);
		if (viewids.contains(viewId)) {
			ret.put("message", "订阅成功");
			return new JsonRepresentation(ret.toString());
		}

		ViewInformation vInfo = vInfoService.getById(viewId);

		subService.save(username, vInfo);
		ret.put("message", "订阅成功");

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 
	 * @param viewId
	 * @return
	 * @throws Exception 
	 */
	private Representation unsubscriptionView(String viewId) throws Exception {
		JSONObject ret = new JSONObject();
		User user = getUser();
		// TODO 判断视图是否被组合视图使用,如果被使用则不让取消
		// 获取我订阅的全部组合视图和视图中使用的
		List<String> vpIds = subViewPortService
				.getViewPortfoliosBySubscriber(user.getLoginName());
		for (String vpId : vpIds) {
			List<String> viewIds = viewRelToProtService
					.getViewIdByProtfolio(vpId);
			if (viewIds.contains(viewId)) {
				throw new MException("视图已经被组合视图使用.无法取消订阅!");
			}
		}
		subService.delete(user.getLoginName(), viewId);
		ret.put("message", "取消订阅成功");

		return new JsonRepresentation(ret.toString());
	}
}
