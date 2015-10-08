package com.mmdb.model.info;

import java.util.HashMap;
import java.util.Map;

/**
 * 组合视图
 * 
 * @author XIE
 */
public class ViewPortfolio {
	/**
	 * 视图id
	 */
	private String id;

	/**
	 * true时是公有的,false为私有的
	 */
	private boolean open;

	/**
	 * 视图name
	 */
	private String name;
	/**
	 * 要保存的二维数组内容
	 */
	private String content;
	/**
	 * 视图创建用户
	 */
	private String userName;
	/**
	 * 新建/更新的时间
	 */
	private long time;

	/**
	 * 默认[构造函数]
	 */
	public ViewPortfolio() {
	}

	/**
	 * [构造函数] 初始化指定分类下的视图
	 * 
	 * @param id
	 *            组合视图id
	 * @param content
	 *            组合视图内容
	 * @throws Exception
	 */
	public ViewPortfolio(String id, boolean open, String content,
			String userName) throws Exception {
		this.id = id;
		this.name = id;
		if (content == null || content.equals("")) {
			throw new Exception("组合视图内容为空");
		} else {
			this.setContent(content);
		}
		this.userName = userName;
		this.open = open;
		this.time = System.currentTimeMillis();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;

		ViewPortfolio that = (ViewPortfolio) o;

		if (!id.equals(that.id))
			return false;

		return true;
	}

	public Map<String, Object> asMap() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("id", this.getId());
		ret.put("name", this.getName());
		ret.put("userName", this.userName);
		ret.put("content", this.getContent());
		ret.put("time", this.getTime());
		ret.put("open", open);
		return ret;
	}
}