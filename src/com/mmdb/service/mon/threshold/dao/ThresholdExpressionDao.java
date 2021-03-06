/**
 * 
 */
package com.mmdb.service.mon.threshold.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.service.mon.threshold.model.ThresholdExpression;

/**
 * 操作阈值表达式定义的数据访问对象。
 * 
 * @author aol_aog@163.com(James Gao)
 * 
 */
//@Repository("ThresholdExpressionDao")
public class ThresholdExpressionDao {
	private Log log = LogFactory.getLogger("ThresholdExpressionDao");
	/**
	 * 实时数据库的数据源。
	 */
	@Resource(name = "commonDS")
	private DataSource commonDS;

	/**
	 * 
	 */
	public ThresholdExpressionDao() {

	}

	/**
	 * 添加一组阈值定义。
	 * 
	 * @param tes
	 */
	public void add(ThresholdExpression[] tess) throws Exception {
		if(tess==null||tess.length<=0) return;
		String sql = "insert into mon_viewkpiciuser_rel(id,kpi_class,kpi,ci,instance,viewid,"
				+ "userid,threshold1,threshold2,threshold3,threshold4,enabled,update_time) "
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement st = null;
		Connection conn = null;

		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);
			for (ThresholdExpression tes : tess) {
				st.setString(1, tes.getId());
				st.setString(2, tes.getKpiClass());
				st.setString(3, tes.getKpi());
				st.setString(4, tes.getCi());
				st.setString(5, tes.getInstance());
				st.setString(6, tes.getView());
				// System.out.println("--------------"+tes.getUser());
				st.setString(7, tes.getUser());
				st.setString(8, tes.getThreshold1());
				st.setString(9, tes.getThreshold2());
				st.setString(10, tes.getThreshold3());
				st.setString(11, tes.getThreshold4());
				st.setString(12, tes.getEnabled());
				st.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
				st.addBatch();
			}
			st.executeBatch();
		} catch (SQLException ex) {
			log.eLog("数据查询异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, null);
		}
	}

	/**
	 * 更新一组阈值定义。
	 * 
	 * @param tes
	 */
	public void update(ThresholdExpression[] tess) throws Exception {
		if(tess==null||tess.length<=0) return;
		String sql = "update mon_viewkpiciuser_rel set ci=?,instance=?,viewid=?,"
				+ "userid=?,threshold1=?,threshold2=?,threshold3=?,threshold4=?,enabled=?,update_time=? "
				+ "where id=?";
		PreparedStatement st = null;
		Connection conn = null;

		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);
			for (ThresholdExpression tes : tess) {

				st.setString(1, tes.getCi());
				st.setString(2, tes.getInstance());
				st.setString(3, tes.getView());
				st.setString(4, tes.getUser());
				st.setString(5, tes.getThreshold1());
				st.setString(6, tes.getThreshold2());
				st.setString(7, tes.getThreshold3());
				st.setString(8, tes.getThreshold4());
				st.setString(9, tes.getEnabled());
				st.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
				st.setString(11, tes.getId());
				st.addBatch();
			}
			st.executeBatch();
		} catch (SQLException ex) {
			log.eLog("数据更新异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, null);
		}
	}

	/**
	 * 删除一组阈值定义。
	 * 
	 * @param ids
	 */
	public void delete(String ids[]) throws Exception {
		if(ids==null||ids.length<=0) return;
		String sql = "delete from mon_viewkpiciuser_rel where id=? ";
		PreparedStatement st = null;
		Connection conn = null;

		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);
			for (String id : ids) {
				st.setString(1, id);
				st.addBatch();
			}
			st.executeBatch();
		} catch (SQLException ex) {
			log.eLog("数据删除异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, null);
		}
	}

	/**
	 * 通过ID得到阈值定义。
	 * 
	 * @param id
	 * @return
	 */
	public ThresholdExpression getByID(String id) throws Exception {
		String sql = "select id,kpi_class,kpi,ci,instance,viewid,"
				+ "userid,threshold1,threshold2,threshold3,threshold4,enabled,update_time from mon_viewkpiciuser_rel where id=? ";
		PreparedStatement st = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);

			st.setString(1, id);

			rs = st.executeQuery();
			if (rs.next()) {
				ThresholdExpression te = new ThresholdExpression();
				te.setId(rs.getString(1));
				te.setKpiClass(rs.getString(2));
				te.setKpi(rs.getString(3));
				te.setCi(rs.getString(4));
				te.setInstance(rs.getString(5));
				te.setView(rs.getString(6));
				te.setUser(rs.getString(7));
				te.setThreshold1(rs.getString(8));
				te.setThreshold2(rs.getString(9));
				te.setThreshold3(rs.getString(10));
				te.setThreshold4(rs.getString(11));
				te.setEnabled(rs.getString(12));
				te.setUpdateTime(rs.getTimestamp(13));
				return te;
			}
		} catch (SQLException ex) {
			log.eLog("数据查询异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, rs);
		}
		return null;
	}

	/**
	 * 得到所有的阀值定义。
	 * 
	 * @param kpi
	 * @return
	 */
	public ThresholdExpression[] getAll() throws Exception {
		String sql = "select id,kpi_class,kpi,ci,instance,viewid,"
				+ "userid,threshold1,threshold2,threshold3,threshold4,enabled,update_time from mon_viewkpiciuser_rel ";
		PreparedStatement st = null;
		Connection conn = null;
		ResultSet rs = null;
		List<ThresholdExpression> tess = new LinkedList<ThresholdExpression>();
		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);

			rs = st.executeQuery();

			while (rs.next()) {
				ThresholdExpression te = new ThresholdExpression();
				te.setId(rs.getString(1));
				te.setKpiClass(rs.getString(2));
				te.setKpi(rs.getString(3));
				te.setCi(rs.getString(4));
				te.setInstance(rs.getString(5));
				te.setView(rs.getString(6));
				te.setUser(rs.getString(7));
				te.setThreshold1(rs.getString(8));
				te.setThreshold2(rs.getString(9));
				te.setThreshold3(rs.getString(10));
				te.setThreshold4(rs.getString(11));
				te.setEnabled(rs.getString(12));
				te.setUpdateTime(rs.getTimestamp(13));
				tess.add(te);
			}
		} catch (SQLException ex) {
			log.eLog("数据查询异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, rs);
		}
		return tess.toArray(new ThresholdExpression[0]);
	}

	/**
	 * 通过CI得到其下所有的KPI定义。
	 * 
	 * @param kpi
	 * @return
	 */
	public ThresholdExpression[] getByCi(String ci) throws Exception {
		String sql = "select id,kpi_class,kpi,ci,instance,viewid,"
				+ "userid,threshold1,threshold2,threshold3,threshold4,enabled,update_time from mon_viewkpiciuser_rel where ci=? ";
		PreparedStatement st = null;
		Connection conn = null;
		ResultSet rs = null;
		List<ThresholdExpression> tess = new LinkedList<ThresholdExpression>();
		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);
			st.setString(1, ci);
			rs = st.executeQuery();
			while (rs.next()) {
				ThresholdExpression te = new ThresholdExpression();
				te.setId(rs.getString(1));
				te.setKpiClass(rs.getString(2));
				te.setKpi(rs.getString(3));
				te.setCi(rs.getString(4));
				te.setInstance(rs.getString(5));
				te.setView(rs.getString(6));
				te.setUser(rs.getString(7));
				te.setThreshold1(rs.getString(8));
				te.setThreshold2(rs.getString(9));
				te.setThreshold3(rs.getString(10));
				te.setThreshold4(rs.getString(11));
				te.setEnabled(rs.getString(12));
				te.setUpdateTime(rs.getTimestamp(13));
				tess.add(te);
			}
		} catch (SQLException ex) {
			log.eLog("数据查询异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, rs);
		}
		return tess.toArray(new ThresholdExpression[0]);
	}

	/**
	 * 通过KPI得到其下所有的KPI定义。
	 * 
	 * @param kpi
	 * @return
	 */
	public ThresholdExpression[] getByKpi(String kpi) throws Exception {
		String sql = "select id,kpi_class,kpi,ci,instance,viewid,"
				+ "userid,threshold1,threshold2,threshold3,threshold4,enabled,update_time from mon_viewkpiciuser_rel where kpi=? ";
		PreparedStatement st = null;
		Connection conn = null;
		ResultSet rs = null;
		List<ThresholdExpression> tess = new LinkedList<ThresholdExpression>();
		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);
			st.setString(1, kpi);
			rs = st.executeQuery();
			while (rs.next()) {
				ThresholdExpression te = new ThresholdExpression();
				te.setId(rs.getString(1));
				te.setKpiClass(rs.getString(2));
				te.setKpi(rs.getString(3));
				te.setCi(rs.getString(4));
				te.setInstance(rs.getString(5));
				te.setView(rs.getString(6));
				te.setUser(rs.getString(7));
				te.setThreshold1(rs.getString(8));
				te.setThreshold2(rs.getString(9));
				te.setThreshold3(rs.getString(10));
				te.setThreshold4(rs.getString(11));
				te.setEnabled(rs.getString(12));
				te.setUpdateTime(rs.getTimestamp(13));
				tess.add(te);
			}
		} catch (SQLException ex) {
			log.eLog("数据查询异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, rs);
		}
		return tess.toArray(new ThresholdExpression[0]);
	}

	/**
	 * 通过VIEW得到其下所有的KPI定义。
	 * 
	 * @param kpi
	 * @return
	 */
	public ThresholdExpression[] getByView(String view) throws Exception {
		String sql = "select id,kpi_class,kpi,ci,instance,viewid,"
				+ "userid,threshold1,threshold2,threshold3,threshold4,enabled,update_time from mon_viewkpiciuser_rel where viewid=? ";
		PreparedStatement st = null;
		Connection conn = null;
		ResultSet rs = null;
		List<ThresholdExpression> tess = new LinkedList<ThresholdExpression>();
		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);
			st.setString(1, view);
			rs = st.executeQuery();
			while (rs.next()) {
				ThresholdExpression te = new ThresholdExpression();
				te.setId(rs.getString(1));
				te.setKpiClass(rs.getString(2));
				te.setKpi(rs.getString(3));
				te.setCi(rs.getString(4));
				te.setInstance(rs.getString(5));
				te.setView(rs.getString(6));
				te.setUser(rs.getString(7));
				te.setThreshold1(rs.getString(8));
				te.setThreshold2(rs.getString(9));
				te.setThreshold3(rs.getString(10));
				te.setThreshold4(rs.getString(11));
				te.setEnabled(rs.getString(12));
				te.setUpdateTime(rs.getTimestamp(13));
				tess.add(te);
			}
		} catch (SQLException ex) {
			log.eLog("数据查询异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, rs);
		}
		return tess.toArray(new ThresholdExpression[0]);
	}

	/**
	 * 通过KPI，CI得到其下所有的KPI定义。
	 * 
	 * @param kpi
	 * @param ci
	 * @return
	 */
	public ThresholdExpression[] getBy(String kpi, String ci) throws Exception {
		String sql = "select id,kpi_class,kpi,ci,instance,viewid,"
				+ "userid,threshold1,threshold2,threshold3,threshold4,enabled,update_time from mon_viewkpiciuser_rel where kpi=?"
				+ " and ci=? ";
		PreparedStatement st = null;
		Connection conn = null;
		ResultSet rs = null;
		List<ThresholdExpression> tess = new LinkedList<ThresholdExpression>();
		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);

			st.setString(1, kpi);
			st.setString(2, ci);

			rs = st.executeQuery();

			while (rs.next()) {
				ThresholdExpression te = new ThresholdExpression();
				te.setId(rs.getString(1));
				te.setKpiClass(rs.getString(2));
				te.setKpi(rs.getString(3));
				te.setCi(rs.getString(4));
				te.setInstance(rs.getString(5));
				te.setView(rs.getString(6));
				te.setUser(rs.getString(7));
				te.setThreshold1(rs.getString(8));
				te.setThreshold2(rs.getString(9));
				te.setThreshold3(rs.getString(10));
				te.setThreshold4(rs.getString(11));
				te.setEnabled(rs.getString(12));
				te.setUpdateTime(rs.getTimestamp(13));
				tess.add(te);
			}
		} catch (SQLException ex) {
			log.eLog("数据查询异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, rs);
		}
		return tess.toArray(new ThresholdExpression[0]);
	}

	/**
	 * 通过KPI，CI，Instance得到其下所有的KPI定义。
	 * 
	 * @param kpi
	 * @param ci
	 * @param instance
	 * @return
	 */
	public ThresholdExpression[] getBy(String kpi, String ci, String instance)
			throws Exception {
		String sql = "select id,kpi_class,kpi,ci,instance,viewid,"
				+ "userid,threshold1,threshold2,threshold3,threshold4,enabled,update_time from mon_viewkpiciuser_rel where kpi=?"
				+ " and ci=? and instance=?";
		PreparedStatement st = null;
		Connection conn = null;
		ResultSet rs = null;
		List<ThresholdExpression> tess = new LinkedList<ThresholdExpression>();
		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);

			st.setString(1, kpi);
			st.setString(2, ci);
			st.setString(3, instance);

			rs = st.executeQuery();

			while (rs.next()) {
				ThresholdExpression te = new ThresholdExpression();
				te.setId(rs.getString(1));
				te.setKpiClass(rs.getString(2));
				te.setKpi(rs.getString(3));
				te.setCi(rs.getString(4));
				te.setInstance(rs.getString(5));
				te.setView(rs.getString(6));
				te.setUser(rs.getString(7));
				te.setThreshold1(rs.getString(8));
				te.setThreshold2(rs.getString(9));
				te.setThreshold3(rs.getString(10));
				te.setThreshold4(rs.getString(11));
				te.setEnabled(rs.getString(12));
				te.setUpdateTime(rs.getTimestamp(13));
				tess.add(te);
			}
		} catch (SQLException ex) {
			log.eLog("数据查询异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, rs);
		}
		return tess.toArray(new ThresholdExpression[0]);
	}

	/**
	 * 通过KPI，CI,instance,view得到其下所有的KPI定义。
	 * 
	 * @param kpi
	 * @param ci
	 * @param instance
	 * @param view
	 * @return
	 */
	public ThresholdExpression[] getBy(String kpi, String ci, String instance,
			String view) throws Exception {
		String sql = "select id,kpi_class,kpi,ci,instance,viewid,"
				+ "userid,threshold1,threshold2,threshold3,threshold4,enabled,update_time from mon_viewkpiciuser_rel where kpi=?"
				+ " and ci=? and instance= ? and viewid=?";
		PreparedStatement st = null;
		Connection conn = null;
		ResultSet rs = null;
		List<ThresholdExpression> tess = new LinkedList<ThresholdExpression>();
		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);

			st.setString(1, kpi);
			st.setString(2, ci);
			st.setString(3, instance);
			st.setString(4, view);
			rs = st.executeQuery();

			while (rs.next()) {
				ThresholdExpression te = new ThresholdExpression();
				te.setId(rs.getString(1));
				te.setKpiClass(rs.getString(2));
				te.setKpi(rs.getString(3));
				te.setCi(rs.getString(4));
				te.setInstance(rs.getString(5));
				te.setView(rs.getString(6));
				te.setUser(rs.getString(7));
				te.setThreshold1(rs.getString(8));
				te.setThreshold2(rs.getString(9));
				te.setThreshold3(rs.getString(10));
				te.setThreshold4(rs.getString(11));
				te.setEnabled(rs.getString(12));
				te.setUpdateTime(rs.getTimestamp(13));
				tess.add(te);
			}
		} catch (SQLException ex) {
			log.eLog("数据查询异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, rs);
		}
		return tess.toArray(new ThresholdExpression[0]);
	}

	/**
	 * 通过KPI，CI,instance,view,user得到其下所有的KPI定义。
	 * 
	 * @param kpi
	 * @param ci
	 * @param instance
	 * @param view
	 * @param user
	 * @return
	 */
	public ThresholdExpression getBy(String kpi, String ci, String instance,
			String view, String user) throws Exception {
		String sql = "select id,kpi_class,kpi,ci,instance,viewid,"
				+ "userid,threshold1,threshold2,threshold3,threshold4,enabled,update_time from mon_viewkpiciuser_rel where kpi=?"
				+ " and ci=? and instance= ? and viewid=? and user=?";
		PreparedStatement st = null;
		Connection conn = null;
		ResultSet rs = null;

		try {
			conn = this.commonDS.getConnection();
			st = conn.prepareStatement(sql);

			st.setString(1, kpi);
			st.setString(2, ci);
			st.setString(3, instance);
			st.setString(4, view);
			st.setString(5, user);
			rs = st.executeQuery();

			if (rs.next()) {
				ThresholdExpression te = new ThresholdExpression();
				te.setId(rs.getString(1));
				te.setKpiClass(rs.getString(2));
				te.setKpi(rs.getString(3));
				te.setCi(rs.getString(4));
				te.setInstance(rs.getString(5));
				te.setView(rs.getString(6));
				te.setUser(rs.getString(7));
				te.setThreshold1(rs.getString(8));
				te.setThreshold2(rs.getString(9));
				te.setThreshold3(rs.getString(10));
				te.setThreshold4(rs.getString(11));
				te.setEnabled(rs.getString(12));
				te.setUpdateTime(rs.getTimestamp(13));
				return te;
			}
		} catch (SQLException ex) {
			log.eLog("数据查询异常：" + ex.getMessage(), ex);
			throw ex;
		} finally {
			DbUtils.closeQuietly(conn, st, rs);
		}
		return null;
	}
}
