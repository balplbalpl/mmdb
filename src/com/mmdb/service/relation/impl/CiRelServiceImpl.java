package com.mmdb.service.relation.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.shell.util.json.JSONArray;
import org.neo4j.shell.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.core.utils.SerializableUtil;
import com.mmdb.model.bean.Attribute;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.categroy.storage.CiCateStorage;
import com.mmdb.model.db.neo4jdb.Neo4jConnect;
import com.mmdb.model.db.neo4jdb.Neo4jDao;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.storage.CiInfoStorage;
import com.mmdb.model.relation.CiRelation;
import com.mmdb.model.relation.storage.CiRelStorage;
import com.mmdb.service.category.IRelCateService;
import com.mmdb.service.relation.ICiRelService;
import com.mmdb.util.HexString;
import com.mmdb.util.Neo4jStringUtils;

/**
 * CI关系处理
 * 
 * @author XIE
 */
@Service
public class CiRelServiceImpl implements ICiRelService {
	@Autowired
	private CiInfoStorage infoStorage;
	@Autowired
	private CiRelStorage relStorage;
	@Autowired
	private IRelCateService rCateService;
	@Autowired
	private CiCateStorage ciCateStorage;

	@Override
	public List<CiRelation> getAll() throws Exception {
		return relStorage.getAll();
	}

	@Override
	public CiRelation getById(String id) throws Exception {
		return relStorage.getById(id);
	}

	@Override
	public CiRelation getById(Long rNeoId) throws Exception {
		return relStorage.getOne(rNeoId);
	}

	@Override
	public CiRelation save(CiRelation relation) throws Exception {
		return relStorage.save(relation);
	}

	@Override
	public void save(List<CiRelation> relations) throws Exception {
		for (CiRelation relation : relations) {
			relStorage.save(relation);
		}
	}

	@Override
	public void delete(CiRelation relation) throws Exception {
		relStorage.delete(relation);
	}

	@Override
	public void delete(List<CiRelation> relations) throws Exception {
		for (CiRelation relation : relations) {
			relStorage.delete(relation);
		}
	}

	@Override
	public void delete(RelCategory rCategory) throws Exception {
		List<CiRelation> relations = relStorage
				.getByCategory(rCategory.getId());
		relStorage.delete(relations);
	}

	@Override
	public void deleteAll() throws Exception {
		relStorage.deleteAll();
	}

	@Override
	public CiRelation update(CiRelation ciRelation) throws Exception {
		return relStorage.update(ciRelation);
	}

	@Override
	public CiRelation update(Long id, CiRelation ciRelation) throws Exception {
		return relStorage.update(id, ciRelation);
	}

	@Override
	public void update(List<CiRelation> ciRelations) throws Exception {
		for (CiRelation ciRelation : ciRelations) {
			relStorage.update(ciRelation);
		}
	}

	@Override
	public List<CiRelation> qureyByAdvanced(RelCategory cate,
			Map<String, String> must, Map<String, String> or, boolean extend)
			throws Exception {
		String cateStr = "";
		if (cate != null) {
			cateStr = "l.type='" + Neo4jStringUtils.cypherESC(cate.getId())
					+ "' ";
			if (extend) {
				List<RelCategory> children = cate.getAllChildren();
				for (RelCategory child : children) {
					cateStr = cateStr + "or l.type='"
							+ Neo4jStringUtils.cypherESC(child.getId()) + "'";
				}
			}
		}
		String mustStr = "";
		if (must != null && must.size() > 0) {
			if (must.containsKey("*")) {
				String val = must.get("*").toLowerCase();
				List<Attribute> attrs = cate.getAllAttributes();
				for (Attribute attr : attrs) {
					mustStr = mustStr + "lower(l.`data"
							+ Neo4jStringUtils.cypherESC(attr.getName())
							+ "`)='" + Neo4jStringUtils.cypherESC(val)
							+ "' and ";
				}
			} else {
				Set<String> mustSet = must.keySet();
				for (String attr : mustSet) {
					mustStr = mustStr
							+ "lower(l.`data"
							+ Neo4jStringUtils.cypherESC(attr)
							+ "`)='"
							+ Neo4jStringUtils.cypherESC(must.get(attr)
									.toLowerCase()) + "' and ";
				}
			}
		}
		String orStr = "";
		if (or != null && or.size() > 0) {
			if (or.containsKey("*")) {
				String val = or.get("*").toLowerCase().replace("*", ".*");
				List<Attribute> attrs = cate.getAllAttributes();
				for (Attribute attr : attrs) {
					orStr = orStr + "lower(l.`data"
							+ Neo4jStringUtils.cypherESC(attr.getName())
							+ "`)=~'" + Neo4jStringUtils.cypherESC(val)
							+ "' or ";
				}
			} else {
				Set<String> orSet = or.keySet();
				for (String attr : orSet) {
					orStr = orStr
							+ "lower(l.`data"
							+ Neo4jStringUtils.cypherESC(attr)
							+ "`)=~'"
							+ Neo4jStringUtils.cypherESC(or.get(attr)
									.toLowerCase().replace("*", ".*"))
							+ "' or ";
				}
			}
		}
		String cypher = "match(a:Ci)-[l:Ci2Ci]->(b:Ci) ";
		if (cateStr.length() > 0) {
			if (cypher.endsWith("(b:Ci) ")) {
				cypher = cypher + "where (" + cateStr + ") ";
			}
		}
		if (mustStr.length() > 0) {
			if (cypher.endsWith("(b:Ci) ")) {
				cypher = cypher + "where "
						+ mustStr.substring(0, mustStr.length() - 4);
			} else {
				cypher = cypher + "and "
						+ mustStr.substring(0, mustStr.length() - 4);
			}
		}
		if (orStr.length() > 0) {
			if (cypher.endsWith("(b:Ci) ")) {
				cypher = cypher + "where "
						+ orStr.substring(0, orStr.length() - 3);
			} else {
				cypher = cypher + "and ("
						+ orStr.substring(0, orStr.length() - 4) + ") ";
			}
		}
		cypher = cypher + "return a,l,b";
		return relStorage.query(cypher, 0, 1, 2);
	}

