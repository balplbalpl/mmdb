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

import static com.mmdb.mongo.mongodb.jdbc.Consts.MONGODB_ID;
import static com.mmdb.mongo.mongodb.jdbc.Consts.MONGODB_SET;
import static com.mmdb.mongo.mongodb.jdbc.Consts.isJdbcName;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;

import org.bson.types.ObjectId;

import com.mmdb.mongo.helpinput.utils.Utils;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

class JDBCExecutor  {
	private final DB _db;
	private String _sql;
	private Statement _statement = null;
	
	private List<?> _params = null;
	private int _pos;
	
	public JDBCExecutor(DB db, String sql) {
		_db = db;
		_sql = sql;
	}
	
	public void setParams(List<?> params) {
		_pos = 1;
		_params = params;
	}
	
	private void parseSQL() throws MongoSQLException {
		_statement = parse(_sql);
	}
	
	private BasicDBObject parserOrder(PlainSelect ps) {
		BasicDBObject order = new BasicDBObject();
		List<OrderByElement> orderBylist = ps.getOrderByElements();
		if (Utils.hasLength(orderBylist)) {
			for (OrderByElement o : orderBylist) {
				Expression ex = o.getExpression();
				if (ex instanceof Column) {
					Column cl = (Column) ex;
					order.put(cl.getColumnName(), o.isAsc() ? 1 : -1);
				}
				else {
					throw new UnsupportedOperationException("unknown Expression:" + ex);
				}
			}
		}
		return order;
	}
	
	private DBCursor queryCommon(PlainSelect ps, BasicDBObject fields, DBCollection coll) {
		for (Object o : ps.getSelectItems()) {
			SelectItem si = (SelectItem) o;
			if (si instanceof AllColumns) {
				if (fields.size() > 0)
					throw new UnsupportedOperationException("can't have * and fields");
				break;
			}
			else if (si instanceof SelectExpressionItem) {
				SelectExpressionItem sei = (SelectExpressionItem) si;
				Expression ex = sei.getExpression();
				//				String alias =sei.getAlias();
				//				logger.info(alias);
				//				Object aliasName =Utils.hasLength(alias)?alias:1;
				fields.put(parseFieldName(ex), 1);
			}
			else {
				throw new UnsupportedOperationException("unknown select item: " + si.getClass());
			}
		}
		// where
		DBObject queryObject = parseWhere(ps.getWhere());
		
		
		// done with basics, build DBCursor
		DBCursor c = coll.find(queryObject, fields);
		
		Limit limitExpress = ps.getLimit();
		Long skip = null;
		Long limit = null;
		if (limitExpress != null && !limitExpress.isLimitAll()) {
			if (limitExpress.isOffsetJdbcParameter())
				skip = (Long) _params.get(_pos++);
			else
				skip = limitExpress.getOffset();
			
			if (limitExpress.isRowCountJdbcParameter())
				limit = (Long) _params.get(_pos++);
			else
				limit = limitExpress.getRowCount();
		}
		
		if (skip != null && skip > 0)
			c = c.skip(skip.intValue());
		if (limit != null && limit > 0)
			c = c.limit(limit.intValue());
		
		//logger.info(MessageFormat.format("limit:{0},skip:{1}", new Object[]{limt,skip}));
		BasicDBObject order = parserOrder(ps);
		if (!order.isEmpty()) {
			return c.sort(order);
		}
		return c;
	}
	
	private String getAliasName(HashSet<String> aliasNames, String destAliasName) {
		String result = destAliasName;
		while (aliasNames.contains(destAliasName)) {
			result += "1";
		}
		aliasNames.add(result);
		return result;
	}
	
	private boolean hasLength(BasicDBObject basicDBObject) {
		return basicDBObject != null && !basicDBObject.isEmpty();
	}
	
