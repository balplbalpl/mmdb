package com.mmdb.service.info.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.exception.MException;
import com.mmdb.model.bean.Attribute;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.storage.CiCateStorage;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.storage.CiInfoStorage;
import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.relation.CiRelation;
import com.mmdb.model.relation.storage.CiKpiRelStorage;
import com.mmdb.model.relation.storage.CiRelStorage;
import com.mmdb.model.task.Task;
import com.mmdb.model.task.TaskStorage;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.mapping.IInCiCateMapService;
import com.mmdb.service.relation.ICiKpiRelService;
import com.mmdb.service.relation.ICiRelService;
import com.mmdb.service.relation.ICiViewRelService;
import com.mmdb.service.relation.impl.CiRelServiceImpl;
import com.mmdb.util.Neo4jStringUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * 配置项 数据 - 实现类
 * 
 * @author XIE
 */
@Component("ciInfoService")
public class CiInfoServiceImpl implements ICiInfoService {
	@Autowired
	private CiInfoStorage infoStorage;
	@Autowired
	private CiCateStorage cateStorage;
	@Autowired
	private TaskStorage taskStorage;
	@Autowired
	private IInCiCateMapService imService;
	@Autowired
	private ICiKpiRelService ciKpiRelService;
	@Autowired
	private ICiViewRelService ciViewRelService;
	@Autowired
	private CiKpiRelStorage ciKpiRelStorage;
	@Autowired
	private CiRelStorage ciRelStorage;
	

	@Override
	public CiInformation save(CiInformation information) throws Exception {
		CiInformation has = infoStorage.getByJsonId(information.getCiHex());
		if (has != null) {
			throw new MException("数据[" + information.getName() + "]已存在");
		} else {
			CiCategory category = information.getCategory();
			CiInformation info = infoStorage.save(information);
			info.setCategory(category);
			List<CiInformation> infos = new ArrayList<CiInformation>();
			infos.add(info);
			List<Task> ts = taskStorage.getAll();
			for (Task t : ts) {
				if (t.getOpen()) {
					List<InCiCateMap> ims = t.getInCiCateMap();
					for (InCiCateMap im : ims) {
						imService.runNow(im, infos);
					}
				}
			}
			return info;
		}
	}

	@Override
	public void save(List<CiInformation> informations) throws Exception {
		List<CiInformation> infos = new ArrayList<CiInformation>();
		for (CiInformation information : informations) {
			CiCategory nc = information.getCategory();
			if (nc == null) {
				throw new Exception("数据分类不能为空");
			}
			CiInformation has = infoStorage.getInfoInCate(nc.getId(),
					information.getId());
			if (has != null) {
				throw new Exception("数据[" + information.getId() + "]已存在");
			} else {
				CiInformation info = infoStorage.save(information);
				infos.add(info);
				// 发送消息
				// CiInfoMsg msg = new CiInfoMsg(info);
				// msg.ADD();
			}
		}
		List<Task> ts = taskStorage.getAll();
		for (Task t : ts) {
			if (t.getOpen()) {
				List<InCiCateMap> ims = t.getInCiCateMap();
				for (InCiCateMap im : ims) {
					imService.runNow(im, infos);
				}
			}
		}
	}

	@Override
	public List<CiInformation> getByProperty(String key, Object value)
			throws Exception {
		List<CiInformation> infos = infoStorage.getByProperty(key, value);
		if (infos != null) {
			Map<String, CiCategory> allMap = cateStorage.getAllMap();
			for (CiInformation info : infos) {
				info.setCategory(allMap.get(info.getCategoryId()));
			}
		}
		return infos != null ? infos : new ArrayList<CiInformation>();
	}

	@Override
	public CiInformation getById(String jsonId) throws Exception {
		CiInformation info = infoStorage.getByJsonId(jsonId);
		if (info != null) {
			CiCategory ciCategory = cateStorage.getById(info.getCategoryId());
			info.setCategory(ciCategory);
		}
		return info;
	}

	@Override
	public List<CiInformation> getByCategory(CiCategory cate) throws Exception {
		List<CiInformation> cinfos = infoStorage.getByCategory(cate.getId());
		for (CiInformation info : cinfos) {
			info.setCategory(cate);
		}
		return cinfos;
	}

