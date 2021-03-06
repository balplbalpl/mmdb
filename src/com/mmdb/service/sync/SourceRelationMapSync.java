package com.mmdb.service.sync;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdbc.JdbcConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.framework.neo4j.entity.Dynamic;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.database.bean.DataBaseConfig;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.mapping.SourceToRelationMapping;
import com.mmdb.model.mapping.storage.SourceToRelationStorage;
import com.mmdb.model.relation.CiRelation;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.db.IDataBaseConfigService;
import com.mmdb.service.db.IDataSourceService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.relation.ICiRelService;
import com.mmdb.util.JdbcOtherTools;

/**
 * 数据库关系同步
 * Created by XIE on 2015/3/31.
 */
@Component
public class SourceRelationMapSync {
    private Log log = LogFactory.getLogger("SourceRelationMapSync");

    @Autowired
    private ICiRelService relService;
    @Autowired
    private ICiInfoService infoService;
    @Autowired
    private IDataBaseConfigService dbConfigService;
    @Autowired
	private SourceToRelationStorage sourceToRelationStorage;
    @Autowired
    private IDataSourceService dataSourceService;

    /**
     * 处理关系属性空值
     *
     * @param rc     分类
     * @param relVal 关系值
     * @return
     */
    private Dynamic<String, Object> paddingRelValue(RelCategory rc, Map<String, Object> relVal) {
        List<String> atrrs = rc.getAttributeNames();
        Dynamic<String, Object> relValue = new Dynamic<String, Object>();
        for (String attr : atrrs) {
            if (relVal.containsKey(attr)) {
                relValue.put(attr, relVal.get(attr));
            } else {
                relValue.put(attr, "");
            }
        }
        return relValue;
    }
    /**
     * 
     * @param srm
     * @return
     * @throws Exception
     */
    public Map<String, Integer> run(String srmId, Map<String, CiCategory> ciCateMap, Map<String, RelCategory> relCateMap) throws Exception {
    	SourceToRelationMapping srm = sourceToRelationStorage.getById(srmId);
    	DataSourcePool pool = dataSourceService.getById(srm.getDataSourceId());
    	srm.setDataSource(pool);
    	srm.setSourceCate(ciCateMap.get(srm.getSourceCateId()));
    	srm.setTargetCate(ciCateMap.get(srm.getTargetCateId()));
    	String tag = System.currentTimeMillis() + "";
        Map<String, Integer> reMap = new HashMap<String, Integer>();
        int save = 0, update = 0, delete = 0;
        Connection connection = null;
        RelCategory rc = relCateMap.get(srm.getRelCateId());
        String outMapId = srm.getId();
        String owner = srm.getOwner();
        try {
        	DataBaseConfig dc = dbConfigService.getById(srm.getDataSource().getDatabaseConfigId());
//            DataBaseSelf ds = srm.getDataSource().getDs();
        	DataSourcePool ds = srm.getDataSource();
            log.dLog("同步CMDB[" + ds.getTableName() + "]表数据");
            Map<String, String> am = dc.asMap();
            if (dc.getRac()) {
                am.put("url", dc.getRacAddress());
            }
            connection = JdbcOtherTools.getConnection(dc.getRac(), am);
            if (connection == null) {
                throw new Exception("获取数据库连接失败");
            }
            log.dLog("同步DB[" + ds.getTableName() + "]表数据");
            int count = JdbcConnection.getCountSize(connection, ds.getSchema(), ds.getTableName(), ds.getCustomSql());
            log.dLog("COUNT:" + count);
            String sFiled = srm.getSourceField(), eFiled = srm.getTargetField();
            int pageSize = 2000;
            int page = count % pageSize == 0 ? count / pageSize : (count / pageSize) + 1;
            log.dLog("页数:" + page);
            for (int i = 0; i < page; i++) {
                Set<CiRelation> rs = new HashSet<CiRelation>();
                List<Map<String, Object>> data;
                int startCount = i * pageSize + 1, endCount = (i + 1) * pageSize;
                log.iLog("startCount:" + startCount + ",endCount:" + endCount);
                if (ds.isSelf()) {
                    data = JdbcConnection.getDataByTable(connection, "", "",
                            ds.getCustomSql(), startCount, endCount);
                } else {
                    data = JdbcConnection.getDataByTable(connection, ds.getSchema(), ds.getTableName(),
                            "", startCount, endCount);
                }
                //
                for (Map<String, Object> m : data) {
                    String sid = m.get(sFiled).toString(), eid = m.get(eFiled).toString();
                    if(sid==null||eid ==null||"".equals(sid)||"".equals(eid)){
                    	continue;
                    }
                    List<CiInformation> sis = new ArrayList<CiInformation>();
                    List<CiInformation> eis = new ArrayList<CiInformation>();
                    if (srm.getSourceCate() != null) {
                    	CiCategory sourceCate = srm.getSourceCate();
                        Map<String, String> must = new HashMap<String, String>();
//                        must.put("id", sid);
                        must.put(sourceCate.getOwnMajor().getName(), sid);
                        int sPage = 1;
                        Map<String, Object> sm = infoService.qureyByAdvancedEQ(srm.getSourceCate(), must, null, false, null, (sPage-1)*Tool.getBuff, Tool.getBuff);
                        List<CiInformation> sList = (List<CiInformation>)sm.get("data");
                        int sCount = (Integer)sm.get("count");
                        int sIndex = 0;
                        while(true){
                        	for(CiInformation info:sList){
                        		sis.add(info);
                        		sIndex++;
                        	}
                        	if(sIndex>=sCount){
                        		break;
                        	}
                        	sPage++;
                        	sm = infoService.qureyByAdvancedEQ(srm.getSourceCate(), must, null, false, null, (sPage-1)*Tool.getBuff, Tool.getBuff);
                            sList = (List<CiInformation>)sm.get("data");
                            sCount = (Integer)sm.get("count");
                        }
                    } else {
                        sis = infoService.getByProperty(  "id", sid);
                    }
                    if (srm.getTargetCate() != null) {
                        Map<String, String> must = new HashMap<String, String>();
                        CiCategory targetCate = srm.getTargetCate();
                        must.put(targetCate.getOwnMajor().getName(), eid);
                        int ePage = 1;
                        Map<String, Object> em = infoService.qureyByAdvancedEQ(srm.getTargetCate(), must, null, false, null, (ePage-1)*Tool.getBuff, Tool.getBuff);
                        List<CiInformation> eList = (List<CiInformation>)em.get("data");
                        int eCount = (Integer)em.get("count");
                        int eIndex = 0;
                        while(true){
                        	for(CiInformation info:eList){
                        		eis.add(info);
                        		eIndex++;
                        	}
                        	if(eIndex>=eCount){
                        		break;
                        	}
                        	ePage++;
                        	em = infoService.qureyByAdvancedEQ(srm.getTargetCate(), must, null, false, null, (ePage-1)*Tool.getBuff, Tool.getBuff);
                            eList = (List<CiInformation>)em.get("data");
                            eCount = (Integer)em.get("count");
                        }
                    } else {
                        eis = infoService.getByProperty(  "id", eid);
                    }
                    for (CiInformation si : sis) {
                        for (CiInformation ei : eis) {
                            CiRelation r = new CiRelation(si, ei, rc, this.paddingRelValue(rc, srm.getRelValue()));
                            r.setSource("db");
                            r.setOutMapId(outMapId);
                            r.setTag(tag);
                            r.setOwner(owner);
                            rs.add(r);
                        }
                    }
                }
                if (rs.size() > 0) {
                    Map<String, Long> sMap = relService.saveOrUpdate(rc, rs);
                    save += sMap.get("save");
                    update += sMap.get("update");
                    log.dLog("关系[" + rc.getId() + "]保存[" + sMap.get("save") + "]更新[" + sMap.get("update") + "]");
                }
            }
        } finally {
            JdbcOtherTools.closeConnection(connection);
        }
        Map<String, String> mustMap = new HashMap<String, String>();
        mustMap.put("source", "db");
        mustMap.put("relCateId", rc.getId());
        mustMap.put("outMapId", outMapId);
        Map<String, String> mustNotMap = new HashMap<String, String>();
        mustNotMap.put("tag", tag);
        List<CiRelation> removeRels = relService.qureyByTerm(mustMap, mustNotMap);
        delete = removeRels.size();
        relService.delete(removeRels);
        reMap.put("save", save);
        reMap.put("update", update);
        reMap.put("delete", delete);
        log.dLog("关系同步完成,保存关系[" + save + "]更新关系[" + update + "]删除关系[" + delete + "]");
        return reMap;
    }
}