	private BasicDBList queryGroup(PlainSelect ps, BasicDBObject fields, DBCollection coll) {
		HashSet<String> aliasNames = new HashSet<String>();
		
		BasicDBObject queryFields = new BasicDBObject();
		for (Object o : ps.getSelectItems()) {
			SelectItem si = (SelectItem) o;
			if (si instanceof SelectExpressionItem) {
				SelectExpressionItem sei = (SelectExpressionItem) si;
				Expression ex = sei.getExpression();
				if (ex instanceof Function) {
					String alias = sei.getAlias();
					Function fn = (Function) ex;
					String fnName = fn.getName().toLowerCase();
					
					if (fnName.equals("count")) {
						alias = Utils.hasLength(alias) ? alias : getAliasName(aliasNames, "count");
						if (fn.isAllColumns())
							queryFields.put(alias, new BasicDBObject("$sum", 1));
						else {
							String field = ex.toString().trim();
							field = field.substring(6, field.length() - 1);
							queryFields.put(alias, new BasicDBObject("$sum", "$" + field));
						}
					}
					else if (fnName.equals("sum")) {
						alias = Utils.hasLength(alias) ? alias : getAliasName(aliasNames, "total");
						String field = ex.toString().trim();
						field = field.substring(4, field.length() - 1);
						queryFields.put(alias, new BasicDBObject("$sum", "$" + field));
					}
				}
			}
		}
		aliasNames = null;
		List<Expression> groupsExpress = ps.getGroupByColumnReferences();
		
		if (Utils.hasLength(groupsExpress)) {
			BasicDBObject groupsFields = new BasicDBObject();
			for (Expression expression : groupsExpress) {
				String fieldName = expression.toString().trim();
				groupsFields.put(fieldName, "$" + fieldName);
			}
			queryFields.put("_id", groupsFields);
		}
		else {
			queryFields.put("_id", null);
		}
		
		
		List<DBObject> additionalList = new ArrayList<DBObject>(10);
		additionalList.add(new BasicDBObject("$group", queryFields));
		
		BasicDBObject queryObject = parseWhere(ps.getWhere());
		if (hasLength(queryObject)){
			additionalList.add(new BasicDBObject("$match", queryObject));
		}
		
		BasicDBObject having = parseWhere(ps.getHaving());
		if (hasLength(having))
			additionalList.add(new BasicDBObject("$match", having));
		
		BasicDBObject order = parserOrder(ps);
		if (hasLength(order))
			additionalList.add(new BasicDBObject("$sort", order));
		
		//TODO match 鍜宺egex 涓�捣宸ヤ綔
		AggregationOutput output = coll.aggregate(additionalList);

		
		if (output != null) {
			@SuppressWarnings("deprecation")
			CommandResult commandResult = output.getCommandResult();
			BasicDBList result = (BasicDBList) commandResult.get("result");
			for (int i = 0; i < result.size(); i++) {
				BasicDBObject dbObject = (BasicDBObject) result.get(i);
				BasicDBObject idObject = (BasicDBObject) dbObject.get("_id");
				if (idObject != null) {
					Set<String> keyset = idObject.keySet();
					for (String key : keyset) {
						dbObject.put(key, idObject.get(key));
					}
				}
			}
			additionalList = null;			
			return result;
		}
		return new BasicDBList();
	}
	
	@SuppressWarnings("unchecked")
	<T> T query() throws MongoSQLException {
		parseSQL();
		if (!(_statement instanceof Select))
			throw new IllegalArgumentException("not a query sql statement");
		
		Select select = (Select) _statement;
		if (!(select.getSelectBody() instanceof PlainSelect))
			throw new UnsupportedOperationException("can only handle PlainSelect so far");
		
		PlainSelect ps = (PlainSelect) select.getSelectBody();
		if (!(ps.getFromItem() instanceof Table))
			throw new UnsupportedOperationException("can only handle regular tables");
		
		DBCollection coll = getCollection((Table) ps.getFromItem());
		
		BasicDBObject fields = new BasicDBObject();
		
		boolean isGroup = false;
		
		for (Object o : ps.getSelectItems()) {
			SelectItem si = (SelectItem) o;
			if (si instanceof SelectExpressionItem) {
				SelectExpressionItem sei = (SelectExpressionItem) si;
				Expression ex = sei.getExpression();
				if (ex instanceof Function) {
					isGroup = true;
					break;
				}
			}
		}
		if (!isGroup) {
			List<Expression> groups = ps.getGroupByColumnReferences();
			isGroup = Utils.hasLength(groups);
		}
		
		if (!isGroup) {
			return (T) queryCommon(ps, fields, coll);
		}
		else {
			return (T) queryGroup(ps, fields, coll);
		}
	}
	
