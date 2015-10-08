package com.mmdb.rest.subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.model.bean.User;
import com.mmdb.model.bean.UserProfile;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.info.IViewInfoService;
import com.mmdb.service.relation.ICiViewRelService;
import com.mmdb.service.subscription.ISubscriptionService;

public class UserProfileRest extends BaseRest {
	private IViewInfoService vInfoService;
	private ISubscriptionService subService;
	private ICiViewRelService ciViewRelService;

	@Override
	public void ioc(ApplicationContext context) {
		vInfoService = context.getBean(IViewInfoService.class);
		subService = context.getBean(ISubscriptionService.class);
		ciViewRelService = context.getBean(ICiViewRelService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		return getSub(param1);
	}

	private Representation getSub(String name) throws Exception {
		// TODO 增加数据集和映射的profiles
		JSONObject ret = new JSONObject();
		User user = getUser();
		String username = user.getLoginName();
		List<String> viewIds = subService.getViewBySubscriber(username);
		List<ViewInformation> vInfos = vInfoService.getByids(viewIds);
		if (vInfos == null) {
			vInfos = new ArrayList<ViewInformation>();
		}
		Map<String, Map<String, Object>> content = new HashMap<String, Map<String, Object>>();

		for (ViewInformation info : vInfos) {
			Map<String, Object> asMap = info.asMapForRest();
			List<String> ciIds = ciViewRelService.getByView(info
					.getCategoryId());
			if (ciIds == null) {
				asMap.put("ciSize", 0);
			} else {
				asMap.put("ciSize", ciIds.size());
			}
			content.put(info.getId(), asMap);
		}
		UserProfile userProfile = new UserProfile(vInfos, user);
		userProfile.setViews(content);

		// userProfile.setViews(views);
		ret.put("message", "获取用户概况成功");
		ret.put("data", userProfile.asMap());
		return new JsonRepresentation(ret.toString());
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}
}
