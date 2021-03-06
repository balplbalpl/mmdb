package com.mmdb.service.relation.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.model.relation.storage.ViewViewPortfolioRelStorage;
import com.mmdb.service.relation.IViewViewProtfolioRelService;

@Service
public class ViewViewProtfolioRelServiceImpl implements
		IViewViewProtfolioRelService {
	@Autowired
	ViewViewPortfolioRelStorage relStorage;

	@Override
	public void save(String protfolioId, List<String> viewIds) throws Exception {
		relStorage.save(protfolioId, viewIds);
	}

	@Override
	public void update(String id, List<String> viewIds) throws Exception {
		relStorage.update(id, viewIds);
	}

	@Override
	public Map<String, List<String>> getAll() throws Exception {
		return relStorage.getAll();
	}
	
	@Override
	public List<String> getProtfolioViewIdByView(String viewId) throws Exception {
		return relStorage.getByView(viewId);
	}

	@Override
	public List<String> getViewIdByProtfolio(String protfolioId) throws Exception {
		return relStorage.getByProtfolio(protfolioId);
	}

	@Override
	public void deleteByProtfolio(String protfolioId) throws Exception {
		relStorage.deleteByProtfolio(protfolioId);
	}

	@Override
	public void deleteByView(String viewId) throws Exception {
		relStorage.deleteByView(viewId);
	}

	@Override
	public void deleteAll() throws Exception {
		relStorage.deleteAll();
	}

}
