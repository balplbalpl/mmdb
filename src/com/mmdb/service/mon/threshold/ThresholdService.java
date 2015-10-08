/**
 *
 */
package com.mmdb.service.mon.threshold;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import com.ccb.iomp.monitoring.eap.common.util.DBUtils;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.core.utils.UUID;
import com.mmdb.service.mon.threshold.model.PerfData;
import com.mmdb.service.mon.threshold.model.ThresholdExpression;
import com.mmdb.service.mon.threshold.model.ThresholdExpressionCache;
import com.mmdb.service.mon.threshold.model.ThresholdStatusPair;

/**
 * 计算阈值的服务类。
 *
 * @author aol_aog@163.com(James Gao)
 *
 */
//@Service("ThresholdService")
public class ThresholdService {
    private Log log = LogFactory.getLogger("ThresholdService");

    /**
     * 阈值定义的DAO。
     */
    @Resource(name = "ThresholdExpressionService")
    private ThresholdExpressionService teService;

    /**
     * 报警值与视图相关的阈值定义Map。
     */
    private Map<String, ThresholdStatusPair> viewMap;

    /**
     * 报警值与视图、用户相关的阈值定义Map。
     */
    private Map<String, ThresholdStatusPair> viewUserMap;

    /**
     * 报警值与默认的阈值定义Map。
     */
    private Map<String, ThresholdStatusPair> defaultThresholdMap;

    /**
     * 实际计算阈值的处理器。
     */
    private ThresholdHandler handler;

    /**
     * 数据源。
     */
    @Resource(name = "eventDS")
    private DataSource ds;

    /**
     * 插入到预警事件表。
     */
    private String sql = "insert into mon_proactive_event"
            + " (id,kpi,kpi_class,severity,ci,instance,viewid,userid,threshold,tally,summary,kpi_value,kpi_last_value,first_occur_time,last_occur_time)"
            + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    /**
     *
     */
    public ThresholdService() {
        // 多线程环境下，需要使用线程安全的Map.
        viewMap = Collections
                .synchronizedMap(new LinkedHashMap<String, ThresholdStatusPair>());
        viewUserMap = Collections
                .synchronizedMap(new LinkedHashMap<String, ThresholdStatusPair>());

        defaultThresholdMap = Collections
                .synchronizedMap(new LinkedHashMap<String, ThresholdStatusPair>());

        this.handler = new ThresholdHandler();
    }

