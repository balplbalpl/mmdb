package com.mmdb.model.mapping.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.mapping.PerfToDbMapping;
import com.mmdb.mongo.MongoConnect;

/**
 * 数据集和性能数据映射规则的数据库操作类
 * 
 * @author yuhao.guan
 * @version 1.0 2015-7-18
 */
@Component("perfToDbMapStorage")
public class PerfToDbMapStorage {
	private Log logger = LogFactory.getLogger("PerfToDbMapStorage");
	
	/**
	 * 保存匹配规则
	 * 
	 * @param PerfToDbMappingRule 规则对象
	 * @throws SQLException 
	 */
	public void save(PerfToDbMapping mapping) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			conn = MongoConnect.getConnection();
			
			String sql = "insert into PerfToDbMapping(name,dataSourceId,active," +
					"ciCondition,kpiCondition,ciConditionJson,kpiConditionJson,fieldMap,customFieldsMap,valExp,ciHex,kpiHex,owner,isAddSync) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, mapping.getName());
			ps.setString(2, mapping.getDataSourceId());
			ps.setString(3, mapping.getActive());
			ps.setString(4, mapping.getCiCondition());
			ps.setString(5, mapping.getKpiCondition());
			ps.setString(6, mapping.getCiConditionJson().toString());
			ps.setString(7, mapping.getKpiConditionJson().toString());
			ps.setString(8, mapping.getFieldMap().toString());
			ps.setString(9, mapping.getCustomFieldsMap().toString());
			ps.setString(10, mapping.getValExp());
			ps.setString(11, mapping.getCiHex());
			ps.setString(12, mapping.getKpiHex());
			ps.setString(13, mapping.getOwner());
			ps.setString(14, mapping.getIsAddSync());
			ps.executeUpdate();
		}catch(Exception e){
			logger.eLog("Save PerfToDbMapping to db error!"+e.getMessage(),e);
			throw e;
		}finally{
			close(null,ps,null);
		}
	}
	
	/**
	 * 更新匹配规则
	 * 
	 * @param PerfToDbMappingRule 规则对象
	 * @throws SQLException 
	 */
	public void update(PerfToDbMapping mapping) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			conn = MongoConnect.getConnection();
			
			String sql = "update PerfToDbMapping set name=?,dataSourceId=?,active=?,ciCondition=?," +
					"kpiCondition=?,ciConditionJson=?,kpiConditionJson=?,fieldMap=?,customFieldsMap=?,valExp=?,ciHex=?,kpiHex=?,isAddSync=? where _id=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, mapping.getName());
			ps.setString(2, mapping.getDataSourceId());
			ps.setString(3, mapping.getActive());
			ps.setString(4, mapping.getCiCondition());
			ps.setString(5, mapping.getKpiCondition());
			ps.setString(6, mapping.getCiConditionJson().toString());
			ps.setString(7, mapping.getKpiConditionJson().toString());
			ps.setString(8, mapping.getFieldMap().toString());
			ps.setString(9, mapping.getCustomFieldsMap().toString());
			ps.setString(10, mapping.getValExp());
			ps.setString(11, mapping.getCiHex());
			ps.setString(12, mapping.getKpiHex());
			ps.setString(13, mapping.getIsAddSync());
			ps.setString(14, mapping.getId());
			ps.executeUpdate();
		}catch(Exception e){
			logger.eLog("Update PerfToDbMapping error!"+e.getMessage(),e);
			throw e;
		}finally{
			close(null,ps,null);
		}
	}
	
	/**
	 * 删除匹配规则
	 * 
	 * @param id 规则唯一标识
	 * @throws SQLException 
	 */
	public void deleteById(String id) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			conn = MongoConnect.getConnection();
			
			String sql = "delete from PerfToDbMapping where _id=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, id);
			ps.executeUpdate();
		}catch(Exception e){
			logger.eLog("Delete PerfToDbMapping from db error!"+e.getMessage(),e);
			throw e;
		}finally{
			close(null,ps,null);
		}
	}
	
	/**
	 * 更改匹配规则激活状态
	 * 
	 * @param id 规则唯一标识
	 * @param statue 激活状态
	 * @throws SQLException 
	 */
	public void changeActiveStatue(String id,String statue) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			conn = MongoConnect.getConnection();
			
			String sql = "update PerfToDbMapping set active=? where _id=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, statue);
			ps.setString(2, id);
			ps.executeUpdate();
		}catch(Exception e){
			logger.eLog("Active/UnActive PerfToDbMapping error!"+e.getMessage(),e);
			throw e;
		}finally{
			close(null,ps,null);
		}
	}
	
	/**
	 * 获取到所有的映射
	 * 
	 * @return List<PerfToDbMapping>
	 */
	public List<PerfToDbMapping> getAllMapping() throws Exception{
		
		String sql = "select * from PerfToDbMapping";
		return getMappingsBySQL(sql);
	}
	
	/**
	 * 通过名称查询映射
	 * 
	 * @return List<PerfToDbMapping>
	 */
	public List<PerfToDbMapping> getByName(String name) throws Exception{
		
		String sql = "select * from PerfToDbMapping where name='"+name+"'";
		return getMappingsBySQL(sql);
	}
	
	public List<PerfToDbMapping> getByOwner(String username) throws Exception{
		String sql = "select * from PerfToDbMapping where `owner`='"+username+"'";
		return getMappingsBySQL(sql);
	}
	
	/**
	 * 获取到指定类型的规则列表
	 * 
	 * @return List<PerfToDbMapping>
	 */
	private List<PerfToDbMapping> getMappingsBySQL(String sql) throws Exception{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<PerfToDbMapping> ruleList = new ArrayList<PerfToDbMapping>();
		try{
			conn = MongoConnect.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				PerfToDbMapping mapping = new PerfToDbMapping();
				mapping.setId(rs.getString("_id"));
				mapping.setName(rs.getString("name"));
				mapping.setDataSourceId(rs.getString("dataSourceId"));
				mapping.setCiCondition(rs.getString("ciCondition"));
				mapping.setKpiCondition(rs.getString("kpiCondition"));
				JSONObject ciConditionJson = null;
				try{
					ciConditionJson = JSONObject.fromObject(rs.getString("ciConditionJson"));
				}catch(Exception e){
					ciConditionJson = new JSONObject();
				}
				mapping.setCiConditionJson(ciConditionJson);
				JSONObject kpiConditionJson = null;
				try{
					kpiConditionJson = JSONObject.fromObject(rs.getString("kpiConditionJson"));
				}catch(Exception e){
					kpiConditionJson = new JSONObject();
				}
				mapping.setKpiConditionJson(kpiConditionJson);
				mapping.setFieldMap(JSONObject.fromObject(rs.getString("fieldMap")));
				mapping.setCustomFieldsMap(JSONObject.fromObject(rs.getString("customFieldsMap")));
				mapping.setValExp(rs.getString("valExp"));
				mapping.setCiHex(rs.getString("ciHex"));
				mapping.setKpiHex(rs.getString("kpiHex"));
				mapping.setOwner(rs.getString("owner"));
				mapping.setIsAddSync(rs.getString("isAddSync"));
				ruleList.add(mapping);
			}
			return ruleList;
		}catch(Exception e){
			logger.eLog("Get mapping list error!",e);
			throw e;
		}finally{
			close(rs,stmt,null);
		}
	}
	
	/**
	 * 通过ID获取到指定的规则
	 * 
	 * @return PerfToDbMapping
	 */
	public PerfToDbMapping getById(String id){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			PerfToDbMapping mapping = new PerfToDbMapping();
			conn = MongoConnect.getConnection();
			String sql = "select * from PerfToDbMapping where _id=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1,id);
			rs = ps.executeQuery();
			while(rs.next()){
				mapping.setId(rs.getString("_id"));
				mapping.setName(rs.getString("name"));
				mapping.setDataSourceId(rs.getString("dataSourceId"));
				mapping.setCiCondition(rs.getString("ciCondition"));
				mapping.setKpiCondition(rs.getString("kpiCondition"));
				JSONObject ciConditionJson = null;
				try{
					ciConditionJson = JSONObject.fromObject(rs.getString("ciConditionJson"));
				}catch(Exception e){
					ciConditionJson = new JSONObject();
				}
				mapping.setCiConditionJson(ciConditionJson);
				JSONObject kpiConditionJson = null;
				try{
					kpiConditionJson = JSONObject.fromObject(rs.getString("kpiConditionJson"));
				}catch(Exception e){
					kpiConditionJson = new JSONObject();
				}
				mapping.setKpiConditionJson(kpiConditionJson);
				mapping.setFieldMap(JSONObject.fromObject(rs.getString("fieldMap")));
				mapping.setCustomFieldsMap(JSONObject.fromObject(rs.getString("customFieldsMap")));
				mapping.setValExp(rs.getString("valExp"));
				mapping.setCiHex(rs.getString("ciHex"));
				mapping.setKpiHex(rs.getString("kpiHex"));
				mapping.setOwner(rs.getString("owner"));
				mapping.setIsAddSync(rs.getString("isAddSync"));
			}
			return mapping;
		}catch(Exception e){
			logger.eLog("Get mapping by id error!",e);
			return null;
		}finally{
			close(rs,ps,null);
		}
	}
	
	/**
	 * 关闭数据库连接
	 * @param resultSet
	 *            ResultSet 查询结果集
	 * @param statement
	 *            Statement
	 * @param connection
	 *            Connection 数据库连接
	 */
	private void close(ResultSet resultSet, Statement statement,
			Connection connection) {
		if (resultSet != null) {
			try {
				resultSet.close();
				resultSet = null;
			} catch (Exception ex) {
				if (resultSet != null) {
					try {
						resultSet.close();
						resultSet = null;
					} catch (SQLException ex1) {
						logger.eLog("关闭resultSet出现异常:" + ex1.getMessage(), ex1);
					}
				}
			}
		}
		if (statement != null) {
			try {
				statement.close();
				statement = null;
			} catch (Exception ex) {
				if (statement != null) {
					try {
						statement.close();
						statement = null;
					} catch (Exception ex1) {
						logger.eLog("关闭statement出现异常:" + ex1.getMessage(), ex1);
					}
				}
			}
		}
		if (connection != null) {
			try {
				connection.close();
				connection = null;
			} catch (Exception ex) {
				if (connection != null) {
					try {
						connection.close();
						connection = null;
					} catch (Exception ex1) {
						logger.eLog("关闭connection出现异常:" + ex1.getMessage(),
								ex1);
					}
				}
			}
		}
	}
	
	public Boolean matchPerfMapping2(String exp, String table){
		Connection conn = null;
		Statement ps = null;
		ResultSet rs = null;
		try{
			conn = MongoConnect.getConnection();
			String sql = "select * from "+table+(exp.equals("") ? "":" where "+exp);
			ps = conn.createStatement();
			rs = ps.executeQuery(sql);
			if(rs.next()){
				
			}
			return true;
		}catch(Exception e){
			logger.eLog("matchPerfMapping2 error!",e);
			return false;
		}finally{
			close(rs,ps,null);
		}
	}
}
