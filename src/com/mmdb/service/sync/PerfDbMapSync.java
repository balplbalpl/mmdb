package com.mmdb.service.sync;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jdbc.JdbcConnection;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.database.bean.DataBaseConfig;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.model.mapping.PerfToDbMapping;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.category.IKpiCateService;
import com.mmdb.service.db.IDataBaseConfigService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.mq.SenderPerfService;
import com.mmdb.util.JdbcOtherTools;

/**
 * 从生产库同步性能数据到MMDB
 * 
 * @author yuhao.guan
 * @version 1.0 2015-7-19
 */
@Component
public class PerfDbMapSync {
    private Log log = LogFactory.getLogger("PerfDbMapSync");

    @Autowired
    private IDataBaseConfigService dbConfigService;
    
	@Autowired
	private ICiInfoService ciInfoService;
	
	@Autowired
	private IKpiCateService kpicateService;
    
/*    @Autowired
    private SenderPerfService senderPerfService;*/

    public Map<String, Integer> run(PerfToDbMapping pdMapping) throws Exception {
        //String tag = System.currentTimeMillis() + "";
        Map<String, Integer> reMap = new HashMap<String, Integer>();
        int send = 0;
        Connection connection = null;
        try {
            DataBaseConfig dc = dbConfigService.getById(pdMapping.getDataSource().getDatabaseConfigId());
            DataSourcePool ds = pdMapping.getDataSource();
            //获取字段映射关系
            JSONObject fieldMap = pdMapping.getFieldMap();
            //获取自定义字段
            JSONObject customFieldsMap = pdMapping.getCustomFieldsMap();
            String valExp = pdMapping.getValExp();
            
            Map<String, String> am = dc.asMap();
            if (dc.getRac()) {
                am.put("url", dc.getRacAddress());
            }
            System.out.println(am);
            connection = JdbcOtherTools.getConnection(dc.getRac(), am);
            if (connection == null) {
                throw new Exception("获取数据库连接失败");
            }
            //-----------------------------------------
            //是否是增量更新
            boolean isAddSync = true;//pdMapping.getActive();
            String flag = pdMapping.getIsAddSync();
            if(flag!=null && !"".equals(flag)){
            	isAddSync= Boolean.parseBoolean(flag);
            }
            
            String timeColumnName = "";
            String newSql = "";
            if(isAddSync){
	        	Iterator<?> fieldKey= fieldMap.keys();
	        	while(fieldKey.hasNext()){
	        		 //MMDB的性能数据字段
	        		 String key = fieldKey.next().toString();
	        		 //获取到时间字段的字段名
	        		 if("time".equalsIgnoreCase(key)){
	        			 timeColumnName = fieldMap.getString(key);;
	        		 }
	        	}
	        	//如果是增量更新,为自定义SQL增加上时间段信息
	        	newSql = this.genTimeAreaSql(isAddSync, timeColumnName, connection, ds);
            }
        	if("".equals(newSql)){
        		newSql = ds.getCustomSql();
        	}
        	log.dLog("The query sql is:"+newSql);
        	//-------------------------------------------
            
            log.dLog("同步DB[" + ds.getTableName() + "]表数据");

            int count = JdbcConnection.getCountSize(connection, ds.getSchema(), ds.getTableName(), newSql);
            log.dLog("COUNT:" + count);
            int pageSize = 1000;
            int page = count % pageSize == 0 ? count / pageSize : (count / pageSize) + 1;
            log.dLog("页数:" + page);
            //初始化MQ连接
            SenderPerfService sendApi = new SenderPerfService();
            
            //以CIName为主键 存放所有CI信息;
            Map<String,CiInformation> ciNameMap = new HashMap<String,CiInformation>();
            //以CIHex为主键 存放所有CI信息
            Map<String,CiInformation> ciHexMap = new HashMap<String,CiInformation>();

            //以KPIName为主键 存放所有CI信息
            Map<String,KpiInformation> kpiNameMap = new HashMap<String,KpiInformation>();
            //以KPIHex为主键 存放所有CI信息
            Map<String,KpiInformation> kpiHexMap = new HashMap<String,KpiInformation>();            

            if(count >0){
                //获取到所有的CI，将CI缓存到Map中
                List<CiInformation> ciList = ciInfoService.getAll();
                for(CiInformation ci:ciList){
                	ciNameMap.put(ci.getName(), ci);
                	ciHexMap.put(ci.getCiHex(), ci);
                }
                List<KpiInformation> kpiList = kpicateService.getAllKpi();
                for(KpiInformation kpi:kpiList){
                	kpiNameMap.put(kpi.getName(), kpi);
                	kpiHexMap.put(kpi.getKpiHex(), kpi);
                }
            }
            
            for (int i = 0; i < page; i++) {
                List<Map<String, Object>> data;
                int startCount = i * pageSize + 1, endCount = (i + 1) * pageSize;
                log.iLog("startCount:" + startCount + ",endCount:" + endCount);
                if (ds.isSelf()) {
                    data = JdbcConnection.getDataByTable(connection, "", "", newSql, startCount, endCount);
                } else {
                    data = JdbcConnection.getDataByTable(connection, ds.getSchema(), ds.getTableName(), "", startCount, endCount);
                }
                if (data == null) {
                    throw new Exception("获取数据库数据失败");
                }
                JSONArray sendArray = new JSONArray();
                for (Map<String, Object> m : data) {
                	//TODO循环m，发送数据至MQ
                	JSONObject jsonPerf = JSONObject.fromObject(m);
                	
                	//jsonPerf.put("source", "mmdbMap");
                	jsonPerf.put("mapRuleId", pdMapping.getId());
                    
                	Iterator<?> objkey= fieldMap.keys();
                	while(objkey.hasNext()){
                		 //MMDB的性能数据字段
                		 String key = objkey.next().toString();
                		 //获取到被映射外部数据库的字段
                		 String outDbKey = fieldMap.getString(key);
                		 
                		 //如果是val需要转成double
                		 if("val".equalsIgnoreCase(key)){
                			 if(customFieldsMap.containsKey(key)){
                				 //如果是自定义字段，outDbkey就是这个字段的值
                    			 double val = 
                    					 Tool.executePerfVal(outDbKey, valExp);
                    			 jsonPerf.put("value", val);
                			 }else{
                    			 double val = 
                    					 Tool.executePerfVal((String)m.get(outDbKey), valExp);
                    			 jsonPerf.put("value", val);
                			 }
                		 }else if("time".equalsIgnoreCase(key)){
                			 Long time = -1L;
                			 if(customFieldsMap.containsKey(key)){
                				 //如果是自定义字段，outDbkey就是这个字段的值
	                			 time = Tool.transPerfValDate(outDbKey);
                			 }else{
	                			 time = Tool.transPerfValDate(m.get(outDbKey));
                			 }
                			 if(time != -1L){
                				 jsonPerf.put(key, time);
                			 }else{ //如果时间没有能成功转换，存入当前时间
                				 jsonPerf.put(key,System.currentTimeMillis());
                			 }
                		 }else if("ci".equalsIgnoreCase(key)){
                			 //如果CI是通过数据库字段映射，则直接去缓存中获取到CI
                			 CiInformation ciInfo = null;
                			 //ci如果是自定义值，用户应该选择分类和名称，所以使用HexId获取CI
                			 if(customFieldsMap.containsKey(key)){
                				 //ciInfo = ciNameMap.get(outDbKey);
                				 ciInfo = ciHexMap.get(pdMapping.getCiHex());
                			 }else{
                				 ciInfo = ciNameMap.get(m.get(outDbKey));
                			 }
            				 if(ciInfo != null){
            					 jsonPerf.put("ciName", ciInfo.getName());
            					 jsonPerf.put("ciId", ciInfo.getId());
            					 jsonPerf.put("ciCategoryId", ciInfo.getCategoryId());
            					 jsonPerf.put("ciCategoryName", ciInfo.getCategoryName());
            					 jsonPerf.put("ciHex", ciInfo.getCiHex());
            				 }
                		 }else if("kpi".equalsIgnoreCase(key)){
                			 KpiInformation kpiInfo = null;
                			 //kpi如果是自定义值，用户应该选择分类和名称，所以使用HexId获取KPI
                			 if(customFieldsMap.containsKey(key)){
                				 //kpiInfo = kpiNameMap.get(outDbKey);
                				 kpiInfo = kpiHexMap.get(pdMapping.getKpiHex());
                			 }else{
                				 kpiInfo = kpiNameMap.get(m.get(outDbKey));
                			 }
            				 if(kpiInfo != null){
            					 jsonPerf.put("kpiName", kpiInfo.getName());
            					 jsonPerf.put("kpiId", kpiInfo.getId());
            					 jsonPerf.put("kpiCategoryId", kpiInfo.getKpiCategoryId());
            					 jsonPerf.put("kpiCategoryName", kpiInfo.getKpiCategoryName());
            					 jsonPerf.put("kpiHex", kpiInfo.getKpiHex());
            				 }
                		 }else{
                			 if(customFieldsMap.containsKey(key)){
                				 //如果是自定义字段，outDbkey就是这个字段的值
                				 jsonPerf.put(key,outDbKey);
                			 }else{
                				 jsonPerf.put(key, m.get(outDbKey));
                			 }
                		 }
                	}
                	
                	sendArray.add(jsonPerf);
                    send++;
                }
                //发送到 性能处理MQ
               // sendToMQ(m);
                sendApi.sendJSONArrayMessage(sendArray);
            }
            //关闭MQ连接
            sendApi.closeConection();
        } finally {
            JdbcOtherTools.closeConnection(connection);
        }

        reMap.put("send", send);
        log.dLog("性能数据同步完成,共同步[" + send + "]条性能数据");
        return reMap;
    }
    