    /**
     * 处理性能的数据。
     *
     * @param data
     */
    public void onData(Object[] data) {
        // log.dLog("处理性能数据：" + Arrays.asList(data) + "的阈值...");
        PerfData pd = new PerfData();
        pd.ci = (String) data[0];
        pd.instance = (String) data[1];
        pd.kpi = (String) data[2];
        pd.value = (String) data[4];
        if (data[3] == null) {
            pd.curTime = new Timestamp(System.currentTimeMillis());
        } else {
            pd.curTime = Timestamp.valueOf((String) data[3]);
        }

        if (data.length > 5) {
            if (data.length >= 6)
                pd.reservedField1 = (String) data[5];
            if (data.length >= 7)
                pd.reservedField2 = (String) data[6];
            if (data.length >= 8)
                pd.reservedField3 = (String) data[7];
            if (data.length >= 9)
                pd.reservedField4 = (String) data[8];
            if (data.length >= 10)
                pd.reservedField5 = (String) data[9];
        }
        // 处理阈值。
        try {
            this.handleThreshold(pd);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // log.dLog("处理性能数据阈值完毕。");
    }

    /**
     * 处理一条性能数据的阈值。
     *
     * @param pd
     * @param view
     * @param user
     * @throws Exception
     */
    public void handleThreshold(PerfData pd) throws Exception {

        ThresholdExpressionCache level1Cache = this.teService.getBy(pd.kpi,
                pd.ci, pd.instance);
        if (level1Cache == null) {
            System.out.println("(" + pd.ci + pd.instance + pd.kpi
                    + ")=======null");
            return;
        }
        ThresholdExpressionCache[] cache2s = level1Cache.getCaches();
        String key1 = level1Cache.getKey(); // ci+instance+kpi
        if (cache2s == null || cache2s.length == 0) {
            ThresholdExpression te1 = level1Cache.gettExp();
            // 否则检查默认阈值定义缓存。

            handleThreshold1(key1, pd, this.defaultThresholdMap, te1);
        } else {

            for (ThresholdExpressionCache cache2 : cache2s) {
                ThresholdExpressionCache[] cache3s = cache2.getCaches();
                if (cache3s == null || cache3s.length == 0) {
                    // 检查view
                    handleThreshold1(key1 + cache2.getKey(), pd, this.viewMap,
                            cache2.gettExp());
                } else {
                    for (ThresholdExpressionCache cache3 : cache3s) { // 从user级别检查。
                        ThresholdExpression te3 = cache3.gettExp();
                        if (te3 == null) {
                            continue;
                        }
                        handleThreshold1(
                                key1 + cache2.getKey() + cache3.getKey(), pd,
                                this.viewUserMap, te3);

                    }
                }
            }
        }

    }

    public PerfData[] getData(String ci, String viewID, String userID) {
        Map<String, PerfData> tmp = new LinkedHashMap<String, PerfData>();
        if (userID != null && !userID.trim().equals("")) {
            PerfData[] rv = this.getData(ci, this.viewUserMap);
            if (rv != null && rv.length != 0) {
                for (PerfData pd : rv) {
                    if (tmp.containsKey(pd.getKey()))
                        continue;
                    tmp.put(pd.getKey(), pd);
                }
            }
        }

        if (viewID != null && !viewID.trim().equals("")) {
            PerfData[] rv = this.getData(ci, this.viewMap);
            if (rv != null && rv.length != 0) {
                for (PerfData pd : rv) {
                    if (tmp.containsKey(pd.getKey()))
                        continue;
                    tmp.put(pd.getKey(), pd);
                }
            }
        }

        if (viewID != null && !viewID.trim().equals("")) {
            PerfData[] rv = this.getData(ci, this.defaultThresholdMap);
            if (rv != null && rv.length != 0) {
                for (PerfData pd : rv) {
                    if (tmp.containsKey(pd.getKey()))
                        continue;
                    tmp.put(pd.getKey(), pd);
                }
            }
        }

        return tmp.values().toArray(new PerfData[0]);

    }

    private PerfData[] getData(String ci,
                               Map<String, ThresholdStatusPair> dataCacheMap) {

        List<PerfData> tmp = new LinkedList<PerfData>();
        String[] keys = dataCacheMap.keySet().toArray(new String[0]);
        for (String key : keys) {
            if (key.startsWith(ci)) { // key中以ci开头的。
                // 交换数据.
                PerfData pd = new PerfData();
                ThresholdStatusPair tsp = dataCacheMap.get(key);
                pd.status = tsp.currentStatus;
                pd.lastStatus = tsp.lastStatus;
                pd.lastValue = tsp.lastValue;
                pd.threshold = tsp.threshold;
                pd.value = tsp.currentValue;
                pd.ci = ci;
                pd.instance = tsp.instance;
                pd.kpi = tsp.kpi;
                pd.kpiClass = tsp.kpiClass;
                pd.curTime = tsp.currentTime;
                pd.reservedField1 = tsp.reservedField1;
                pd.reservedField2 = tsp.reservedField2;
                pd.reservedField3 = tsp.reservedField3;
                pd.reservedField4 = tsp.reservedField4;
                pd.reservedField5 = tsp.reservedField5;

                tmp.add(pd);
            }
        }

        return tmp.toArray(new PerfData[0]);
    }

    /**
     * 处理用户在某个视图查看时的数据
     *
     * @param perfDatas
     * @param view
     * @param user
     */
    private void handleThreshold1(String key, PerfData pd,
                                  Map<String, ThresholdStatusPair> dataCacheMap,
                                  ThresholdExpression te) {
        ThresholdStatusPair oldTsp = dataCacheMap.get(key);
        // 如果当前数据与前一条数据时间相同，认为是同一条数据，不做执行。
        if (oldTsp != null) {
            if (pd.curTime.equals(oldTsp.currentTime)) {
                // log.dLog("当前数据在缓存中已存在");
                return;
            }
        }
        // . 如果存在阈值定义
        if (te != null) {
            // 2014-6-15,如果阈值设置为不可用，则忽略。
            int status = 6;
            if (te.getEnabled().equalsIgnoreCase("Y")) { // 只有启用的阈值才判断。
                // 正常来说此处查询出来的阈值定义只有一条。
                status = this.handler.handler(te, pd);
            }

            // 缓存
            ThresholdStatusPair tsp = new ThresholdStatusPair();
            dataCacheMap.put(key, tsp);
            tsp.key = key;
            tsp.threshold = pd.threshold;
            tsp.currentStatus = status;
            tsp.currentValue = pd.value;
            tsp.ci = pd.ci;
            tsp.instance = pd.instance;
            tsp.kpi = pd.kpi;
            tsp.kpiClass = te.getKpiClass();
            tsp.currentTime = pd.curTime;
            pd.status = status;
            pd.kpiClass = te.getKpiClass();
            tsp.reservedField1 = pd.reservedField1;
            tsp.reservedField2 = pd.reservedField2;
            tsp.reservedField3 = pd.reservedField3;
            tsp.reservedField4 = pd.reservedField4;
            tsp.reservedField5 = pd.reservedField5;
            if (oldTsp == null) {
                tsp.lastStatus = status;
                tsp.lastValue = pd.value;

            } else {
                tsp.lastStatus = oldTsp.currentStatus;
                tsp.lastValue = oldTsp.currentValue;

                if (te.getEnabled().equalsIgnoreCase("Y")) { // 只有启用的阈值产生事件。
                    // 如果两次状态有变化，产生事件。
                    if (oldTsp.currentStatus != tsp.currentStatus) {
                        // log.dLog("数据状态有变化，产生阈值事件...");

                        pd.lastValue = tsp.lastValue;
                        genEvent(pd, te.getView(), te.getUser(),
                                te.getKpiClass());
                    }
                }
            }
            try {
                insertPerfData(pd, te.getView(), te.getUser());
            } catch (Exception ex) {
                ex.printStackTrace();
                log.dLog(ex.getMessage());
            }
            // System.out.println("________________status: " +
            // tsp.currentStatus);
            return;
        }

    }

    /**
     * 产生事件。
     *
     * @param pd
     * @param view
     * @param user
     */
    private void genEvent(PerfData pd, String view, String user, String kpiClass) {
        // id,identifier,kpi,ci,instance,view,user,threshold,status,tally,summary,first_occur_time,last_occur_time
        PreparedStatement st = null;
        Connection conn = null;
        ResultSet rs = null;

        try {
            conn = this.ds.getConnection();
            st = conn.prepareStatement(sql);
            st.setString(1, UUID.getID());
            st.setString(2, pd.kpi);
            st.setString(3, kpiClass);
            st.setInt(4, pd.status);
            st.setString(5, pd.ci);
            st.setString(6, pd.instance);
            st.setString(7, view);
            st.setString(8, user);
            st.setString(9, pd.threshold);

            st.setInt(10, 1);
            String summary = pd.ci + " " + pd.instance + "的" + pd.kpi + ": "
                    + pd.value + "，阈值：" + pd.threshold;
            st.setString(11, summary);
            st.setString(12, pd.value);
            st.setString(13, pd.lastValue);
            st.setTimestamp(14, pd.curTime);
            st.setTimestamp(15, pd.curTime);
            st.executeUpdate();
            // log.dLog(summary);
        } catch (SQLException ex) {
            log.eLog("数据查询异常：" + ex.getMessage(), ex);

        } finally {
            DbUtils.closeQuietly(conn, st, rs);
        }
    }

    private void insertPerfData(PerfData pd, String view, String user)
            throws Exception {
        Connection conn = this.ds.getConnection();
        PreparedStatement ps = null;
        String sql = "insert into MON_PERF_DATA(RESOURCE_NAME,INSTANCE_NAME,METRIC_CLASS,"
                + "METRIC_NAME,USERID,VIEWID,METRIC_TIME,METRIC_STATUS,METRIC_VALUE,"
                + "RESERVED_FIELD1,RESERVED_FIELD2,RESERVED_FIELD3,RESERVED_FIELD4,RESERVED_FIELD5) "
                + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        // truncate table mon_perf_data
        try {
            ps = conn.prepareStatement(sql);

            ps.setString(1, pd.ci);
            ps.setString(2, pd.instance);
            ps.setString(3, pd.kpiClass);
            ps.setString(4, pd.kpi);
            ps.setString(5, user);
            // System.out.println(user);
            ps.setString(6, view);
            ps.setTimestamp(7, pd.curTime);
            ps.setInt(8, pd.status);
            ps.setObject(9, pd.value);
            ps.setObject(10, pd.reservedField1);
            ps.setObject(11, pd.reservedField2);
            ps.setObject(12, pd.reservedField3);
            ps.setObject(13, pd.reservedField4);
            ps.setObject(14, pd.reservedField5);

            ps.execute();
        } finally {
            DBUtils.closeAll(conn, ps, null);
        }
    }
}
