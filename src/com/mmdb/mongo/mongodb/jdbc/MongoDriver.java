// MongoDriver.java

/**
 *      Copyright (C) 2008 10gen Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.mmdb.mongo.mongodb.jdbc;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import com.mongodb.*;

public class MongoDriver implements Driver {

	static final String PREFIX = "mongodb://";

	@Override
	public boolean acceptsURL(String url) {
		return url.startsWith(PREFIX);
	}

	@Override
	@SuppressWarnings("deprecation")
	public Connection connect(String url, Properties info) throws SQLException {
	
		if (url.startsWith(PREFIX)){
			url = url.substring(PREFIX.length());
		}else{
			return null;
		}
	
		if (url.indexOf("/") < 0)
			throw new MongoSQLException("bad url: " + url);

		try {
			List<ServerAddress> list = new ArrayList<ServerAddress>();
			String[] urls = url.split("/");
			String[] addresses = urls[0].split(",");
			for (String address : addresses) {
				String[] hostPort = address.split(":");
				String host = hostPort[0];
				Integer port = Integer.parseInt(hostPort[1]);
				ServerAddress serverAddress = new ServerAddress(host, port);
				list.add(serverAddress);
			}
			String dbName = urls[1];
			MongoClientOptions options = new MongoClientOptions.Builder().socketKeepAlive(true) // 是否保持长链接
					.connectTimeout(30000) // 链接超时时间
					.socketTimeout(30000) // read数据超时时间
					.readPreference(ReadPreference.secondaryPreferred()) // 最近优先策略
					.autoConnectRetry(true) // 是否重试机制
					.connectionsPerHost(50) // 每个地址最大请求数
					.maxWaitTime(1000 * 60 * 2) // 长链接的最大等待时间
					.threadsAllowedToBlockForConnectionMultiplier(50) // 一个socket最大的等待请求数
					.writeConcern(WriteConcern.NORMAL).build();
			   MongoClient client = new MongoClient(list,options);
               DB db = client.getDB(dbName);
            return new MongoConnection(db);
		} catch (java.net.UnknownHostException uh) {
			throw new MongoSQLException("bad url: " + uh);
		}
	}

	@Override
	public int getMajorVersion() {
		return 0;
	}

	@Override
	public int getMinorVersion() {
		return 1;
	}

	@Override
	@Deprecated
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
		throw new UnsupportedOperationException(
				"getPropertyInfo doesn't work yet");
	}

	@Override
	public boolean jdbcCompliant() {
		return false;
	}

	public static void install() {
		// NO-OP, handled in static
	}

	static {
		try {
			DriverManager.registerDriver(new MongoDriver());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}
}
