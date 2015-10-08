package com.mmdb.service.category.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.categroy.storage.RelCategoryStorage;
import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.mapping.SourceToRelationMapping;
import com.mmdb.model.mapping.storage.InCiCateMapStorage;
import com.mmdb.model.relation.CiRelation;
import com.mmdb.model.relation.storage.CiRelStorage;
import com.mmdb.service.category.IRelCateService;
import com.mmdb.service.mapping.ISourceRelationMapService;
import com.mmdb.service.relation.ICiRelService;

/**
 * 关系分类 - 服务层
 * 
 * @author XIE
 */
@Component("relCateService")
public class RelCateServiceImpl implements IRelCateService {
	@Autowired
	private RelCategoryStorage rcStorage;
	@Autowired
	private CiRelStorage relStorage;
	@Autowired
	private ICiRelService relService;
	@Autowired
	private InCiCateMapStorage imStorage;
	@Autowired
	private ISourceRelationMapService omStorage;

	@Override
	public RelCategory save(RelCategory rCate) throws Exception {
		return rcStorage.save(rCate);
	}

	@Override
	public void saveHasId(RelCategory rCate) {
		rcStorage.saveHasId(rCate);
	}

	@Override
	public RelCategory update(RelCategory rCate) throws Exception {
		return rcStorage.update(rCate);
	}

	@Override
	public void delete(RelCategory rCate) throws Exception {
		List<CiRelation> ciRels = relService.qureyByAdvanced(rCate, null, null,
				false);
		for (CiRelation rel : ciRels) {
			relStorage.delete(rel);
		}
		rcStorage.delete(rCate);
	}

	@Override
	public void deleteAll() throws Exception {
		rcStorage.deleteAll();
	}

	@Override
	public RelCategory getByName(String name) throws Exception {
		String id = rcStorage.getIdByName(name);
		if (id == null) {
			return null;
		}
		return rcStorage.getById(id);
	}

	@Override
	public RelCategory getById(String id) throws Exception {
		return rcStorage.getById(id);
	}

	// @Override
	// public RelCategory getByIdLazy(String id) throws Exception {
	// RelCategory rel = rcStorage.getByidLazy(id);
	// if(rel==null){
	// return rel;
	// }
	// RelCategory parent = null;
	// if (rel.getParentId() != null && !"".equals(rel.getParentId())) {
	// parent = rcStorage.getByidLazy(rel.getParentId());
	// }
	// rel.setParent(parent);
	// return rel;
	// }

	// @Override
	// public RelCategory getById(Long neoId) throws Exception {
	// return rcStorage.getOne(neoId);
	// }

	@Override
	public List<RelCategory> getAll() throws Exception {
		return rcStorage.getAll();
	}

	@Override
	public List<String> getCateNames() throws Exception {
		List<String> names = new ArrayList<String>();
		List<RelCategory> rcs = rcStorage.getAll();
		for (RelCategory rc : rcs) {
			String name = rc.getId();
			if (!names.contains(name)) {
				names.add(name);
			}
		}
		return names;
	}

	@Override
	//
	public RelCategory update(RelCategory rCate, List<CiRelation> rels,
			List<InCiCateMap> ims, List<SourceToRelationMapping> oms)
			throws Exception {
		if (rels != null)
			for (CiRelation rel : rels) {
				// rel = rel.unLazy();
				relStorage.update(rel);
			}
		if (ims != null)
			for (InCiCateMap im : ims) {
				// im = im.unLazy();
				imStorage.update(im);
			}
		if (oms != null) {
			for (SourceToRelationMapping om : oms) {
				// om = om.unLazy();
				omStorage.update(om);
			}
		}
		return rcStorage.update(rCate);
	}

	@Override
	public RelCategory updateAndAddAttr(RelCategory rCate, String attr,
			String defaulVal) throws Exception {
		relStorage.addAttr2CiRel(rCate, attr, defaulVal);
		return rcStorage.update(rCate);
	}

	@Override
	public RelCategory updateAndDelAttr(RelCategory rCate, String attr,
			List<InCiCateMap> ims, List<SourceToRelationMapping> oms)
			throws Exception {
		relStorage.delAttr2CiRel(rCate, attr);
		if (ims != null)
			for (InCiCateMap im : ims) {
				// im = im.unLazy();
				imStorage.update(im);
			}
		if (oms != null) {
			for (SourceToRelationMapping om : oms) {
				// om = om.unLazy();
				omStorage.update(om);
			}
		}
		return rcStorage.update(rCate);
	}

	@Override
	public RelCategory updateAndEditAttr(RelCategory rCate, String oldAttr,
			String newAttr, List<InCiCateMap> ims,
			List<SourceToRelationMapping> oms) throws Exception {
		relStorage.updAttr2CiRel(rCate, oldAttr, newAttr);
		if (ims != null)
			for (InCiCateMap im : ims) {
				// im = im.unLazy();
				imStorage.update(im);
			}
		if (oms != null) {
			for (SourceToRelationMapping om : oms) {
				// om = om.unLazy();
				omStorage.update(om);
			}
		}
		return rcStorage.update(rCate);
	}

}
