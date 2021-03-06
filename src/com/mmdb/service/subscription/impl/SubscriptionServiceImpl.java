package com.mmdb.service.subscription.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.model.bean.Page;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.model.subscription.storage.SubscriptionViewStorage;
import com.mmdb.service.info.IViewInfoService;
import com.mmdb.service.notify.impl.NotifyService;
import com.mmdb.service.relation.ICiKpiRelService;
import com.mmdb.service.relation.ICiViewRelService;
import com.mmdb.service.subscription.ISubscriptionService;

/**
 * 用于管理视图的订阅
 * <p>
 * kpiId(HexId,String) ciId(HexId,String) subscriber(用户名,String本质是List)
 * viewId(视图id,String) viewAuthor(视图创建人,String) threshold(阈值,String)
 * 
 * @author xiongjian
 * 
 */
@Service("subscriptionService")
public class SubscriptionServiceImpl implements ISubscriptionService {

	@Autowired
	private ICiViewRelService ciViewRelService;

	@Autowired
	private ICiKpiRelService ciKpiRel;

	@Autowired
	private SubscriptionViewStorage subView;

	@Autowired
	private IViewInfoService vInfo;

	@Autowired
	private NotifyService notifyService;

	@Override
	public void save(String username, String viewId) throws Exception {
		ViewInformation info = vInfo.getById(viewId);
		if (info == null)
			throw new Exception("指定视图不存在");
		save(username, info);
	}

	@Override
	public void save(String username, ViewInformation info) throws Exception {
		subView.subscriptionView(info.getId(), username);
		if (username.equals(info.getUserName())) {
			List<String> ciHexIds = ciViewRelService.getByView(info.getId());
			Map<String, List<KpiInformation>> ciKpis = ciKpiRel
					.getKpiByCi(ciHexIds);
			if (ciKpis == null || ciKpis.size() == 0)
				return;
			List<Map<String, Object>> unfoldCiKpis = unfoldData(username,
					info.getId(), info.getUserName(), ciKpis);
			for (Map<String, Object> map : unfoldCiKpis) {
				JSONArray subs = new JSONArray();
				JSONArray enables = new JSONArray();
				JSONArray notifys = new JSONArray();
				String sub = map.get("subscriber").toString();
				boolean enable = (Boolean) map.get("ifEnable");
				boolean notify = (Boolean) map.get("ifNotify");
				subs.add(sub);
				enables.add(enable);
				notifys.add(notify);
				map.put("subscriber", subs);
				map.put("ifEnable", enables);
				map.put("ifNotify", notifys);
			}
			List<Map<String, Object>> save = subView.save(unfoldCiKpis);

			for (Map<String, Object> map : save) {
				notifyService.refreshCache("SubscriptionKpiViewRel", "ADD",
						map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
								+ map.get("kpiId"), map);
			}
		} else {
			List<Map<String, Object>> data = subView.getDataBySubUserAndView(
					info.getId(), info.getUserName());

			for (Map<String, Object> map : data) {
				JSONArray subArr = JSONArray.fromObject(map.get("subscriber"));
				JSONArray ifNotify = JSONArray.fromObject(map.get("ifNotify"));
				JSONArray ifEnable = JSONArray.fromObject(map.get("ifEnable"));
				if (!subArr.contains(username)) {
					subArr.add(username);
					ifNotify.add(ifNotify.get(0));
					ifEnable.add(ifEnable.get(0));
				}
				map.put("subscriber", subArr);
				map.put("ifNotify", ifNotify);
				map.put("ifEnable", ifEnable);

				notifyService.refreshCache("SubscriptionKpiViewRel", "UPD",
						map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
								+ map.get("kpiId"), map);
			}
			subView.update(data);
		}
	}

	@Override
	public void asSave(String username, String viewId,
			List<Map<String, Object>> unfoldCiKpis) throws Exception {
		subView.subscriptionView(viewId, username);
		for (Map<String, Object> map : unfoldCiKpis) {
			JSONArray subs = new JSONArray();
			JSONArray enables = new JSONArray();
			JSONArray notifys = new JSONArray();
			// String sub = map.get("subscriber").toString();
			boolean enable = (Boolean) map.get("ifEnable");
			boolean notify = (Boolean) map.get("ifNotify");
			subs.add(username);
			enables.add(enable);
			notifys.add(notify);
			map.put("subscriber", subs);
			map.put("ifEnable", enables);
			map.put("ifNotify", notifys);
		}
		List<Map<String, Object>> save = subView.save(unfoldCiKpis);
		for (Map<String, Object> map : save) {
			notifyService.refreshCache(
					"SubscriptionKpiViewRel",
					"ADD",
					map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
							+ map.get("kpiId"), map);
		}
	}