	@Override
	public List<CiRelation> qureyByTerm(Map<String, String> must,
			Map<String, String> mustNot) throws Exception {
		String mustStr = "";
		if (must != null && must.size() > 0) {
			Set<String> mustSet = must.keySet();
			for (String attr : mustSet) {
				mustStr = mustStr
						+ "lower(l."
						+ Neo4jStringUtils.cypherKey(attr)
						+ ")='"
						+ Neo4jStringUtils.cypherESC(must.get(attr)
								.toLowerCase()) + "' and ";
			}
		}
		String mustNotStr = "";
		if (mustNot != null && mustNot.size() > 0) {
			Set<String> mustNotSet = mustNot.keySet();
			for (String attr : mustNotSet) {
				mustNotStr = mustNotStr
						+ "lower(l."
						+ Neo4jStringUtils.cypherKey(attr)
						+ ")<>'"
						+ Neo4jStringUtils.cypherESC(mustNot.get(attr)
								.toLowerCase()) + "' and ";
			}
		}
		String cypher = "match(a:Ci)-[l:Ci2Ci]->(b:Ci) ";
		if (mustStr.length() > 0) {
			if (cypher.endsWith("(b:Ci) ")) {
				cypher = cypher + "where "
						+ mustStr.substring(0, mustStr.length() - 4);
			}
		}
		if (mustNotStr.length() > 0) {
			if (cypher.endsWith("(b:Ci) ")) {
				cypher = cypher + "where "
						+ mustNotStr.substring(0, mustNotStr.length() - 4);
			} else {
				cypher = cypher + "and "
						+ mustNotStr.substring(0, mustNotStr.length() - 4);
			}
		}
		cypher = cypher + "return a,l,b";
		return relStorage.query(cypher, 0, 1, 2);
	}

	@Override
	public List<CiRelation> qureyCiRelation(CiInformation info,
			List<String> rships) throws Exception {
		// List<CiRelation> ciRelations = new ArrayList<CiRelation>();
		// Node node = infoStorage.getOne(info);
		// RelationshipType[] rts = new RelationshipType[rships.size()];
		// for (int i = 0; i < rships.size(); i++) {
		// rts[i] = StorageUtil.str2RelationType(rships.get(i));
		// }
		// Iterable<Relationship> rs = node.getRelationships(rts);
		// for (Relationship r : rs) {
		// CiRelation relation = relStorage.getOne(r.getId());
		// ciRelations.add(relation);
		// }
		// return ciRelations;
		String types = "";
		for (String type : rships) {
			types = types + "l.type='" + Neo4jStringUtils.cypherESC(type)
					+ "' or ";
		}
		String cypher = "match(a:Ci)-[l:Ci2Ci]-(b:Ci) where a.jsonId='"
				+ HexString.encode(HexString.json2Str(info.getCategoryId(),
						info.getId())) + "' ";
		if (types.length() > 0) {
			cypher = cypher + "and (" + types.substring(0, types.length() - 4)
					+ ") ";
		}
		cypher = cypher + "return startNode(l),l,endNode(l)";
		return relStorage.query(cypher, 0, 1, 2);
	}

