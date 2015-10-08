package com.mmdb.service.task;

import java.util.List;

import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.mapping.SourceToCategoryMapping;
import com.mmdb.model.mapping.SourceToRelationMapping;
import com.mmdb.model.task.Task;

/**
 * 任务 服务层
 * 
 * @author XIE
 */
public interface ITaskService {
	/**
	 * 获取所有任务
	 * 
	 * @return
	 * @throws Exception
	 */
	List<Task> getAll() throws Exception;

	/**
	 * 根据名称获取任务
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	Task getByName(String name) throws Exception;

	Task getById(String id) throws Exception;

	/**
	 * 保存任务
	 * 
	 * @param task
	 *            任务
	 * @throws Exception
	 */

	Task save(Task task) throws Exception;

	/**
	 * 编辑任务
	 * 
	 * @param task
	 *            任务
	 * @throws Exception
	 */

	Task update(Task task) throws Exception;

	/**
	 * 删除任务
	 * 
	 * @param task
	 *            任务
	 * @throws Exception
	 */

	void delete(Task task) throws Exception;

	/**
	 * 清空任务(做数据初始化的时候用)
	 * 
	 * @throws Exception
	 */

	void deleteAll() throws Exception;

	/**
	 * 设置任务开关状态
	 * 
	 * @param task
	 *            任务
	 * @param status
	 *            开关状态(true:开)
	 * @param timeOut
	 *            过期状态(true:过期)
	 * @throws Exception
	 */

	Task setStatus(Task task, boolean status, boolean timeOut) throws Exception;

	/**
	 * 执行任务
	 * 
	 * @param task
	 *            任务
	 * @return
	 * @throws Exception
	 */

	void runNow(Task task) throws Exception;

	/**
	 * 获取已分配任务的数据库映射id
	 * 
	 * @return
	 * @throws Exception
	 */
	List<String> getDataBaseIds() throws Exception;

	/**
	 * 获取DB映射分配的任务名称
	 * 
	 * @param scm
	 *            数据库映射
	 * @return
	 * @throws Exception
	 */
	List<String> getTaskIdsByDb(SourceToCategoryMapping scm) throws Exception;

	List<String> getTaskIdsByDb(SourceToCategoryMapping scm, List<Task> tasks)
			throws Exception;

	/**
	 * 获取已分配任务的内部映射id
	 * 
	 * @return
	 * @throws Exception
	 */
	List<String> getInCiCateMapIds() throws Exception;

	/**
	 * 获取CI内部映射分配的任务名称
	 * 
	 * @param im
	 *            CI内部映射
	 * @return
	 * @throws Exception
	 */
	List<String> getTaskIdsByInCiMap(InCiCateMap im) throws Exception;

	/**
	 * 根据已分配任务获取CI外部映射id
	 * 
	 * @return
	 * @throws Exception
	 */
	List<String> getOutCiCateMapIds() throws Exception;

	/**
	 * 获取CI外部映射分配的任务名称
	 * 
	 * @param om
	 *            CI内部映射
	 * @return
	 * @throws Exception
	 */
	List<String> getTaskIdsByOutCiMap(SourceToRelationMapping om)
			throws Exception;

	/**
	 * 通过映射Id获取所有正在使用此映射的任务列表
	 * 
	 * @param mappingId
	 * @return List<String> 任务名称
	 * @throws Exception
	 */
	public List<String> getTaskNamesByMapId(String mappingId) throws Exception;

}
