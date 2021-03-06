package com.mmdb.model.icon;

public class ViewIcon {
	private static final long serialVersionUID = 1L;

	/**
	 * 图标id
	 */
	private String id;

	private String name;
	/**
	 * admin中当做theme使用,
	 */
	private String username;

	private String contentType;

	private Object content;

	private String md5;

	private int width;
	private int height;

	public ViewIcon() {
	}

	public ViewIcon(String name, String username, Object content) {
		super();
		this.name = name;
		this.username = username;
		this.content = content;
	}

	public ViewIcon(String name, String username, String contentType,
			Object content, String md5) {
		super();
		this.name = name;
		this.username = username;
		this.contentType = contentType;
		this.content = content;
		this.md5 = md5;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	// public Map<String, String> asMap() {
	// Map<String, String> retData = new HashMap<String, String>();
	// retData.put("id", this.id);
	// retData.put("name", this.name);
	// retData.put("contentType", this.contentType);
	// retData.put("content", this.content);
	// retData.put("md5", this.md5);
	// return retData;
	// }

}
