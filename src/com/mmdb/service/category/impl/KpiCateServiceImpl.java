package com.mmdb.service.category.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.categroy.storage.KpiCateStorage;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.model.info.storage.KpiInfoStorage;
import com.mmdb.service.category.IKpiCateService;
import com.mmdb.service.relation.ICiKpiRelService;

/**
 * KPI分类 服务 - 实现类
 * 
 * @version 1.0 2015-7-10
 */
@Component("kpiCateService")
public class KpiCateServiceImpl implements IKpiCateService {
	@Autowired
	private KpiCateStorage cateStorage;
	@Autowired
	private KpiInfoStorage infoStorage;
	@Autowired
	private ICiKpiRelService ciKpiRelService;

	@Override
	public KpiCategory save(KpiCategory nCategory) throws Exception {
		return cateStorage.save(nCategory);
	}

	@Override
	public KpiCategory update(KpiCategory nCategory) throws Exception {
		return cateStorage.update(nCategory);
	}

	@Override
	public long delete(KpiCategory nCategory, boolean bool) throws Exception {
		long i = 0;
		if (bool) {
			deleteByCategory(nCategory);
		}
		cateStorage.delete(nCategory);
		return i;
	}

	@Override
	public void deleteAll() throws Exception {
		cateStorage.deleteAll();
	}

	@Override
	public KpiCategory getById(String id) throws Exception {
		return cateStorage.getById(id);
	}

	@Override
	public KpiCategory getByName(String name) throws Exception {
		return cateStorage.getByName(name);
	}

	@Override
	public List<KpiCategory> getAll() throws Exception {
		return cateStorage.getAll();
	}
	
	@Override
	public List<KpiCategory> getKpiCateByUserName(String userName) throws Exception {
		return cateStorage.getByUser(userName);
	}

	@Override
	public List<KpiInformation> getKpiByCategory(String cateId)
			throws Exception {
		return infoStorage.getByCategory(cateId);
	}

	@Override
	public KpiInformation getKpiById(String kpiId) throws Exception {
		return infoStorage.getKpiById(kpiId);
	}

	@Override
	public List<KpiInformation> getKpiByIds(List<String> kpiIds) throws Exception {
		return infoStorage.getByKpiIds(kpiIds);
	}
	
	@Override
	public KpiInformation getKpiByName(String cateId,String name) throws Exception {
		return infoStorage.getKpiByName(cateId,name);
	}

	@Override
	public KpiInformation getKpiByHex(String hexId) throws Exception {
		return infoStorage.getKpiByHex(hexId);
	}

	@Override
	public List<KpiInformation> find(String cateId, String param, 
			String userName,int page,int pageSize)throws Exception {
		int start = -1;;
		if(page != -1){
			start = (page - 1) * pageSize;
		}
		return infoStorage.find(cateId, param, userName,start,pageSize);
	}

	@Override
	public int countFind(String cateId, String param, 
			String userName)throws Exception {
		return infoStorage.countFind(cateId, param, userName);
	}
	
	@Override
	public List<KpiInformation> findBySql(String whereParam) throws Exception {
		return infoStorage.findBySql(whereParam);
	}
	
	@Override
	public List<KpiInformation> getKpiByUserName(String userName) throws Exception {
		return infoStorage.getByUser(userName);
	}
	