	@Override
	public Map<String, Object> qureyByAdvanced(CiCategory category,
			Map<String, String> must, Map<String, String> or, boolean extend,
			String username, int start, int limit) throws Exception {
		// 'cate1' and 'must' and ('or1' or 'or2')
		boolean queryAll = true;// 当参数没有值的时候,就查询全部的.
		StringBuffer match = new StringBuffer("select * from Ci where ");
		// 用于判断是否出现继承和是否有categroyid这个条件
		List<String> cgIds = new ArrayList<String>();
		Map<String, CiCategory> cache = new HashMap<String, CiCategory>();
		if (category != null) {
			cache.put(category.getId(), category);
			cgIds.add(category.getId());
			if (extend) {// ciCate是否继承
				List<CiCategory> children = category.getAllChildren();
				for (CiCategory child : children) {
					cgIds.add(child.getId());
					cache.put(child.getId(), child);
				}
			}
		}

		if (cgIds.size() != 0) {
			queryAll = false;
			match.append("(");
			for (String cgid : cgIds) {
				match.append(" categoryId = '");
				match.append(cgid);
				match.append("' or");
			}
			match.delete(match.length() - 2, match.length());
			match.append(")");
		}

		// 必要字段
		if (must != null && must.size() > 0) {
			if (!queryAll) {// 出现了categoryid where n.xx =='xx' and
				match.append(" and");
			}
			queryAll = false;
			if (must.containsKey("*") && category != null) {
				String value = transNeo4jValue(must.get("*"));
				List<String> attrs = category.getAttributeNames();
				if (attrs.size() > 0) {
					for (String attr : attrs) {
						match.append("`data$");
						match.append(attr);
						match.append("` like '");
						match.append(value);
						match.append("' and");
					}
				}
			} else {
				for (Entry<String, String> entry : must.entrySet()) {
					String key = entry.getKey().trim();
					String value = transNeo4jValue(entry.getValue());

					if ("scene".equals(key) || "categoryId".equals(key)
							|| "id".equals(key) || "tag".equals(key)
							|| "name".equals(key) || "source".equals(key)
							|| "createTime".equals(key)
							|| "updateTime".equals(key) || "record".equals(key)
							|| "relCateId".equals(key)) {
						match.append(" `");
					} else {
						match.append(" `data$");
					}
					match.append(key);
					match.append("` like '");
					match.append(value);
					match.append("' and");
				}
			}
			match.delete(match.length() - 3, match.length());// 去掉一个多余的and
		}//

		if (or != null && or.size() > 0) {
			if (!queryAll) {// 出现了categoryid where n.xx =='xx' and
				match.append(" and( ");
			}
			queryAll = false;
			if (or.containsKey("*") && category != null) {
				String value = transNeo4jValue(or.get("*"));
				List<String> attrs = category.getAttributeNames();
				for (String attr : attrs) {
					match.append(" `data$");
					match.append(attr);
					match.append("` like '");
					match.append(value);
					match.append("' or");
				}
			} else {
				for (Entry<String, String> entry : or.entrySet()) {
					String key = entry.getKey().trim();
					String value = transNeo4jValue(entry.getValue());
					// match.append(" `data$");
					if ("scene".equals(key) || "categoryId".equals(key)
							|| "id".equals(key) || "tag".equals(key)
							|| "name".equals(key) || "source".equals(key)
							|| "createTime".equals(key)
							|| "updateTime".equals(key) || "record".equals(key)
							|| "relCateId".equals(key)) {
						match.append(" `");
					} else {
						match.append(" `data$");
					}
					match.append(key);
					match.append("` like '");
					match.append(value);
					match.append("' or");
				}
			}
			match.delete(match.length() - 2, match.length());// 去掉一个多余的or
			match.append(")");
		}

		if (username != null) {
			if (!queryAll) {
				match.append(" and");
			}
			match.append(" owner='" + username + "' ");
			queryAll = false;
		}

		if (queryAll) {
			match.delete(match.length() - 6, match.length());// 将多出的where 删除掉
		}
		
		Integer count = infoStorage.queryCount(match.toString());
		match.append(" order by categoryName,id limit "+start+" , "+limit);
		List<CiInformation> cinfos = infoStorage.query(match.toString());
		for (CiInformation info : cinfos) {
			info.setCategory(cache.get(info.getCategoryId()));
		}
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("data", cinfos);
		ret.put("count", count);
		return ret;
	}
	
