package com.mmdb.service.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.framework.neo4j.proxy.AbstractDomain;
import com.mmdb.core.utils.MapUtil;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.mapping.SourceToCategoryMapping;
import com.mmdb.model.mapping.SourceToRelationMapping;
import com.mmdb.model.relation.CiRelation;
import com.mmdb.model.task.Task;
import com.mmdb.model.task.TaskStorage;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.relation.ICiRelService;

@Component("taskService")
public class TaskServiceImpl extends AbstractDomain implements ITaskService {
	@Autowired
	private TaskStorage taskStorage;
	@Autowired
	private ICiInfoService ciInfoService;
	@Autowired
	private ICiRelService ciInfoRelService;

	@Override
	public List<Task> getAll() throws Exception {
		return taskStorage.getAll();
	}

	@Override
	public Task getByName(String name) throws Exception {
		return taskStorage.getByName(name);
	}

	@Override
	public Task getById(String id) throws Exception {
		return taskStorage.getById(id);
	}

	@Override
	public Task save(Task task) throws Exception {
		return taskStorage.save(task);
	}

	@Override
	public Task update(Task task) throws Exception {
		return taskStorage.update(task);
	}

	@Override
	public void delete(Task task) throws Exception {
		taskStorage.delete(task);
	}

	@Override
	public void deleteAll() throws Exception {
		taskStorage.deleteAll();
	}

	@Override
	public Task setStatus(Task task, boolean status, boolean timeOut)
			throws Exception {
		// task = task.unLazy();
		task.setOpen(status);
		task.setTimeOut(timeOut);
		return taskStorage.update(task);
	}

	@Override
	public void runNow(Task task) throws Exception {
		List<InCiCateMap> ims = task.getInCiCateMap();
		for (InCiCateMap im : ims) {
			CiCategory sCate = im.getStartCate();
			CiCategory eCate = im.getEndCate();
			int sPage = 1;
			Map<String, Object> sm = ciInfoService.qureyByAdvanced(sCate, null, null, false, null, (sPage-1)*Tool.getBuff, Tool.getBuff);
			List<CiInformation> sInfos = (List<CiInformation>)sm.get("data");
			int sCount = (Integer)sm.get("count");
			int sIndex = 0;
			while(true){
				for (CiInformation sinfo : sInfos) {
					// 获取起点映射的值
					Object sVal = sinfo.getData().get(im.getStartCateField());
					Map<String, String> map = MapUtil.hashMap(im.getEndCateField(),
							(sVal == null ? null : sVal.toString()));
					// 获取终点分类下与起点值相同的终点数据
					int ePage = 1;
					Map<String, Object> em = ciInfoService.qureyByAdvanced(eCate, map, null, false, null, (ePage-1)*Tool.getBuff, Tool.getBuff);
					List<CiInformation> eInfos = (List<CiInformation>)em.get("data");
					int eCount = (Integer)em.get("count");
					int eIndex = 0;
					while(true){
						for (CiInformation eInfo : eInfos) {
							CiRelation ir = new CiRelation(sinfo, eInfo, im);
							CiRelation cir = ciInfoRelService.getById(ir.getId());
							if (cir == null) {
								ciInfoRelService.save(ir);
							} else {
								// cir = cir.unLazy();
								ciInfoRelService.update(cir);
							}
							eIndex++;
						}
						if(eIndex>=eCount){
							break;
						}
						ePage++;
						em = ciInfoService.qureyByAdvanced(eCate, map, null, false, null, (ePage-1)*Tool.getBuff, Tool.getBuff);
						eInfos = (List<CiInformation>)em.get("data");
						eCount = (Integer)em.get("count");
					}
					sIndex++;
				}
				if(sIndex>=sCount){
					break;
				}
				sPage++;
				sm = ciInfoService.qureyByAdvanced(sCate, null, null, false, null, (sPage-1)*Tool.getBuff, Tool.getBuff);
				sInfos = (List<CiInformation>)sm.get("data");
				sCount = (Integer)sm.get("count");
			}
			
		}

	}

