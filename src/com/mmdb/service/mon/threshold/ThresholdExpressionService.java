/**
 *
 */
package com.mmdb.service.mon.threshold;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.axis.utils.StringUtils;
import com.mmdb.core.utils.UUID;
import com.mmdb.service.mon.threshold.dao.ThresholdExpressionDao;
import com.mmdb.service.mon.threshold.model.ThresholdExpression;
import com.mmdb.service.mon.threshold.model.ThresholdExpressionCache;

/**
 * 阈值定义服务类，用于阈值增删改查。
 *
 * @author aol_aog@163.com(James Gao)
 *
 */
//@Service("ThresholdExpressionService")
public class ThresholdExpressionService {

    @Resource(name = "ThresholdExpressionDao")
    private ThresholdExpressionDao teDao;

    private Map<String, ThresholdExpressionCache> key2ThresholdMap;

    private Map<String, ThresholdExpression> id2ThresholdMap;

    /**
     *
     */
    public ThresholdExpressionService() {
        key2ThresholdMap = Collections
                .synchronizedMap(new LinkedHashMap<String, ThresholdExpressionCache>());
        id2ThresholdMap = Collections
                .synchronizedMap(new LinkedHashMap<String, ThresholdExpression>());
    }

    public void startService() {
        // 加载阈值规则。
        try {
            ThresholdExpression[] tes = this.teDao.getAll();

            // 放入缓存，分别存储。
            put2Cache(tes);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void put2Cache(ThresholdExpression[] tes) {
        // 放入缓存，分别存储。
        for (ThresholdExpression te : tes) {
            id2ThresholdMap.put(te.getId(), te);
            String key1 = te.getCi() + te.getInstance() + te.getKpi();
            ThresholdExpressionCache level1Cache = this.key2ThresholdMap
                    .get(key1);
            if (level1Cache == null) {
                level1Cache = new ThresholdExpressionCache(key1);
                this.key2ThresholdMap.put(key1, level1Cache);
                // 没有view,user,则默认。
                if (StringUtils.isEmpty(te.getView())
                        && StringUtils.isEmpty(te.getUser())) {
                    level1Cache.settExp(te);
                    continue;
                }
            }

            if (!StringUtils.isEmpty(te.getView())) {
                String key2 = te.getCi() + te.getInstance() + te.getKpi()
                        + te.getView();
                ThresholdExpressionCache level2Cache = level1Cache
                        .getCache(key2);
                if (level2Cache == null) {
                    level2Cache = new ThresholdExpressionCache(key2);
                    level1Cache.setCache(level2Cache);
                }
                // 没有user
                if (StringUtils.isEmpty(te.getUser())) {
                    level2Cache.settExp(te);
                    continue;
                } else {
                    String key3 = te.getCi() + te.getInstance() + te.getKpi()
                            + te.getView() + te.getUser();
                    ThresholdExpressionCache level3Cache = level2Cache
                            .getCache(key3);
                    if (level3Cache == null) {
                        level3Cache = new ThresholdExpressionCache(key3);
                        level2Cache.setCache(level3Cache);
                    }

                    level3Cache.settExp(te);
                }
            } else {
                level1Cache.settExp(te);
            }

        }
    }

    public void stopService() {
        // 清理缓存。
        this.id2ThresholdMap.clear();
        this.key2ThresholdMap.clear();
    }

    /**
     * 保存一组阈值定义，需要检查其中是否已经在系统中存，如果已经存在则更新，否则添加一条新记录。
     *
     * @param tes
     */
    public void save(ThresholdExpression[] tess) throws Exception {
        List<ThresholdExpression> addList = new LinkedList<ThresholdExpression>();
        List<ThresholdExpression> updateList = new LinkedList<ThresholdExpression>();
        // 先将添加和更新的分开。
        for (ThresholdExpression te : tess) {
            if (te.getId() != null && !"".equalsIgnoreCase(te.getId().trim())) {
                updateList.add(te);
                continue;
            }
            // 取出所有kpi与ci相同的
            ThresholdExpression[] tts = this.teDao.getBy(te.getKpi(),
                    te.getCi());
            ThresholdExpression te1 = null;
            boolean isUpdated = false;
            for (ThresholdExpression tmp : tts) {
                // 检查instance, view, user
                if (te.getUser() != null && te.getView() != null)
                    if (te.getInstance().equalsIgnoreCase(tmp.getInstance())
                            && te.getView().equalsIgnoreCase(tmp.getView())
                            && te.getUser().equalsIgnoreCase(tmp.getUser())) {
                        isUpdated = true;
                        te1 = tmp;
                        break;
                    }
                // 检查instance, view
                if (te.getView() != null && !te.getView().trim().equals(""))
                    if (te.getInstance().equalsIgnoreCase(tmp.getInstance())
                            && te.getView().equalsIgnoreCase(tmp.getView())) {
                        isUpdated = true;
                        te1 = tmp;
                        break;
                    }
                if (te.getInstance() != null
                        && !te.getInstance().trim().equals("")) {
                    // 检查instance
                    if (te.getInstance().equalsIgnoreCase(tmp.getInstance())) {
                        isUpdated = true;
                        te1 = tmp;
                        break;
                    }
                }

                // 只有kpi与ci对应
                if (te.getCi() != null && !te.getCi().trim().equals(""))
                    if (te.getCi().equals(tmp.getCi())
                            && te.getKpi().equalsIgnoreCase(tmp.getKpi())) {
                        isUpdated = true;
                        te1 = tmp;
                        break;
                    }
                // 只有kpi
                if (te.getKpi().equalsIgnoreCase(tmp.getKpi())) {
                    isUpdated = true;
                    te1 = tmp;
                    break;
                }
            }

            // 如果已存在此阈值定义，则更新。
            if (isUpdated) {
                te1.setThreshold1(te.getThreshold1());
                te1.setThreshold2(te.getThreshold2());
                te1.setThreshold3(te.getThreshold3());
                te1.setThreshold4(te.getThreshold4());
                te1.setEnabled(te.getEnabled());
                updateList.add(te1);
            } else {
                te.setId(UUID.getID());
                addList.add(te);
            }
        }

        // 添加
        if (addList.size() > 0) {
            this.teDao.add(addList.toArray(new ThresholdExpression[0]));

        }

        // 更新。
        if (updateList.size() > 0) {
            this.teDao.update(updateList.toArray(new ThresholdExpression[0]));
        }

        this.put2Cache(addList.toArray(new ThresholdExpression[0]));
        this.put2Cache(updateList.toArray(new ThresholdExpression[0]));
    }

    public void sync(ThresholdExpression[] tts) {

        List<String> tmp = new LinkedList<String>();
        try {
            ThresholdExpression[] tes = this.teDao.getAll();
            for (ThresholdExpression te : tes) {

                //找到user,view为空的
                if ((te.getView() == null || te.getView().trim().equals(""))
                        && (te.getUser() == null || te.getUser().trim()
                        .equals(""))) {
                    tmp.add(te.getId());
                }

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            // 先删除user,view为空.
            this.teDao.delete(tmp.toArray(new String[0]));
            for (ThresholdExpression te : tts) {
                te.setId(UUID.getID());
            }
            this.teDao.add(tts);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 删除一组阈值定义。
     *
     * @param ids
     */
    public void delete(String ids[]) throws Exception {
        this.teDao.delete(ids);
        for (String id : ids) {
            this.id2ThresholdMap.remove(id);
        }
    }

    /**
     * 通过ID得到阈值定义。
     *
     * @param id
     * @return
     */
    public ThresholdExpression getByID(String id) throws Exception {
        if (!this.id2ThresholdMap.containsKey(id)) {
            ThresholdExpression te = this.teDao.getByID(id);
            if (te == null)
                return null;
            this.id2ThresholdMap.put(id, te);
        }

        return this.id2ThresholdMap.get(id);
    }

    /**
     * 通过CI得到其下所有的KPI定义。
     *
     * @param ci
     * @return
     */
    public ThresholdExpression[] getByCi(String ci) throws Exception {
        return this.teDao.getByCi(ci);
    }

    /**
     * 通过KPI得到其下所有的KPI定义。
     *
     * @param kpi
     * @return
     */
    public ThresholdExpression[] getByKpi(String kpi) throws Exception {
        return this.teDao.getByKpi(kpi);
    }

    /**
     * 通过VIEW得到其下所有的KPI定义。
     *
     * @param ci
     * @return
     */
    public ThresholdExpression[] getByView(String view) throws Exception {
        return this.teDao.getByView(view);
    }

    /**
     * 通过KPI，CI得到其下所有的KPI定义。
     *
     * @param kpi
     * @param ci
     * @return
     */
    public ThresholdExpression[] getBy(String kpi, String ci) throws Exception {
        return this.teDao.getBy(kpi, ci);
    }

    /**
     * 通过KPI，CI，Instance得到其下所有的KPI定义。
     *
     * @param kpi
     * @param ci
     * @param instance
     * @return
     */
    public ThresholdExpressionCache getBy(String kpi, String ci, String instance)
            throws Exception {
        String key = ci + instance + kpi;
        if (!this.key2ThresholdMap.containsKey(key)) {
            ThresholdExpression[] tmp = this.teDao.getBy(kpi, ci, instance);

            this.put2Cache(tmp);
        }

        return this.key2ThresholdMap.get(key);

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
    public ThresholdExpressionCache getBy(String kpi, String ci,
                                          String instance, String view) throws Exception {

        String key1 = ci + instance + kpi;
        String key2 = ci + instance + kpi + view;
        if (!this.key2ThresholdMap.containsKey(key1)) {
            ThresholdExpression[] tmp = this.teDao.getBy(kpi, ci, instance,
                    view);

            this.put2Cache(tmp);
        }
        ThresholdExpressionCache tec1 = this.key2ThresholdMap.get(key1);
        if (tec1 == null)
            return null;

        return tec1.getCache(key2);

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
        String key1 = ci + instance + kpi;

        if (!this.key2ThresholdMap.containsKey(key1)) {
            ThresholdExpression tmp = this.teDao.getBy(kpi, ci, instance, view,
                    user);

            this.put2Cache(new ThresholdExpression[] { tmp });
        }

        ThresholdExpressionCache tec1 = this.key2ThresholdMap.get(key1);
        if (tec1 == null)
            return null;
        String key2 = ci + instance + kpi + view;
        ThresholdExpressionCache tec2 = tec1.getCache(key2);
        if (tec2 == null) {
            return null;
        }
        String key3 = ci + instance + kpi + view + user;
        return tec2.getCache(key3).gettExp();

    }
}