	@Override
	public Map<String, Object> qureyByAdvancedEQ(CiCategory category,
			Map<String, String> must, Map<String, String> or, boolean extend,
			String username, int start, int limit) throws Exception {
		// 'cate1' and 'must' and ('or1' or 'or2')
		boolean queryAll = true;// 当参数没有值的时候,就查询全部的.
		StringBuffer match = new StringBuffer("select * from Ci where ");
		// 用于判断是否出现继承和是否有categroyid这个条件
		List<String> cgIds = new ArrayList<String>();
		Map<String, CiCategory> cache = new HashMap<String, CiCategory>();
		if (category != null) {
			cache.put(category.getId(), category);
			cgIds.add(category.getId());
			if (extend) {// ciCate是否继承
				List<CiCategory> children = category.getAllChildren();
				for (CiCategory child : children) {
					cgIds.add(child.getId());
					cache.put(child.getId(), child);
				}
			}
		}

		if (cgIds.size() != 0) {
			queryAll = false;
			match.append("(");
			for (String cgid : cgIds) {
				match.append(" categoryId = '");
				match.append(cgid);
				match.append("' or");
			}
			match.delete(match.length() - 2, match.length());
			match.append(")");
		}

		// 必要字段
		if (must != null && must.size() > 0) {
			if (!queryAll) {// 出现了categoryid where n.xx =='xx' and
				match.append(" and");
			}
			queryAll = false;
			if (must.containsKey("*") && category != null) {
				String value = transNeo4jValue(must.get("*"));
				List<String> attrs = category.getAttributeNames();
				if (attrs.size() > 0) {
					for (String attr : attrs) {
						match.append("`data$");
						match.append(attr);
						match.append("` = '");
						match.append(value);
						match.append("' and");
					}
				}
			} else {
				for (Entry<String, String> entry : must.entrySet()) {
					String key = entry.getKey().trim();
					String value = transNeo4jValue(entry.getValue());

					if ("scene".equals(key) || "categoryId".equals(key)
							|| "id".equals(key) || "tag".equals(key)
							|| "name".equals(key) || "source".equals(key)
							|| "createTime".equals(key)
							|| "updateTime".equals(key) || "record".equals(key)
							|| "relCateId".equals(key)) {
						match.append(" `");
					} else {
						match.append(" `data$");
					}
					match.append(key);
					match.append("` = '");
					match.append(value);
					match.append("' and");
				}
			}
			match.delete(match.length() - 3, match.length());// 去掉一个多余的and
		}//

		if (or != null && or.size() > 0) {
			if (!queryAll) {// 出现了categoryid where n.xx =='xx' and
				match.append(" and( ");
			}
			queryAll = false;
			if (or.containsKey("*") && category != null) {
				String value = transNeo4jValue(or.get("*"));
				List<String> attrs = category.getAttributeNames();
				for (String attr : attrs) {
					match.append(" `data$");
					match.append(attr);
					match.append("` = '");
					match.append(value);
					match.append("' or");
				}
			} else {
				for (Entry<String, String> entry : or.entrySet()) {
					String key = entry.getKey().trim();
					String value = transNeo4jValue(entry.getValue());
					// match.append(" `data$");
					if ("scene".equals(key) || "categoryId".equals(key)
							|| "id".equals(key) || "tag".equals(key)
							|| "name".equals(key) || "source".equals(key)
							|| "createTime".equals(key)
							|| "updateTime".equals(key) || "record".equals(key)
							|| "relCateId".equals(key)) {
						match.append(" `");
					} else {
						match.append(" `data$");
					}
					match.append(key);
					match.append("` = '");
					match.append(value);
					match.append("' or");
				}
			}
			match.delete(match.length() - 2, match.length());// 去掉一个多余的or
			match.append(")");
		}

		if (username != null) {
			if (!queryAll) {
				match.append(" and");
			}
			match.append(" owner='" + username + "' ");
			queryAll = false;
		}

		if (queryAll) {
			match.delete(match.length() - 6, match.length());// 将多出的where 删除掉
		}
		
		Integer count = infoStorage.queryCount(match.toString());
		match.append(" order by categoryname,id limit "+start+" , "+limit);
		List<CiInformation> cinfos = infoStorage.query(match.toString());
		for (CiInformation info : cinfos) {
			info.setCategory(cache.get(info.getCategoryId()));
		}
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("data", cinfos);
		ret.put("count", count);
		return ret;
	}