	protected int writeOperation() throws MongoSQLException {
		parseSQL();
		if (_statement instanceof Insert)
			return insert((Insert) _statement);
		
		else if (_statement instanceof Update)
			return update((Update) _statement);
		
		else if (_statement instanceof Drop)
			return drop((Drop) _statement);
		
		else if (_statement instanceof Delete)
			return delete((Delete) _statement);
		
		throw new RuntimeException("unknown write: " + _statement.getClass());
	}
	
	private int delete(Delete del) throws MongoException {
		if (!(_statement instanceof Delete))
			throw new IllegalArgumentException("not a delete sql statement");
		
		Delete delete = (Delete) _statement;
		DBCollection coll = getCollection(delete.getTable());
		// where
		DBObject where = parseWhere(delete.getWhere());
		coll.remove(where);
		return 1;
	}
	
	private int insert(Insert in) throws MongoSQLException {
		
		if (in.getColumns() == null)
			throw new MongoSQLException.BadSQL("have to give column names to insert");
		DBCollection coll = getCollection(in.getTable());
		
		if (!(in.getItemsList() instanceof ExpressionList))
			throw new UnsupportedOperationException("need ExpressionList");
		
		BasicDBObject bo = new BasicDBObject();
		
		List<?> valueList = ((ExpressionList) in.getItemsList()).getExpressions();
		if (in.getColumns().size() != valueList.size())
			throw new MongoSQLException.BadSQL(_sql + "\nnumber of values and columns have to match");
		
		for (int i = 0; i < valueList.size(); i++) {
			String fieldName = getFieldName(in.getColumns().get(i));
			Object value = toConstant((Expression) valueList.get(i));
			if (!fieldName.equals(MONGODB_ID)) {
				bo.put(fieldName, value);
			}
			else { 
				bo.put(fieldName, new ObjectId((String) value));
			}
		}
		coll.insert(bo);
		return bo.size();
	}
	
	private int update(Update up) throws MongoSQLException {
		BasicDBObject set = new BasicDBObject();
		
		for (int i = 0; i < up.getColumns().size(); i++) {
			String k = getFieldName(up.getColumns().get(i));
			Expression v = (up.getExpressions().get(i));
			Object constant = toConstant(v);
			if (!k.equals(MONGODB_ID))
				set.put(k.toString(), constant);
			else {
				//璨屼技mongodb 涓嶈兘鏇存柊_id
				//set.put(k.toString(), getObjectId(toConstant(v)));
			}
		}
		DBObject mod = new BasicDBObject(MONGODB_SET, set);
		DBCollection coll = getCollection(up.getTable());
		DBObject queryObject = parseWhere(up.getWhere());
		
		//閬电収涓ユ牸鐨刯dbc鏍囧噯,绗簩涓弬鏁拌缃垚false,鍗冲綋鏁版嵁涓嶅瓨鍦ㄦ椂锛屾洿鏂颁笉鎴愬姛????
		//鐜版敼鎴恡rue
		coll.update(queryObject, mod, true, true);
		//oll.update(queryObject, mod,false,false);
		return 1; // TODO
	}
	
	private int drop(Drop d) {
		DBCollection c = _db.getCollection(d.getName());
		c.drop();
		return 1;
	}
	
	// ---- helpers -----
	
	private Object toConstant(Expression e) {
		if (e instanceof JdbcParameter)
			return _params.get(_pos++);
		if (e instanceof JdbcNamedParameter)
			return _params.get(_pos++);
		if (e instanceof Column)
			return _params.get(_pos++);
		if (e instanceof StringValue)
			return ((StringValue) e).getValue();
		if (e instanceof DoubleValue)
			return ((DoubleValue) e).getValue();
		if (e instanceof LongValue)
			return ((LongValue) e).getValue();
		if (e instanceof NullValue)
			return null;
		
		throw new UnsupportedOperationException("can't turn [" + e + "] " + e.getClass().getName() + " into constant ");
	}
	
