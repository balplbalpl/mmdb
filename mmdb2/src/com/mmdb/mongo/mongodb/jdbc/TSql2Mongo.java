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

import java.util.Map;

import com.mmdb.mongo.helpinput.maps.CaseInsensitiveHashMap;
import com.mongodb.QueryOperators;

class TSql2Mongo {
	
	static String regex = "$regex";
	@SuppressWarnings("serial")
	static Map<String, String> jdbcExresses = new CaseInsensitiveHashMap<String>() {
		
		{
			put("<", QueryOperators.LT); // 小于("$lt")
			put("<=", QueryOperators.LTE);// 小于等于("$lte")
			put(">", QueryOperators.GT); // 大于("$gt")
			put(">=", QueryOperators.GTE);// 大于等于("$gte")
			put("<>", QueryOperators.NE);// 不等于("$ne")
			put("IN", QueryOperators.IN);
			put("NOT IN", QueryOperators.NIN);
			put("MOD", QueryOperators.MOD);
			put("EXISTS", QueryOperators.EXISTS);
			put("LIKE", regex);
		}
	};
	
	public static String getExpress(String jdbcExpress) {
		String result = jdbcExresses.get(jdbcExpress);
		
		if (result == null)
			throw new UnsupportedOperationException("unsupported jdbc express:" + jdbcExpress);
		
		return result;
	}
	
}