	@Override
	public List<KpiInformation> findAllByCate(String cateId, String param,
			String userName,int page, int pageSize) throws Exception{
		
		List<KpiCategory> cateList = cateStorage.getAllHasChildren();
		KpiCategory category = null;
		for(KpiCategory cate: cateList){
			if(cateId.equals(cate.getId())){
				category = cate;
			}
		}
		int start = (page - 1) * pageSize;
		StringBuffer match = new StringBuffer();
		// 用于判断是否出现继承和是否有categroyid这个条件
		List<String> cgIds = new ArrayList<String>();
		if (category != null) {
			cgIds.add(category.getId());
			List<KpiCategory> children = category.getAllChildren();
			for (KpiCategory child : children) {
				cgIds.add(child.getId());
			}
		}
		if (cgIds.size() != 0) {
			match.append("(");
			for (String cgid : cgIds) {
				match.append(" kpiCategoryId = '");
				match.append(cgid);
				match.append("' or");
			}
			match.delete(match.length() - 2, match.length());
			match.append(")");
		}
		
		if (param != null && param.length() > 0) {
				match.append(" and name like '%" + param + "%'");
		}
		
		if (userName != null && userName.length() > 0) {
			match.append(" and owner = '" + userName + "'");
		}
		if(page != -1){
			match.append(" order by name limit "+start+ ","+pageSize);
		}
		return infoStorage.findBySql(match.toString());
	}

	
	@Override
	public int countFindAllByCate(String cateId, String param,
			String userName) throws Exception{
		
		List<KpiCategory> cateList = cateStorage.getAllHasChildren();
		KpiCategory category = null;
		for(KpiCategory cate: cateList){
			if(cateId.equals(cate.getId())){
				category = cate;
			}
		}
		
		StringBuffer match = new StringBuffer();
		// 用于判断是否出现继承和是否有categroyid这个条件
		List<String> cgIds = new ArrayList<String>();
		if (category != null) {
			cgIds.add(category.getId());
			List<KpiCategory> children = category.getAllChildren();
			for (KpiCategory child : children) {
				cgIds.add(child.getId());
			}
		}
		if (cgIds.size() != 0) {
			match.append("(");
			for (String cgid : cgIds) {
				match.append(" kpiCategoryId = '");
				match.append(cgid);
				match.append("' or");
			}
			match.delete(match.length() - 2, match.length());
			match.append(")");
		}
		
		if (param != null && param.length() > 0) {
				match.append(" and name like '%" + param + "%'");
		}
		
		if (userName != null && userName.length() > 0) {
			match.append(" and owner = '" + userName + "'");
		}
		return infoStorage.countBySql(match.toString());
	}

	
	@Override
	public KpiInformation save(KpiInformation info) throws Exception {
		return infoStorage.save(info);
	}

	@Override
	public KpiInformation update(KpiInformation info) throws Exception {
		return infoStorage.update(info);
	}

	@Override
	public void delete(KpiInformation info) throws Exception {
		// 处理关系
		ciKpiRelService.delCiKpiRelByKpi(info.getKpiHex());
		infoStorage.delete(info);
	}

	@Override
	public void deleteByCategory(KpiCategory cate) throws Exception {
		/*List<String> kpiHexs = infoStorage.getHexByCateId(cate.getId());
		for (String kpiHex : kpiHexs) {
			ciKpiRelService.delCiKpiRelByKpi(kpiHex);
		}*/
		//删除分类下KPI关联的ci kpi关系
		ciKpiRelService.delRelByKpiCate(cate.getName());
		infoStorage.deleteByCategory(cate);
	}

	@Override
	public void deleteAllKpi() throws Exception {
		
		ciKpiRelService.deletelAll();
		infoStorage.deleteAllKpi();
	}

	@Override
	public void deleteKpiByIds(List<String> kpiIds) throws Exception {
		/*for (String id : kpiIds) {
			String hexById = infoStorage.getHexById(id);
			ciKpiRelService.delCiKpiRelByKpi(hexById);
		}*/
		//通过ID列表批量查询KPI
		List<KpiInformation> kpis = infoStorage.getByKpiIds(kpiIds);
		for(KpiInformation kpi:kpis){
			String kpiHex = kpi.getKpiHex();
			//删除ci kpi关联关系
			ciKpiRelService.delCiKpiRelByKpi(kpiHex);
		}
		infoStorage.deleteKpiByIds(kpiIds);
	}
	
	@Override
	public Map<String, Long> saveOrUpdate(KpiCategory category,
			List<KpiInformation> informations) throws Exception {
		long stime = System.currentTimeMillis();
		int save = 0, update = 0;
		List<List<KpiInformation>> saveOrUpdate = infoStorage.saveOrUpdate(
				category, informations);
		List<KpiInformation> crt = saveOrUpdate.get(0);
		List<KpiInformation> upd = saveOrUpdate.get(1);
		save = crt.size();
		update = upd.size();

		List<KpiInformation> infos = new ArrayList<KpiInformation>();
		if (crt != null)
			infos.addAll(crt);
		if (upd != null)
			infos.addAll(upd);
		if (infos.size() != informations.size()) {
			new RuntimeException("infos.size() =" + infos.size()
					+ "  informations.size() =" + informations.size());
		}

		Map<String, Long> retMap = new HashMap<String, Long>();
		// TODO 关系尚未处理
		retMap.put("save", (long) save);
		retMap.put("update", (long) update);
		System.out.println("批量上传配置项[" + category.getId() + "],耗时:"
				+ (System.currentTimeMillis() - stime));
		return retMap;
	}
	
	@Override
	public List<KpiInformation> getAllKpi() throws Exception {
		return infoStorage.getAll();
	}
}
