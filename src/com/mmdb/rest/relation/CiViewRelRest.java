package com.mmdb.rest.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.common.Global;
import com.mmdb.core.exception.MException;
import com.mmdb.core.utils.SysProperties;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.ICiCateService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.info.IViewInfoService;
import com.mmdb.service.relation.ICiViewRelService;

public class CiViewRelRest extends BaseRest {
	private ICiCateService cateService;
	private ICiInfoService ciInfoService;
	private ICiViewRelService ciViewRelService;
	private IViewInfoService vInfoService;

	@Override
	public void ioc(ApplicationContext context) {
		cateService = context.getBean(ICiCateService.class);

		ciInfoService = context.getBean(ICiInfoService.class);

		ciViewRelService = context.getBean(ICiViewRelService.class);

		vInfoService = context.getBean(IViewInfoService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		String id = getValue("param2");
		if ("ci".equals(param1)) {
			return getViewsByCi(id);
		} else if ("view".equals(param1)) {
			return getCisByView(id);
		}
		return notFindMethod(null);
	}

	private Representation getCisByView(String viewId) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("getCisByView");
		List<String> ciIds = ciViewRelService.getByView(viewId);
		List<CiInformation> ciInfos = ciInfoService.getByIds(ciIds);
		if (ciInfos == null || ciInfos.size() == 0) {
			throw new MException("未发现相关ci");
		}
		List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
		for (CiInformation ciInfo : ciInfos) {
			retData.add(ciInfo.asMapForRest());
		}
		ret.put("data", retData);
		ret.put("message", "获取相关ci成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation getViewsByCi(String ciId) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("getViewsByCi");
		List<String> viewIds = ciViewRelService.getByCi(ciId);
		List<ViewInformation> vInfos = vInfoService.getByids(viewIds);
		if (vInfos == null || vInfos.size() == 0) {
			throw new MException("未发现相关视图");
		}
		List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
		for (ViewInformation vInfo : vInfos) {
			Map<String, Object> asMap = vInfo.asMapForRest();
			List<String> ciIds = ciViewRelService.getByView(vInfo
					.getCategoryId());
			if (ciIds == null) {
				asMap.put("ciSize", 0);
			} else {
				asMap.put("ciSize", ciIds.size());
			}
			retData.add(asMap);
		}
		ret.put("data", retData);
		ret.put("message", "获取相关视图成功");
		return new JsonRepresentation(ret.toString());
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}
}
