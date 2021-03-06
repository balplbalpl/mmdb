package com.mmdb.model.icon;

import java.io.InputStream;

import com.mmdb.core.framework.neo4j.annotation.Space;
import com.mmdb.core.framework.neo4j.annotation.Uuid;

@Space("icon")
public class Icon {
	private static final long serialVersionUID = 1L;
	/**
	 * 图标id
	 */
	@Uuid
	private String id;

	private String userId;

	/**
	 * 图标
	 */
	private String name;

	private String md5;

	private InputStream inputStream;

	/**
	 * 构造函数
	 */
	public Icon() {
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

}
