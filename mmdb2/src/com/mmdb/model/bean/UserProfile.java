package com.mmdb.model.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mmdb.common.MyException;
import com.mmdb.model.info.ViewInformation;

/**
 * 所有的排序多少desc的.
 * 
 * @author xiongjian
 * 
 */
public class UserProfile {
	private int totalView;
	private int totalPublicView;
	private int totalPrivateView;
	private int totalSubscription;// 总共被多少人订阅了.
	private User user;
	private List<String> lastUpdate;
	private List<String> privateLastUpdate;
	private List<String> publicLastUpdate;
	private List<String> subscriptionLastUpdate;
	// 被订阅的共有视图排序 (desc)
	private List<String> HotView;
	private Map<String, Map<String, Object>> views;

	/**
	 * 用户默认订阅了自己的视图,所有传入的视图应该是自己的的共有视图+私有视图+订阅视图
	 * 
	 * @param myViews
	 * @param subViews
	 * @throws MyException
	 */
	public UserProfile(List<ViewInformation> vInfos, User user)
			throws MyException {
		// TODO 区分共有和私有视图.
		if (vInfos == null) {
			vInfos = new ArrayList<ViewInformation>();
		}
		if (user == null) {
			throw new MyException("用户不能为空");
		}
		List<ViewInformation> subView = new ArrayList<ViewInformation>();
		List<ViewInformation> ownView = new ArrayList<ViewInformation>();

		List<ViewInformation> ownPubView = new ArrayList<ViewInformation>();
		List<ViewInformation> ownPriView = new ArrayList<ViewInformation>();
		int totalSub = 0;
		//将视图分拣开,自己的和订阅的
		for (ViewInformation info : vInfos) {
			if(info.getUserName().equals(user.getLoginName())){
				ownView.add(info);
			}else{
				subView.add(info);
			}
		}
		//将视图分拣开计算被订阅的数量
		for (ViewInformation info : ownView) {
			if (info.getOpen()) {
				ownPubView.add(info);
				totalSub += info.getSubscripers().size();
				totalSub--;
			} else {
				ownPriView.add(info);
			}
		}

		// 排hot
		List<String> hot = new ArrayList<String>();
		Collections.sort(ownPubView, new Comparator<ViewInformation>() {
			@Override
			public int compare(ViewInformation o1, ViewInformation o2) {
				// 订阅最多的在前面
				return o2.getSubscripers().size() - o1.getSubscripers().size();
			}
		});
		for (ViewInformation info : ownPubView) {
			hot.add(info.getId());
		}
		//定义一个以时间排序的 Comparator
		Comparator<ViewInformation> updateComparator = new Comparator<ViewInformation>() {
			@Override
			public int compare(ViewInformation o1, ViewInformation o2) {
				return (int) (o2.getUpdateTime() - o1.getUpdateTime());
			}
		};

		// 排最后修改时间
		List<String> lastUpdate = new ArrayList<String>();
		Collections.sort(ownView, updateComparator);
		for (ViewInformation info : ownView) {
			lastUpdate.add(info.getId());
		}
		
		// 排订阅视图最后修改时间
		List<String> subLastUpdate = new ArrayList<String>();
		Collections.sort(subView, updateComparator);
		for (ViewInformation info : subView) {
			subLastUpdate.add(info.getId());
		}
		
		List<String> ownPriLastUpdate = new ArrayList<String>();
		Collections.sort(ownPriView, updateComparator);
		for (ViewInformation info : ownPriView) {
			ownPriLastUpdate.add(info.getId());
		}

		List<String> ownPubLastUpdate = new ArrayList<String>();
		Collections.sort(ownPubView, updateComparator);
		for (ViewInformation info : ownPubView) {
			ownPubLastUpdate.add(info.getId());
		}

		this.totalView = ownView.size();
		this.totalPrivateView = ownPriView.size();
		this.totalPublicView = ownPubView.size();
		this.totalSubscription = totalSub;// 被订阅数
		this.HotView = hot;
		this.lastUpdate = lastUpdate;
		this.subscriptionLastUpdate = subLastUpdate;

		this.privateLastUpdate = ownPriLastUpdate;
		this.publicLastUpdate = ownPubLastUpdate;
		this.user = user;
	}

	// public UserProfile(int totalView, int totalPublicView,
	// int totalPrivateView, int totalSubscription, User user,
	// List<String> lastUpdate, List<String> subscriptionLastUpdate,
	// List<String> hotView) {
	// super();
	// this.totalView = totalView;
	// this.totalPublicView = totalPublicView;
	// this.totalPrivateView = totalPrivateView;
	// this.totalSubscription = totalSubscription;
	// this.user = user;
	// this.lastUpdate = lastUpdate;
	// this.subscriptionLastUpdate = subscriptionLastUpdate;
	// HotView = hotView;
	// }

	public int getTotalView() {
		return totalView;
	}

	public void setTotalView(int totalView) {
		this.totalView = totalView;
	}

	public int getTotalPublicView() {
		return totalPublicView;
	}

	public void setTotalPublicView(int totalPublicView) {
		this.totalPublicView = totalPublicView;
	}

	public int getTotalPrivateView() {
		return totalPrivateView;
	}

	public void setTotalPrivateView(int totalPrivateView) {
		this.totalPrivateView = totalPrivateView;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<String> getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(List<String> lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public List<String> getSubscriptionLastUpdate() {
		return subscriptionLastUpdate;
	}

	public void setSubscriptionLastUpdate(List<String> subscriptionLastUpdate) {
		this.subscriptionLastUpdate = subscriptionLastUpdate;
	}

	public List<String> getHotView() {
		return HotView;
	}

	public void setHotView(List<String> hotView) {
		HotView = hotView;
	}

	public Map<String, Map<String, Object>> getViews() {
		return views;
	}

	public void setViews(Map<String, Map<String, Object>> views) {
		this.views = views;
	}

	public int getTotalSubscription() {
		return totalSubscription;
	}

	public void setTotalSubscription(int totalSubscription) {
		this.totalSubscription = totalSubscription;
	}

	public Map<String, Object> asMap() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("totalView", totalView);
		ret.put("totalPublicView", totalPublicView);
		ret.put("totalPrivateView", totalPrivateView);
		ret.put("totalSubscription", totalSubscription);
		ret.put("user", user.asMapForRest());
		ret.put("lastUpdate", lastUpdate);
		ret.put("privateLastUpdate", privateLastUpdate);
		ret.put("publicLastUpdate", publicLastUpdate);
		ret.put("subscriptionLastUpdate", subscriptionLastUpdate);
		ret.put("hotView", HotView);
		ret.put("views", views);
		return ret;
	}
}