	@Override
	public List<CiRelation> qureyCiRelationWithCiCate(CiInformation info,
			List<String> rships, List<CiCategory> all) throws Exception {
		String types = "";
		for (String type : rships) {
			types = types + "l.type='" + Neo4jStringUtils.cypherESC(type)
					+ "' or ";
		}
		String cypher = "match(a:Ci)-[l:Ci2Ci]-(b:Ci) where a.jsonId='"
				+ HexString.encode(HexString.json2Str(info.getCategoryId(),
						info.getId())) + "' ";
		if (types.length() > 0) {
			cypher = cypher + "and (" + types.substring(0, types.length() - 4)
					+ ") ";
		}
		cypher = cypher + "return startNode(l),l,endNode(l)";
		return relStorage.queryWithCiCate(cypher, 0, 1, 2, all);
	}

	// public List<Long> qureyCiByRelation(CiInformation info, String rship)
	// throws Exception {
	// // List<Long> ids = new ArrayList<Long>();
	// // Node node = infoStorage.getOne(info);
	// // long ownId = info.getNeo4jid();
	// // RelationshipType rt = StorageUtil.str2RelationType(rship);
	// // Iterable<Relationship> rs = node.getRelationships(rt);
	// // for (Relationship r : rs) {
	// // Long id = null;
	// // if (r.getStartNode().getId() == ownId) {
	// // id = r.getEndNode().getId();
	// // } else if (r.getEndNode().getId() == ownId) {
	// // id = r.getStartNode().getId();
	// // }
	// // if (!ids.contains(id)) {
	// // ids.add(id);
	// // }
	// // }
	// // return ids;
	// List<Long> ids = new ArrayList<Long>();
	// String cypher = "match(a:Ci)-[l:Ci2Ci]-(b:Ci) where a.jsonId='"
	// + HexString.encode(HexString.json2Str(info.getCategoryId(),
	// info.getId())) + "' and l.type='"
	// + Neo4jStringUtils.cypherESC(rship) + "' return a,l,b";
	// List<CiRelation> rels = relStorage.query(cypher, 0, 1, 2);
	// for (CiRelation rel : rels) {
	// ids.add(rel.getEndInfo().get);
	// }
	// return ids;
	// }

	@Override
	public Set<String> qureyCiRelation(CiInformation info, List<String> rships,
			List<String> jsonIds) throws Exception {
		Set<String> ciRelations = new HashSet<String>();
		List<CiRelation> rels = this.qureyCiRelation(info, rships);
		for (CiRelation rel : rels) {
			if (jsonIds.contains(HexString.decode(rel.getStartInfo().asMap()
					.get("_neo4jid_")
					+ ""))
					&& jsonIds.contains(HexString.decode(rel.getEndInfo()
							.asMap().get("_neo4jid_")
							+ ""))) {
				ciRelations.add(rel.getStartInfo().asMap().get("_neo4jid_")
						+ "_" + rel.getRelCateId() + "_"
						+ rel.getEndInfo().asMap().get("_neo4jid_"));
			}
		}
		return ciRelations;
	}

	@Override
	public Set<String> qureyCiRelationWithCiCate(CiInformation info,
			List<String> rships, List<String> jsonIds, List<CiCategory> all)
			throws Exception {
		Set<String> ciRelations = new HashSet<String>();
		List<CiRelation> rels = this.qureyCiRelationWithCiCate(info, rships,
				all);
		for (CiRelation rel : rels) {
			if (jsonIds.contains(HexString.decode(rel.getStartInfo().asMap()
					.get("_neo4jid_")
					+ ""))
					&& jsonIds.contains(HexString.decode(rel.getEndInfo()
							.asMap().get("_neo4jid_")
							+ ""))) {
				ciRelations.add(rel.getStartInfo().asMap().get("_neo4jid_")
						+ "_" + rel.getRelCateId() + "_"
						+ rel.getEndInfo().asMap().get("_neo4jid_"));
			}
		}
		return ciRelations;
	}

