package com.mmdb.model.event.storage;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.event.Event;
import com.mmdb.model.event.EventView;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.util.HexString;

@Component("eventStorage")
public class EventStorage {
	private Log log = LogFactory.getLogger("EventStorage");
	private String driver = null;
	private String url = null;
	private String username = null;
	private String password = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Connection getConnection() {
		try {
			if (driver == null) {
				ResourceBundle init = ResourceBundle
						.getBundle("config.demo.demo-global");
				driver = init.getString("mysql.driver");
				url = init.getString("mysql.url");
				username = init.getString("mysql.username");
				password = init.getString("mysql.password");
			}

			Class.forName(driver);
			return DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}
	}

	public List<Event> getEvents(String input, List<String> ciConf,
			List<Map<String, Object>> kpiConf, List<Integer> severities,
			Integer status, Long startTime, Long endTime, Boolean isHistory,
			Map<String, String> titles, Map<String, String> varcharTitles,
			Map<String, Map<String, String>> severityMap,
			Map<String, String> statusMap, JSONObject order, String userName)
			throws Exception {
		List<Event> ret = new ArrayList<Event>();
		Connection conn = getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder("select * from "
					+ (isHistory ? "MON_EAP_EVENT" : "MON_EAP_EVENT_MEMORY")
					+ " where SERIAL=DUPLICATESERIAL");
			List<Object> valList = new ArrayList<Object>();
			sql.append(" and (subuser=? or subuser is null)");
			valList.add(userName);
			if (severities != null && severities.size() > 0) {
				sql.append(" and severity in (");
				for (Integer severity : severities) {
					sql.append("?,");
					valList.add(severity);
				}
				sql = sql.delete(sql.length() - 1, sql.length());
				sql.append(")");
			}
			if (status != null) {
				sql.append(" and status=?");
				valList.add(status);
			}
			if (startTime != null) {
				sql.append(" and firstoccurrence>=?");
				valList.add(startTime);
			}
			if (endTime != null) {
				sql.append(" and firstoccurrence<=?");
				valList.add(endTime);
			}
			if (input != null) {
				sql.append(" and (");
				Set<String> varcharTitleSet = varcharTitles.keySet();
				for (String title : varcharTitleSet) {
					sql.append(varcharTitles.get(title) + " like ? or ");
					valList.add(input);
				}
				sql.delete(sql.length() - 3, sql.length());
				sql.append(")");
			}
			if (ciConf != null && ciConf.size() > 0) {
				sql.append(" and cicategory in (");
				for (String ciCate : ciConf) {
					sql.append("?,");
					valList.add(ciCate);
				}
				sql = sql.delete(sql.length() - 1, sql.length());
				sql.append(")");
			}
			if (kpiConf != null && kpiConf.size() > 0) {
				sql.append(" and (");
				for (Map<String, Object> kpiCate : kpiConf) {
					List<String> kpiList = kpiCate.containsKey("kpis") ? (List<String>) kpiCate
							.get("kpis") : null;
					if (kpiList != null && kpiList.size() > 0) {
						sql.append(" kpiid in (");
						for (String kpiid : kpiList) {
							sql.append("?,");
							valList.add(kpiid);
						}
						sql = sql.delete(sql.length() - 1, sql.length());
						sql.append(") or");
					} else {
						sql.append(" kpicategory=? or");
						valList.add(kpiCate.get("id"));
					}
				}
				sql = sql.delete(sql.length() - 3, sql.length());
				sql.append(")");
			}
			if (order != null) {
				sql.append(" order by "
						+ order.getString("column")
						+ (order.containsKey("desc") ? (order
								.getBoolean("desc") ? " desc" : "") : ""));
			} else {
				sql.append(" order by firstoccurrence desc");
			}
			sql.append(" limit 1000");

			pst = conn.prepareStatement(sql.toString());
			int i = 1;
			for (Object val : valList) {
				if (val instanceof String) {
					pst.setString(i, (String) val);
				} else if (val instanceof Integer) {
					pst.setInt(i, ((Integer) val).intValue());
				} else {
					pst.setTimestamp(i, new Timestamp((Long) val));
				}
				i++;
			}

			rs = pst.executeQuery();
			if (rs != null) {
				Set<String> keys = titles.keySet();
				while (rs.next()) {
					JSONArray ciHex = null;
					String ciHexId = rs.getString("cihex");
					if (ciHexId != null && ciHexId.length() > 0) {
						ciHex = JSONArray.fromObject(HexString.decode(ciHexId));
					}
					JSONArray kpiHex = null;
					String kpiHexId = rs.getString("kpihex");
					if (kpiHexId != null && kpiHexId.length() > 0) {
						kpiHex = JSONArray.fromObject(HexString
								.decode(kpiHexId));
					}
					Event event = new Event();
					event.setSerial(rs.getString("serial"));
					event.setDuplicateSerial(rs.getString("duplicateserial"));
					event.setCiCategory(rs.getString("cicategory"));
					event.setCiid(rs.getString("ciid"));
					event.setCiHex(ciHexId);
					event.setKpiCategory(rs.getString("kpicategory"));
					event.setKpi(rs.getString("kpiid"));
					event.setKpiInstance(rs.getString("kpiinstance"));
					event.setKpiHex(kpiHexId);
					event.setEventTitle(rs.getString("eventtitle"));
					event.setSummary(rs.getString("summary"));
					event.setStatus(rs.getInt("status") + "");
					event.setSeverity(rs.getInt("severity") + "");
					event.setCloseInfo(rs.getString("closeinfo"));
					event.setAckInfo(rs.getString("ackInfo"));
					event.setFirstOccurrence(rs.getTimestamp("firstoccurrence")
							.getTime());
					event.setLastOccurrence(rs.getTimestamp("lastoccurrence")
							.getTime());
					event.setCloseTime(rs.getDate("closetime") == null ? null
							: rs.getDate("closetime").getTime());
					event.setAckTime(rs.getDate("acktime") == null ? null : rs
							.getDate("acktime").getTime());
					event.setCloseUid(rs.getString("closeuid"));
					event.setAckUid(rs.getString("ackuid"));
					event.setTally(new Long(rs.getInt("tally")));
					event.setViewId(rs.getString("viewid"));
					event.setUserName(rs.getString("subuser"));
					Map<String, String> data = new HashMap<String, String>();
					for (String key : keys) {
						String column = titles.get(key);
						Object val = rs.getObject(column);
						if (val == null) {
							data.put(key, "");
						} else {
							if (val instanceof String) {
								if ("ciid".equals(column.toLowerCase())) {
									data.put(
											key,
											ciHex == null ? "" : ciHex
													.getString(1));
								} else if ("cicategory".equals(column
										.toLowerCase())) {
									data.put(
											key,
											ciHex == null ? "" : ciHex
													.getString(0));
								} else if ("kpiid".equals(column.toLowerCase())) {
									data.put(
											key,
											kpiHex == null ? "" : kpiHex
													.getString(1));
								} else if ("kpicategory".equals(column
										.toLowerCase())) {
									data.put(
											key,
											kpiHex == null ? "" : kpiHex
													.getString(0));
								} else {
									data.put(key, (String) val);
								}
							} else if (val instanceof Timestamp) {
								data.put(key, sdf.format((Timestamp) val));
							} else if (val instanceof Date) {
								data.put(key, sdf.format((Date) val));
							} else if (val instanceof Integer) {
								if ("severity".equals(column.toLowerCase())) {
									data.put(key, severityMap.get(val + "")
											.get("name"));
								} else if ("status"
										.equals(column.toLowerCase())) {
									data.put(key, statusMap.get(val + ""));
								} else {
									data.put(key, val + "");
								}
							} else {
								data.put(key, val + "");
							}
						}
					}
					event.setData(data);
					ret.add(event);
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (rs != null) {
				rs.close();
			}
			rs = null;
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}

	public Map<String, Long> getEventSeverity(EventView view,
			Boolean isHistory, String userName) throws Exception {
		Map<String, Long> ret = new HashMap<String, Long>();
		Connection conn = getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder(
					"select severity,count(severity) c from "
							+ (isHistory ? "MON_EAP_EVENT"
									: "MON_EAP_EVENT_MEMORY")
							+ " where SERIAL=DUPLICATESERIAL");
			List<Object> valList = new ArrayList<Object>();
			sql.append(" and (subuser=? or subuser is null)");
			valList.add(userName);
			if (view != null) {
				if (view.getSeverities() != null
						&& view.getSeverities().size() > 0) {
					sql.append(" and severity in (");
					for (String severity : view.getSeverities()) {
						sql.append("?,");
						valList.add(Integer.parseInt(severity));
					}
					sql.delete(sql.length() - 1, sql.length());
					sql.append(")");
				}
				if (view.getCiConf() != null && view.getCiConf().size() > 0) {
					sql.append(" and cicategory in (");
					for (String ciCate : view.getCiConf()) {
						sql.append("?,");
						valList.add(ciCate);
					}
					sql = sql.delete(sql.length() - 1, sql.length());
					sql.append(")");
				}
				if (view.getKpiConf() != null && view.getKpiConf().size() > 0) {
					sql.append(" and (");
					for (Map<String, Object> kpiCate : view.getKpiConf()) {
						List<String> kpiList = kpiCate.containsKey("kpis") ? (List<String>) kpiCate
								.get("kpis") : null;
						if (kpiList != null && kpiList.size() > 0) {
							sql.append(" kpiid in (");
							for (String kpiid : kpiList) {
								sql.append("?,");
								valList.add(kpiid);
							}
							sql = sql.delete(sql.length() - 1, sql.length());
							sql.append(") or");
						} else {
							sql.append(" kpicategory=? or");
							valList.add(kpiCate.get("id"));
						}
					}
					sql = sql.delete(sql.length() - 3, sql.length());
					sql.append(")");
				}
				if (view.getLastTime() != null && view.getLastTime() > 0) {
					sql.append(" and firstoccurrence>=?");
					sql.append(" and firstoccurrence<=?");
					Long endTime = System.currentTimeMillis();
					Long startTime = endTime - view.getLastTime();
					valList.add(new Timestamp(startTime));
					valList.add(new Timestamp(endTime));
				}
			}
			sql.append(" group by severity");
			pst = conn.prepareStatement(sql.toString());
			int i = 1;
			for (Object val : valList) {
				if (val instanceof String) {
					pst.setString(i, (String) val);
				} else if (val instanceof Integer) {
					pst.setInt(i, (Integer) val);
				} else {
					pst.setTimestamp(i, (Timestamp) val);
				}
				i++;
			}
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					ret.put(rs.getInt("severity") + "", rs.getLong("c"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (rs != null) {
				rs.close();
			}
			rs = null;
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
		return ret;
	}

	public Event getEventBySerial(String serial, Map<String, String> titles,
			Map<String, Map<String, String>> severityMap,
			Map<String, String> statusMap) throws Exception {
		Connection conn = getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder(
					"select * from MON_EAP_EVENT where SERIAL=?");
			pst = conn.prepareStatement(sql.toString());
			pst.setString(1, serial);
			rs = pst.executeQuery();
			if (rs != null) {
				Set<String> keys = titles.keySet();
				if (rs.next()) {
					JSONArray ciHex = null;
					String ciHexId = rs.getString("cihex");
					if (ciHexId != null && ciHexId.length() > 0) {
						ciHex = JSONArray.fromObject(HexString.decode(ciHexId));
					}
					JSONArray kpiHex = null;
					String kpiHexId = rs.getString("kpihex");
					if (kpiHexId != null && kpiHexId.length() > 0) {
						kpiHex = JSONArray.fromObject(HexString
								.decode(kpiHexId));
					}
					Event event = new Event();
					event.setSerial(rs.getString("serial"));
					event.setDuplicateSerial(rs.getString("duplicateserial"));
					event.setCiCategory(rs.getString("cicategory"));
					event.setCiid(rs.getString("ciid"));
					event.setCiHex(ciHexId);
					event.setKpiCategory(rs.getString("kpicategory"));
					event.setKpi(rs.getString("kpiid"));
					event.setKpiInstance(rs.getString("kpiinstance"));
					event.setKpiHex(kpiHexId);
					event.setEventTitle(rs.getString("eventtitle"));
					event.setSummary(rs.getString("summary"));
					event.setStatus(rs.getInt("status") + "");
					event.setSeverity(rs.getInt("severity") + "");
					event.setCloseInfo(rs.getString("closeinfo"));
					event.setAckInfo(rs.getString("ackInfo"));
					event.setFirstOccurrence(rs.getTimestamp("firstoccurrence")
							.getTime());
					event.setLastOccurrence(rs.getTimestamp("lastoccurrence")
							.getTime());
					event.setCloseTime(rs.getDate("closetime") == null ? null
							: rs.getDate("closetime").getTime());
					event.setAckTime(rs.getDate("acktime") == null ? null : rs
							.getDate("acktime").getTime());
					event.setCloseUid(rs.getString("closeuid"));
					event.setAckUid(rs.getString("ackuid"));
					event.setTally(new Long(rs.getInt("tally")));
					event.setViewId(rs.getString("viewid"));
					event.setUserName(rs.getString("subuser"));
					Map<String, String> data = new HashMap<String, String>();
					for (String key : keys) {
						String column = titles.get(key);
						Object val = rs.getObject(column);
						if (val == null) {
							data.put(key, "");
						} else {
							if (val instanceof String) {
								if ("ciid".equals(column.toLowerCase())) {
									data.put(
											key,
											ciHex == null ? "" : ciHex
													.getString(1));
								} else if ("cicategory".equals(column
										.toLowerCase())) {
									data.put(
											key,
											ciHex == null ? "" : ciHex
													.getString(0));
								} else if ("kpiid".equals(column.toLowerCase())) {
									data.put(
											key,
											kpiHex == null ? "" : kpiHex
													.getString(1));
								} else if ("kpicategory".equals(column
										.toLowerCase())) {
									data.put(
											key,
											kpiHex == null ? "" : kpiHex
													.getString(0));
								} else {
									data.put(key, (String) val);
								}
							} else if (val instanceof Timestamp) {
								data.put(key, sdf.format((Timestamp) val));
							} else if (val instanceof Date) {
								data.put(key, sdf.format((Date) val));
							} else if (val instanceof Integer) {
								if ("severity".equals(column.toLowerCase())) {
									data.put(key, severityMap.get(val + "")
											.get("name"));
								} else if ("status"
										.equals(column.toLowerCase())) {
									data.put(key, statusMap.get(val + ""));
								} else {
									data.put(key, val + "");
								}
							} else {
								data.put(key, val + "");
							}
						}
					}
					event.setData(data);
					return event;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (rs != null) {
				rs.close();
			}
			rs = null;
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}

	public List<Event> getEventByDuplicateSerial(String serial)
			throws Exception {
		List<Event> ret = new ArrayList<Event>();
		Connection conn = getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder(
					"select * from MON_EAP_EVENT where DUPLICATESERIAL=? order by FIRSTOCCURRENCE");
			pst = conn.prepareStatement(sql.toString());
			pst.setString(1, serial);
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					Event event = new Event();
					event.setSerial(rs.getString("serial"));
					event.setDuplicateSerial(rs.getString("duplicateserial"));
					event.setCiCategory(rs.getString("cicategory"));
					event.setCiid(rs.getString("ciid"));
					event.setKpiCategory(rs.getString("kpicategory"));
					event.setKpi(rs.getString("kpiid"));
					event.setKpiInstance(rs.getString("kpiinstance"));
					event.setEventTitle(rs.getString("eventtitle"));
					event.setSummary(rs.getString("summary"));
					event.setStatus(rs.getInt("status") + "");
					event.setSeverity(rs.getInt("severity") + "");
					event.setCloseInfo(rs.getString("closeinfo"));
					event.setAckInfo(rs.getString("ackInfo"));
					event.setFirstOccurrence(rs.getTimestamp("firstoccurrence")
							.getTime());
					event.setLastOccurrence(rs.getTimestamp("lastoccurrence")
							.getTime());
					event.setCloseTime(rs.getDate("closetime") == null ? null
							: rs.getDate("closetime").getTime());
					event.setAckTime(rs.getDate("acktime") == null ? null : rs
							.getDate("acktime").getTime());
					event.setCloseUid(rs.getString("closeuid"));
					event.setAckUid(rs.getString("ackuid"));
					event.setTally(new Long(rs.getInt("tally")));
					event.setViewId(rs.getString("viewid"));
					event.setUserName(rs.getString("subuser"));
					ret.add(event);
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (rs != null) {
				rs.close();
			}
			rs = null;
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}

	public List<Event> getEventForView(List<String> ciHexes, String viewId,
			String userName, Long startTime, Long endTime) throws Exception {
		System.out.println(ciHexes + "----" + startTime + "----" + endTime);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<Event> ret = new ArrayList<Event>();
		Connection conn = getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder(
					"select * from mon_eap_event where firstoccurrence <= ? and (closetime is null or closetime >= ?) and (viewid=? or viewid is null) and (subuser=? or subuser is null) and serial=duplicateserial and cihex in (");
			List<String> valList = new ArrayList<String>();
			if (ciHexes == null || ciHexes.size() == 0) {
				return ret;
			}
			for (String ciHex : ciHexes) {
				sql.append("?,");
				valList.add(ciHex);
			}
			sql.delete(sql.length() - 1, sql.length());
			sql.append(")");
			pst = conn.prepareStatement(sql.toString());
			pst.setTimestamp(1, new Timestamp(endTime));
			System.out.println(new Timestamp(endTime));
			pst.setTimestamp(2, new Timestamp(startTime));
			System.out.println(new Timestamp(startTime));
			pst.setString(3, viewId);
			System.out.println(viewId);
			pst.setString(4, userName);
			System.out.println(userName);
			int i = 5;
			for (String val : valList) {
				pst.setString(i, val);
				i++;
			}

			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					Event event = new Event();
					event.setSerial(rs.getString("serial"));
					event.setDuplicateSerial(rs.getString("duplicateserial"));
					event.setCiCategory(rs.getString("cicategory"));
					event.setCiid(rs.getString("ciid"));
					event.setCiHex(rs.getString("cihex"));
					event.setKpiCategory(rs.getString("kpicategory"));
					event.setKpi(rs.getString("kpiid"));
					event.setKpiHex(rs.getString("kpihex"));
					event.setKpiInstance(rs.getString("kpiinstance"));
					event.setEventTitle(rs.getString("eventtitle"));
					event.setSummary(rs.getString("summary"));
					event.setStatus(rs.getInt("status") + "");
					event.setSeverity(rs.getInt("severity") + "");
					event.setCloseInfo(rs.getString("closeinfo"));
					event.setAckInfo(rs.getString("ackInfo"));
					event.setFirstOccurrence(rs.getTimestamp("firstoccurrence")
							.getTime());
					event.setLastOccurrence(rs.getTimestamp("lastoccurrence")
							.getTime());
					event.setCloseTime(rs.getTimestamp("closetime") == null ? null
							: rs.getTimestamp("closetime").getTime());
					event.setAckTime(rs.getDate("acktime") == null ? null : rs
							.getDate("acktime").getTime());
					event.setCloseUid(rs.getString("closeuid"));
					event.setAckUid(rs.getString("ackuid"));
					event.setTally(new Long(rs.getInt("tally")));
					event.setViewId(rs.getString("viewid"));
					event.setUserName(rs.getString("subuser"));
					ret.add(event);
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (rs != null) {
				rs.close();
			}
			rs = null;
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}

	public int getCountEventForView(String ciHex, String kpiHex, String viewId,
			String userName, Long startTime, Long endTime) {
		Connection conn = getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder(
					"select count(`kpihex`) from mon_eap_event where firstoccurrence <= ? and (closetime is null or closetime >= ?) and (viewid=? or viewid is null) "
							+ "and (subuser=? or subuser is null) and serial=duplicateserial and cihex = ? and kpihex = ? ");
			pst = conn.prepareStatement(sql.toString());
			pst.setTimestamp(1, new Timestamp(endTime));
			pst.setTimestamp(2, new Timestamp(startTime));
			pst.setString(3, viewId);
			pst.setString(4, userName);
			pst.setString(5, ciHex);
			pst.setString(6, kpiHex);
			rs = pst.executeQuery();
			while (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			return 0;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				rs = null;
				if (pst != null) {
					try {
						pst.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				pst = null;
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				conn = null;
			}
		}
		return 0;
	}

	public List<Event> getEventForView(String ciHex, String kpiHex,
			String viewId, String userName, Long startTime, Long endTime,
			int start, int limit) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<Event> ret = new ArrayList<Event>();
		Connection conn = getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder(
					"select * from mon_eap_event where firstoccurrence <= ? and (closetime is null or closetime >= ?) and (viewid=? or viewid is null) "
							+ "and (subuser=? or subuser is null) and serial=duplicateserial and cihex = ? and kpihex = ? "
							+ "ORDER BY cihex,kpihex,severity DESC LIMIT ?,?");
			pst = conn.prepareStatement(sql.toString());
			pst.setTimestamp(1, new Timestamp(endTime));
			pst.setTimestamp(2, new Timestamp(startTime));
			pst.setString(3, viewId);
			pst.setString(4, userName);
			pst.setString(5, ciHex);
			pst.setString(6, kpiHex);
			pst.setInt(7, start);
			pst.setInt(8, limit);

			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					Event event = new Event();
					event.setSerial(rs.getString("serial"));
					event.setDuplicateSerial(rs.getString("duplicateserial"));
					event.setCiCategory(rs.getString("cicategory"));
					event.setCiid(rs.getString("ciid"));
					event.setCiHex(rs.getString("cihex"));
					event.setKpiCategory(rs.getString("kpicategory"));
					event.setKpi(rs.getString("kpiid"));
					event.setKpiHex(rs.getString("kpihex"));
					event.setKpiInstance(rs.getString("kpiinstance"));
					event.setEventTitle(rs.getString("eventtitle"));
					event.setSummary(rs.getString("summary"));
					event.setStatus(rs.getInt("status") + "");
					event.setSeverity(rs.getInt("severity") + "");
					event.setCloseInfo(rs.getString("closeinfo"));
					event.setAckInfo(rs.getString("ackInfo"));
					event.setFirstOccurrence(rs.getTimestamp("firstoccurrence")
							.getTime());
					event.setLastOccurrence(rs.getTimestamp("lastoccurrence")
							.getTime());
					event.setCloseTime(rs.getTimestamp("closetime") == null ? null
							: rs.getTimestamp("closetime").getTime());
					event.setAckTime(rs.getDate("acktime") == null ? null : rs
							.getDate("acktime").getTime());
					event.setCloseUid(rs.getString("closeuid"));
					event.setAckUid(rs.getString("ackuid"));
					event.setTally(new Long(rs.getInt("tally")));
					event.setViewId(rs.getString("viewid"));
					event.setUserName(rs.getString("subuser"));
					ret.add(event);
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (rs != null) {
				rs.close();
			}
			rs = null;
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}
	/**
	 * 获取指定视图.作者.ci.kpi上事件中最严重的事件级别
	 * @param viewId
	 * @param cikpi
	 * @param userName
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws Exception
	 */
	public Map<String, Map<String, String>> getMaxSeverityByCiKPi(
			String viewId, List<Map<String, Object>> cikpi, String userName,
			long startTime, long endTime) throws Exception {

		Map<String, Map<String, String>> ret = new HashMap<String, Map<String, String>>();
		if (cikpi == null || cikpi.size() == 0)
			return ret;

		Connection conn = getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder(
					"select `CIHEX` as `ciHex`,`KPIHEX` as `kpiHex`,max(SEVERITY) as `serverity` from mon_eap_event where firstoccurrence <= ? and (closetime is null or closetime >= ?) and (viewid=? or viewid is null) "
							+ "and (subuser=? or subuser is null) and serial=duplicateserial and (");
			for (Map<String, Object> map : cikpi) {
				String ciHex =(String) map.get("ciId");
				String kpiHex =(String) map.get("kpiHex");
				sql.append("(`CIHEX` = '").append(ciHex)
						.append("' and `KPIHEX` = '").append(kpiHex).append("') or");
			}
			sql.delete(sql.length()-2, sql.length());
			sql.append(") group by subuser,viewid,ciHex,kpiHex");
			
			pst = conn.prepareStatement(sql.toString());
			pst.setTimestamp(1, new Timestamp(endTime));
			System.out.println(new Timestamp(endTime));
			pst.setTimestamp(2, new Timestamp(startTime));
			System.out.println(new Timestamp(startTime));
			pst.setString(3, viewId);
			pst.setString(4, userName);

			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String ci = rs.getString("ciHex");
					String kpi = rs.getString("kpiHex");
					String serverity = rs.getString("serverity");
					Map<String, String> map = ret.get(ci);
					if (map == null) {
						Map<String, String> kpiMap = new HashMap<String, String>();
						kpiMap.put(kpi, serverity);
						ret.put(ci, kpiMap);
					} else {
						map.put(kpi, serverity);
					}
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (rs != null) {
				rs.close();
			}
			rs = null;
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}

	public List<Map<String, String>> getSeverityMap() throws Exception {
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		Connection conn = getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sql = "select CHINESE_NAME,SEVERITY,COLOR,LINECOLOR from MON_SYS_SEVERITY order by SEVERITY";
			pst = conn.prepareStatement(sql);
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					Map<String, String> m = new HashMap<String, String>();
					m.put("id", rs.getString(2));
					m.put("name", rs.getString(1));
					m.put("color", rs.getString(3));
					m.put("lineColor", rs.getString(4));
					ret.add(m);
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (rs != null) {
				rs.close();
			}
			rs = null;
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}

	public List<Map<String, String>> getStatusMap() throws Exception {
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		Connection conn = getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sql = "select a.CHINESE_NAME,a.CODE from MON_SYS_DICT a,MON_SYS_DICT_GROUP b where a.GROUP_ID=b.ID and b.GROUP_NAME='事件状态' order by a.CODE";
			pst = conn.prepareStatement(sql);
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					Map<String, String> m = new HashMap<String, String>();
					m.put("id", rs.getString(2));
					m.put("name", rs.getString(1));
					ret.add(m);
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (rs != null) {
				rs.close();
			}
			rs = null;
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}

	public Map<String, String> getTitleMap(String type, String dataType)
			throws Exception {
		Map<String, String> ret = new HashMap<String, String>();
		FileInputStream fis = null;
		try {
			SAXBuilder builder = new SAXBuilder(
					"org.apache.xerces.parsers.SAXParser");
			File file = new File(Tool.getRealPath()
					+ "WEB-INF/classes/config/event/event_model.xml");
			fis = new FileInputStream(file);
			Document doc = builder.build(fis);
			Element root = doc.getRootElement();

			String version = root.getAttributeValue("version");
			if (version.equals("1.0")) {
				List attributes = root.getChildren("field");
				for (Object obj : attributes) {
					Element attrEle = (Element) obj;
					if (type == null) {
						if (dataType == null) {
							ret.put(attrEle.getChildTextTrim("title"),
									attrEle.getChildTextTrim("field-name"));
						} else {
							if (attrEle.getChildTextTrim("data-type")
									.toLowerCase()
									.contains(dataType.toLowerCase())) {
								ret.put(attrEle.getChildTextTrim("title"),
										attrEle.getChildTextTrim("field-name"));
							}
						}
					} else {
						if (attrEle.getChildTextTrim("source-type")
								.equals(type)) {
							if (dataType == null) {
								ret.put(attrEle.getChildTextTrim("title"),
										attrEle.getChildTextTrim("field-name"));
							} else {
								if (attrEle.getChildTextTrim("data-type")
										.toLowerCase()
										.contains(dataType.toLowerCase())) {
									ret.put(attrEle.getChildTextTrim("title"),
											attrEle.getChildTextTrim("field-name"));
								}
							}
						}
					}
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw e;
		} finally {
			if (fis != null) {
				fis.close();
			}
			fis = null;
		}
	}

	public void ackEvent(String serial, String ackInfo, String ackUid,
			Long ackTime) throws Exception {
		Connection conn = getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "update MON_EAP_EVENT set ACKINFO=?,ACKUID=?,ACKTIME=? where DUPLICATESERIAL=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, ackInfo);
			pst.setString(2, ackUid);
			pst.setDate(3, new Date(ackTime));
			pst.setString(4, serial);
			pst.execute();
			sql = "update MON_EAP_EVENT_MEMORY set ACKINFO=?,ACKUID=?,ACKTIME=? where DUPLICATESERIAL=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, ackInfo);
			pst.setString(2, ackUid);
			pst.setDate(3, new Date(ackTime));
			pst.setString(4, serial);
			pst.execute();
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}

	public void closeEvent(String serial, String closeInfo, String closeUid,
			Long closeTime, Integer status) throws Exception {
		Connection conn = getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "update MON_EAP_EVENT set CLOSEINFO=?,CLOSEUID=?,CLOSETIME=?,STATUS=? where DUPLICATESERIAL=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, closeInfo);
			pst.setString(2, closeUid);
			pst.setDate(3, new Date(closeTime));
			pst.setInt(4, status);
			pst.setString(5, serial);
			pst.execute();
			sql = "delete from MON_EAP_EVENT_MEMORY where SERIAL=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, serial);
			pst.execute();
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}

	public void removeAllEvent() throws Exception {
		Connection conn = getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from MON_EAP_EVENT";
			pst = conn.prepareStatement(sql);
			pst.execute();
			sql = "delete from MON_EAP_EVENT_MEMORY";
			pst = conn.prepareStatement(sql);
			pst.execute();
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw new Exception("数据库异常");
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}
}
