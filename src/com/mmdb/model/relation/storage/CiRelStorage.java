package com.mmdb.model.relation.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.neo4j.shell.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.framework.neo4j.entity.Dynamic;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.categroy.storage.CiCateStorage;
import com.mmdb.model.db.neo4jdb.Neo4jDao;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.storage.CiInfoStorage;
import com.mmdb.model.relation.CiRelation;
import com.mmdb.util.Neo4jStringUtils;

/**
 * 配置项数据[关系映射] - 存储仓库
 * 
 * @author XIE
 * 
 */
@Component("ciRelStorage")
public class CiRelStorage {
	private Log log = LogFactory.getLogger("CiRelStorage");

	@Autowired
	private CiCateStorage ciCateStorage;
	@Autowired
	private CiInfoStorage ciInfoStorage;

	/**
	 * 根据id获取数据间的关系
	 * 
	 * @param id
	 *            分类id（当前分类中唯一）
	 * @return
	 * @throws Exception
	 */
	public CiRelation getById(String id) throws Exception {
		List<CiRelation> list = this.getByProperty("id", id);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("CI数据间关系[" + id + "]不唯一");
			throw new Exception("CI数据间关系[" + id + "]不唯一");
		} else {
			return null;
		}
	}

	/**
	 * 根据id获取数据间的关系
	 * 
	 * @param id
	 *            分类id（当前分类中唯一）
	 * @return
	 * @throws Exception
	 */
	public CiRelation getByIdWithCiCate(String id, List<CiCategory> all)
			throws Exception {
		List<CiRelation> list = this.getByPropertyWithCiCate("id", id, all);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("CI数据间关系[" + id + "]不唯一");
			throw new Exception("CI数据间关系[" + id + "]不唯一");
		} else {
			return null;
		}
	}

	/**
	 * 获取关系分类下的数据
	 * 
	 * @param cateId
	 *            分类id
	 * @return
	 * @throws Exception
	 */
	public List<CiRelation> getByCategory(String cateId) throws Exception {
		return this.query(
				"match(a:Ci)-[l:Ci2Ci{type:'"
						+ Neo4jStringUtils.cypherESC(cateId)
						+ "'}]->(b:Ci) return a,l,b", 0, 1, 2);
	}

	/**
	 * 获取所有关系分类下的数据
	 */
	public List<CiRelation> getAll() throws Exception {
		return this
				.query("match(a:Ci)-[l:Ci2Ci]->(b:Ci) return a,l,b", 0, 1, 2);
	}

	/**
	 * 通过NEO4JID获取关系分类下的数据
	 */
	public CiRelation getOne(Long neo4jId) throws Exception {
		List<CiRelation> list = this.query(
				"match(a:Ci)-[l:Ci2Ci]->(b:Ci) where id(l)=" + neo4jId
						+ " return a,l,b", 0, 1, 2);
		if (list.size() == 0) {
			return null;
		} else if (list.size() > 1) {
			throw new Exception("CI数据间关系[" + neo4jId + "]不唯一");
		} else {
			return list.get(0);
		}
	}

	/**
	 * 保存关系分类下的数据
	 */
	public void saveWithoutRet(List<CiRelation> relations) throws Exception {

		int r = relations.size() / 50;
		if (relations.size() % 50 != 0) {
			r++;
		}
		for (int i = 0; i < r; i++) {
			int s = i * 50;
			int e = s + 50;
			if (e > relations.size()) {
				e = relations.size();
			}

			StringBuffer datas = new StringBuffer("[");
			for (int j = s; j < e; j++) {
				CiRelation ciRelation = relations.get(j);
				datas.append(toString(ciRelation)).append(",");
			}
			datas.delete(datas.length() - 1, datas.length());
			datas.append("]");
			StringBuffer ex = new StringBuffer("foreach ( data in ");
			ex.append(datas)
					.append(" | merge (sCi:Ci {jsonId:data.startJson,categoryId:data.startCate}) ");
			ex.append("merge (eCi:Ci {jsonId:data.endJson,categoryId:data.endCate}) ");
			ex.append("merge sCi-[l:Ci2Ci]->eCi set l={},l=data remove l.startJson,l.endJson");
			ex.append(")");
			Neo4jDao.executeNoRest(ex.toString());
		}
	}

	private String toString(CiRelation relation) {
		CiInformation startCi = relation.getStartInfo();
		CiInformation endCi = relation.getEndInfo();

		String startId = startCi.getId();
		String startCateId = startCi.getCategoryId();

		startId = Neo4jStringUtils.cypherESC(startId);
		startCateId = Neo4jStringUtils.cypherESC(startCateId);

		String endId = endCi.getId();
		String endCateId = endCi.getCategoryId();
		String startJson = startCi.getCiHex();
		String endJson = endCi.getCiHex();
		endId = Neo4jStringUtils.cypherESC(endId);
		endCateId = Neo4jStringUtils.cypherESC(endCateId);

		String relationId = Neo4jStringUtils.cypherESC(relation.getId());
		String type = Neo4jStringUtils.cypherESC(relation.getRelCateId());
		String owner = Neo4jStringUtils.cypherESC(relation.getOwner());
		
		StringBuffer map = new StringBuffer("{");
		map.append(" startJson:'").append(startJson).append("',");
		map.append(" endJson:'").append(endJson).append("',");
		map.append(" id:'").append(relationId).append("',");
		map.append(" owner:'").append(owner).append("',");

		map.append("startCi:'").append(startId).append("',");
		map.append(" endCi:'").append(endId).append("',");

		map.append(" startCate:'").append(startCateId).append("',");
		map.append(" endCate:'").append(endCateId).append("',");
		map.append(" type:'").append(type).append("',");
		Dynamic<String, Object> relValue = relation.getRelValue();
		Set<String> relSet = relValue.keySet();
		for (String key : relSet) {
			Object v = relValue.get(key);
			if(v==null){
				v="";
			}
			if (v.getClass().getName().toLowerCase().contains("string")) {
				map.append(" data").append(Neo4jStringUtils.cypherESC(key))
						.append(":'")
						.append(Neo4jStringUtils.cypherESC((String) v))
						.append("',");
			} else if (v.getClass().getName().toLowerCase().contains("long")) {
				map.append(" data").append(Neo4jStringUtils.cypherESC(key))
						.append(":'").append(Long.parseLong(v + ""))
						.append("',");
			} else if (v.getClass().getName().toLowerCase().contains("boolean")) {
				map.append(" data").append(Neo4jStringUtils.cypherESC(key))
						.append(":'").append(v.toString()).append("',");
			} else if (v.getClass().getName().toLowerCase().contains("date")) {
				map.append(" data").append(Neo4jStringUtils.cypherESC(key))
						.append(":'").append(((Date) v).getTime()).append("',");
			}
		}
		map.delete(map.length() - 1, map.length());
		map.append("}");
		return map.toString();
	}

	/**
	 * 保存关系分类下的数据
	 */
	public CiRelation save(CiRelation relation) throws Exception {
		List<CiRelation> list = new ArrayList<CiRelation>();
		list.add(relation);
		saveWithoutRet(list);
		return getById(relation.getId());
	}

	/**
	 * 删除关系分类下的数据
	 */
	public void delete(CiRelation relation) throws Exception {
		String id = relation.getId();
		String[] split = id.split("_");
		String hexId1 = split[0];
		String hexId2 = split[2];

		String cypher = "match(a:Ci {`jsonId`:'" + hexId1
				+ "'})-[l:Ci2Ci]->(b:Ci {`jsonId`:'" + hexId2
				+ "'}) where id(l)=" + relation.getRelationId() + " delete l";
		Neo4jDao.executeNoRest(cypher);

		try {
			cypher = "match(a:Ci) where not a-[]-() delete a";
			Neo4jDao.executeNoRest(cypher);
		} catch (Exception e) {
		}

	}

	/**
	 * 删除关系分类下的数据(批量)
	 */
	public void delete(List<CiRelation> relations) throws Exception {
		StringBuilder ids = new StringBuilder();
		for (CiRelation rel : relations) {
			ids.append(rel.getRelationId() + ",");
		}
		String idsCypher = ids.toString();
		if (idsCypher.length() > 0) {
			idsCypher = idsCypher.substring(0, idsCypher.length() - 1);
		}
		String cypher = "match(a:Ci)-[l:Ci2Ci]->(b:Ci) where id(l) in ["
				+ idsCypher + "] delete l";
		Neo4jDao.executeNoRest(cypher);
		
		try {
			cypher = "match(a:Ci) where not a-[]-() delete a";
			Neo4jDao.executeNoRest(cypher);
		} catch (Exception e) {
		}
	}

	/**
	 * 删除所有关系分类下的数据
	 */
	public void deleteAll() throws Exception {
		String cypher = "match(a:Ci)-[l:Ci2Ci]->(b:Ci) delete l";
		Neo4jDao.executeNoRest(cypher);
		try {
			cypher = "match(a:Ci) where not a-[]-() delete a";
			Neo4jDao.executeNoRest(cypher);
		} catch (Exception e) {
		}
	}

	/**
	 * 修改关系分类下的数据
	 */
	public void updateWithoutRet(CiRelation ciRelation) throws Exception {
		delete(ciRelation);
		List<CiRelation> list = new ArrayList<CiRelation>();
		list.add(ciRelation);
		saveWithoutRet(list);
	}

	/**
	 * 修改关系分类下的数据
	 */
	public CiRelation update(CiRelation ciRelation) throws Exception {
		delete(ciRelation);
		return save(ciRelation);
	}

	/**
	 * 按NEO4JID修改关系分类下的数据
	 */
	public CiRelation update(Long id, CiRelation ciRelation) throws Exception {
		ciRelation.setRelationId(id);
		delete(ciRelation);
		return save(ciRelation);
	}

	/**
	 * 增加关系属性
	 */
	public void addAttr2CiRel(RelCategory cate, String attr, String defaultVal)
			throws Exception {
		String typeStr = "l.type='" + Neo4jStringUtils.cypherESC(cate.getId())
				+ "'";
		List<RelCategory> children = cate.getAllChildren();
		for (RelCategory rc : children) {
			typeStr = typeStr + " or l.type='"
					+ Neo4jStringUtils.cypherESC(rc.getId()) + "'";
		}
		String attrStr = "l.`data" + Neo4jStringUtils.cypherESC(attr) + "`='"
				+ Neo4jStringUtils.cypherESC(defaultVal) + "'";
		// if(attr.getType().getName().equals("String")){
		// attrStr = attrStr + "'" +
		// Neo4jStringUtils.cypherESC(attr.getDefaultValue()) + "'";
		// }else if(attr.getType().getName().equals("Time")){
		// attrStr = attrStr + "0";
		// }else if(attr.getType().getName().equals("Integer")){
		// attrStr = attrStr + "0";
		// }else{
		// attrStr = attrStr + "true";
		// }
		String cypher = "match(:Ci)-[l:Ci2Ci]-(:Ci) where " + typeStr + " set "
				+ attrStr;
		Neo4jDao.executeNoRest(cypher);
	}

	/**
	 * 删除关系属性
	 */
	public void delAttr2CiRel(RelCategory cate, String attr) throws Exception {
		String typeStr = "l.type='" + Neo4jStringUtils.cypherESC(cate.getId())
				+ "'";
		List<RelCategory> children = cate.getAllChildren();
		for (RelCategory rc : children) {
			typeStr = typeStr + " or l.type='"
					+ Neo4jStringUtils.cypherESC(rc.getId()) + "'";
		}
		String attrStr = "l.`data" + Neo4jStringUtils.cypherESC(attr) + "`";
		String cypher = "match(:Ci)-[l:Ci2Ci]-(:Ci) where " + typeStr
				+ " remove " + attrStr;
		Neo4jDao.executeNoRest(cypher);
	}

	/**
	 * 修改关系属性
	 */
	public void updAttr2CiRel(RelCategory cate, String oldAttr, String newAttr)
			throws Exception {
		// delAttr2CiRel(cate, oldAttr);
		// addAttr2CiRel(cate, newAttr,defaultVal);
		if (cate != null && !StringUtils.isEmpty(oldAttr)) {
			// 获取全部的子孙节点
			// List<RelCategory> children = cate.getAllChildren();
			String typeStr = "l.type='"
					+ Neo4jStringUtils.cypherESC(cate.getId()) + "'";
			List<RelCategory> children = cate.getAllChildren();
			for (RelCategory rc : children) {
				typeStr = typeStr + " or l.type='"
						+ Neo4jStringUtils.cypherESC(rc.getId()) + "'";
			}

			children.add(cate);
			String alter = "match (m:Ci)-[l:Ci2Ci]-(n:Ci)" + " where "
					+ typeStr + " with l,l.`data" + oldAttr + "` as val"
					+ " set l.`data" + newAttr + "`= val remove l.`data"
					+ oldAttr + "`";
			Neo4jDao.base(alter);
		}
	}

	@SuppressWarnings("unused")
	private String toString(Iterator<RelCategory> all) {
		StringBuffer ciCateidStr = new StringBuffer();
		ciCateidStr.append("['");
		while (all.hasNext()) {
			RelCategory ciCate2 = all.next();
			ciCateidStr.append(Neo4jStringUtils.cypherESC(ciCate2.getId()));
			ciCateidStr.append("','");
		}
		if (ciCateidStr.length() == 2) {
			ciCateidStr.replace(1, 2, "]");
		} else {
			ciCateidStr.replace(ciCateidStr.length() - 2, ciCateidStr.length(),
					"]");
		}
		return ciCateidStr.toString();
	}

	/**
	 * 删除关系分类下所有关系
	 */
	public void delCiRelByCate(RelCategory cate) throws Exception {
		String typeStr = "l.type='" + Neo4jStringUtils.cypherESC(cate.getId())
				+ "'";
		String cypher = "match(:Ci)-[l:Ci2Ci]-(:Ci) where " + typeStr
				+ " delete l";
		Neo4jDao.executeNoRest(cypher);
	}
	
	public void delCiRelByCis(List<String> ciHexs) throws Exception{
		StringBuffer cihexs = new StringBuffer();
		cihexs.append("['");
		for (String string : ciHexs) {
			cihexs.append(string);
			cihexs.append("','");
		}
		if (cihexs.length() == 2) {
			cihexs.replace(1, 2, "]");
		} else {
			cihexs.replace(cihexs.length() - 2, cihexs.length(),
					"]");
		}
		String cypher = "match(n:Ci)-[l:Ci2Ci]-(m:Ci) where n.jsonId in "+cihexs+" delete l";
		try {
			cypher = "match(a:Ci) where not a-[]-() delete a";
			Neo4jDao.executeNoRest(cypher);
		} catch (Exception e) {
		}
	}

	public void delCiRelByCiCate(String ciCateId) throws Exception{
		String cypher = "match(n:Ci)-[l:Ci2Ci]-(m:Ci) where n.categoryId='"+ciCateId+"' delete l";
		Neo4jDao.executeNoRest(cypher);
		try {
			cypher = "match(a:Ci) where not a-[]-() delete a";
			Neo4jDao.executeNoRest(cypher);
		} catch (Exception e) {
		}
	}

	/**
	 * CYPHER查询关系分类下的数据
	 */
	public List<CiRelation> query(String bq, int start, int line, int end)
			throws Exception {
		List<CiCategory> all = ciCateStorage.getAll();
		return queryWithCiCate(bq, start, line, end, all);
	}

	public List<CiRelation> queryWithCiCate(String bq, int start, int line,
			int end, List<CiCategory> all) throws Exception {
		Map<String,CiCategory> ciCateMap = new HashMap<String,CiCategory>();
		for (CiCategory ciCategory : all) {
			ciCateMap.put(ciCategory.getId(), ciCategory);
		}
		List<List<JSONObject>> rels = Neo4jDao.getDataMulMap(bq);
		List<CiRelation> result = new ArrayList<CiRelation>();
		Set<String> jsonIdSet = new HashSet<String>();
		for (List<JSONObject> l : rels) {
			JSONObject startMap = l.get(start);
			JSONObject lineMap = l.get(line);
			JSONObject endMap = l.get(end);

			CiRelation rel = new CiRelation();
			rel.setRelationId(Long.parseLong(lineMap.get("id") + ""));

			String startCate = startMap.getJSONObject("data").has("categoryId") ? startMap
					.getJSONObject("data").getString("categoryId") : "";

			String startJs = startMap.getJSONObject("data").has("jsonId") ? startMap
					.getJSONObject("data").getString("jsonId") : "";
			jsonIdSet.add(startJs);

			String endCate = endMap.getJSONObject("data").has("categoryId") ? endMap
					.getJSONObject("data").getString("categoryId") : "";

			String endJs = endMap.getJSONObject("data").has("jsonId") ? endMap
					.getJSONObject("data").getString("jsonId") : "";
			jsonIdSet.add(endJs);

			rel.setStartCateId(startCate);
			rel.setEndCateId(endCate);
			rel.setRelCateId(lineMap.getJSONObject("data").getString("type"));
			rel.setId(startJs + "_" + rel.getRelCateId() + "_" + endJs);
			Dynamic<String, Object> rv = new Dynamic<String, Object>();
			JSONObject relValues = lineMap.getJSONObject("data");
			
			String owner = String.valueOf(relValues.has("owner")?relValues.get("owner"):"");
			Iterator it = relValues.keys();
			while (it.hasNext()) {
				String key = (String) it.next();
				if (key.startsWith("data")) {
					rv.put(key.substring(4), relValues.get(key));
				}
			}
			rel.setRelValue(rv);
			rel.setOwner(owner);
			result.add(rel);
		}

		List<String> jsonIds = new ArrayList<String>();
		for (String jsonId : jsonIdSet) {
			jsonIds.add(jsonId);
		}
		List<CiInformation> cis = ciInfoStorage.getByJsonIds(jsonIds);
	
		
		Map<String, CiInformation> cisMap = new HashMap<String, CiInformation>();
		if (cis != null) {
			for (CiInformation ciInformation : cis) {
				ciInformation.setCategory(ciCateMap.get(ciInformation.getCategoryId()));
			}
			for (CiInformation ci : cis) {
				cisMap.put(ci.getCiHex(), ci);
			}
		}
		for (CiRelation rel : result) {
			String[] idArr = rel.getId().split("_");
			rel.setStartInfo(cisMap.get(idArr[0]));
			rel.setEndInfo(cisMap.get(idArr[2]));
		}

		long stime = System.currentTimeMillis();

		System.out.println("getAllCi查询所有节点耗时："
				+ (System.currentTimeMillis() - stime));
		return result;
	}

	/**
	 * 按K-V查询关系分类下的数据
	 */
	public List<CiRelation> getByProperty(String key, String value)
			throws Exception {
		return this.query(
				"match(a:Ci)-[l:Ci2Ci{" + Neo4jStringUtils.cypherKey(key)
						+ ":'" + Neo4jStringUtils.cypherESC(value)
						+ "'}]->(b:Ci) return a,l,b", 0, 1, 2);
	}

	/**
	 * 按K-V查询关系分类下的数据
	 */
	public List<CiRelation> getByPropertyWithCiCate(String key, String value,
			List<CiCategory> all) throws Exception {
		return this.queryWithCiCate(
				"match(a:Ci)-[l:Ci2Ci{" + Neo4jStringUtils.cypherKey(key)
						+ ":'" + Neo4jStringUtils.cypherESC(value)
						+ "'}]->(b:Ci) return a,l,b", 0, 1, 2, all);
	}

	/**
	 * 查询关系条数
	 */
	public Long getCountRels(Set<CiRelation> rels, Long group) throws Exception {
		Long count = 0L;
		Long index = 0L;
		StringBuilder sb = new StringBuilder();
		for (CiRelation rel : rels) {
			sb.append("'");
			sb.append(Neo4jStringUtils.cypherESC(rel.getId()));
			sb.append("',");
			index++;
			if (index % group == 0L) {
				String ids = sb.toString();
				String cypher = "match(a:Ci)-[l:Ci2Ci]->(b:Ci) where l.id in ["
						+ (ids.substring(0, ids.length() - 1))
						+ "] return count(l)";
				count = count + Neo4jDao.getDataLong(cypher);
				sb = new StringBuilder();
			}
		}
		String ids = sb.toString();
		if (ids.length() > 0) {
			String cypher = "match(a:Ci)-[l:Ci2Ci]->(b:Ci) where l.id in ["
					+ (ids.substring(0, ids.length() - 1))
					+ "] return count(l)";
			count = count + Neo4jDao.getDataLong(cypher);
		}
		return count;
	}
}
