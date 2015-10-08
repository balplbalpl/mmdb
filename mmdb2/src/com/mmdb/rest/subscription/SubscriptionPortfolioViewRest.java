package com.mmdb.rest.subscription;

import java.util.List;

import net.sf.json.JSONObject;

import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.model.info.ViewPortfolio;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.info.IViewInfoService;
import com.mmdb.service.info.IViewPortfolioService;
import com.mmdb.service.relation.IViewViewProtfolioRelService;
import com.mmdb.service.subscription.ISubscriptionService;
import com.mmdb.service.subscription.ISubscriptionViewPortfolioService;

/**
 * 用于管理订阅,可以订阅视图和组合视图
 * 
 * @author xiongjian
 * @path /subscription/view
 */
public class SubscriptionPortfolioViewRest extends BaseRest {
	private IViewInfoService vInfoService;
	private IViewPortfolioService vpService;
	private IViewViewProtfolioRelService viewRelToProtService;
	private ISubscriptionService subViewService;
	private ISubscriptionViewPortfolioService subViewPortService;

	@Override
	public void ioc(ApplicationContext context) {
		vInfoService = context.getBean(IViewInfoService.class);

		vpService = context.getBean(IViewPortfolioService.class);

		viewRelToProtService = context
				.getBean(IViewViewProtfolioRelService.class);
		subViewService = context.getBean(ISubscriptionService.class);

		subViewPortService = context
				.getBean(ISubscriptionViewPortfolioService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		return notFindMethod(null);
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		return subscriptionView(param1);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}
	
	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		return unsubscriptionView(param1);
	}

	/**
	 * 订阅视图
	 * 
	 * @param viewPId
	 *            组合视图id
	 * @return
	 */
	private Representation subscriptionView(String viewPId) {
		JSONObject ret = new JSONObject();
		try {
			ViewPortfolio viewPortfolio = vpService.getById(viewPId);
			if (viewPortfolio == null) {
				throw new Exception("组合视图不存在");
			}
			// 将没有订阅的视图订阅一份
			List<String> viewIds = viewRelToProtService
					.getViewIdByProtfolio(viewPId);
			String username = getUsername();
			// 已经订阅的视图id
			List<String> subViewIds = subViewService
					.getViewBySubscriber(username);
			for (String viewId : viewIds) {
				// 将没有订阅的视图订阅一遍
				if (!subViewIds.contains(viewId)) {
					subViewService.save(username, viewId);
				}
			}
			// 订阅这个组合视图
			subViewPortService.save(viewPId, username);
			ret.put("message", "订阅成功");
		} catch (Exception e) {
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 
	 * @param viewPId
	 *            组合视图id
	 * @return
	 */
	private Representation unsubscriptionView(String viewPId) {
		JSONObject ret = new JSONObject();
		try {
			subViewPortService.del(viewPId, getUsername());
			ret.put("message", "取消订阅成功");
		} catch (Exception e) {
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}

		return new JsonRepresentation(ret.toString());
	}

}