	@Override
	public List<CiInformation> qureyByTerm(Map<String, String> must,
			Map<String, String> mustNot) throws Exception {
		boolean queryAll = true;// 当参数没有值的时候,就查询全部的.
		StringBuffer match = new StringBuffer("select * from Ci where ");
		if (must != null && must.size() != 0) {
			queryAll = false;
			Set<String> keySet = must.keySet();
			for (String key : keySet) {
				match.append(" `data$");
				match.append(key);
				match.append("` = '");
				match.append(must.get(key));
				match.append("' and");
			}
		}

		if (mustNot != null && mustNot.size() != 0) {
			queryAll = false;
			Set<String> keySet = mustNot.keySet();
			for (String key : keySet) {
				match.append(" `data$");
				match.append(key);
				match.append("` != '");
				match.append(mustNot.get(key));
				match.append("' and");
			}
		}
		if (queryAll) {
			match.delete(match.length() - 6, match.length());// 将多出的where 删除掉
		} else {
			match.delete(match.length() - 3, match.length());
		}

		Map<String, CiCategory> cates = cateStorage.getAllMap();
		List<CiInformation> cInfos = infoStorage.query(match.toString());
		for (CiInformation info : cInfos) {
			info.setCategory(cates.get(info.getCategoryId()));
		}
		return cInfos;
	}

	@Override
	public List<CiInformation> qureyByFuzzy(Map<String, String> must,
			Map<String, String> or) throws Exception {

		Set<String> attrs = new HashSet<String>();
		Set<String> classAttr = new HashSet<String>();
		classAttr.add("id");
		classAttr.add("categoryName");
		List<CiCategory> ciCategories = cateStorage.getAll();
		BasicDBList mustC = null;
		BasicDBList orC = null;

		if (must != null && must.size() > 0) {
			mustC = new BasicDBList();
			if (must.containsKey("*")) {
				String value = transNativeMongoValue(must.get("*"));

				for (String attr : attrs) {
					mustC.add(new BasicDBObject("data$" + attr,
							new BasicDBObject("$regex", value)));
				}

				for (String attr : classAttr) {
					mustC.add(new BasicDBObject(attr, new BasicDBObject(
							"$regex", value)));
				}

			} else {
				for (Entry<String, String> entry : must.entrySet()) {
					String key = entry.getKey().trim();
					String value = transNativeMongoValue(entry.getValue());

					mustC.add(new BasicDBObject("data$" + key,
							new BasicDBObject("$regex", value)));
				}
			}
		}

		if (or != null && or.size() > 0) {
			orC = new BasicDBList();

			if (or.containsKey("*")) {
				for (CiCategory ciCategory : ciCategories) {
					List<String> as = ciCategory.getAttributeNames();
					for (String a : as) {
						attrs.add(a);
					}
				}

				String value = transNativeMongoValue(or.get("*"));
				for (String attr : attrs) {
					orC.add(new BasicDBObject("data$" + attr,
							new BasicDBObject("$regex", value)));
				}
				for (String attr : classAttr) {
					orC.add(new BasicDBObject(attr, new BasicDBObject("$regex",
							value)));
				}
			} else {
				for (Entry<String, String> entry : or.entrySet()) {
					String key = entry.getKey().trim();
					String value = transNativeMongoValue(entry.getValue());

					orC.add(new BasicDBObject("data$" + key, new BasicDBObject(
							"$regex", value)));
				}
			}
		}
		if (mustC == null && orC == null) {
			return new ArrayList<CiInformation>();
		}

		if (mustC == null) {
			return infoStorage.query(new BasicDBObject("$or", orC));
		} else if (orC == null) {
			return infoStorage.query(new BasicDBObject("$or", mustC));
		} else {
			BasicDBObject query = new BasicDBObject();
			BasicDBList and = new BasicDBList();
			query.append("$and", and);
			and.add(new BasicDBObject("$or", mustC));
			and.add(new BasicDBObject("$or", orC));
			return infoStorage.query(query);
		}
	}

	@Override
	public List<CiInformation> qureyByWhereSQL(String whereParam)
			throws Exception {
		return infoStorage.queryByWhereParam(whereParam);
	}