    /**
     * 为数据集的自定义sql增加时间条件限制，目前时间条件为：当前系统时间-5分钟
     * 
     * @param isAddSync 是否是增量更新
     * @param timeColumnName 时间字段的字段名
     * @param connection 数据库连接
     * @param ds 数据集对象
     * 
     * @return 包含时间段信息的SQL
     * @throws Exception
     */
    public String genTimeAreaSql(Boolean isAddSync,String timeColumnName,
    		Connection connection,DataSourcePool ds) throws Exception{
    	String newSql = "";
    	try{
			//如果是增量更新，并且从外部数据库中成功获取到了时间字段
			if(isAddSync && !"".equals(timeColumnName)){
				String customSql = ds.getCustomSql();
				Map<String, Map<String, String>> columns = null;
				//获取到所有的字段信息
				if(customSql!=null && customSql.length()>0){
					columns = JdbcConnection.getColumns(connection, ds.getSchema(), "", customSql);
				}else{
					columns = JdbcConnection.getColumns(connection, ds.getSchema(), ds.getTableName(), "");
				}
				//获取到数据库中的时间字段的字段类型
				Map<String,String> columnMap = columns.get(timeColumnName);
				String columnType = columnMap.get("字段类型");
				
				String dbType = JdbcConnection.getType(connection);
				
				//如果时间是LONG型的
				if("LONG".equalsIgnoreCase(columnType)){
					long endTime = System.currentTimeMillis();
					//粒度设定为5分钟
					long startTime = endTime - 300*1000;
					if(customSql!=null && customSql.length()>0){
						newSql = "select * from ("+customSql+") as uinnova " +
								"where uinnova."+timeColumnName+" < "+endTime + " and uinnova."+timeColumnName+" > "+startTime;
					}else{
						//如果自定义SQL为空，使用db.tableName作为自定义SQL
						newSql = "select * from "+ ds.getSchema()+"."+ds.getTableName()+" as uinnova " +
								"where uinnova."+timeColumnName+" < "+endTime + " and uinnova."+timeColumnName+" > "+startTime;
					}
					
				}else if("DATE".equalsIgnoreCase(columnType) || "DATETIME".equalsIgnoreCase(columnType)
						|| "TIMESTAMP".equalsIgnoreCase(columnType) 
						|| "DATETIME2".equalsIgnoreCase(columnType)){
					 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					 Calendar nowTime = Calendar.getInstance();
					 String endTime = sdf.format(nowTime.getTime());
					 nowTime.add(Calendar.MINUTE, -5);
					 String startTime = sdf.format(nowTime.getTime());
		 			if(customSql!=null && customSql.length()>0){
		 				customSql = "("+customSql+")";
						newSql = genSqlByDbType(dbType, customSql, timeColumnName, endTime, startTime);
					}else{
						//如果自定义SQL为空，使用db.tableName作为自定义SQL
						customSql = ds.getSchema()+"."+ds.getTableName();
						newSql = genSqlByDbType(dbType, customSql, timeColumnName, endTime, startTime);
					}
				}
			}
			return newSql;
    	}catch (Exception e){
    		log.eLog("同步性能数据，生成时间段SQL出错",e);
    		return newSql;
    	}
    }
    
