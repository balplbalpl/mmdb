package com.mmdb.service.category.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.storage.CiCateStorage;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.storage.CiInfoStorage;
import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.mapping.storage.InCiCateMapStorage;
import com.mmdb.model.relation.storage.CiRelStorage;
import com.mmdb.service.category.ICiCateService;

/**
 * 配置项分类 服务 - 实现类
 * 
 * @author XIE
 */
@Component("ciCateService")
public class CiCateServiceImpl implements ICiCateService {
	@Autowired
	private CiCateStorage cateStorage;
	@Autowired
	private CiInfoStorage infoStorage;
	@Autowired
	private CiRelStorage relStorage;
	@Autowired
	private InCiCateMapStorage imStorage;

	@Override
	public CiCategory save(CiCategory nCategory) throws Exception {
		return cateStorage.save(nCategory);
	}

	@Override
	public CiCategory update(CiCategory nCategory) throws Exception {
		return cateStorage.update(nCategory);
	}

	@Override
	public CiCategory update(CiCategory nCategory, List<CiInformation> infos,
			List<InCiCateMap> ims) throws Exception {
		if (infos != null)
			for (CiInformation ci : infos) {
				infoStorage.update(ci);
			}
		if (ims != null) {
			for (InCiCateMap im : ims) {
				// im = im.unLazy();
				imStorage.update(im);
			}
		}
		return cateStorage.update(nCategory);
	}

	@Override
	public long delete(CiCategory nCategory, boolean bool) throws Exception {
		long i = 0;
		if (bool) {
			i = infoStorage.deleteCisByCiCate(nCategory.getId());
		}
		cateStorage.delete(nCategory);
		return i;
	}

	@Override
	public List<CiInformation> getByCategory(String cateId) throws Exception {
		return infoStorage.getByCategory(cateId);
	}

	@Override
	public void clearAll() throws Exception {
		relStorage.deleteAll();
		infoStorage.deleteAll();
		cateStorage.deleteAll();
	}

	@Override
	public CiCategory getById(String id) throws Exception {
		return cateStorage.getById(id);
	}

	@Override
	public List<CiCategory> getAll() throws Exception {
		return cateStorage.getAll();
	}

	@Override
	public List<String> getUseImages() throws Exception {
		List<String> images = new ArrayList<String>();
		List<CiCategory> ncs = cateStorage.getAll();
		for (CiCategory nc : ncs) {
			if (nc.getImage() != null) {
				String[] ims = nc.getImage().split("/");
				String image = ims[ims.length - 1];
				if (!images.contains(image))
					images.add(image);
			}
		}
		return images;
	}

	@Override
	public CiCategory updateAndAddAttr(CiCategory nCategory, String attr,
			String defaultval) throws Exception {
//		infoStorage.addAttr(nCategory, attr, defaultval);
		return cateStorage.update(nCategory);
	}

	@Override
	public CiCategory updateAndDelAttr(CiCategory nCategory, String attr)
			throws Exception {
//		infoStorage.deleteAttr(nCategory, attr);
		return cateStorage.update(nCategory);
	}

	@Override
	public CiCategory updateAndeditAttr(CiCategory nCategory, String oldAttr,
			String newAttr, List<InCiCateMap> ims) throws Exception {
//		infoStorage.alterAttr(nCategory, oldAttr, newAttr);
		if (ims != null) {
			for (InCiCateMap im : ims) {
				// im = im.unLazy();
				imStorage.update(im);
			}
		}
		return cateStorage.update(nCategory);
	}

	@Override
	public CiCategory getByName(String name) throws Exception {
		String id = cateStorage.getIdByName(name);
		return getById(id);
	}
	
}