	@Override
	//
	public void delete(CiInformation information) throws Exception {
		String hexId = information.getCiHex();
		// 删除ci与视图的关系
		ciViewRelService.deleteByCi(hexId);
		// 删除ci与kpi的关系
		ciKpiRelService.delCiKpiRelByCiId(hexId);
		List<String> ciHexs = new ArrayList<String>();
		ciHexs.add(hexId);
		ciRelStorage.delCiRelByCis(ciHexs);
		infoStorage.delete(information);
	}

	@Override
	//
	public void delete(List<CiInformation> informations) throws Exception {
		List<String> ciHexs = new ArrayList<String>();
		for (CiInformation cInfo : informations) {
			String hexId = cInfo.getCiHex();
			// 删除ci与视图的关系
			ciViewRelService.deleteByCi(hexId);
			// 删除ci与kpi的关系
			ciKpiRelService.delCiKpiRelByCiId(hexId);
			ciHexs.add(hexId);
		}
		ciRelStorage.delCiRelByCis(ciHexs);
		infoStorage.delete(informations);
	}

	@Override
	public void deleteByJsonIds(List<String> jsonIds) throws Exception {
		for (String hexid : jsonIds) {
			// 删除ci与视图的关系
			ciViewRelService.deleteByCi(hexid);
			// 删除ci与kpi的关系
			ciKpiRelService.delCiKpiRelByCiId(hexid);
		}
		ciRelStorage.delCiRelByCis(jsonIds);
		infoStorage.deleteByJsonIds(jsonIds);
	}

	@Override
	//
	public void clearAll() throws Exception {
		// 删除全部的ci与view的关系,和ci与kpi的关系
		infoStorage.deleteAll();
		ciRelStorage.deleteAll();
		ciKpiRelStorage.deleteAll();
		ciViewRelService.deleteAll();
	}

	/**
	 * 检测参数是否符合要求
	 * 
	 * @param info
	 *            CI数据
	 * @param data
	 *            新的数据
	 * @throws Exception
	 */
	private Map<String, Object> checkParameter(CiInformation info,
			Map<String, Object> data) throws Exception {
		Map<String, Object> ret = new HashMap<String, Object>();
		CiCategory category = info.getCategory();
		// 处理id,或许可能是继承id
		Attribute major = category.getMajor();
		String majorName = major.getName();
		if (data.containsKey(majorName)) {// 判断数据中对象的主键
			Object id = data.get(majorName);// 把主键的值做为该分类数据的唯一标识
			if (id == null || id.equals("")) {
				throw new Exception("主键不能为空");
			}
			try {
				id = major.convert(id.toString());
				ret.put(majorName, id);
			} catch (Exception e) {
				throw new Exception("数据[" + data + "]" + e.getMessage());
			}
		} else {
			throw new Exception("数据[" + data + "],缺少分类[" + category.getName()
					+ "]的主键[" + majorName + "]数据");
		}
		// 处理属性，或许有继承属性
		List<Attribute> attributes = category.getAllAttributes();
		for (Attribute attribute : attributes) {
			String field = attribute.getName();
			String value = data.containsKey(field) ? data.get(field).toString()
					: attribute.getDefaultValue();
			if (attribute.getRequired() && value.equals("")) {
				throw new Exception("分类[" + category.getName() + "]中属性["
						+ field + "]是必填项");
			}
			ret.put(field, attribute.convert(value));
		}
		return ret;
	}

	@Override
	public CiInformation update(CiInformation information,
			Map<String, Object> data, String source) throws Exception {
		CiInformation info = information;
		info.setData(this.checkParameter(information, data));
		info.setSource(source);
		CiCategory category = info.getCategory();
		info =infoStorage.update(info);
		info.setCategory(category);
		return info;
	}

	@Override
	public Map<String, Long> saveOrUpdate(CiCategory category,
			List<CiInformation> informations) throws Exception {
		long stime = System.currentTimeMillis();
		int save = 0, update = 0;
		List<List<CiInformation>> saveOrUpdate = infoStorage.saveOrUpdate(
				category.getId(), informations);
		List<CiInformation> crt = saveOrUpdate.get(0);
		List<CiInformation> upd = saveOrUpdate.get(1);
		save = crt.size();
		update = upd.size();

		List<CiInformation> infos = new ArrayList<CiInformation>();
		if (crt != null)
			infos.addAll(crt);
		if (upd != null)
			infos.addAll(upd);
		if (infos.size() != informations.size()) {
			new RuntimeException("infos.size() =" + infos.size()
					+ "  informations.size() =" + informations.size());
		}

		Map<String, Long> retMap = new HashMap<String, Long>();
		List<Task> ts = taskStorage.getAll();
		for (Task t : ts) {
			if (t.getOpen()) {
				List<InCiCateMap> ims = t.getInCiCateMap();
				for (InCiCateMap im : ims) {
					imService.runNow(im, infos);
				}
			}
		}
		// TODO 关系尚未处理
		retMap.put("save", (long) save);
		retMap.put("update", (long) update);
		System.out.println("批量上传配置项[" + category.getId() + "],耗时:"
				+ (System.currentTimeMillis() - stime));
		return retMap;
	}