	@Override
	public List<String> getDataBaseIds() throws Exception {
		List<String> retList = new ArrayList<String>();
		List<Task> ts = taskStorage.getAll();
		for (Task t : ts) {
			List<SourceToCategoryMapping> dbms = t.getDbCiCateMap();
			for (SourceToCategoryMapping dbm : dbms) {
				// Long id = dbm.getNeo4jid();
				String id = dbm.getId();
				if (!retList.contains(id)) {
					retList.add(id);
				}
			}
		}
		return retList;
	}

	@Override
	public List<String> getTaskIdsByDb(SourceToCategoryMapping db)
			throws Exception {
		List<String> retList = new ArrayList<String>();
		List<Task> ts = taskStorage.getAll();
		for (Task t : ts) {
			List<SourceToCategoryMapping> dbms = t.getDbCiCateMap();
			if (dbms.contains(db)) {
				String name = t.getName();
				if (!retList.contains(name))
					retList.add(name);
			}
		}
		return retList;
	}

	@Override
	public List<String> getTaskIdsByDb(SourceToCategoryMapping db,
			List<Task> tasks) throws Exception {
		List<String> retList = new ArrayList<String>();
		List<Task> ts = tasks;
		for (Task t : ts) {
			List<SourceToCategoryMapping> dbms = t.getDbCiCateMap();
			if (dbms.contains(db)) {
				String name = t.getName();
				if (!retList.contains(name))
					retList.add(name);
			}
		}
		return retList;
	}

	@Override
	public List<String> getInCiCateMapIds() throws Exception {
		List<String> retList = new ArrayList<String>();
		List<Task> ts = taskStorage.getAll();
		for (Task t : ts) {
			List<InCiCateMap> ims = t.getInCiCateMap();
			for (InCiCateMap im : ims) {
				// Long id = im.getNeo4jid();
				String id = im.getName();
				if (!retList.contains(id)) {
					retList.add(id);
				}
			}
		}
		return retList;
	}

	@Override
	public List<String> getTaskIdsByInCiMap(InCiCateMap im) throws Exception {
		List<String> retList = new ArrayList<String>();
		List<Task> ts = taskStorage.getAll();
		String name = im.getName();
		for (Task t : ts) {
			List<InCiCateMap> ims = t.getInCiCateMap();
			for (InCiCateMap m : ims) {
				String id = m.getName();
				if (id.equals(name)) {
					String tn = t.getName();
					if (!retList.contains(tn))
						retList.add(tn);
				}
			}
		}
		return retList;
	}

	@Override
	public List<String> getOutCiCateMapIds() throws Exception {
		List<String> retList = new ArrayList<String>();
		List<Task> ts = taskStorage.getAll();
		for (Task t : ts) {
			List<SourceToRelationMapping> oms = t.getOutCiCateMap();
			for (SourceToRelationMapping om : oms) {
				String id = om.getId();
				if (!retList.contains(id)) {
					retList.add(id);
				}
			}
		}
		return retList;
	}

	@Override
	public List<String> getTaskIdsByOutCiMap(SourceToRelationMapping om)
			throws Exception {
		List<String> retList = new ArrayList<String>();
		List<Task> ts = taskStorage.getAll();
		String name = om.getId();
		for (Task t : ts) {
			List<SourceToRelationMapping> oms = t.getOutCiCateMap();
			for (SourceToRelationMapping m : oms) {
				String id = m.getId();
				if (id.equals(name)) {
					String tn = t.getName();
					if (!retList.contains(tn))
						retList.add(tn);
				}
			}
		}
		return retList;
	}

	/**
	 * 通过性能数据映射Id获取所有正在使用此映射的任务列表
	 * 
	 * @param mappingId
	 *            性能数据映射ID
	 * @return List<String> 任务名称
	 * @throws Exception
	 */
	@Override
	public List<String> getTaskNamesByMapId(String mappingId) throws Exception {
		List<String> retList = new ArrayList<String>();
		List<Task> ts = taskStorage.getAll();
		for (Task t : ts) {
			List<String> mapIds = t.getPerfDbMapIds();
			if (mapIds.contains(mappingId)) {
				String name = t.getName();
				if (!retList.contains(name))
					retList.add(name);
			}
		}
		return retList;
	}
}
