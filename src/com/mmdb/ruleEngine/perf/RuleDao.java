package com.mmdb.ruleEngine.perf;

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
import com.mmdb.model.rule.LinkRule;
import com.mmdb.mongo.MongoConnect;

/**
 * 规则的数据库操作类
 * 
 * @author yuhao.guan
 * @version 1.0 2015-7-2 
 */
@Component("ruleDao")
public class RuleDao {
	
	private Log logger = LogFactory.getLogger("RuleDao");
	
	/**
	 * 保存匹配规则
	 * 
	 * @param LinkRule 规则对象
	 * @throws SQLException 
	 */
	public void saveLinkRule(LinkRule rule) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			conn = MongoConnect.getConnection();
			
			String sql = "insert into Rule(id,name,ruleType,ruleGroup,active,priority," +
					"description,condition,condtionJson,owner) values (?,?,?,?,?,?,?,?,?,?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, rule.getId());
			ps.setString(2, rule.getName());
			ps.setString(3, rule.getRuleType());
			ps.setString(4, rule.getRuleGroup());
			ps.setString(5, rule.getActive());
			ps.setString(6, rule.getPriority());
			ps.setString(7, rule.getDescription());
			ps.setString(8, rule.getCondition());
			ps.setString(9, rule.getConditionJson().toString());
			ps.setString(10, rule.getOwner());
			ps.executeUpdate();
		}catch(Exception e){
			logger.eLog("Save LinkRule to db error!"+e.getMessage(),e);
			throw e;
		}finally{
			close(null,ps,null);
		}
	}
	
	/**
	 * 更新匹配规则
	 * 
	 * @param LinkRule 规则对象
	 * @throws SQLException 
	 */
	public void updateLinkRule(LinkRule rule) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			conn = MongoConnect.getConnection();
			
			String sql = "update Rule set name=?,ruleType=?,ruleGroup=?,active=?," +
					"priority=?,description=?,condition=?,condtionJson=? where id=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, rule.getName());
			ps.setString(2, rule.getRuleType());
			ps.setString(3, rule.getRuleGroup());
			ps.setString(4, rule.getActive());
			ps.setString(5, rule.getPriority());
			ps.setString(6, rule.getDescription());
			ps.setString(7, rule.getCondition());
			ps.setString(8, rule.getConditionJson().toString());
			ps.setString(9, rule.getId());
			ps.executeUpdate();
		}catch(Exception e){
			logger.eLog("Update LinkRule error!"+e.getMessage(),e);
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
	public void deleteLinkRule(String id) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			conn = MongoConnect.getConnection();
			
			String sql = "delete from Rule where id=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, id);
			ps.executeUpdate();
		}catch(Exception e){
			logger.eLog("Delete LinkRule from db error!"+e.getMessage(),e);
			throw e;
		}finally{
			close(null,ps,null);
		}
	}
	
	/**
	 * 更改匹配规状态
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
			
			String sql = "update Rule set active=? where id=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, statue);
			ps.setString(2, id);
			ps.executeUpdate();
		}catch(Exception e){
			logger.eLog("Active/UnActive LinkRule error!"+e.getMessage(),e);
			throw e;
		}finally{
			close(null,ps,null);
		}
	}
	
	/**
	 * 获取到所有的已激活的规则
	 * 
	 * @return List<LinkRule>
	 */
	public List<LinkRule> getActiveRules(String ruleType) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<LinkRule> ruleList = new ArrayList<LinkRule>();
		try{
			conn = MongoConnect.getConnection();
			String sql = "select * from Rule where active = '1' and ruleType=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1,ruleType);
			rs = ps.executeQuery();
			while(rs.next()){
				LinkRule rule = new LinkRule();
				rule.setId(rs.getString("id"));
				rule.setName(rs.getString("name"));
				rule.setRuleType(rs.getString("ruleType"));
				rule.setRuleGroup(rs.getString("ruleGroup"));
				rule.setActive(rs.getString("active"));
				rule.setPriority(rs.getString("priority"));
				rule.setDescription(rs.getString("description"));
				rule.setCondition(rs.getString("condition"));
				if(rs.getString("condtionJson")!= null){
					JSONObject condJson = 
							JSONObject.fromObject(rs.getObject("condtionJson"));
					rule.setConditionJson(condJson);
				}
				rule.setOwner(rs.getString("owner"));
				ruleList.add(rule);
			}
			return ruleList;
		}catch(Exception e){
			logger.eLog("Get active rules error!",e);
			throw e;
		}finally{
			close(rs,ps,null);
		}
	}
	
	/**
	 * 获取到指定类型的规则列表
	 * 
	 * @return List<LinkRule>
	 */
	public List<LinkRule> getRulesByType(String ruleType) throws Exception{
		String sql = "select * from Rule where ruleType= '"+ruleType+"'";
		return getRulesBySQL(sql);
		/*Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<LinkRule> ruleList = new ArrayList<LinkRule>();
		try{
			conn = MongoConnect.getConnection();
			String sql = "select * from rule where ruleType=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1,ruleType);
			rs = ps.executeQuery();
			while(rs.next()){
				LinkRule rule = new LinkRule();
				rule.setId(rs.getString("id"));
				rule.setName(rs.getString("name"));
				rule.setRuleType(rs.getString("ruleType"));
				rule.setRuleGroup(rs.getString("ruleGroup"));
				rule.setActive(rs.getString("active"));
				rule.setPriority(rs.getString("priority"));
				rule.setDescription(rs.getString("description"));
				rule.setCondition(rs.getString("condition"));
				if(rs.getString("condtionJson")!= null){
					JSONObject condJson = 
							JSONObject.fromObject(rs.getObject("condtionJson"));
					rule.setConditionJson(condJson);
				}
				ruleList.add(rule);
			}
			return ruleList;
		}catch(Exception e){
			logger.eLog("Get active rules error!",e);
			throw e;
		}finally{
			close(rs,ps,null);
		}*/
	}
	
	/**
	 * 获取某个用户的规则
	 * 
	 * @return List<LinkRule>
	 */
	public List<LinkRule> getRulesByUser(String ruleType,String userName) throws Exception{
		String sql = "select * from Rule where ruleType= '"+ruleType+"' and owner = '"+userName+"'";
		return getRulesBySQL(sql);
	}
	
	/**
	 * 通过规则名称和类型获取到指定类型的规则列表
	 * 
	 * @param name
	 * @param type
	 * @return List<LinkRule>
	 */
	public List<LinkRule> getRulesByName(String name,String type) throws Exception{
		String sql = "select * from Rule where ruleType= '"+type+"' and name = '"+name+"'";
		
		return getRulesBySQL(sql);
	}
	
	/**
	 * 获取到指定类型的规则列表
	 * 
	 * @return List<LinkRule>
	 */
	private List<LinkRule> getRulesBySQL(String sql) throws Exception{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<LinkRule> ruleList = new ArrayList<LinkRule>();
		try{
			conn = MongoConnect.getConnection();
			//sql = "select * from rule where ruleType= 'link2ci";
			stmt = conn.createStatement();
			//ps.setString(1,ruleType);
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				LinkRule rule = new LinkRule();
				rule.setId(rs.getString("id"));
				rule.setName(rs.getString("name"));
				rule.setRuleType(rs.getString("ruleType"));
				rule.setRuleGroup(rs.getString("ruleGroup"));
				rule.setActive(rs.getString("active"));
				rule.setPriority(rs.getString("priority"));
				rule.setDescription(rs.getString("description"));
				rule.setCondition(rs.getString("condition"));
				if(rs.getString("condtionJson")!= null){
					JSONObject condJson = 
							JSONObject.fromObject(rs.getObject("condtionJson"));
					rule.setConditionJson(condJson);
				}
				rule.setOwner(rs.getString("owner"));
				ruleList.add(rule);
			}
			return ruleList;
		}catch(Exception e){
			logger.eLog("Get rule list error!",e);
			throw e;
		}finally{
			close(rs,stmt,null);
		}
	}
	
	/**
	 * 通过ID获取到指定的规则
	 * 
	 * @return LinkRule
	 */
	public LinkRule getRulesById(String id){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			LinkRule rule = new LinkRule();
			conn = MongoConnect.getConnection();
			String sql = "select * from Rule where id=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1,id);
			rs = ps.executeQuery();
			while(rs.next()){
				rule.setId(rs.getString("id"));
				rule.setName(rs.getString("name"));
				rule.setRuleType(rs.getString("ruleType"));
				rule.setRuleGroup(rs.getString("ruleGroup"));
				rule.setActive(rs.getString("active"));
				rule.setPriority(rs.getString("priority"));
				rule.setDescription(rs.getString("description"));
				rule.setCondition(rs.getString("condition"));
				if(rs.getString("condtionJson")!= null){
					JSONObject condJson = 
							JSONObject.fromObject(rs.getObject("condtionJson"));
					rule.setConditionJson(condJson);
				}
				rule.setOwner(rs.getString("owner"));
			}
			return rule;
		}catch(Exception e){
			logger.eLog("Get rule by id ["+id+"] error!",e);
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
	
}
