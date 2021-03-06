package com.mmdb.service.info.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.model.bean.User;
import com.mmdb.model.info.ViewPortfolio;
import com.mmdb.model.info.storage.ViewPortfolioStorage;
import com.mmdb.model.relation.storage.ViewViewPortfolioRelStorage;
import com.mmdb.service.info.IViewPortfolioService;

@Component("viewPortfolioService")
public class ViewPortfolioServiceImpl implements IViewPortfolioService {
	@Autowired
	private ViewPortfolioStorage viewPortfolioStorage;
	@Autowired
	private ViewViewPortfolioRelStorage relStorage;

	@Override
	public ViewPortfolio save(ViewPortfolio info) throws Exception {
		return viewPortfolioStorage.save(info);
	}

	@Override
	public void deleteAll() throws Exception {
		relStorage.deleteAll();
		viewPortfolioStorage.deleteAll();
	}

	@Override
	public ViewPortfolio update(ViewPortfolio view) throws Exception {
		return viewPortfolioStorage.update(view);
	}

	@Override
	public ViewPortfolio getById(String id) throws Exception {
		return viewPortfolioStorage.getById(id);
	}

	@Override
	public List<ViewPortfolio> getAll() throws Exception {
		return viewPortfolioStorage.getAll();
	}

	@Override
	public boolean exist(boolean open, String name, String userName) {
		return viewPortfolioStorage.exists(open, name, userName);
	}

	@Override
	public void deleteById(String id) throws Exception {
		relStorage.deleteByProtfolio(id);
		viewPortfolioStorage.deleteById(id);
	}

	@Override
	public void deleteAllOpenViewPort() throws Exception {
		List<ViewPortfolio> all = viewPortfolioStorage.getAllOpenViewPort();
		for (ViewPortfolio viewPortfolio : all) {
			relStorage.deleteByProtfolio(viewPortfolio.getId());
		}
		viewPortfolioStorage.deleteAllOpenViewPort();
	}

	@Override
	public void deleteAllByUser(User user) throws Exception {
		List<ViewPortfolio> all = viewPortfolioStorage.getAllByUser(user
				.getLoginName());
		for (ViewPortfolio viewPortfolio : all) {
			relStorage.deleteByProtfolio(viewPortfolio.getId());
		}
		viewPortfolioStorage.deleteByUser(user.getLoginName());
	}

	@Override
	public List<ViewPortfolio> getAllOpenViewPort() throws Exception {
		return viewPortfolioStorage.getAllOpenViewPort();
	}

	@Override
	public List<ViewPortfolio> getAllByUser(User user) throws Exception {
		return viewPortfolioStorage.getAllByUser(user.getLoginName());
	}

	@Override
	public List<String> getAllOpenViewPorAuthor() throws Exception {
		return viewPortfolioStorage.getAllOpenViewPortfolioAuthor();
	}

	@Override
	public List<ViewPortfolio> getAllOpenViewPortByUser(String user)
			throws Exception {
		return viewPortfolioStorage.getAllOpenViewPortByUser(user);
	}
}
