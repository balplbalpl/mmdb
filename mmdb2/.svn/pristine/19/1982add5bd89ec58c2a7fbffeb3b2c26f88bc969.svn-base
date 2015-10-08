package com.mmdb.model.relation.storage;

import java.util.List;

import org.springframework.stereotype.Component;

import com.mmdb.core.framework.neo4j.storage.RelationStorage;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.relation.CiVirtualRelation;

@Component
public class CiVirtualRelStorage extends RelationStorage<CiVirtualRelation> {
	private Log log = LogFactory.getLogger("CiVirtualRelStorage");

	/**
	 * 根据id获取数据间的关系
	 * 
	 * @param id
	 *            分类id（当前分类中唯一）
	 * @return
	 * @throws Exception
	 */
	public CiVirtualRelation getById(String id) throws Exception {
		List<CiVirtualRelation> list = this.getByProperty("id", id);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("虚拟CI数据间关系[" + id + "]不唯一");
			throw new Exception("虚拟数据间关系[" + id + "]不唯一");
		} else {
			return null;
		}
	}
}