	private void parseOrAnd(BasicDBObject o, Expression e) {
		// or灏哸nd宸﹁竟涓�垎涓�
		BinaryExpression subE = (BinaryExpression) e;
		Expression leftE = subE.getLeftExpression();
		Expression rightE = subE.getRightExpression();
		
		BasicDBList subList = new BasicDBList();
		
		BasicDBObject leftO = parseWhere(leftE);
		BasicDBObject rightO = parseWhere(rightE);
		
		if (!leftO.isEmpty())
			subList.add(leftO);
		if (!rightO.isEmpty())
			subList.add(rightO);
		o.append("$" + subE.getStringExpression().toLowerCase(), subList);
	}
	
	@SuppressWarnings("deprecation")
	public ObjectId getObjectId(Object objectId) {
		
		return ObjectId.massageToObjectId(objectId);
	}
	
	private BasicDBObject parseWhere(Expression e) {
		BasicDBObject o = null;
		if (e == null) {
			o = new BasicDBObject();
			return o;
		}
		
		// and
		if (e instanceof AndExpression) {
			o = new BasicDBObject();
			parseOrAnd(o, e);
		}
		// or
		else if (e instanceof OrExpression) {
			o = new BasicDBObject();
			parseOrAnd(o, e);
		}
		// ();
		else if (e instanceof Parenthesis) {
			Expression subE = ((Parenthesis) e).getExpression();
			return parseWhere(subE);
		}
		// = ,>,>= <,<=
		else if (e instanceof Parenthesis) {
			throw new UnsupportedOperationException("can't handle: " + e.getClass() + " yet");
		}
		else if (e instanceof BinaryExpression) {
			o = new BasicDBObject();
			BinaryExpression subE = (BinaryExpression) e;
			Expression leftE = subE.getLeftExpression();
			Expression rightE = subE.getRightExpression();
			
			Object constant = null;
			String fieldName = parseFieldName(leftE);
			
			if (fieldName != null) {
				constant = toConstant(rightE);
			}
			else {
				fieldName = parseFieldName(rightE);
				constant = toConstant(leftE);
			}
			
			if (fieldName != null) {
				if (subE instanceof EqualsTo) {
					boolean changedId = false;
					if (fieldName.equals(MONGODB_ID)) {
						ObjectId objectId = getObjectId(constant);
						if (objectId != null) {
							o.put(fieldName, objectId);
							changedId = true;
						}
					}
					if (!changedId)
						o.put(fieldName, constant);
				}
				else {
					String express = subE.getStringExpression();
					express = TSql2Mongo.getExpress(express);
					BasicDBObject subDbObject = null;
					if (express.equals(TSql2Mongo.regex)) {
						String constantStr = constant.toString();
						constantStr = constantStr.replace("%", "/*");
						subDbObject = new BasicDBObject(express, constantStr);
						subDbObject.put("$options", "i");
					}
					else
						subDbObject = new BasicDBObject(express, constant);
					
					if (hasLength(subDbObject))
						o.put(fieldName, subDbObject);
				}
			}
		}
		else {
			throw new UnsupportedOperationException("can't handle: " + e.getClass() + " yet");
		}
		return o;
	}
	
	private Statement parse(String s) throws MongoSQLException {
		s = s.trim();
		
		try {
			return (new CCJSqlParserManager()).parse(new StringReader(s));
		}
		catch (Exception e) {
			throw new MongoSQLException.BadSQL(s);
		}
	}
	
	// ----
	private String trimBrace(String wraped) {
		if (isJdbcName(wraped))
			return wraped.substring(1, wraped.length() - 1);
		else
			return wraped;
	}
	
	private DBCollection getCollection(Table t) {
		String tableName = t.getName();
		tableName = trimBrace(tableName);
		return _db.getCollection(tableName);
	}
	
	private String getFieldName(Column cln) {
		String fieldName = trimBrace(cln.getColumnName());
		String tableName = cln.getTable().getName();
		if (Utils.hasLength(tableName)) {
			tableName = trimBrace(cln.getTable().getName());
		}
		String wholeName = Utils.hasLength(tableName) ? tableName + "." + fieldName : fieldName;
		return wholeName;
	}
	
	//todo where 1=1 娌℃湁瑙ｅ喅
	private String parseFieldName(Expression e) {
		if (e instanceof StringValue)
			return e.toString();
		if (e instanceof Column) {
			return getFieldName((Column) e);
		}
		
		return null;
	}
}
