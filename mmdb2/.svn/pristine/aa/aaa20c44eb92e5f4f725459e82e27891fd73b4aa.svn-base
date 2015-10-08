package com.mmdb.service.subscription.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.model.relation.storage.UserViewPortfolioRelStorage;
import com.mmdb.service.subscription.ISubscriptionViewPortfolioService;

@Service("subscriptionViewPortfolioService")
public class SubscriptionViewPortfolioServiceImpl implements
		ISubscriptionViewPortfolioService {
	@Autowired
	UserViewPortfolioRelStorage subVpStorage;

	@Override
	public void save(String viewPId, String loginName) throws Exception {
		subVpStorage.save(viewPId, loginName);
	}

	@Override
	public void del(String viewPId, String loginName) throws Exception {
		subVpStorage.del(viewPId, loginName);
	}

	@Override
	public List<String> getSubscribersByViewPortfolioId(String viewPId)
			throws Exception {
		return subVpStorage.getSubscribersByViewPortfolioId(viewPId);
	}

	@Override
	public List<String> getViewPortfoliosBySubscriber(String sub)
			throws Exception {
		return subVpStorage.getViewPortfoliosBySubscriber(sub);
	}

	@Override
	public void delForCloseView(String viewId, String loginName)
			throws Exception {
		subVpStorage.delForCloseView(viewId, loginName);
	}

}
