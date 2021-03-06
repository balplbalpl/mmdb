package com.mmdb.service.relation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.categroy.storage.CiCateStorage;
import com.mmdb.model.categroy.storage.KpiCateStorage;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.model.info.storage.CiInfoStorage;
import com.mmdb.model.info.storage.KpiInfoStorage;
import com.mmdb.model.relation.storage.CiKpiRelStorage;
import com.mmdb.service.relation.ICiKpiRelService;
import com.mmdb.service.subscription.ISubscriptionService;

@Component("ciKpiRelService")
public class CiKpiRelServiceImpl implements ICiKpiRelService {
	@Autowired
	private CiKpiRelStorage ciKpiRelStorage;
	@Autowired
	private KpiCateStorage kpiCateStorage;
	@Autowired
	private KpiInfoStorage kpiInfoStorage;
	@Autowired
	private CiCateStorage ciCateStorage;
	@Autowired
	private CiInfoStorage ciInfoStorage;
	@Autowired
	private ISubscriptionService subscriptionService;

	@Override
	public List<Map<String, String>> getAllCiKpiRel(List<String> cateIds,
			Boolean hasChildren) throws Exception {
		
		
		List<Map<String, String>> rels = ciKpiRelStorage.getAllCiKpiRel();
		List<KpiCategory> kpiCates = kpiCateStorage.getAll();
		Map<String, KpiCategory> kpiCateMap = new HashMap<String, KpiCategory>();
		Map<String, KpiInformation> kpiInfoMap = new HashMap<String, KpiInformation>();
		for (KpiCategory kpiCate : kpiCates) {
			kpiCateMap.put(kpiCate.getId(), kpiCate);
			List<KpiInformation> kpiInfos = kpiInfoStorage
					.getByCategory(kpiCate.getId());
			for (KpiInformation kpiInfo : kpiInfos) {
				kpiInfoMap.put(kpiInfo.getKpiHex(), kpiInfo);
			}
		}
		List<CiCategory> ciCateList = ciCateStorage.getAll();
		Map<String, CiCategory> ciCateMap = new HashMap<String, CiCategory>();
		for (CiCategory ciCate : ciCateList) {
			ciCateMap.put(ciCate.getName(), ciCate);
		}

		Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>> ret = new HashMap<String, Map<String, Map<String, Map<String, Map<String, String>>>>>();
		List<Map<String, String>> retTmp = new ArrayList<Map<String, String>>();
		List<String> ciidsQuery = new ArrayList<String>();
		String ciidQuery = "";
		for (Map<String, String> rel : rels) {
			Map<String, String> ck = new HashMap<String, String>();
			String ciid = rel.get("ciid");
			String kpiid = rel.get("kpiid");
			String hasData = rel.get("hasData");
			String autoRelation = rel.get("autoRelation");
			if (!ciidQuery.equals(ciid)) {
				ciidsQuery.add(ciid);
				ciidQuery = ciid;
			}
			KpiInformation kpiInfo = kpiInfoMap.get(kpiid);
			if (kpiInfo != null) {
				KpiCategory kpiCate = kpiCateMap
						.get(kpiInfo.getKpiCategoryId());
				if (kpiCate != null) {
					ck.put("kpiCate", kpiCate.getName());
					ck.put("kpi", kpiInfo.getName());
					ck.put("hasData", hasData);
					ck.put("autoRelation", autoRelation);
					ck.put("ci", ciid);
					retTmp.add(ck);
				}
			}

			if (ciidsQuery.size() == 51) {
				String lastId = ciidsQuery.get(50);
				ciidsQuery.remove(50);
				Map<String, String> lastInfo = retTmp.get(retTmp.size() - 1);
				retTmp.remove(retTmp.size() - 1);
				List<CiInformation> ciInfos = ciInfoStorage
						.getByJsonIds(ciidsQuery);
				Map<String, Map<String, String>> ciRet = new HashMap<String, Map<String, String>>();
				for (CiInformation ciInfo : ciInfos) {
					ciInfo.setCategory(ciCateMap.get(ciInfo.getCategoryName()));
					Map<String, String> ci = new HashMap<String, String>();
					ci.put("ciid", ciInfo.getId());
					ci.put("ciName", ciInfo.getName());
					ci.put("ciCategoryId", ciInfo.getCategoryId());
					ci.put("ciCateName", ciInfo.getCategoryName());
					ci.put("hexId", ciInfo.getCiHex());
					ciRet.put(ci.get("hexId"), ci);
				}
				for (Map<String, String> info : retTmp) {
					Map<String, String> ciInfo = ciRet.get(info.get("ci"));
					if (ciInfo != null) {
						info.put("ci", ciInfo.get("ciName"));
						info.put("ciCate", ciInfo.get("ciCateName"));
						info.put("hexId", ciInfo.get("hexId"));
						Map<String, Map<String, Map<String, Map<String, String>>>> ccMap = ret
								.get(info.get("ciCate"));
						if (ccMap == null) {
							ccMap = new HashMap<String, Map<String, Map<String, Map<String, String>>>>();
							ret.put(info.get("ciCate"), ccMap);
						}
						Map<String, Map<String, Map<String, String>>> cMap = ccMap
								.get(info.get("ci"));
						if (cMap == null) {
							cMap = new HashMap<String, Map<String, Map<String, String>>>();
							ccMap.put(info.get("ci"), cMap);
						}
						Map<String, Map<String, String>> ckMap = cMap.get(info
								.get("kpiCate"));
						if (ckMap == null) {
							ckMap = new HashMap<String, Map<String, String>>();
							cMap.put(info.get("kpiCate"), ckMap);
						}
						ckMap.put(info.get("kpi"), info);
					}
				}
				retTmp = new ArrayList<Map<String, String>>();
				retTmp.add(lastInfo);
				ciidsQuery = new ArrayList<String>();
				ciidsQuery.add(lastId);
			}
		}
		if (ciidsQuery.size() > 0) {
			List<CiInformation> ciInfos = ciInfoStorage
					.getByJsonIds(ciidsQuery);
			Map<String, Map<String, String>> ciRet = new HashMap<String, Map<String, String>>();
			for (CiInformation ciInfo : ciInfos) {
				ciInfo.setCategory(ciCateMap.get(ciInfo.getCategoryName()));
				Map<String, String> ci = new HashMap<String, String>();
				ci.put("ciid", ciInfo.getId());
				ci.put("ciName", ciInfo.getName());
				ci.put("ciCategoryId", ciInfo.getCategoryId());
				ci.put("ciCateName", ciInfo.getCategoryName());
				ci.put("hexId", ciInfo.getCiHex());
				ciRet.put(ci.get("hexId"), ci);
			}
			for (Map<String, String> info : retTmp) {
				Map<String, String> ciInfo = ciRet.get(info.get("ci"));
				if (ciInfo != null) {
					info.put("ci", ciInfo.get("ciName"));
					info.put("ciCate", ciInfo.get("ciCateName"));
					info.put("hexId", ciInfo.get("hexId"));
					Map<String, Map<String, Map<String, Map<String, String>>>> ccMap = ret
							.get(info.get("ciCate"));
					if (ccMap == null) {
						ccMap = new HashMap<String, Map<String, Map<String, Map<String, String>>>>();
						ret.put(info.get("ciCate"), ccMap);
					}
					Map<String, Map<String, Map<String, String>>> cMap = ccMap
							.get(info.get("ci"));
					if (cMap == null) {
						cMap = new HashMap<String, Map<String, Map<String, String>>>();
						ccMap.put(info.get("ci"), cMap);
					}
					Map<String, Map<String, String>> ckMap = cMap.get(info
							.get("kpiCate"));
					if (ckMap == null) {
						ckMap = new HashMap<String, Map<String, String>>();
						cMap.put(info.get("kpiCate"), ckMap);
					}
					ckMap.put(info.get("kpi"), info);
				}
			}
		}

		List<Map<String, String>> r = new ArrayList<Map<String, String>>();
		Set<String> ciCateSet = ret.keySet();
		Set<String> cateIdSet = new HashSet<String>();
		if (cateIds == null) {
			for (CiCategory cate : ciCateList) {
				cateIdSet.add(cate.getId());
			}
		} else {
			for (String cateId : cateIds) {
				if (hasChildren) {
					for (CiCategory cate : ciCateList) {
						if (cate.getId().equals(cateId)) {
							List<CiCategory> allChildren = cate
									.getAllChildren();
							for (CiCategory child : allChildren) {
								cateIdSet.add(child.getId());
							}
							break;
						}
					}
				}
				cateIdSet.add(cateId);
			}
		}
		for (String ciCate : ciCateSet) {
			CiCategory cc = ciCateMap.get(ciCate);
			if (cateIdSet.contains(cc.getId())) {
				String attr = cc.getMajor().getName();
				Map<String, Map<String, Map<String, Map<String, String>>>> ciMap = ret
						.get(ciCate);
				Set<String> ciSet = ciMap.keySet();
				for (String ci : ciSet) {
					Map<String, Map<String, Map<String, String>>> kcm = ciMap
							.get(ci);
					Set<String> kpiCateSet = kcm.keySet();
					for (String kpiCate : kpiCateSet) {
						Map<String, Map<String, String>> kpiMap = kcm
								.get(kpiCate);
						Set<String> kpiSet = kpiMap.keySet();
						for (String kpi : kpiSet) {
							Map<String, String> info = kpiMap.get(kpi);
							info.put("ciAttr", attr);
							r.add(info);
						}
					}
				}
			}
		}
		return r;
	}