	@Override
	public Map<String, Long> saveOrUpdate(RelCategory cate, Set<CiRelation> rs)
			throws Exception {
		Map<String, Long> retMap = new HashMap<String, Long>();
		// long save = 0, update = 0;
		Long exist = relStorage.getCountRels(rs, 500L);
		List<CiRelation> list = new ArrayList<CiRelation>();
		for (CiRelation rel : rs) {
			// CiRelation info = relStorage.getByIdWithCiCate(rel.getId(),
			// allMap);
			// if (info == null) {
			// relStorage.saveWithoutRet(rel);
			// save++;
			// } else {
			// // info = info.unLazy();
			// info.setRelCateId(rel.getRelCateId());
			// info.setInMapId(rel.getInMapId());
			// info.setOutMapId(rel.getOutMapId());
			// info.setRelValue(rel.getRelValue());
			// info.setTag(rel.getTag());
			// relStorage.updateWithoutRet(info);
			// update++;
			// }
			// relStorage.saveWithoutRet(rel);
			list.add(rel);
		}
		relStorage.saveWithoutRet(list);
		// retMap.put("save", save);
		// retMap.put("update", update);
		retMap.put("save", rs.size() - exist);
		retMap.put("update", exist);
		return retMap;
	}

	// public Map<String, Object> Traversal2(CiInformation info, List<String>
	// rts,
	// final Map<String, Map<String, String>> termMap, final int depth,
	// Direction dir, boolean bool) throws Exception {
	// Map<String, Object> ret = new HashMap<String, Object>();
	// Map<RelationshipType, Direction> rsAndDir = new HashMap<RelationshipType,
	// Direction>();
	// for (String rt : rts) {
	// RelationshipType rs = StorageUtil.str2RelationType(rt);
	// rsAndDir.put(rs, dir);
	// }
	// Node node = infoStorage.getOne(info);
	// Iterator<Path> ite = Neo4jUtil.Traversal(node, rsAndDir,
	// new Evaluator() {
	// public Evaluation evaluate(Path arg0) {
	// if (depth < 0) {
	// return Evaluation.INCLUDE_AND_CONTINUE;
	// } else {
	// if (arg0.length() >= depth) {
	// return Evaluation.INCLUDE_AND_PRUNE;
	// } else {
	// return Evaluation.INCLUDE_AND_CONTINUE;
	// }
	// }
	// }
	// });
	// List<Map<String, Object>> cis = new ArrayList<Map<String, Object>>();
	// List<String> rList = new ArrayList<String>();
	// while (ite.hasNext()) {
	// Path path = ite.next();
	// CiInformation ci = infoStorage.getOne(path.endNode().getId());
	// if (termMap != null && termMap.size() > 0) {
	// if (termMap.containsKey(ci.getCategoryId())) {
	// Map<String, Object> data = ci.getData();
	// Map<String, String> vMap = termMap.get(ci.getCategoryId());
	// boolean bol = true;
	// if (vMap.containsKey("*")) {
	// String tval = vMap.get("*");
	// if (!tval.equals("*") && !data.containsValue(tval)) {
	// bol = false;
	// }
	// } else {
	// Iterator<String> viter = vMap.keySet().iterator();
	// while (viter.hasNext()) {
	// String key = viter.next();
	// String val = vMap.get(key);
	// if (data.containsKey(key)) {
	// if (!val.equals("*")
	// && !data.containsValue(val)) {
	// bol = false;
	// }
	// } else {
	// bol = false;
	// }
	// }
	// }
	// if (bol && !ci.equals(info)) {
	// Iterator<Relationship> ite2 = path.relationships()
	// .iterator();
	// while (ite2.hasNext()) {
	// Relationship rs = ite2.next();
	// String rid = rs.getProperty("id").toString();
	// if (!rList.contains(rid)) {
	// rList.add(rid);
	// cis.add(ci.asMap());
	// }
	// }
	// }
	// }
	// } else {
	// if (!ci.equals(info)) {
	// cis.add(ci.asMap());
	// Iterator<Relationship> ite2 = path.relationships()
	// .iterator();
	// while (ite2.hasNext()) {
	// Relationship rs = ite2.next();
	// String rid = rs.getProperty("id").toString();
	// if (!rList.contains(rid))
	// rList.add(rid);
	// }
	// }
	// }
	// }
	// ret.put("node", cis);
	// ret.put("relation", rList);
	// // System.out.println(JsonUtil.encodeByJackSon(ret));
	// return ret;
	// }

	// @Override
	// public Map<String, Object> Traversal(CiInformation info, List<String>
	// rts,
	// final Map<String, Map<String, String>> termMap, final int depth,
	// Direction dir, boolean bool) throws Exception {
	// Map<String, Object> ret = new HashMap<String, Object>();
	// Set<Map<String, Object>> cis = new HashSet<Map<String, Object>>();
	// Set<String> rels = new HashSet<String>();
	// this.TraversalCi(info, rts, termMap, depth, dir, cis, rels);
	// ret.put("node", cis);
	// ret.put("relation", rels);
	// return ret;
	// }

