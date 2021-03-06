package com.mmdb.service.sync;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jdbc.JdbcConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.exception.MException;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.database.bean.DataBaseConfig;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.mapping.SourceToCategoryMapping;
import com.mmdb.model.mapping.storage.SourceToCategoryStorage;
import com.mmdb.service.db.IDataBaseConfigService;
import com.mmdb.service.db.IDataSourceService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.util.JdbcOtherTools;

/**
 * 数据同步类 该类用于将生产DB数据同步到开发库
 * Created by XIE on 2015/3/31.
 */
@Component
public class SourceCategoryMapSync {
    private Log log = LogFactory.getLogger("SourceCategoryMapSync");
    @Autowired
    private ICiInfoService infoService;
    @Autowired
    private IDataBaseConfigService dbConfigService;
    @Autowired
	private SourceToCategoryStorage sourceToCategoryStorage;
    @Autowired
    private IDataSourceService dataSourceService;

    public Map<String, Integer> run(String scmId, Map<String, CiCategory> ciCateMap) throws Exception {
    	SourceToCategoryMapping scm = sourceToCategoryStorage.getById(scmId);
    	DataSourcePool pool = dataSourceService.getById(scm.getDataSourceId());
    	scm.setDataSource(pool);
        String tag = System.currentTimeMillis() + "";
        Map<String, Integer> reMap = new HashMap<String, Integer>();
        int save = 0, update = 0, delete = 0;
        Connection connection = null;
        CiCategory category = ciCateMap.get(scm.getCateId());
        try {
            DataBaseConfig dc = dbConfigService.getById(scm.getDataSource().getDatabaseConfigId());
//            DataBaseSelf ds = scm.getDataSource().getDs();
            DataSourcePool ds = scm.getDataSource();
            Map<String, String> am = dc.asMap();
            if (dc.getRac()) {
                am.put("url", dc.getRacAddress());
            }
//            am.put("database", "");
//            System.out.println(am);
            connection = JdbcOtherTools.getConnection(dc.getRac(), am);
            if (connection == null) {
                throw new MException("获取数据库连接失败");
            }
            Map<String, List<String>> fms = scm.reversalFieldMap();
            log.dLog("同步DB[" + ds.getTableName() + "]表数据");
            String owner = scm.getOwner();
            
            int count = JdbcConnection.getCountSize(connection, ds.getSchema(), ds.getTableName(), ds.getCustomSql());
            log.dLog("COUNT:" + count);
            int pageSize = 1000;
            int page = count % pageSize == 0 ? count / pageSize : (count / pageSize) + 1;
            log.dLog("页数:" + page);
            for (int i = 0; i < page; i++) {
                List<Map<String, Object>> data;
                List<CiInformation> cis = new ArrayList<CiInformation>();
                int startCount = i * pageSize + 1, endCount = (i + 1) * pageSize;
                log.iLog("startCount:" + startCount + ",endCount:" + endCount);
                if (ds.isSelf()) {
                    data = JdbcConnection.getDataByTable(connection, "", "", ds.getCustomSql(), startCount, endCount);
                } else {
                    data = JdbcConnection.getDataByTable(connection, ds.getSchema(), ds.getTableName(), "", startCount, endCount);
                }
                if (data == null) {
                    throw new MException("获取数据库数据失败");
                }
                for (Map<String, Object> m : data) {
                    Map<String, Object> dm = new HashMap<String, Object>();
                    Iterator<Map.Entry<String, List<String>>> it = fms.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, List<String>> entry = it.next();
                        String key = entry.getKey();
                        List<String> vs = entry.getValue();
                        for (int j = 0; j < vs.size(); j++) {
                            dm.put(vs.get(j), m.get(key));
                        }
                    }
                    CiInformation info = new CiInformation(category, "db", dm);
                    info.setTag(tag);
                    info.setOwner(owner);
                    cis.add(info);
                }
                if (cis.size() > 0) {
                    Map<String, Long> sMap = infoService.saveOrUpdate(category, cis);
                    save += sMap.get("save");
                    update += sMap.get("update");
                    log.dLog("分类[" + category.getId() + "]保存[" + sMap.get("save") + "]更新[" + sMap.get("update") + "]");
                }
            }
        } finally {
            JdbcOtherTools.closeConnection(connection);
        }
        Map<String, String> mustMap = new HashMap<String, String>();
        mustMap.put("categoryId", category.getId());
        mustMap.put("source", "db");
        Map<String, String> mustNotMap = new HashMap<String, String>();
        mustNotMap.put("tag", tag);
        List<CiInformation> removeCis = infoService.qureyByTerm(mustMap, mustNotMap);
        delete = removeCis.size();
        infoService.delete(removeCis);
        reMap.put("save", save);
        reMap.put("update", update);
        reMap.put("delete", delete);
        log.dLog("CMDB数据同步完成,保存CI[" + save + "]更新CI[" + update + "]" + "]删除CI[" + delete + "]");
        return reMap;
    }
}