	@Override
	public void updateInfos(List<CiInformation> infos) throws Exception {
		infoStorage.updateInfo(infos);
	}

	/**
	 * 
	 * @param CategoryId
	 * @throws Exception
	 */
	@Override
	public void deleteCiByCategory(CiCategory category) throws Exception {
		ciKpiRelService.delRelByCiCate(category.getName());
		ciRelStorage.delCiRelByCiCate(category.getId());
		infoStorage.deleteCisByCiCate(category.getId());
	}

	@Override
	public void alterAttr(CiCategory ciCate,
			java.util.Map<String, Attribute> data) throws Exception {
		infoStorage.alterAttr(ciCate.getId(), data);
	}

	@Override
	public void deleteAttr(CiCategory ciCate, List<Attribute> attrs)
			throws Exception {
		infoStorage.deleteAttr(ciCate.getId(), attrs);
	}

	@Override
	public void addAttr(CiCategory ciCate, List<Attribute> attrs)
			throws Exception {
		infoStorage.addAttr(ciCate.getId(), attrs);
	}

	/**
	 * 自动将value lower trim 转换成neo4j模糊匹配的值
	 * 
	 * @param value
	 *            *12* 需要转换为 .*12.* ,*要转换为.*
	 * @return
	 */
	private String transNeo4jValue(String value) {
		// value = Neo4jStringUtils.cypherESC(value.toLowerCase().trim());
		value = Neo4jStringUtils.replace(value);
		if ("*".equals(value)) {
			return "%";
		} else if (value.startsWith("*") && value.endsWith("*")) {
			return "%" + value.substring(1, value.length() - 1) + "%";
		}
		return value;
	}

	private String transNativeMongoValue(String value) {
		value = Neo4jStringUtils.replace(value);
		if ("*".equals(value)) {
			return "";
		} else if (value.startsWith("*") && value.endsWith("*")) {
			return "" + value.substring(1, value.length() - 1) + "";
		}
		return value;
	}

	@Override
	public int delete(CiCategory category, JSONArray datas) throws Exception {
		List<CiInformation> infos = new ArrayList<CiInformation>();
		if (datas.size() > 0) {
			for (int j = 0; j < datas.size(); j++) {
				JSONObject data = datas.getJSONObject(j);
				String id = data.getString("id");
				//
				CiInformation info = new CiInformation();
				info.setCategoryId(category.getId());
				info.setId(id);
				infos.add(info);
			}
			infoStorage.delete(infos);
			return infos.size();
		}
		return 0;
	}

	@Override
	public List<CiInformation> getByIds(List<String> jsonid) {
		try {
			List<CiInformation> all = infoStorage.getByJsonIds(jsonid);
			if (all != null) {
				Map<String, CiCategory> allMap = cateStorage.getAllMap();
				for (CiInformation info : all) {
					info.setCategory(allMap.get(info.getCategoryId()));
				}
			}
			return all;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<CiInformation> getAll() throws Exception {
		List<CiInformation> all = infoStorage.getAll();
		if (all != null) {
			Map<String, CiCategory> allMap = cateStorage.getAllMap();
			for (CiInformation info : all) {
				info.setCategory(allMap.get(info.getCategoryId()));
			}
		}
		return all;
	}

	// ------------------------------------没有被使用的方法-------------------------------//
	@Override
	public CiInformation getById(String cateId, String infoId) throws Exception {
		CiInformation info = infoStorage.getInfoInCate(cateId, infoId);
		if (info != null) {
			CiCategory byId = cateStorage.getById(cateId);
			info.setCategory(byId);
		}
		return info;
	}
	// ------------------------^^^^^-------没有被使用的方法---------^^^^^--------------//

}