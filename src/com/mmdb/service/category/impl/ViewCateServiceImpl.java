package com.mmdb.service.category.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.ViewCategory;
import com.mmdb.model.categroy.storage.ViewCateStorage;
import com.mmdb.model.info.storage.ViewInfoStorage;
import com.mmdb.service.category.IViewCateService;
import com.mmdb.service.info.IViewInfoService;

/**
 * 视图分类 服务 - 实现类
 * 
 * @author XIE
 */
@Component("viewCateService")
public class ViewCateServiceImpl implements IViewCateService {
	@Autowired
	private ViewCateStorage vCateStorage;
	@Autowired
	private ViewInfoStorage vInfoStorage;
	@Autowired
	private IViewInfoService vInfoService;

	@Override
	public ViewCategory save(ViewCategory category) throws Exception {
		return vCateStorage.save(category);
	}

	@Override
	public ViewCategory update(ViewCategory category) throws Exception {
		return vCateStorage.update(category);
	}

//	@Override
//	public void deleteAll() throws Exception {
//		vInfoStorage.deleteAll();
//		vCateStorage.deleteAll();
//	}
//
//	@Override
//	public void deleteAllByUser(User user) throws Exception {
//		List<ViewCategory> allByUser = vCateStorage.getAllByUser(user);
//		vInfoService.deleteByViewCategory(allByUser);
//		vCateStorage.deleteByUser(user);
//	}
//
//	@Override
//	public void deleteAllOpenViewCate() throws Exception {
//		vInfoService.deleteAllOpenView();
//		vCateStorage.deleteAllOpenVeiwCate();
//	}

	@Override
	public void deleteById(String id) throws Exception {
		List<ViewCategory> viewCates = new ArrayList<ViewCategory>();
		
		ViewCategory viewCate = new ViewCategory();
		viewCate.setId(id);
		viewCates.add(viewCate);
		vInfoService.deleteByViewCategory(viewCates);
		
		vCateStorage.deleteById(id);
	}

	@Override
	public ViewCategory getById(String id) throws Exception {
		return vCateStorage.getById(id);
	}

	@Override
	public List<ViewCategory> getAllByUser(User user) throws Exception {
		return vCateStorage.getAllByUser(user);
	}

	@Override
	public List<ViewCategory> getAllOpenViewCate() throws Exception {
		return vCateStorage.getAllOpenViewCate();
	}

	@Override
	public List<ViewCategory> getAll() throws Exception {
		return vCateStorage.getAll();
	}

	@Override
	public boolean exist(boolean open,String username, String name, String parentId) {
		return vCateStorage.exists(open,username, name, parentId);
	}
}
