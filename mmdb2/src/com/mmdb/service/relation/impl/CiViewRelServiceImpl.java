package com.mmdb.service.relation.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.model.categroy.storage.CiCateStorage;
import com.mmdb.model.info.storage.CiInfoStorage;
import com.mmdb.model.info.storage.ViewInfoStorage;
import com.mmdb.model.relation.storage.CiViewRelStorage;
import com.mmdb.service.relation.ICiViewRelService;

@Service
public class CiViewRelServiceImpl implements ICiViewRelService {
	@Autowired
	private CiViewRelStorage ciViewRelStorage;
	@Autowired
	private ViewInfoStorage vInfoStorage;
	@Autowired
	private CiInfoStorage infoStorage;
	@Autowired
	private CiCateStorage cateStorage;

	@Override
	public void save(String cateId, List<String> ciHexIds) throws Exception {
		ciViewRelStorage.save(cateId, ciHexIds);
	}

	@Override
	public void update(String cateId, List<String> ciHexIds) throws Exception {
		ciViewRelStorage.update(cateId, ciHexIds);
	}

	@Override
	public void deleteAll() throws Exception {
		ciViewRelStorage.deleteAll();
	}

	@Override
	public void deleteByCi(String hexId) throws Exception {
		ciViewRelStorage.deleteByCi(hexId);
	}

	@Override
	public Map<String, List<String>> getAll() throws Exception {
		return ciViewRelStorage.getAll();
	}

	@Override
	public List<String> getByView(String cateId) throws Exception {
		return ciViewRelStorage.getByView(cateId);
	}

	@Override
	public List<String> getByCi(String hexId) throws Exception {
		return ciViewRelStorage.getByCi(hexId);
	}

	@Override
	public void deleteByView(String viewId) throws Exception {
		ciViewRelStorage.deleteByView(viewId);
	}
}