    /**
     * 通过类型生成SQL(除了Oracle语法不同需特殊处理，其余三个语法其实可以通用)
     * 
     * @param dbType 数据库类型
     * @param customSql 自定义SQL
     * @param timeColumnName 时间字段名
     * @param endTime 结束时间
     * @param startTime 开始时间
     */
    private String genSqlByDbType(String dbType,String customSql,
    		String timeColumnName,String endTime,String startTime){
    	
    	String newSql = "";
    	if ((dbType.equalsIgnoreCase("oracle")) || (dbType.equalsIgnoreCase("oraclerac"))) {
    		newSql = "select * from "+customSql+" as uinnova " +
    				"where uinnova."+timeColumnName+" between to_date('"+startTime + "','yyyy-mm-dd hh24:mi:ss') and to_date('"+endTime+"','yyyy-mm-dd hh24:mi:ss')";

    	} else {
    		newSql = "select * from "+customSql+" as uinnova " +
    				"where uinnova."+timeColumnName+" < '"+endTime + "' and uinnova."+timeColumnName+" >= '"+startTime+"'";
    	}/* else if (dbType.equals("mysql")) {
        	customSql = "select * from ("+customSql+") as uinnova " +
    				"where uinnova."+timeColumnName+" < '"+endTime + "' and uinnova."+timeColumnName+" >= '"+startTime+"')";

    	} else if ((dbType.equals("sqlserver")) || (dbType.equals("sqlserver2000"))) {
        	customSql = "select * from ("+customSql+") as uinnova " +
    				"where uinnova."+timeColumnName+" < '"+endTime + "' and uinnova."+timeColumnName+" >= '"+startTime+"')";
    	} else if (dbType.equals("db2")) {
        	customSql = "select * from ("+customSql+") as uinnova " +
    				"where uinnova."+timeColumnName+" < '"+endTime + "' and uinnova."+timeColumnName+" >= '"+startTime+"')";
    	}*/
    	return newSql;
    }
}