	/**
	 * 条件过滤器
	 * 
	 * @param ci
	 * @param termMap
	 * @return
	 */
	private boolean conditionFilter(CiInformation ci,
			Map<String, Map<String, String>> termMap) {
		boolean bol = false;
		if (termMap != null && termMap.size() > 0) {
			if (termMap.containsKey(ci.getCategoryId())) {
				Map<String, Object> data = ci.getData();
				Map<String, String> tMap = termMap.get(ci.getCategoryId());
				if (tMap.containsKey("*")) {
					String tval = tMap.get("*");
					if (!tval.equals("*") && !tval.equals("")) {
						Iterator<String> viter = data.keySet().iterator();
						while (viter.hasNext()) {
							String key = viter.next(), val = data.get(key)
									.toString();
							if (val.indexOf(tval) != -1) {
								bol = true;
								break;
							}
						}
					} else {
						bol = true;
					}
				} else {
					Iterator<String> viter = tMap.keySet().iterator();
					while (viter.hasNext()) {
						String key = viter.next(), val = tMap.get(key);
						if (data.containsKey(key)) {
							if (val.equals("*")) {
								bol = true;
								break;
							} else {
								String vald = data.get(key).toString();
								if (vald.indexOf(val) != -1) {
									bol = true;
									break;
								}
							}
						}
					}
				}
			}
		} else {
			bol = true;
		}
		return bol;
	}

	/**
	 * 穿深查询
	 * 
	 * @param info
	 * @param rts
	 * @param termMap
	 * @param depth
	 * @param dir
	 * @param cis
	 * @param rels
	 * @throws Exception
	 */
	// private void TraversalCi(CiInformation info, List<String> rts,
	// Map<String, Map<String, String>> termMap, final int depth,
	// final Direction dir, Set<Map<String, Object>> cis, Set<String> rels)
	// throws Exception {
	// final Node node = infoStorage.getOne(info);
	// TraversalDescription td = Traversal.description().breadthFirst();
	// for (String rt : rts) {
	// RelationshipType rs = StorageUtil.str2RelationType(rt);
	// td = td.relationships(rs, dir);
	// }
	// td = td.evaluator(new Evaluator() {
	// public Evaluation evaluate(Path arg0) {
	// Node sNode = arg0.startNode(), eNode = arg0.endNode();
	// if ((sNode.equals(node) || eNode.equals(node))
	// && arg0.length() >= depth) {
	// return Evaluation.INCLUDE_AND_PRUNE;
	// } else {
	// return Evaluation.INCLUDE_AND_CONTINUE;
	// }
	// }
	// });
	// System.out.println(info.getNeo4jid() + "==============");
	// Map<String, CiInformation> nodes = new HashMap<String, CiInformation>();
	// Set<String> ids = new HashSet<String>();
	// Iterator<Path> ite = td.traverse(node).iterator();
	// while (ite.hasNext()) {
	// Path path = ite.next();
	// long id = path.endNode().getId();
	// CiInformation ci = infoStorage.getOne(id);
	// nodes.put(id + "", ci);
	// // System.out.println(id);
	// if (!ci.equals(info) && conditionFilter(ci, termMap)) {
	// Iterator<Relationship> ite2 = path.relationships().iterator();
	// while (ite2.hasNext()) {
	// Relationship rs = ite2.next();
	// String rid = rs.getProperty("id").toString();
	// if (!rels.contains(rid)) {
	// // System.out.println(rid);
	// rels.add(rid);
	// String[] rids = rid.split("_");
	// ids.add(rids[0]);
	// ids.add(rids[2]);
	// }
	// }
	// }
	// }
	// for (String id : ids) {
	// cis.add(nodes.get(id).asMap());
	// }
	// }

	// public Map<String, Object> newTraversal2(CiInformation info,
	// List<String> rts, Map<String, Map<String, String>> termMap,
	// Map<String, String> dirDepth) throws Exception {
	// Map<String, Object> ret = new HashMap<String, Object>();
	// Set<Map<String, Object>> cis = new HashSet<Map<String, Object>>();
	// Set<String> rels = new HashSet<String>();
	// if (dirDepth.containsKey("up")) {
	// int depth = Integer.valueOf(dirDepth.get("up"));
	// if (depth > 0) {
	// this.TraversalCi(info, rts, termMap, depth, Direction.INCOMING,
	// cis, rels);
	// }
	// }
	// if (dirDepth.containsKey("down")) {
	// int depth = Integer.valueOf(dirDepth.get("down"));
	// if (depth > 0) {
	// this.TraversalCi(info, rts, termMap, depth, Direction.OUTGOING,
	// cis, rels);
	// }
	// }
	// ret.put("node", cis);
	// ret.put("relation", rels);
	// return ret;
	// }