	@Override
	public void update(String username, String viewId, String viewAuthor,
			List<Map<String, Object>> unfoldCiKpis) throws Exception {
		if (username.equals(viewAuthor)) {// 当订阅人为视图的作者时,则认为第一次订阅视图
			List<Map<String, Object>> update = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> oldData = subView
					.getDataBySubUserAndView(viewId, username);
			for (Map<String, Object> map : unfoldCiKpis) {
				Object ciId = map.get("ciId");
				Object kpiId = map.get("kpiId");
				Object threshold = map.get("threshold");

				boolean ifNotify = false;
				try {
					ifNotify = Boolean.parseBoolean((String) map
							.get("ifNotify"));
				} catch (Exception e) {
					ifNotify = (Boolean) map.get("ifNotify");
				}
				boolean ifEnable = false;
				try {
					ifEnable = Boolean.parseBoolean((String) map
							.get("ifEnable"));
				} catch (Exception e) {
					ifEnable = (Boolean) map.get("ifEnable");
				}
				int index = 0;
				for (Map<String, Object> oldMap : oldData) {
					Object oldCiId = oldMap.get("ciId");
					Object oldKpiId = oldMap.get("kpiId");
					if (ciId.equals(oldCiId) && kpiId.equals(oldKpiId)) {
						oldMap.put("threshold",
								sortThreshold(threshold.toString()));
						JSONArray oldifNotify = JSONArray.fromObject(oldMap
								.get("ifNotify"));
						JSONArray oldifEnable = JSONArray.fromObject(oldMap
								.get("ifEnable"));
						// for (int i = 0; i < oldifNotify.size(); i++) {
						oldifNotify.set(0, ifNotify);
						oldifEnable.set(0, ifEnable);
						// }
						oldMap.put("ifNotify", oldifNotify);
						oldMap.put("ifEnable", oldifEnable);
						update.add(oldMap);
						oldData.remove(index);
						break;
					}
					index++;
				}
				subView.update(update);
			}

			return;
		}
		this.delete(username, viewId);
		// 可以融合的就更新
		List<Map<String, Object>> update = new ArrayList<Map<String, Object>>();
		// 没有的新建
		List<Map<String, Object>> save = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : unfoldCiKpis) {
			String sortThreshold = sortThreshold(map.get("threshold")
					.toString());
			map.put("threshold", sortThreshold);
			// 融合存储
			Map<String, Object> result = subView.getdata(map.get("viewId")
					.toString(), map.get("ciId").toString(), map.get("kpiId")
					.toString(), map.get("threshold").toString());
			if (result == null) {
				JSONArray arrayList = new JSONArray();
				JSONArray ifNotify = new JSONArray();
				JSONArray ifEnable = new JSONArray();
				arrayList.add(map.get("subscriber").toString());
				ifNotify.add(map.get("ifNotify"));
				ifEnable.add(map.get("ifEnable"));
				map.put("ifNotify", ifNotify);
				map.put("ifEnable", ifEnable);
				map.put("subscriber", arrayList);
				save.add(map);
				continue;
			} else {
				JSONArray subArr = JSONArray.fromObject(result
						.get("subscriber"));
				JSONArray ifNotify = JSONArray.fromObject(result
						.get("ifNotify"));
				JSONArray ifEnable = JSONArray.fromObject(result
						.get("ifEnable"));
				if (!subArr.contains(username)) {
					subArr.add(username);
					ifNotify.add(map.get("ifNotify"));
					ifEnable.add(map.get("ifEnable"));
				}
				map.put("subscriber", subArr.toString());
				map.put("ifNotify", ifNotify.toString());
				map.put("ifEnable", ifEnable.toString());
				map.put("id", result.get("id"));
				notifyService.refreshCache("SubscriptionKpiViewRel", "UPD",
						map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
								+ map.get("kpiId"), map);
				update.add(map);
			}
		}
		if (update.size() > 0)
			subView.update(update);
		if (save.size() > 0) {
			List<Map<String, Object>> save2 = subView.save(save);
			for (Map<String, Object> map : save2) {
				notifyService.refreshCache("SubscriptionKpiViewRel", "ADD",
						map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
								+ map.get("kpiId"), map);
			}
		}

	}

	// @Override
	// public void incUpdate(String username, String viewId, String viewAuthor,
	// List<Map<String, Object>> unfoldCiKpis) throws Exception {
	//
	// if (username.equals(viewAuthor)) {// 当订阅人为视图的作者时,则认为第一次订阅视图
	// List<Map<String, Object>> update = new ArrayList<Map<String, Object>>();
	//
	// for (Map<String, Object> map : unfoldCiKpis) {
	// String id = (String) map.get("id");
	// Map<String, Object> oldMap = subView.getById(id);
	//
	// String threshold = sortThreshold(map.get("threshold")
	// .toString());
	//
	// boolean ifNotify = false;
	// try {
	// ifNotify = Boolean.parseBoolean((String) map
	// .get("ifNotify"));
	// } catch (Exception e) {
	// ifNotify = (Boolean) map.get("ifNotify");
	// }
	// boolean ifEnable = false;
	// try {
	// ifEnable = Boolean.parseBoolean((String) map
	// .get("ifEnable"));
	// } catch (Exception e) {
	// ifEnable = (Boolean) map.get("ifEnable");
	// }
	// oldMap.put("threshold", sortThreshold(threshold.toString()));
	// JSONArray oldifNotify = JSONArray.fromObject(oldMap
	// .get("ifNotify"));
	// JSONArray oldifEnable = JSONArray.fromObject(oldMap
	// .get("ifEnable"));
	// oldifNotify.set(0, ifNotify);
	// oldifEnable.set(0, ifEnable);
	// oldMap.put("ifNotify", oldifNotify);
	// oldMap.put("ifEnable", oldifEnable);
	// notifyService.refreshCache("SubscriptionKpiViewRel", "UPD",
	// oldMap.get("viewId") + "^_^" + oldMap.get("ciId")
	// + "^_^" + oldMap.get("kpiId"), oldMap);
	// update.add(oldMap);
	// }
	// subView.update(update);
	// return;
	// }
	// // 可以融合的就更新
	// List<Map<String, Object>> update = new ArrayList<Map<String, Object>>();
	// // 没有的新建
	// List<Map<String, Object>> save = new ArrayList<Map<String, Object>>();
	// for (Map<String, Object> map : unfoldCiKpis) {
	// String id = (String) map.get("id");
	// Map<String, Object> oldMap = subView.getById(id);
	//
	// String sortThreshold = sortThreshold(map.get("threshold")
	// .toString());
	// String oldThreshold = (String) oldMap.get("threshold");
	// if (oldThreshold.equals(sortThreshold)) {
	// // 更新
	// JSONArray subArr = JSONArray.fromObject(oldMap
	// .get("subscriber"));
	// JSONArray ifNotify = JSONArray.fromObject(oldMap
	// .get("ifNotify"));
	// JSONArray ifEnable = JSONArray.fromObject(oldMap
	// .get("ifEnable"));
	// int index = contains(subArr, username);
	//
	// boolean newNotify = (Boolean) map.get("ifNotify");
	// boolean newEnable = (Boolean) map.get("ifEnable");
	//
	// ifNotify.set(index, newNotify);
	// ifEnable.set(index, newEnable);
	//
	// oldMap.put("ifNotify", ifNotify);
	// oldMap.put("ifEnable", ifEnable);
	//
	// notifyService.refreshCache("SubscriptionKpiViewRel", "UPD",
	// oldMap.get("viewId") + "^_^" + oldMap.get("ciId")
	// + "^_^" + oldMap.get("kpiId"), oldMap);
	// update.add(oldMap);
	// } else {
	// // 新建
	// JSONArray arrayList = new JSONArray();
	// JSONArray ifNotify = new JSONArray();
	// JSONArray ifEnable = new JSONArray();
	// arrayList.add(map.get("subscriber").toString());
	// ifNotify.add(map.get("ifNotify"));
	// ifEnable.add(map.get("ifEnable"));
	// map.put("ifNotify", ifNotify);
	// map.put("ifEnable", ifEnable);
	// map.put("subscriber", arrayList);
	// save.add(map);
	//
	// //将数据从老数据中删除掉.
	// JSONArray oldSubArr = JSONArray.fromObject(oldMap
	// .get("subscriber"));
	// JSONArray oldIfNotify = JSONArray.fromObject(oldMap
	// .get("ifNotify"));
	// JSONArray oldIfEnable = JSONArray.fromObject(oldMap
	// .get("ifEnable"));
	// int index = contains(oldSubArr, username);
	// if(index <0){
	// continue;
	// }
	// oldSubArr.remove(index);
	// oldIfNotify.remove(index);
	// oldIfEnable.remove(index);
	//
	// oldMap.put("ifNotify", oldIfEnable);
	// oldMap.put("ifEnable", oldIfEnable);
	// oldMap.put("subscriber", oldSubArr);
	// notifyService.refreshCache("SubscriptionKpiViewRel", "UPD",
	// oldMap.get("viewId") + "^_^" + oldMap.get("ciId")
	// + "^_^" + oldMap.get("kpiId"), oldMap);
	// if(oldIfEnable.size()==0){
	// notifyService.refreshCache("SubscriptionKpiViewRel", "DEL",
	// oldMap.get("viewId") + "^_^" + oldMap.get("ciId") + "^_^"
	// + oldMap.get("kpiId"), oldMap);
	// ArrayList<String> dels = new ArrayList<String>();
	// dels.add((String)oldMap.get("id"));
	// subView.deleteByIds(dels);
	// }else{
	// update.add(oldMap);
	// }
	// }
	// }
	// if (update.size() > 0)
	// subView.update(update);
	// if (save.size() > 0) {
	// List<Map<String, Object>> save2 = subView.save(save);
	// for (Map<String, Object> map : save2) {
	// notifyService.refreshCache("SubscriptionKpiViewRel", "ADD",
	// map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
	// + map.get("kpiId"), map);
	// }
	// }
	// }
	@Override
	public void incUpdate(String username, String viewId, String viewAuthor,
			List<Map<String, Object>> unfoldCiKpis) throws Exception {

		if (username.equals(viewAuthor)) {// 当订阅人为视图的作者时,则认为第一次订阅视图
			List<Map<String, Object>> update = new ArrayList<Map<String, Object>>();

			for (Map<String, Object> map : unfoldCiKpis) {
				String id = (String) map.get("id");
				Map<String, Object> oldMap = subView.getById(id);

				String threshold = sortThreshold(map.get("threshold")
						.toString());

				boolean ifNotify = false;
				try {
					ifNotify = Boolean.parseBoolean((String) map
							.get("ifNotify"));
				} catch (Exception e) {
					ifNotify = (Boolean) map.get("ifNotify");
				}
				boolean ifEnable = false;
				try {
					ifEnable = Boolean.parseBoolean((String) map
							.get("ifEnable"));
				} catch (Exception e) {
					ifEnable = (Boolean) map.get("ifEnable");
				}
				oldMap.put("threshold", sortThreshold(threshold.toString()));
				JSONArray oldifNotify = JSONArray.fromObject(oldMap
						.get("ifNotify"));
				JSONArray oldifEnable = JSONArray.fromObject(oldMap
						.get("ifEnable"));
				oldifNotify.set(0, ifNotify);
				oldifEnable.set(0, ifEnable);
				oldMap.put("ifNotify", oldifNotify);
				oldMap.put("ifEnable", oldifEnable);
				notifyService.refreshCache("SubscriptionKpiViewRel", "UPD",
						oldMap.get("viewId") + "^_^" + oldMap.get("ciId")
								+ "^_^" + oldMap.get("kpiId"), oldMap);
				update.add(oldMap);
			}
			subView.update(update);
			return;
		}
		// 可以融合的就更新
		List<Map<String, Object>> update = new ArrayList<Map<String, Object>>();
		// 没有的新建
		List<Map<String, Object>> save = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : unfoldCiKpis) {
			String id = (String) map.get("id");
			String ciHex = String.valueOf(map.get("ciId"));
			String kpiHex = String.valueOf(map.get("kpiId"));
			Map<String, Object> oldMap = subView.getById(id);

			String sortThreshold = sortThreshold(map.get("threshold")
					.toString());
			String oldThreshold = (String) oldMap.get("threshold");
			if (oldThreshold.equals(sortThreshold)) {
				// 更新,notity 和 enable
				JSONArray subArr = JSONArray.fromObject(oldMap
						.get("subscriber"));
				JSONArray ifNotify = JSONArray.fromObject(oldMap
						.get("ifNotify"));
				JSONArray ifEnable = JSONArray.fromObject(oldMap
						.get("ifEnable"));
				int index = contains(subArr, username);

				boolean newNotify = (Boolean) map.get("ifNotify");
				boolean newEnable = (Boolean) map.get("ifEnable");

				ifNotify.set(index, newNotify);
				ifEnable.set(index, newEnable);

				oldMap.put("ifNotify", ifNotify);
				oldMap.put("ifEnable", ifEnable);

				notifyService.refreshCache("SubscriptionKpiViewRel", "UPD",
						oldMap.get("viewId") + "^_^" + oldMap.get("ciId")
								+ "^_^" + oldMap.get("kpiId"), oldMap);
				update.add(oldMap);
			} else {
				// 将数据从老数据中删除掉.
				JSONArray oldSubArr = JSONArray.fromObject(oldMap
						.get("subscriber"));
				JSONArray oldIfNotify = JSONArray.fromObject(oldMap
						.get("ifNotify"));
				JSONArray oldIfEnable = JSONArray.fromObject(oldMap
						.get("ifEnable"));
				int index = contains(oldSubArr, username);
				if (index < 0) {
					continue;
				}
				Map<String, Object> mergeData = subView.getdata(viewId, ciHex,
						kpiHex, sortThreshold);

				// 只有这一条记录是且不需要融合
				if (mergeData == null) {
					if (oldSubArr.size() == 1) {
						oldMap.put("threshold", sortThreshold);
						JSONArray ifNotify = new JSONArray();
						JSONArray ifEnable = new JSONArray();
						ifNotify.add(map.get("ifNotify"));
						ifEnable.add(map.get("ifEnable"));
						map.put("ifNotify", ifNotify);
						map.put("ifEnable", ifEnable);
						notifyService.refreshCache(
								"SubscriptionKpiViewRel",
								"UPD",
								oldMap.get("viewId") + "^_^"
										+ oldMap.get("ciId") + "^_^"
										+ oldMap.get("kpiId"), oldMap);
						continue;
					} else {
						JSONArray arrayList = new JSONArray();
						JSONArray ifNotify = new JSONArray();
						JSONArray ifEnable = new JSONArray();
						arrayList.add(map.get("subscriber").toString());
						ifNotify.add(map.get("ifNotify"));
						ifEnable.add(map.get("ifEnable"));
						map.put("ifNotify", ifNotify);
						map.put("ifEnable", ifEnable);
						map.put("subscriber", arrayList);
						save.add(map);
					}
				} else { // 融合
					String newSub = map.get("subscriber").toString();
					Object newN = map.get("ifNotify");
					Object newE = map.get("ifEnable");
					JSONArray oldSubArr1 = JSONArray.fromObject(mergeData
							.get("subscriber"));
					JSONArray oldIfNotify1 = JSONArray.fromObject(mergeData
							.get("ifNotify"));
					JSONArray oldIfEnable1 = JSONArray.fromObject(mergeData
							.get("ifEnable"));
					if (!oldSubArr1.contains(newSub)) {
						oldSubArr1.add(newSub);
						oldIfNotify1.add(newN);
						oldIfEnable1.add(newE);
					}
					mergeData.put("subscriber", oldSubArr1);
					mergeData.put("ifNotify", oldIfNotify1);
					mergeData.put("ifEnable", oldIfEnable1);
					notifyService.refreshCache(
							"SubscriptionKpiViewRel",
							"UPD",
							mergeData.get("viewId") + "^_^"
									+ mergeData.get("ciId") + "^_^"
									+ mergeData.get("kpiId"), mergeData);
					update.add(mergeData);
				}

				if (oldSubArr.size() > 1) {
					oldSubArr.remove(index);
					oldIfNotify.remove(index);
					oldIfEnable.remove(index);

					oldMap.put("ifNotify", oldIfEnable);
					oldMap.put("ifEnable", oldIfEnable);
					oldMap.put("subscriber", oldSubArr);
					notifyService.refreshCache("SubscriptionKpiViewRel", "UPD",
							oldMap.get("viewId") + "^_^" + oldMap.get("ciId")
									+ "^_^" + oldMap.get("kpiId"), oldMap);
					update.add(oldMap);
				} else {
					ArrayList<String> ids = new ArrayList<String>();
					ids.add(String.valueOf(oldMap.get("id")));
					notifyService.refreshCache("SubscriptionKpiViewRel", "DEL",
							oldMap.get("viewId") + "^_^" + oldMap.get("ciId")
									+ "^_^" + oldMap.get("kpiId"), oldMap);
					subView.deleteByIds(ids);
				}
			}
		}
		if (update.size() > 0)
			subView.update(update);
		if (save.size() > 0) {
			List<Map<String, Object>> save2 = subView.save(save);
			for (Map<String, Object> map : save2) {
				notifyService.refreshCache("SubscriptionKpiViewRel", "ADD",
						map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
								+ map.get("kpiId"), map);
			}
		}
	}

	@Override
	public void delete(String viewId, boolean viewDeleted) throws Exception {
		if (viewDeleted) {
			// 清除这张视图相关联的全部记录
			List<Map<String, Object>> historyDatas = subView
					.getByViewId(viewId);

			for (Map<String, Object> map : historyDatas) {
				notifyService.refreshCache("SubscriptionKpiViewRel", "DEL",
						map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
								+ map.get("kpiId"), map);
			}

			subView.deleteByViewId(viewId);
			subView.unSubscriptionByView(viewId);
		} else {
			// 视图变私有了.
			List<Map<String, Object>> historyDatas = subView
					.getByViewId(viewId);
			if (historyDatas != null && historyDatas.size() > 0) {

				String author = historyDatas.get(0).get("viewAuthor")
						.toString();
				for (Map<String, Object> map : historyDatas) {
					notifyService.refreshCache("SubscriptionKpiViewRel", "DEL",
							map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
									+ map.get("kpiId"), map);
				}

				List<Map<String, Object>> save = subView
						.getDataBySubUserAndView(viewId, author);

				// 删除原数据
				subView.deleteByViewId(viewId);

				for (Map<String, Object> map : save) {
					JSONArray subs = new JSONArray();
					subs.add(author);
					map.put("subscriber", subs);
				}

				List<Map<String, Object>> save2 = subView.save(save);

				for (Map<String, Object> map : save2) {
					notifyService.refreshCache("SubscriptionKpiViewRel", "ADD",
							map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
									+ map.get("kpiId"), map);
				}
			}
		}
	}

	@Override
	public void delete(String username, String viewId) throws Exception {
		subView.unSubscriptionView(viewId, username);

		List<Map<String, Object>> historyData = subView
				.getDataBySubUserAndView(viewId, username);
		List<Map<String, Object>> updates = new ArrayList<Map<String, Object>>();
		List<String> delete = new ArrayList<String>();

		for (Map<String, Object> map : historyData) {
			JSONArray subs = JSONArray.fromObject(map.get("subscriber"));
			JSONArray ifNotify = JSONArray.fromObject(map.get("ifNotify"));
			JSONArray ifEnable = JSONArray.fromObject(map.get("ifEnable"));

			String mongoId = map.get("id").toString();
			int index = contains(subs, username);
			subs.remove(username);
			ifNotify.remove(index);
			ifEnable.remove(index);

			if (subs.size() != 0) {
				Map<String, Object> update = new HashMap<String, Object>();

				update.putAll(map);
				update.put("subscriber", subs.toString());
				update.put("ifNotify", ifNotify.toString());
				update.put("ifEnable", ifEnable.toString());

				notifyService.refreshCache("SubscriptionKpiViewRel", "UPD",
						map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
								+ map.get("kpiId"), map);
				updates.add(update);
			} else {
				notifyService.refreshCache("SubscriptionKpiViewRel", "DEL",
						map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
								+ map.get("kpiId"), map);

				delete.add(mongoId);
			}
			map.put("subscriber", "['" + username + "']");
		}
		subView.update(updates);
		subView.deleteByIds(delete);
	}

	/**
	 * 将数据展开,方便遍历
	 * 
	 * @param username
	 * @param viewId
	 * @param viewAuthor
	 * @param ciKpis
	 * @return
	 */
	private List<Map<String, Object>> unfoldData(String username,
			String viewId, String viewAuthor,
			Map<String, List<KpiInformation>> ciKpis) {

		List<Map<String, Object>> curCiKpis = new ArrayList<Map<String, Object>>();
		Set<String> keySet = ciKpis.keySet();
		for (String ciHex : keySet) {
			List<KpiInformation> list = ciKpis.get(ciHex);
			for (KpiInformation kpi : list) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("kpiId", kpi.getKpiHex());
				data.put("ciId", ciHex);
				data.put("viewId", viewId);
				data.put("viewAuthor", viewAuthor);
				data.put("subscriber", username);
				data.put("threshold", sortThreshold(kpi.getThreshold()));
				data.put("ifNotify", false);
				data.put("ifEnable", true);
				curCiKpis.add(data);
			}
		}
		return curCiKpis;
	}

	@Override
	public void deleteByKpi(String kpiId) throws Exception {
		List<Map<String, Object>> byKpi = subView.getByKpi(kpiId);
		for (Map<String, Object> map : byKpi) {
			notifyService.refreshCache(
					"SubscriptionKpiViewRel",
					"DEL",
					map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
							+ map.get("kpiId"), map);
		}
		subView.deleteByKpiId(kpiId);
	}

	@Override
	public void deleteByCiKpiRel(String ciId, String kpiId) throws Exception {
		List<Map<String, Object>> byCiKpiRel = subView.getByCiKpiRel(ciId,
				kpiId);
		for (Map<String, Object> map : byCiKpiRel) {
			notifyService.refreshCache(
					"SubscriptionKpiViewRel",
					"DEL",
					map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
							+ map.get("kpiId"), map);
		}
		subView.deleteByCiKpiRel(ciId, kpiId);
	}

	@Override
	public void deleteByCi(String ciId) throws Exception {
		List<Map<String, Object>> byCi = subView.getByCi(ciId);
		for (Map<String, Object> map : byCi) {
			notifyService.refreshCache(
					"SubscriptionKpiViewRel",
					"DEL",
					map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
							+ map.get("kpiId"), map);
		}
		subView.deleteByCiId(ciId);
	}

	@Override
	public void addByCiKpiRel(String ciId, KpiInformation kpi) throws Exception {
		if (subView.exist(ciId, kpi.getKpiHex())) {
			return;
		}

		// 这个ci相关的视图?
		List<String> viewIds = ciViewRelService.getByCi(ciId);
		if (viewIds != null && viewIds.size() != 0) {
			List<Map<String, Object>> save = new ArrayList<Map<String, Object>>();
			List<ViewInformation> vInfos = vInfo.getByids(viewIds);
			String kpiId = kpi.getKpiHex();
			String threshold = kpi.getThreshold();

			// 这个视图中订阅人.
			for (ViewInformation vInfo : vInfos) {
				List<String> subscribers = getSubscriberByView(vInfo.getId());
				JSONArray notifys = new JSONArray();
				JSONArray enables = new JSONArray();
				for (int i = 0, len = subscribers.size(); i < len; i++) {
					notifys.add(false);
					enables.add(true);
				}

				Map<String, Object> data = new HashMap<String, Object>();
				data.put("kpiId", kpiId);
				data.put("ciId", ciId);
				data.put("subscriber", JSONArray.fromObject(subscribers)
						.toString());
				data.put("viewId", vInfo.getId());
				data.put("viewAuthor", vInfo.getUserName());
				data.put("threshold", sortThreshold(threshold));
				data.put("ifNotify", notifys);
				data.put("ifEnable", enables);
				save.add(data);
				//
				notifyService.refreshCache("SubscriptionKpiViewRel", "ADD",
						data.get("viewId") + "^_^" + data.get("ciId") + "^_^"
								+ data.get("kpiId"), data);
			}
			// 保存数据
			subView.save(save);
		}
	}

	@Override
	public List<String> getSubscriberByView(String viewId) throws Exception {
		return subView.getSubscriberByViewId(viewId);
	}

	@Override
	public List<String> getViewBySubscriber(String subscriber) throws Exception {
		return subView.getViewBySubscriber(subscriber);
	}

	@Override
	public void addCiByView(ViewInformation info, List<String> ciHexs)
			throws Exception {
		Map<String, List<KpiInformation>> ciKpis = ciKpiRel.getKpiByCi(ciHexs);
		if (ciKpis == null || ciKpis.size() == 0)
			return;
		List<Map<String, Object>> unfoldCiKpis = unfoldData("", info.getId(),
				info.getUserName(), ciKpis);
		List<String> subscriberByView = getSubscriberByView(info.getId());
		JSONArray subscribers = JSONArray.fromObject(subscriberByView);
		JSONArray notifys = new JSONArray();
		JSONArray enables = new JSONArray();
		for (int i = 0, len = subscribers.size(); i < len; i++) {
			notifys.add(false);
			enables.add(true);
		}
		for (Map<String, Object> map : unfoldCiKpis) {
			map.put("subscriber", subscribers.toString());
			map.put("ifNotify", notifys);
			map.put("ifEnable", enables);
		}
		subView.save(unfoldCiKpis);
	}

	@Override
	public void delCIByView(String viewId, List<String> ciHexs)
			throws Exception {
		for (String ciHex : ciHexs) {
			List<Map<String, Object>> del = subView.getByViewIdAndCi(viewId,
					ciHex);
			for (Map<String, Object> map : del) {
				notifyService.refreshCache("SubscriptionKpiViewRel", "DEL",
						map.get("viewId") + "^_^" + map.get("ciId") + "^_^"
								+ map.get("kpiId"), map);
			}
			subView.deleteByViewAndCi(viewId, ciHex);
		}
	}

	@Override
	public List<Map<String, Object>> getThresholdByView(String viewId,
			String userName) throws Exception {
		// return
		List<Map<String, Object>> data = subView.getDataBySubUserAndView(
				viewId, userName);
		for (Map<String, Object> map : data) {
			JSONArray sub = JSONArray.fromObject(map.get("subscriber"));
			JSONArray ifNotify = JSONArray.fromObject(map.get("ifNotify"));
			JSONArray ifEnable = JSONArray.fromObject(map.get("ifEnable"));
			int index = contains(sub, userName);
			map.put("subscriber", userName);
			map.put("ifNotify", ifNotify.get(index));
			map.put("ifEnable", ifEnable.get(index));
		}
		return data;
	}

	@Override
	public Page<Map<String, Object>> getThresholdByView(String viewId,
			String userName, int page, int pageSize) throws Exception {

		List<Map<String, Object>> thresholds = subView.getDataBySubUserAndView(
				viewId, userName);

		Collections.sort(thresholds, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				String name1 = (String) o1.get("ciId")
						+ (String) o1.get("kpiId");
				String name2 = (String) o2.get("ciId")
						+ (String) o2.get("kpiId");
				return name1.compareTo(name2);
			};
		});

		int count = thresholds.size();
		int start = (page - 1) * pageSize;
		start = start < 0 ? 0 : start;
		int end = page * pageSize;
		start = start > count ? count : start;
		end = end > count ? count : end;
		thresholds = thresholds.subList(start, end);

		for (Map<String, Object> map : thresholds) {
			JSONArray sub = JSONArray.fromObject(map.get("subscriber"));
			JSONArray ifNotify = JSONArray.fromObject(map.get("ifNotify"));
			JSONArray ifEnable = JSONArray.fromObject(map.get("ifEnable"));
			int index = contains(sub, userName);
			map.put("subscriber", userName);
			map.put("ifNotify", ifNotify.get(index));
			map.put("ifEnable", ifEnable.get(index));
		}

		Page<Map<String, Object>> ret = new Page<Map<String, Object>>();
		ret.setCount(thresholds.size());
		ret.setDatas(thresholds);
		ret.setPageSize(pageSize);
		ret.setStart(page);
		ret.setTotalCount(count);
		return ret;

	}

	private int contains(JSONArray data, String target) {
		int t = 0;
		for (Object object : data) {
			if (object.equals(target)) {
				return t;
			}
			t++;
		}
		return -1;
	}

	private String sortThreshold(String threshold) {
		JSONArray t = JSONArray.fromObject(threshold);
		JSONArray ret = new JSONArray();

		for (int i = 0, len = t.size(); i < len; i++) {
			JSONObject thr = t.getJSONObject(i);
			String startTime = thr.getString("startTime");
			String endTime = thr.getString("endTime");
			String repeat = thr.getString("repeat");
			JSONArray sortThresholds = new JSONArray();
			try{
				JSONArray thresholds = thr.getJSONArray("threshold");
				for (int j = 0, len2 = thresholds.size(); j < len2; j++) {
					JSONObject s = thresholds.getJSONObject(j);
					String severity = s.getString("severity");
					String lowLimit = s.getString("lowLimit");
					String highLimit = s.getString("highLimit");
					LinkedHashMap<String, Object> b = new LinkedHashMap<String, Object>();
					b.put("severity", severity);
					b.put("lowLimit", lowLimit);
					b.put("highLimit", highLimit);
					sortThresholds.add(b);
				}
			}catch(Exception e){
				//如果发生异常，阈值设为空数组
			}

			LinkedHashMap<String, Object> a = new LinkedHashMap<String, Object>();

			a.put("repeat", repeat);
			a.put("startTime", startTime);
			a.put("endTime", endTime);
			a.put("threshold", sortThresholds);
			ret.add(a);
		}
		return ret.toString();
	}

	public static void main(String[] args) {
		// String a =
		// sortThreshold("[{\"repeat\":\"0\",\"threshold\":[{\"severity\":\"5\",\"lowLimit\":\"70\",\"highLimit\":\"80\"},{\"severity\":\"4\",\"lowLimit\":\"60\",\"highLimit\":\"70\"},{\"severity\":\"3\",\"lowLimit\":\"50\",\"highLimit\":\"60\"},{\"severity\":\"2\",\"lowLimit\":\"40\",\"highLimit\":\"50\"},{\"severity\":\"1\",\"lowLimit\":\"30\",\"highLimit\":\"40\"}],\"startTime\":\"\",\"endTime\":\"\"}]");
		// System.out.println(a);
	}
}