	@Override
	public Map<String,Integer> addCiKpiRel(Map<String, Object> data,
			Map<String, CiCategory> ciCateMap) {
		try {
			String ciCate = data.get("CI分类") == null ? null : (((String) data
					.get("CI分类")).trim().length() > 0 ? ((String) data
					.get("CI分类")).trim() : null);
			String ciAttr = data.get("CI字段") == null ? null : (((String) data
					.get("CI字段")).trim().length() > 0 ? ((String) data
					.get("CI字段")).trim() : null);
			String ci = data.get("CI值") == null ? null : (((String) data
					.get("CI值")).trim().length() > 0 ? ((String) data
					.get("CI值")).trim() : null);
			String kpiCate = data.get("KPI分类") == null ? null : (((String) data
					.get("KPI分类")).trim().length() > 0 ? ((String) data
					.get("KPI分类")).trim() : null);
			String kpi = data.get("KPI") == null ? null : (((String) data
					.get("KPI")).trim().length() > 0 ? ((String) data
					.get("KPI")).trim() : null);
			Boolean hasData = Boolean
					.parseBoolean(data.get("是否有数据") == null ? null
							: (((String) data.get("是否有数据")).trim().length() > 0 ? ((String) data
									.get("是否有数据")).trim() : null));
			List<String> ciidList = ciInfoStorage.getMongoIdsForCiKpiRel(
					ciCate, ciAttr, ci, ciCateMap);
			List<String> kpiidList = kpiInfoStorage.getMongoIdsForCiKpiRel(
					kpiCate, kpi);
			// 存放 更新/保存 计数信息
			Map<String, Integer> infoObj = new HashMap<String, Integer>();
			boolean isSave = true;
			int saveCount = 0; 
			int updateCount =0;
			for (String ciid : ciidList) {
				for (String kpiid : kpiidList) {
					isSave = ciKpiRelStorage.addCiKpiRel(ciid, kpiid, false,
							hasData);
					if(isSave){
						saveCount++;
					}else{
						updateCount++;
					}
				}
			}
			infoObj.put("save", saveCount);
			infoObj.put("update", updateCount);
			return infoObj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Integer getCiKpiRelCount() throws Exception {
		return ciKpiRelStorage.getCiKpiRelCount();
	}

	@Override
	public Map<String, CiCategory> getCiCateMap() {
		try {
			List<CiCategory> ciCateList = ciCateStorage.getAll();
			Map<String, CiCategory> ciCateMap = new HashMap<String, CiCategory>();
			for (CiCategory cate : ciCateList) {
				ciCateMap.put(cate.getName(), cate);
			}
			return ciCateMap;
		} catch (Exception e) {
			return new HashMap<String, CiCategory>();
		}

	}

	/**
	 * 通过CI Id获取到关联此CI的所有的KPI
	 * 
	 * @param ciId
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<Map<String, String>> getRelByCiId(String ciId) throws Exception {

		// 取出所有的KPI放入缓存中
		Map<String, KpiInformation> kpiInfoMap = new HashMap<String, KpiInformation>();
		List<KpiInformation> kpiInfos = kpiInfoStorage.getAll();
		for (KpiInformation kpiInfo : kpiInfos) {
			// kpiInfoMap.put(kpiInfo.getId(), kpiInfo);
			kpiInfoMap.put(kpiInfo.getKpiHex(), kpiInfo);
		}
		// 存放返回信息
		List<Map<String, String>> retTmp = new LinkedList<Map<String, String>>();
		List<Map<String, String>> rels = ciKpiRelStorage.getRelByCiId(ciId);
		for (Map<String, String> rel : rels) {
			Map<String, String> ck = new HashMap<String, String>();
			String ciHexid = rel.get("ciId");
			String kpiHexid = rel.get("kpiId");
			KpiInformation kpiInfo = kpiInfoMap.get(kpiHexid);
			if (kpiInfo != null) {
				ck.put("kpiCate", kpiInfo.getKpiCategoryName());
				ck.put("kpiId", kpiInfo.getId());
				ck.put("kpiHex", kpiInfo.getKpiHex());
				ck.put("kpiName", kpiInfo.getName());
				ck.put("ciId", ciHexid);
				retTmp.add(ck);
			}
		}
		return retTmp;
	}

	/**
	 * 通过CI Id获取到关联此CI的所有的KPI的分类信息
	 * 
	 * @param ciId
	 * @return List<KpiCategory>
	 * @throws Exception
	 */
	@Override
	public List<KpiCategory> getKpiCateByCiId(String ciId) throws Exception {

		// 取出所有的KPI放入内存中
		Map<String, KpiInformation> kpiInfoMap = new HashMap<String, KpiInformation>();
		List<KpiInformation> kpiInfos = kpiInfoStorage.getAll();

		// 获取到所有的KPI分类
		Map<String, KpiCategory> kpiCateMap = new HashMap<String, KpiCategory>();
		List<KpiCategory> kpiCates = kpiCateStorage.getAll();

		for (KpiCategory kpiCate : kpiCates) {
			kpiCateMap.put(kpiCate.getId(), kpiCate);
		}

		for (KpiInformation kpiInfo : kpiInfos) {
			// kpiInfoMap.put(kpiInfo.getId(), kpiInfo);
			kpiInfoMap.put(kpiInfo.getKpiHex(), kpiInfo);
		}
		// 存放返回信息
		List<KpiCategory> retTmp = new LinkedList<KpiCategory>();

		List<Map<String, String>> rels = ciKpiRelStorage.getRelByCiId(ciId);
		for (Map<String, String> rel : rels) {
			String kpiHexid = rel.get("kpiId");
			KpiInformation kpiInfo = kpiInfoMap.get(kpiHexid);
			if (kpiInfo != null) {
				retTmp.add(kpiCateMap.get(kpiInfo.getKpiCategoryId()));
			}
		}
		return retTmp;
	}

	/**
	 * 通过CI Id获取到关联此CI的所有的KPI
	 * 
	 * @param ciId
	 * @param kpiCateId
	 * @return List<KpiInformation>
	 * @throws Exception
	 */
	@Override
	public List<KpiInformation> getKpiByKpiCate(String ciId, String kpiCateId)
			throws Exception {

		// 取出所有的KPI放入缓存中
		Map<String, KpiInformation> kpiInfoMap = new HashMap<String, KpiInformation>();
		List<KpiInformation> kpiInfos = kpiInfoStorage.getAll();
		for (KpiInformation kpiInfo : kpiInfos) {
			// kpiInfoMap.put(kpiInfo.getId(), kpiInfo);
			kpiInfoMap.put(kpiInfo.getKpiHex(), kpiInfo);
		}
		// 存放返回信息
		List<KpiInformation> retTmp = new LinkedList<KpiInformation>();
		List<Map<String, String>> rels = ciKpiRelStorage.getRelByCiId(ciId);
		for (Map<String, String> rel : rels) {
			String kpiHexid = rel.get("kpiId");
			KpiInformation kpiInfo = kpiInfoMap.get(kpiHexid);
			// 如果kpi属于指定的kpi分类
			if (kpiInfo != null
					&& (kpiInfo.getKpiCategoryId().equals(kpiCateId))) {
				retTmp.add(kpiInfo);
			}
		}
		return retTmp;
	}

	@Override
	public List<KpiInformation> getKpiByCi(String ciHexId) throws Exception {
		List<KpiInformation> retTmp = new ArrayList<KpiInformation>();
		// 全部的kpi
		Map<String, KpiInformation> kpiInfoMap = new HashMap<String, KpiInformation>();
		try {
			List<KpiInformation> kpiInfos = kpiInfoStorage.getAll();
			for (KpiInformation kpiInfo : kpiInfos) {
				kpiInfoMap.put(kpiInfo.getKpiHex(), kpiInfo);
			}
		} catch (Exception e) {
			throw new Exception("获取全部kpi失败");
		}

		// 获取到所有的KPI分类
		Map<String, KpiCategory> kpiCateMap = new HashMap<String, KpiCategory>();
		try {
			List<KpiCategory> kpiCates = kpiCateStorage.getAll();

			for (KpiCategory kpiCate : kpiCates) {
				kpiCateMap.put(kpiCate.getId(), kpiCate);
			}
		} catch (Exception e) {
			throw new Exception("获取全部kpi分类失败");
		}

		List<Map<String, String>> rels = ciKpiRelStorage.getRelByCiId(ciHexId);
		for (Map<String, String> rel : rels) {
			String kpiHex = rel.get("kpiId");
			KpiInformation kpiInfo = kpiInfoMap.get(kpiHex);
			kpiInfo.setKpiCategory(kpiCateMap.get(kpiInfo.getKpiCategoryId()));
			retTmp.add(kpiInfo);
		}
		return retTmp;
	}

	@Override
	public Map<String, List<KpiInformation>> getKpiByCi(List<String> ciHexIds)
			throws Exception {

		Map<String, List<KpiInformation>> retData = new HashMap<String, List<KpiInformation>>();
		// 全部的kpi
		Map<String, KpiInformation> kpiInfoMap = new HashMap<String, KpiInformation>();
		Map<String, KpiCategory> kpiCateMap = new HashMap<String, KpiCategory>();

		try {
			List<KpiInformation> kpiInfos = kpiInfoStorage.getAll();
			for (KpiInformation kpiInfo : kpiInfos) {
				kpiInfoMap.put(kpiInfo.getKpiHex(), kpiInfo);
			}
		} catch (Exception e) {
			throw new Exception("获取全部kpi失败");
		}

		// 获取到所有的KPI分类
		try {
			List<KpiCategory> kpiCates = kpiCateStorage.getAll();
			for (KpiCategory kpiCate : kpiCates) {
				kpiCateMap.put(kpiCate.getId(), kpiCate);
			}
		} catch (Exception e) {
			throw new Exception("获取全部kpi分类失败");
		}

		for (String ciHexId : ciHexIds) {
			List<KpiInformation> retTmp = new ArrayList<KpiInformation>();

			List<Map<String, String>> rels = ciKpiRelStorage
					.getRelByCiId(ciHexId);

			for (Map<String, String> rel : rels) {
				String kpiHex = rel.get("kpiId");
				KpiInformation kpiInfo = kpiInfoMap.get(kpiHex);
				kpiInfo.setKpiCategory(kpiCateMap.get(kpiInfo
						.getKpiCategoryId()));
				retTmp.add(kpiInfo);
			}

			if (retTmp.size() != 0) {
				retData.put(ciHexId, retTmp);
			}
		}
		return retData;
	}

	/**
	 * 手动注册CI KPI关系
	 * 
	 * @param ciId
	 *            ci的16进制ID
	 * @param kpiIds
	 *            kpi的16进制ID列表
	 */
	@Override
	public void saveCiKpiRel(String ciId, List<String> kpiIds) throws Exception {
		// TODO 后续可以优化成批量保存
		for (String kpiId : kpiIds) {
			// 新加入的应该没有数据所以hasData设为false
			ciKpiRelStorage.addCiKpiRel(ciId, kpiId, false, false);
			// 添加关系
			subscriptionService.addByCiKpiRel(ciId,
					kpiInfoStorage.getKpiByHex(kpiId));
		}
	}

	/**
	 * 手动注册CI KPI关系
	 * 
	 * @param ciId
	 *            ci的16进制ID
	 * @param kpiIds
	 *            kpi的16进制ID列表
	 */
	@Override
	public void saveCiKpiRel(List<String> ciIds, List<String> kpiIds)
			throws Exception {
		for (String ciId : ciIds) {
			// TODO 后续可以优化成批量保存
			for (String kpiId : kpiIds) {
				// 新加入的应该没有数据所以hasData设为false
				ciKpiRelStorage.addCiKpiRel(ciId, kpiId, false, false);
				// 添加关系
				subscriptionService.addByCiKpiRel(ciId,
						kpiInfoStorage.getKpiByHex(kpiId));
			}
		}
	}

	/**
	 * 删除CI KPI关系
	 * 
	 * @param ciId
	 * @param kpiIds
	 */
	@Override
	public void delCiKpiRel(String ciId, List<String> kpiIds) throws Exception {
		for (String kpiId : kpiIds) {
			subscriptionService.deleteByCiKpiRel(ciId, kpiId);
		}
		ciKpiRelStorage.deleteCiKpiRel(ciId, kpiIds);
	}

	/**
	 * 通过关系ID批量删除关系
	 * 
	 * @param relIds
	 * @throws Exception
	 */
	@Override
	public void delCiKpiRelByIds(List<String> relIds) throws Exception {
		List<Map<String, String>> relList = ciKpiRelStorage
				.getRelByRelIds(relIds);
		for (Map<String, String> relMap : relList) {
			String ciId = relMap.get("ciId");
			String kpiId = relMap.get("kpiId");
			subscriptionService.deleteByCiKpiRel(ciId, kpiId);
		}
		ciKpiRelStorage.deleteRelByIds(relIds);
	}

	@Override
	public void delCiKpiRelByCiId(String ciId) throws Exception {
		subscriptionService.deleteByCi(ciId);
		ciKpiRelStorage.deleteCiKpiRelByCi(ciId);
	}

	@Override
	public void delCiKpiRelByKpi(String kpiId) throws Exception {
		subscriptionService.deleteByKpi(kpiId);
		ciKpiRelStorage.deleteCiKpiRelByKpi(kpiId);
	}

	/**
	 * 通过Ci Category的名称删除ci kpi关系
	 * @param ciCategoryName
	 * @throws Exception
	 */
	@Override
	public void delRelByCiCate(String ciCategoryName) throws Exception {
		
		//先获取到符合条件的所有CI信息
		List<String> ciList = ciKpiRelStorage.getCiByCiCate(ciCategoryName,"ci");
		
		//删除视图订阅表中的数据
		for(String ciId:ciList){
			subscriptionService.deleteByCi(ciId);
		}
		
		//删除CI KPI关系表中的关系
		ciKpiRelStorage.deleteRelByCiCateName(ciCategoryName);
		
	}
	
	
	/**
	 * 通过Kpi Category的名称删除ci kpi关系
	 * @param kpiCategoryName
	 * @throws Exception
	 */
	@Override
	public void delRelByKpiCate(String kpiCategoryName) throws Exception {
		
		//先获取到符合条件的所有CI信息
		List<String> kpiList = ciKpiRelStorage.getCiByCiCate(kpiCategoryName,"kpi");
		
		//删除视图订阅表中的数据
		for(String kpiId:kpiList){
			subscriptionService.deleteByKpi(kpiId);
		}
		
		//删除CI KPI关系表中的关系
		ciKpiRelStorage.deleteRelByKpiCateName(kpiCategoryName);
	}
	
	@Override
	public void deletelAll() throws Exception {
		//TODO 删除所有的视图订阅关系
		
		//删除CI KPI关系表中的关系
		ciKpiRelStorage.deleteAll();
	}
	
	@Override
	public void delRelByKpis(List<String> kpiHexs) throws Exception {
		ciKpiRelStorage.delByKpiHexs(kpiHexs);
	}
	
	@Override
	public int getCountByCiHexs(List<String> ciHexs) throws Exception {
		//查这么多次库会死的。。。。
/*		int sum = 0;
		for (String ciHex : ciHexs) {
			sum += ciKpiRelStorage.getCountByCiHex(ciHex);
		}*/
		return ciKpiRelStorage.getCountByCiHex(ciHexs);
	}

	@Override
	public int getCountByCiCate(String ciCateName) throws Exception{
		return ciKpiRelStorage.getCountByCiCate(ciCateName);
	}
	

	@Override
	public List<Map<String, String>> getRelbyCiCate(String ciCateName,
			int page, int pageSize) throws Exception {

		int start = (page - 1) * pageSize;
		List<Map<String, String>> relList = 
				ciKpiRelStorage.getRelByCiCate(ciCateName, start, pageSize);

/*		for (Map<String, String> map : relList) {
			KpiInformation kpiInfo = kpiInfoStorage.getKpiByHex(map
					.get("kpiId"));
			if (kpiInfo != null) {
				Map<String, Object> kpiMap = kpiInfo.toMap();
				kpiMap.put("ciId", map.get("ciId"));
				kpiMap.put("id", map.get("id"));
				kpiMap.put("autoRelation", map.get("autoRelation"));
				kpiMap.put("hasData", map.get("hasData"));
				retData.add(kpiMap);
			}
		}*/
		return relList;
	}
	
	@Override
	public List<Map<String, Object>> getCiKpiRelbyCiHexs(List<String> ciHexs,
			int page, int pageSize) throws Exception {
		List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
		if (ciHexs == null || ciHexs.size() == 0)
			return retData;

		int start = (page - 1) * pageSize;
		List<Map<String, String>> kpiByCiHexs = ciKpiRelStorage.getKpiByCiHexs(
				ciHexs, start, pageSize);

		for (Map<String, String> map : kpiByCiHexs) {
			KpiInformation kpiInfo = kpiInfoStorage.getKpiByHex(map
					.get("kpiId"));
			if (kpiInfo != null) {
				Map<String, Object> kpiMap = kpiInfo.toMap();
				kpiMap.put("ciId", map.get("ciId"));
				kpiMap.put("id", map.get("id"));
				kpiMap.put("autoRelation", map.get("autoRelation"));
				kpiMap.put("hasData", map.get("hasData"));
				retData.add(kpiMap);
			}
		}
		return retData;
	}
	
	/**
	 * 通过ciHex查询所有的关系
	 * 
	 * @param ciHexs
	 * @param page
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, String>> getAllRelbyCiHexs(List<String> ciHexs,
			int page, int pageSize) throws Exception {

		int start = (page - 1) * pageSize;
		List<Map<String, String>> kpiByCiHexs = ciKpiRelStorage.getKpiByCiHexs(
				ciHexs, start, pageSize);
		//List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
		//just for test
/*		for (Map<String, String> map : kpiByCiHexs) {
			Map<String, Object> relMap = new HashMap<String,Object>();
			relMap.put("ciId", map.get("ciId"));
			relMap.put("id", map.get("id"));
			relMap.put("autoRelation", map.get("autoRelation"));
			relMap.put("hasData", map.get("hasData"));
			relMap.put("ciName", map.get("ciName"));
			relMap.put("ciCategoryName", map.get("ciCategoryName"));
			relMap.put("kpiName", map.get("kpiName"));
			relMap.put("kpiCategoryName", map.get("kpiCategoryName"));
			retData.add(relMap);
		}*/
		return kpiByCiHexs;
	}
}