	// /**
	// * 根据起点查询关系
	// *
	// * @param node
	// * 起点
	// * @param ships
	// * 关系类型
	// * @param dir
	// * 方向
	// * @param depht
	// * 层数
	// * @param termMap
	// * 过滤条件
	// * @param relationIds
	// * 符合条件的关系ids
	// * @throws Exception
	// */
	// public void queryRelation2(Node node, RelationshipType[] ships,
	// Direction dir, int depht, Map<String, Map<String, String>> termMap,
	// Set<Long> relationIds) throws Exception {
	// long id = node.getId();
	// Iterable<Relationship> rs = node.getRelationships(dir, ships);
	// for (Relationship r : rs) {
	// long sid = r.getStartNode().getId();
	// if (sid == id) {
	// node = r.getEndNode();
	// } else {
	// node = r.getStartNode();
	// }
	// CiInformation ci = infoStorage.getOne(node.getId());
	// if (conditionFilter(ci, termMap)) {
	// int depht2 = depht - 1;
	// relationIds.add(r.getId());
	// if (depht2 > 0) {
	// this.queryRelation2(node, ships, dir, depht2, termMap,
	// relationIds);
	// }
	// }
	// }
	// }

	public void queryRelation(Node node, List<String> rts,
			Map<String, Map<String, String>> termMap, String dir, int depht,
			Set<CiRelation> relations) throws Exception {
		long nid = node.getId();
		Iterable<Relationship> rs = node.getRelationships();
		for (Relationship r : rs) {
			String rtype = r.getType().name();
			if (rts.contains(rtype)) {
				Node sNode = r.getStartNode(), eNode = r.getEndNode();
				long sid = sNode.getId(), eid = eNode.getId();
				String s_id = sNode.getProperty("id").toString(), s_cid = sNode
						.getProperty("categoryId").toString();
				String e_id = eNode.getProperty("id").toString(), e_cid = eNode
						.getProperty("categoryId").toString();
				String rid = HexString.json2Str(s_cid, s_id) + "_" + rtype
						+ "_" + HexString.json2Str(e_cid, e_id);
				if (dir.equals("up") && eid == nid) {
					CiRelation relation = relStorage.getById(rid);
					if (relation != null) {
						CiInformation ci = relation.getStartInfo();
						if (conditionFilter(ci, termMap)) {
							int depht2 = depht - 1;
							relations.add(relation);
							if (depht2 > 0) {
								this.queryRelation(sNode, rts, termMap, dir,
										depht2, relations);
							}
						}
					}
				} else if (dir.equals("down") && sid == nid) {
					CiRelation relation = relStorage.getById(rid);
					if (relation != null) {
						CiInformation ci = relation.getEndInfo();
						if (conditionFilter(ci, termMap)) {
							int depht2 = depht - 1;
							relations.add(relation);
							if (depht2 > 0) {
								this.queryRelation(eNode, rts, termMap, dir,
										depht2, relations);
							}
						}
					}
				}

			}
		}
	}

	/**
	 * 条件过滤器
	 * 
	 * @param pc
	 * @param filter
	 * @return
	 * @throws ClassNotFoundException
	 * @throws java.io.IOException
	 */
	private boolean traversalFilter(PropertyContainer pc,
			Map<String, Map<String, String>> filter) throws IOException,
			ClassNotFoundException {
		boolean bol = false;
		if (filter != null && filter.size() > 0) {
			Object categoryId = pc.getProperty("categoryId");
			if (filter.containsKey(categoryId)) {
				Map<String, Object> data = SerializableUtil.stringToObject(pc
						.getProperty("data").toString());
				Map<String, String> map = filter.get(categoryId);
				if (map.containsKey("*")) {
					String tval = map.get("*");
					if (tval.equals("*") || tval.equals("")) {
						bol = true;
					} else {
						Iterator<Entry<String, String>> iter = map.entrySet()
								.iterator();
						while (iter.hasNext()) {
							Entry<String, String> entry = iter.next();
							String key = entry.getKey(), val = entry.getValue();
							if (data.get(key).toString().indexOf(val) != -1) {
								bol = true;
								break;
							}
						}
					}
				} else {
					Iterator<Entry<String, String>> iter = map.entrySet()
							.iterator();
					while (iter.hasNext()) {
						Entry<String, String> entry = iter.next();
						String key = entry.getKey(), val = entry.getValue();
						if (val.equals("*")) {
							bol = true;
							break;
						} else {
							if (data.get(key).toString().indexOf(val) != -1) {
								bol = true;
							}
						}
					}
				}
			}
		} else {
			bol = true;
		}
		return bol;
	}

