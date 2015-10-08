package com.mmdb.mongo;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class Test {
	public static void main(String[] args) {
		// try {
		// Class.forName("com.mmdb.mongo.mongodb.jdbc.MongoDriver");
		// // Connection c = DriverManager.getConnection("abcdefg");
		// Connection connection = DriverManager.getConnection("aa");
		// Statement stmt = connection.createStatement();
		// ResultSet rs = stmt.executeQuery("select * from CiCategory");
		// while (rs.next()) {
		// System.out.println(rs.getString(0));
		// }
		//
		// Class.forName("com.mysql.jdbc.Driver");
		//
		// Connection connection1 = DriverManager.getConnection("aa", "we",
		// "wr");
		// System.out.println(connection1.getClass().getSimpleName());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		DB db = MongoConnect.getDb();
		DBCollection collection = db.getCollection("Ci");
		collection.remove(new BasicDBObject("_id", new ObjectId(
				"55a71433966ac0546e23bb23")));
	}
}
