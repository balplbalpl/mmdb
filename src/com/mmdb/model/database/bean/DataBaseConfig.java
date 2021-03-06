package com.mmdb.model.database.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库连接信息 {type:
 * 'mysql/sqlserver/oracle/db2',url:'',port:'',database:'',username:'',password:
 * ' ' } Created by XIE on 2015/3/28.
 */
public class DataBaseConfig {// extends NodeEntity
	/**
	 * 数据库连接名
	 */
	private String id;

	private String name;

	/**
	 * 数据库类型
	 */
	private String type;

	/**
	 * 数据库路径
	 */
	private String hostName;
	/**
	 * 端口
	 */
	private int port;
	/**
	 * 数据库实例
	 */
	private String databaseName;
	/**
	 * 是否是RAC连接
	 */
	private Boolean rac;
	/**
	 * RAC地址
	 */
	private String racAddress;
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 密码
	 */
	private String password;

	private String owner;

	public DataBaseConfig() {

	}

	/**
	 * 装载数据库连接
	 * 
	 * @param isRac
	 *            是否是rac地址 true/false
	 * @param dbMap
	 *            数据库连接信息 {name:
	 *            '',type:'mysql/sqlserver/oracle/db2',url:'',port:'',database:'',username
	 *            : ' ' , p a s s w o r d : ' ' }
	 * @throws Exception
	 */
	public DataBaseConfig(boolean isRac, Map<String, String> dbMap)
			throws Exception {
		String url = dbMap.get("url"), _type = dbMap.get("type"), _userName = dbMap
				.get("username"), _passWord = dbMap.get("password"), name = dbMap
				.get("name"), id = dbMap.get("id");
		if (_type == null || _type.equals("")) {
			throw new Exception("数据库类型不能为空");
		}
		if (_userName == null || _userName.equals("")) {
			throw new Exception("用户名不能为空");
		}
		if (_passWord == null) {
			throw new Exception("密码不能为空");
		}
		if (isRac) {
			if (url == null || url.equals("")) {
				throw new Exception("RAC地址不能为空");
			}
			this.racAddress = url;
		} else {
			String _port = dbMap.get("port"), _dataBaseName = dbMap
					.get("database");
			if (url == null || url.equals("")) {
				throw new Exception("HostName不能为空");
			}
			if (_port == null || _port.equals("")) {
				throw new Exception("Port不能为空");
			}
			if (_type.equals("oracle") || _type.equals("db2")) {
				if (_dataBaseName == null || _dataBaseName.equals("")) {
					throw new Exception("数据库实例不能为空");
				}
			}
			this.hostName = url;
			this.port = Integer.valueOf(_port);
			this.databaseName = _dataBaseName;
		}
		this.rac = isRac;
		this.type = _type;
		this.username = _userName;
		this.password = _passWord;
		this.name = name;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getRac() {
		return rac;
	}

	public void setRac(Boolean rac) {
		this.rac = rac;
	}

	public String getRacAddress() {
		return racAddress;
	}

	public void setRacAddress(String racAddress) {
		this.racAddress = racAddress;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataBaseConfig other = (DataBaseConfig) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Map<String, String> asMap() {
		Map<String, String> dbMap = new HashMap<String, String>();
		dbMap.put("id", this.getId());
		dbMap.put("name", this.getName());
		dbMap.put("isRac", String.valueOf(this.getRac()));
		dbMap.put("type", this.getType());
		if (this.getRac()) {
			dbMap.put("url", this.getRacAddress());
		} else {
			dbMap.put("url", this.getHostName());
			dbMap.put("port", String.valueOf(this.getPort()));
			dbMap.put("database", this.getDatabaseName());
		}
		dbMap.put("username", this.getUsername());
		dbMap.put("password", this.getPassword());
		dbMap.put("owner", this.owner);
		return dbMap;
	}
}