	@Override
	public Set<CiInformation> newTraversal(CiInformation info,
			List<String> rts, Map<String, Map<String, String>> filter,
			Map<String, String> dirDepth) throws Exception {
		// Set<Long> relationIds = new HashSet<Long>();
		int up = 0;
		int down = 0;
		if (dirDepth.containsKey("up")) {
			int depth = Integer.valueOf(dirDepth.get("up"));
			if (depth > 0) {
				up = depth;
			}
		}
		if (dirDepth.containsKey("down")) {
			int depth = Integer.valueOf(dirDepth.get("down"));
			if (depth > 0) {
				down = depth;
			}
		}
		Set<CiInformation> cis = qureyCiRelation(info, rts, filter, up, down);
		return cis;
	}

	public Set<CiInformation> qureyCiRelation(CiInformation info,
			List<String> rships, Map<String, Map<String, String>> filter,
			int up, int down) throws Exception {
		String jsonId = HexString.encode(HexString.json2Str(info.getCategory()
				.getName(),
				info.getData().get(info.getCategory().getMajor().getName())));

		String cypher = "match (a:Ci {jsonId:'" + jsonId + "'})-[r *.." + down
				+ "]->(b:Ci) return r";
		JSONArray ret = Neo4jConnect.executionCypher(cypher);
		cypher = "match (b:Ci)-[r *.." + up + "]->(a:Ci {jsonId:'" + jsonId
				+ "'}) return r";
		JSONArray ret1 = Neo4jConnect.executionCypher(cypher);
		for (int i = 0; i < ret1.length(); i++) {
			ret.put(ret1.get(0) );
		}
		ret1 = null;
		List<CiCategory> allCiCate = ciCateStorage.getAll();
		Map<String, CiCategory> ciCateMap = new HashMap<String, CiCategory>();
		for (CiCategory cate : allCiCate) {
			ciCateMap.put(cate.getId(), cate);
		}

		Set<String> ciIds = new HashSet<String>();
		for (int i = 0; i < ret.length(); i++) {
			JSONArray row = ret.getJSONArray(i);
			for (int j = 0, lenj = row.length(); j < lenj; j++) {
				JSONArray jsonArray = row.getJSONArray(j);
				for (int k = 0; k < jsonArray.length(); k++) {
					JSONObject datas = jsonArray.getJSONObject(k);
					JSONObject data = datas.getJSONObject("data");
					String type = data.getString("type");
					if (rships == null || rships.size() == 0
							|| rships.contains(type)) {// 过滤关系
						String id = data.getString("id");
						String[] split = id.split("_");
						ciIds.add(split[0]);
						ciIds.add(split[2]);
					}
				}
			}
		}

		if (ciIds == null || ciIds.size() == 0) {
			return null;
		}

		List<String> tmp = new ArrayList<String>();
		tmp.addAll(ciIds);

		List<CiInformation> infos = infoStorage.getByJsonIds(tmp);
		for (CiInformation cInfo : infos) {
			cInfo.setCategory(ciCateMap.get(cInfo.getCategoryId()));
		}

		tmp = null;

		Set<CiInformation> cis = new HashSet<CiInformation>();

		for (CiInformation ciInformation : infos) {
			if (conditionFilter(ciInformation, filter)) {
				cis.add(ciInformation);
			}
		}
		return cis;
	}

