package com.mmdb.mongo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.mmdb.mongo.mongodb.jdbc.MongoStatement;

public class Client {

	public static void main(String[] args) throws Exception {
		Class.forName("com.mmdb.mongo.mongodb.jdbc.MongoDriver");
		//Class.forName("com.mongodb.jdbc.MongoDriver");
		Connection c = DriverManager.getConnection("mongodb://localhost:40003/test");
		PreparedStatement ps = c.prepareStatement("select * from Performance group by `ciName` desc");
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			System.out.println(rs.getString("ciName") +"   ");
		}
		
		
		String sql = "select * from Ci";
		MongoStatement stmt = (MongoStatement) c.createStatement();
		//stmt.executeUpdate("delete from Ci");
		int count = stmt.executeQueryCount(sql);
		System.out.println(count);

	}

}
