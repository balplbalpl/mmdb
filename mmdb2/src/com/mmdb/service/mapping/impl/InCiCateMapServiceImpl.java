package com.mmdb.service.mapping.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.framework.neo4j.entity.Dynamic;
import com.mmdb.core.framework.neo4j.proxy.AbstractDomain;
import com.mmdb.core.utils.MapUtil;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.categroy.storage.RelCategoryStorage;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.mapping.storage.InCiCateMapStorage;
import com.mmdb.model.relation.CiRelation;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.mapping.IInCiCateMapService;
import com.mmdb.service.relation.ICiRelService;

@Component("inCiCateMapService")
public class InCiCateMapServiceImpl extends AbstractDomain implements
		IInCiCateMapService {
	@Autowired
	private InCiCateMapStorage imStorage;
	@Autowired
	private ICiInfoService infoService;
	@Autowired
	private ICiRelService relService;
	@Autowired
	private RelCategoryStorage relCategoryStorage;

	@Override
	public List<InCiCateMap> getAll() throws Exception {
		return imStorage.getAll();
	}
	@Override
	public List<InCiCateMap> getByAuthor(String username) throws Exception {
		return imStorage.getByProperty("author", username);
	}
	@Override
	public InCiCateMap getByName(String uName) throws Exception {
		return imStorage.getByName(uName);
	}

	@Override
	public InCiCateMap getById(String id) throws Exception {
		return imStorage.getById(id);
	}

	@Override
	public InCiCateMap save(InCiCateMap im) throws Exception {
		return imStorage.save(im);
	}

	@Override
	public void save(List<InCiCateMap> ims) throws Exception {
		for (InCiCateMap im : ims) {
			imStorage.save(im);
		}
	}

	@Override
	public void delete(InCiCateMap im) throws Exception {
		imStorage.delete(im);
	}

	@Override
	public void deleteAll() throws Exception {
		imStorage.deleteAll();
	}

	@Override
	public InCiCateMap update(InCiCateMap im) throws Exception {
		InCiCateMap nim = imStorage.update(im);
		return nim;
	}

	@Override
	public void runNow(InCiCateMap im, List<CiInformation> infos)
			throws Exception {
		// im = im.unLazy();
		CiCategory sCate = im.getStartCate(), eCate = im.getEndCate();
		im.setRelValue(this.paddingRelValue(im));
		imStorage.update(im);
		String sid = sCate.getId(), eid = eCate.getId();
		for (CiInformation info : infos) {
			String cid = info.getCategoryId();
			if (sid.equals(cid)) {
				// 获取起点映射的值
				Object sVal = info.getData().get(im.getStartCateField());
				if (!sVal.equals("")) {
					Map<String, String> map = MapUtil.hashMap(
							im.getEndCateField(),
							(sVal == null ? null : sVal.toString()));
					// 获取终点分类下与起点值相同的终点数据
					int getPage = 1;
					Map<String, Object> m = infoService.qureyByAdvanced(eCate, map, null, false, null, (getPage-1)*Tool.getBuff, Tool.getBuff);
					List<CiInformation> eInfos = (List<CiInformation>)m.get("data");
					int count = (Integer)m.get("count");
					int index = 0;
					while(true){
						for (CiInformation eInfo : eInfos) {
							// 新建关系
							// info = info.unLazy();
							// eInfo = eInfo.unLazy();
							CiRelation ir = new CiRelation(info, eInfo, im);
							CiRelation cir = relService.getById(ir.getId());
							if (cir == null) {
								relService.save(ir);
							} else {
								// cir = cir.unLazy();
								cir.setOutMapId(null);
								cir.setRelValue(new Dynamic<String, Object>()
										.from(im.getRelValue()));
								cir.setInMapId(im.getName());
								cir.setRelCateId(im.getRelCate().getId());
								relService.update(cir);
							}
							index++;
						}
						if(index>=count){
							break;
						}
						getPage++;
						m = infoService.qureyByAdvanced(eCate, map, null, false, null, (getPage-1)*Tool.getBuff, Tool.getBuff);
						eInfos = (List<CiInformation>)m.get("data");
						count = (Integer)m.get("count");
					}
				}
			} else if (eid.equals(cid)) {
				// 获取起点映射的值
				Object sVal = info.getData().get(im.getEndCateField());
				if (!sVal.equals("")) {
					Map<String, String> map = MapUtil.hashMap(
							im.getStartCateField(),
							(sVal == null ? null : sVal.toString()));
					// 获取终点分类下与起点值相同的终点数据
					int getPage = 1;
					Map<String, Object> m = infoService.qureyByAdvanced(eCate, map, null, false, null, (getPage-1)*Tool.getBuff, Tool.getBuff);
					List<CiInformation> sInfos = (List<CiInformation>)m.get("data");
					int count = (Integer)m.get("count");
					int index = 0;
					while(true){
						for (CiInformation sinfo : sInfos) {
							// 获取起点映射的值
							// 新建关系
							// sinfo = sinfo.unLazy();
							// info = info.unLazy();
							CiRelation ir = new CiRelation(sinfo, info, im);
							CiRelation cir = relService.getById(ir.getId());
							if (cir == null) {
								relService.save(ir);
							} else {
								// cir = cir.unLazy();
								cir.setOutMapId(null);
								cir.setRelValue(new Dynamic<String, Object>()
										.from(im.getRelValue()));
								cir.setInMapId(im.getName());
								cir.setRelCateId(im.getRelCate().getId());
								relService.update(cir);
							}
							index++;
						}
						if(index>=count){
							break;
						}
						getPage++;
						m = infoService.qureyByAdvanced(eCate, map, null, false, null, (getPage-1)*Tool.getBuff, Tool.getBuff);
						sInfos = (List<CiInformation>)m.get("data");
						count = (Integer)m.get("count");
					}
				}
			}
		}
	}

	/**
	 * 处理关系属性空值
	 * 
	 * @param im
	 *            CI内部映射
	 * @return
	 */
	private Map<String, Object> paddingRelValue(InCiCateMap im) {
		List<String> attrs = im.getRelCate().getAttributeNames();
		Map<String, Object> relVal = new HashMap<String, Object>();
		Map<String, Object> relValue = im.getRelValue();
		for (String a : attrs) {
			Object obj = null;
			if (relValue.containsKey(a)) {
				obj = relValue.get(a);
				if (obj == null || obj.equals("")) {
					obj = im.getRelCate().getAttributeByName(a)
							.getDefaultValue();
				}
			} else {
				obj = im.getRelCate().getAttributeByName(a).getDefaultValue();
			}
			relVal.put(a, obj);
		}
		return relVal;
	}

	@Override
	public Map<String, Long> runNow(String imId,
			Map<String, CiCategory> ciCateMap,
			Map<String, RelCategory> relCateMap) throws Exception {
		// System.setErr(new PrintStream(new File("d:/a.txt")));
		InCiCateMap im = getById(imId);
		im.setRelCate(relCateMap.get(im.getRelCateId()));
		im.setStartCate(ciCateMap.get(im.getStartCateId()));
		im.setEndCate(ciCateMap.get(im.getEndCateId()));
		String owner = im.getOwner();
		long updateTime = 0;
		// im = im.unLazy();
		Map<String, Long> retMap = null;
		// long snum = 0, unum = 0;
		
		CiCategory sCate = im.getStartCate(), eCate = im.getEndCate();
		RelCategory relCate = im.getRelCate();
		im.setRelValue(this.paddingRelValue(im));
		imStorage.update(im);
		Map<Object, List<CiInformation>> eInfoMapCache = new HashMap<Object, List<CiInformation>>();
		Map<String, CiRelation> ciRelMapCache = new HashMap<String, CiRelation>();
		List<CiRelation> ciRelCache = relService.getAll();
		for (CiRelation ciRel : ciRelCache) {
			ciRelMapCache.put(ciRel.getId(), ciRel);
		}
		Set<CiRelation> newCiRel = new HashSet<CiRelation>();
		
		int sPage = 1;
		Map<String, Object> sm = infoService.qureyByAdvancedEQ(sCate, null, null, false, null, (sPage-1)*Tool.getBuff, Tool.getBuff);
		List<CiInformation> sInfos = (List<CiInformation>)sm.get("data");
		int sCount = (Integer)sm.get("count");
		int sIndex = 0;
		while(true){
			for (CiInformation sinfo : sInfos) {
				long sst = System.currentTimeMillis();
				// 获取起点映射的值
				Object sVal = sinfo.getData().get(im.getStartCateField());

				if (!sVal.equals("")) {
					Map<String, String> map = MapUtil.hashMap(im.getEndCateField(),
							(sVal == null ? null : sVal.toString()));
					// 获取终点分类下与起点值相同的终点数据
					List<CiInformation> eInfos = eInfoMapCache.get(sVal);
					if (eInfos == null) {
						eInfos = new ArrayList<CiInformation>();
						int ePage = 1;
						Map<String, Object> em = infoService.qureyByAdvancedEQ(eCate, map, null, false, null, (ePage-1)*Tool.getBuff, Tool.getBuff);
						List<CiInformation> eList = (List<CiInformation>)em.get("data");
						int eCount = (Integer)em.get("count");
						int eIndex = 0;
						while(true){
							for(CiInformation einfo : eList){
								eInfos.add(einfo);
								eIndex++;
							}
							if(eIndex>=eCount){
								break;
							}
							ePage++;
							em = infoService.qureyByAdvancedEQ(eCate, map, null, false, null, (ePage-1)*Tool.getBuff, Tool.getBuff);
							eList = (List<CiInformation>)em.get("data");
							eCount = (Integer)em.get("count");
						}
						eInfoMapCache.put(sVal, eInfos);
					}

					for (CiInformation eInfo : eInfos) {
						CiRelation ciRelation = new CiRelation(sinfo, eInfo, im);
						ciRelation.setOwner(owner);
						newCiRel.add(ciRelation);
					}
				}
				sIndex++;
			}
			if(sIndex>=sCount){
				break;
			}
			sPage++;
			sm = infoService.qureyByAdvancedEQ(sCate, null, null, false, null, (sPage-1)*Tool.getBuff, Tool.getBuff);
			sInfos = (List<CiInformation>)sm.get("data");
			sCount = (Integer)sm.get("count");
		}
		
		retMap = relService.saveOrUpdate(relCate, newCiRel);
		return retMap;
	}

	@Override
	public List<InCiCateMap> getByRelCate(RelCategory rc) throws Exception {
		List<InCiCateMap> ims = new ArrayList<InCiCateMap>();
		List<InCiCateMap> cMaps = imStorage.getAll();
		String rl = rc.getName();
		for (InCiCateMap cMap : cMaps) {
			String nid = cMap.getRelCate().getName();
			if (rl.equals(nid)) {
				ims.add(cMap);
			}
		}
		return ims;
	}

	@Override
	public List<String> getRelCateIds() throws Exception {
		List<String> retList = new ArrayList<String>();
		List<InCiCateMap> cMaps = imStorage.getAll();

		List<RelCategory> all = relCategoryStorage.getAll();
		Map<String, RelCategory> cache = new HashMap<String, RelCategory>();
		for (RelCategory relCate : all) {
			cache.put(relCate.getId(), relCate);
		}
		all = null;

		for (InCiCateMap cMap : cMaps) {
			RelCategory relCategory = cache.get(cMap.getRelCateId());
			if (relCategory != null) {
				String nid = relCategory.getName();
				if (!retList.contains(nid)) {
					retList.add(nid);
				}
			}
		}
		return retList;
	}

	@Override
	public List<RelCategory> getRelCates() throws Exception {
		List<RelCategory> retList = new ArrayList<RelCategory>();
		List<InCiCateMap> cMaps = imStorage.getAll();
		for (InCiCateMap cMap : cMaps) {
			RelCategory rt = cMap.getRelCate();
			if (!retList.contains(rt)) {
				retList.add(rt);
			}
		}
		return retList;
	}

	@Override
	public List<InCiCateMap> getMapsByCate(CiCategory category)
			throws Exception {
		List<InCiCateMap> retList = new ArrayList<InCiCateMap>();
		String id = category.getId();
		List<InCiCateMap> cMaps = imStorage.getAll();
		for (InCiCateMap cMap : cMaps) {
			String sid = cMap.getStartCateId();
			String eid = cMap.getEndCateId();
			if (sid.equals(id) || eid.equals(id)) {
				if (!retList.contains(cMap)) {
					retList.add(cMap);
				}
			}
		}
		return retList;
	}

	@Override
	public boolean exist(String id) {
		return imStorage.exist(id);
	}

	@Override
	public void update(List<InCiCateMap> ims) throws Exception {
		if (ims != null && ims.size() > 0)
			imStorage.update(ims);
	}
}
