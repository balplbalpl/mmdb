/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/**
 *@Author: niaoge(Zhengsheng Xia)
 *@Email 78493244@qq.com
 *@Date: 2015-6-11
 */


package com.mmdb.mongo.mongodb.jdbc;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;

import com.mongodb.*;

public class MongoConnection implements Connection {
	
	public MongoConnection(DB db) {
		_db = db;
	}
	
	@Override
	@Deprecated
	public SQLWarning getWarnings() {
		throw new RuntimeException("should do get last error");
	}
	
	@Override
	@Deprecated
	public void clearWarnings() {
		throw new RuntimeException("should reset error");
	}
	
	// ---- state -----
	
	@Override
	public void close() {
		_db = null;
	}
	
	@Override
	public boolean isClosed() {
		return _db == null;
	}
	
	// --- commit ----
	
	@Override
	public void commit() {
		// NO-OP
	}
	
	@Override
	public boolean getAutoCommit() {
		return true;
	}
	
	@Override
	@Deprecated
	public void rollback() {
		throw new RuntimeException("can't rollback");
	}
	
	@Override
	@Deprecated
	public void rollback(Savepoint savepoint) {
		throw new RuntimeException("can't rollback");
	}
	
	@Override
	public void setAutoCommit(boolean autoCommit) {
		if (!autoCommit)
			throw new RuntimeException("autoCommit has to be on");
	}
	
	@Override
	@Deprecated
	public void releaseSavepoint(Savepoint savepoint) {
		throw new RuntimeException("no savepoints");
	}
	
	@Override
	@Deprecated
	public Savepoint setSavepoint() {
		throw new RuntimeException("no savepoints");
	}
	
	@Override
	@Deprecated
	public Savepoint setSavepoint(String name) {
		throw new RuntimeException("no savepoints");
	}
	
	@Override
	@Deprecated
	public void setTransactionIsolation(int level) {
		throw new RuntimeException("no TransactionIsolation");
	}
	
	// --- create ----
	@Override
	@Deprecated
	public Array createArrayOf(String typeName, Object[] elements) {
		throw new RuntimeException("no create*");
	}
	
	@Override
	@Deprecated
	public Struct createStruct(String typeName, Object[] attributes) {
		throw new RuntimeException("no create*");
	}
	
	@Override
	@Deprecated
	public Blob createBlob() {
		throw new RuntimeException("no create*");
	}
	
	@Override
	@Deprecated
	public Clob createClob() {
		throw new RuntimeException("no create*");
	}
	
	@Override
	@Deprecated
	public NClob createNClob() {
		throw new RuntimeException("no create*");
	}
	
	@Override
	@Deprecated
	public SQLXML createSQLXML() {
		throw new RuntimeException("no create*");
	}
	
	// ------- meta data ----
	
	@Override
	public String getCatalog() {
		return null;
	}
	
	@Override
	@Deprecated()
	public void setCatalog(String catalog) {
		throw new RuntimeException("can't set catalog");
	}
	
	@Override
	public Properties getClientInfo() {
		return _clientInfo;
	}
	
	@Override
	public String getClientInfo(String name) {
		return (String) _clientInfo.get(name);
	}
	
	@Override
	public void setClientInfo(String name, String value) {
		_clientInfo.put(name, value);
	}
	
	@Override
	public void setClientInfo(Properties properties) {
		_clientInfo = properties;
	}
	
	@Override
	public int getHoldability() {
		return ResultSet.HOLD_CURSORS_OVER_COMMIT;
	}
	
	@Override
	public void setHoldability(int holdability) {
	}
	
	@Override
	@Deprecated
	public int getTransactionIsolation() {
		throw new RuntimeException("not dont yet");
	}
	
	@Override
	@Deprecated
	public DatabaseMetaData getMetaData() {
		throw new RuntimeException("not dont yet");
	}
	
	@Override
	public boolean isValid(int timeout) {
		return _db != null;
	}
	
	@Override
	public boolean isReadOnly() {
		return false;
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		if (readOnly)
			throw new RuntimeException("no read only mode");
	}
	
	@Override
	@Deprecated
	public Map<String, Class<?>> getTypeMap() {
		throw new RuntimeException("not done yet");
	}
	
	@Override
	@Deprecated
	public void setTypeMap(Map<String, Class<?>> map) {
		throw new RuntimeException("not done yet");
	}
	
	// ---- Statement -----
	
	@Override
	public Statement createStatement() {
		return createStatement(0, 0, 0);
	}
	
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) {
		return createStatement(resultSetType, resultSetConcurrency, 0);
	}
	
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
		return new MongoStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
	}
	
	// --- CallableStatement
	
	@Override
	public CallableStatement prepareCall(String sql) {
		return prepareCall(sql, 0, 0, 0);
	}
	
	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) {
		return prepareCall(sql, resultSetType, resultSetConcurrency, 0);
	}
	
	@Override
	@Deprecated
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) {
		throw new RuntimeException("CallableStatement not supported");
	}
	
	// ---- PreparedStatement 
	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return prepareStatement(sql, 0, 0, 0);
	}
	
	@Override
	@Deprecated
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) {
		throw new RuntimeException("no PreparedStatement yet");
	}
	
	@Override
	@Deprecated
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) {
		throw new RuntimeException("no PreparedStatement yet");
	}
	
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return prepareStatement(sql, resultSetType, resultSetConcurrency, 0);
	}
	
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return new MongoPreparedStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability, sql);
	}
	
	@Override
	@Deprecated
	public PreparedStatement prepareStatement(String sql, String[] columnNames) {
		throw new RuntimeException("no PreparedStatement yet");
	}
	
	// ---- random ----
	
	@Override
	public String nativeSQL(String sql) {
		return sql;
	}
	
	@Override
	@Deprecated
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	@Deprecated
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	public DB getDB() {
		return _db;
	}
	
	public DBCollection getCollection(String name) {
		return _db.getCollection(name);
	}
	
	DB _db;
	Properties _clientInfo;
	@Override
	public void setSchema(String schema) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSchema() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public DB getNativeDB(){
		return _db;
	}
}