	// public void qureyCiRelation(CiInformation info, List<String> rships,
	// Map<String, Map<String, String>> filter, int up, int down,
	// Set<CiInformation> cis, Set<CiRelation> rels) throws Exception {
	// String jsonId = HexString.encode(HexString.json2Str(info.getCategory()
	// .getName(),
	// info.getData().get(info.getCategory().getMajor().getName())));
	// String cypher = "match (a:Ci {jsonId:'" + jsonId
	// + "'})-[r1:Ci2Ci]-(b:Ci) ";
	// if (down > 0 || up > 0) {
	// cypher += " where ";
	// }
	// if (down > 0) {
	// cypher += "a-[*.." + down + "]->b ";
	// }
	// if (up > 0 && down > 0) {
	// cypher += " or a<-[*.." + up + "]-b ";
	// }
	// cypher = cypher + " return r1";
	// System.out.println("qureyCiRelation: " + cypher);
	// JSONArray ret = Neo4jConnect.executionCypher(cypher);
	//
	// List<CiCategory> allCiCate = ciCateStorage.getAll();
	// Map<String, CiCategory> ciCateMap = new HashMap<String, CiCategory>();
	// for (CiCategory cate : allCiCate) {
	// ciCateMap.put(cate.getId(), cate);
	// }
	//
	// Set<String> ciIds = new HashSet<String>();
	// Map<String, CiRelation> relMap = new HashMap<String, CiRelation>();
	// for (int i = 0; i < ret.length(); i++) {
	// JSONArray row = ret.getJSONArray(i);
	// for (int j = 0, lenj = row.length(); j < lenj; j++) {
	// JSONObject datas = row.getJSONObject(j);
	// JSONObject data = datas.getJSONObject("data");
	// String type = data.getString("type");
	// if (rships == null || rships.size() == 0
	// || rships.contains(type)) {//过滤关系
	// long neo4jId = datas.getJSONObject("metadata")
	// .getLong("id");
	// String id = data.getString("id");
	// String[] split = id.split("_");
	// ciIds.add(split[0]);
	// ciIds.add(split[2]);
	// Dynamic<String, Object> relValue = new Dynamic<String, Object>();
	// for (Iterator keys = data.keys(); keys.hasNext();) {
	// String next = (String) keys.next();
	// if (next.startsWith("data")) {
	// relValue.put(next.substring(4, next.length()),
	// data.get(next));
	// }
	// }
	// CiRelation rel = new CiRelation();
	// rel.setId(id);
	// rel.setEndCateId(data.getString("endCate"));
	// rel.setStartCateId(data.getString("startCate"));
	// rel.setRelationId(neo4jId);
	// rel.setRelValue(relValue);
	// rel.setRelCateId(type);
	//
	// relMap.put(id, rel);
	// }
	// }
	// }
	//
	// Map<String, CiInformation> cache = new HashMap<String,
	// CiInformation>();
	//
	// List<String> tmp = new ArrayList<String>();
	// tmp.addAll(ciIds);
	//
	// List<CiInformation> infos = infoStorage.getByJsonIds(tmp, allCiCate);
	// for (CiInformation ciInformation : infos) {
	// cache.put(info.getHexId(), ciInformation);
	// }
	// Set<String> keySet = relMap.keySet();
	// for (String string : keySet) {
	// CiRelation ciRelation = relMap.get(string);
	// String id = ciRelation.getId();
	// String[] split = id.split("_");
	// CiInformation startInfo = cache.get(split[0]);
	// CiInformation endInfo = cache.get(split[2]);
	// }
	//
	//
	// }

	@Override
	public List<Map<String, Object>> queryCiInRel(List<String> ciIds)
			throws Exception {
		if (ciIds == null || ciIds.size() < 2)
			return null;
		StringBuffer ids = new StringBuffer("[");
		for (String id : ciIds) {
			ids.append("'");
			ids.append(id);
			ids.append("',");
		}
		ids.delete(ids.length() - 1, ids.length());
		ids.append("]");

		StringBuffer match = new StringBuffer(
				"match (n:Ci)-[r]-(m:Ci) where n.jsonId in");
		match.append(ids);
		match.append(" and m.jsonId in ");
		match.append(ids);
		match.append(" return distinct  r");

		JSONObject base = Neo4jDao.base(match.toString());
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		List<RelCategory> all = rCateService.getAll();
		Map<String, RelCategory> cache = new HashMap<String, RelCategory>();
		for (RelCategory relCategory : all) {
			cache.put(relCategory.getId(), relCategory);
		}

		JSONArray datas = base.getJSONArray("data");
		for (int i = 0; i < datas.length(); i++) {
			JSONArray data = datas.getJSONArray(i);
			JSONObject d = data.getJSONObject(0).getJSONObject("data");
			String id = d.getString("id");
			String relCateId = d.getString("type");
			String sCiCateId = d.getString("startCate");
			String sCiName = d.getString("startCi");
			String eCiCateId = d.getString("endCate");
			String eCiName = d.getString("endCi");
			String[] split = id.split("_");
			String sCiJsonId = split[0];
			String eCiJsonId = split[2];

			Map<String, String> relValue = new HashMap<String, String>();
			for (Iterator keys = d.keys(); keys.hasNext();) {
				String key = keys.next().toString();
				if (key.startsWith("data")) {
					relValue.put(key, d.getString(key));
				}
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", id);
			map.put("startCate", sCiCateId);
			map.put("startCi", sCiJsonId);
			map.put("startCiName", sCiName);
			map.put("endCate", eCiCateId);
			map.put("endCi", eCiJsonId);
			map.put("endCiName", eCiName);
			map.put("relCate", relCateId);
			map.put("relCateName", cache.get(relCateId).getName());
			map.put("relValue", relValue);
			ret.add(map);
		}
		return ret;
	}
}