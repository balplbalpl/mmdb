/**
 * 
 */
package com.mmdb.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.SemaphoreConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.TopicConfig;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;

/**
 * 此类主要是为了生成一个分布式的运行运行，目前只实现了基于hazelcast的分布式环境。 相当于一个工具类。
 * 
 * @author aol_aog@163.com(James Gao)
 * 
 */
public class DistributeEnv {
	private Log log = LogFactory.getLogger("DistributeTaskService");

	private String host;

	private int port;

	private String clusterName;

	private String clusterMembers;

	private String semphoreList;

	private String topicList;

	private HazelcastInstance hazInstance;

	private boolean isShutdown = true;

	private Map<String, InnerTask> taskMap;

	/**
	 * 
	 */
	public DistributeEnv() {
		taskMap = new LinkedHashMap<String, InnerTask>();
	}

	/**
	 * 创建环境。
	 * 
	 * @return
	 */
	private HazelcastInstance createHazelcastInstance() {
		Config hzConfig = new Config();
		hzConfig.setGroupConfig(new GroupConfig(clusterName));
		/*
		 * ExecutorConfig executorConfig = new ExecutorConfig();
		 * executorConfig.setPoolSize(5); executorConfig.setQueueCapacity(3);
		 * executorConfig.setStatisticsEnabled(true);
		 * executorConfig.setName("defaultExecutorService");
		 * hzConfig.addExecutorConfig(executorConfig);
		 */
		MapConfig mc = new MapConfig();
		mc.setName("web_session");
		mc.setBackupCount(1);
		mc.setReadBackupData(false);
		mc.setTimeToLiveSeconds(1800);
		hzConfig.addMapConfig(mc);

		JoinConfig join = new JoinConfig()
				.setMulticastConfig(new MulticastConfig().setEnabled(false));
		String[] members = clusterMembers.split(",");
		join.setTcpIpConfig(new TcpIpConfig().setEnabled(true)
				.setMembers(Arrays.asList(members))
				.setConnectionTimeoutSeconds(10));
		hzConfig.getNetworkConfig().setJoin(join).setPort(port)
				.setPortAutoIncrement(false);

		// 信号量.
		String[] semphores = semphoreList.split(",");
		Map<String, SemaphoreConfig> scm = new LinkedHashMap<String, SemaphoreConfig>();
		for (String semphore : semphores) {

			SemaphoreConfig sc = new SemaphoreConfig();
			sc.setName(semphore);
			sc.setInitialPermits(1);
			sc.setBackupCount(1);
			sc.setAsyncBackupCount(1);

			scm.put(semphore, sc);
		}

		// topic
		String[] totpicNames = this.topicList.split(",");
		Map<String, TopicConfig> topicMap = new LinkedHashMap<String, TopicConfig>();
		for (String topicName : totpicNames) {

			TopicConfig topicConfig = new TopicConfig();
			topicConfig.setGlobalOrderingEnabled(true);
			topicConfig.setStatisticsEnabled(true);
			topicConfig.setName(topicName);			

			topicMap.put(topicName, topicConfig);
		}

		hzConfig.setTopicConfigs(topicMap);

		hzConfig.setSemaphoreConfigs(scm);
		
		return Hazelcast.newHazelcastInstance(hzConfig);

	}

	/**
	 * 初始化分布式环境
	 */
	public void init() {
		log.iLog("初始化hazelcast实例...");
		if (isShutdown)
			hazInstance = createHazelcastInstance();
		isShutdown = false;
		IMap<String,Object> sessionMap =this.hazInstance.getMap("web_session");
		sessionMap.addEntryListener(
				new EntryListener<String, Object>() {
					@Override
					public void entryAdded(
							EntryEvent<String, Object> entryEvent) {
						System.out.println("Node: "+entryEvent.getMember());
						System.out.println("source: "+entryEvent.getSource());;
						System.out.println("新数据加入:"+entryEvent.getEventType()+entryEvent.getKey()+": "+entryEvent.getValue());
					}

					@Override
					public void entryRemoved(
							EntryEvent<String, Object> entryEvent) {
						if (entryEvent.getMember() == null
								|| !entryEvent.getMember().localMember()) {
							
						}
						System.out.println("Node: "+entryEvent.getMember());
						System.out.println("source: "+entryEvent.getSource());;
						System.out.println("删除数据:"+entryEvent.getKey()+": "+entryEvent.getValue());
					}

					@Override
					public void entryUpdated(
							EntryEvent<String, Object> entryEvent) {
						System.out.println("Node: "+entryEvent.getMember());
						System.out.println("source: "+entryEvent.getSource());;
						System.out.println("更新数据:"+entryEvent.getKey()+": "+entryEvent.getValue());
					}

					@Override
					public void entryEvicted(
							EntryEvent<String, Object> entryEvent) {
						entryRemoved(entryEvent);
					}
				}, true);
		log.iLog("hazelcast实例初始化完毕。");
	}

	/**
	 * 关闭分布式环境。
	 */
	public void destroy() {
		log.iLog("关闭hazelcast实例...");
		if (!isShutdown)
			hazInstance.shutdown();
		isShutdown = true;
		for(InnerTask dt : this.taskMap.values().toArray(new InnerTask[0])){
			this.cancelTask(dt);
		}
		this.taskMap.clear();
		log.iLog("hazelcast实例关闭完毕。");
	}

	/**
	 * 提交任务到分布式调度服务中执行。
	 * 
	 * @param task
	 *            需要执行的任务。
	 */
	public void submitTask(DistributeTask task) {
		if (!this.isShutdown) {

			// ISemaphore sem =
			// hazInstance.getSemaphore(task.getTaskGroup()+":"+task.getTaskName());

			log.iLog("提交任务:" + task.getTaskGroup() + ":" + task.getTaskName()
					+ "到调度服务执行...");
			InnerTask it = new InnerTask(task, hazInstance);
			it.setName(task.getTaskGroup() + ":" + task.getTaskName());
			it.start(); // 提交到调度服务执行。
			this.taskMap.put(task.getTaskGroup() + ":" + task.getTaskName(), it);
			log.iLog("任务正在执行...");
		}
	}

	/**
	 * 取消任务。
	 * 
	 * @param task
	 */
	private void cancelTask(InnerTask task) {
		if (!this.isShutdown) {
			log.iLog("取消任务:" + task.getName() 
					+ "...");
			task.interrupt();
			log.iLog("取消完毕。");
		}
	}

	public Log getLog() {
		return log;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getClusterMembers() {
		return clusterMembers;
	}

	public void setClusterMembers(String clusterMembers) {
		this.clusterMembers = clusterMembers;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getSemphoreList() {
		return semphoreList;
	}

	public void setSemphoreList(String semphoreList) {
		this.semphoreList = semphoreList;
	}

	public String getTopicList() {
		return topicList;
	}

	public void setTopicList(String topicList) {
		this.topicList = topicList;
	}
	
	public String getLocalMemberID(){
		return  this.hazInstance.getCluster().getLocalMember().getUuid();
	}

	public HazelcastInstance getHazInstance() {
		return hazInstance;
	}

	public void setHazInstance(HazelcastInstance hazInstance) {
		this.hazInstance = hazInstance;
	}

	/**
	 * 向一个TOPIC上注册一个监听器
	 * 
	 * @param topicName
	 *            topic名。
	 * @param topicListener
	 *            监听器
	 */
	public ITopic<Serializable> addTopicListener(String topicName,
			MessageListener<Serializable> topicListener) {
		ITopic<Serializable> topic = this.hazInstance.getTopic(topicName);
		topic.addMessageListener(topicListener);
		return topic;
	}

	private class InnerTask extends Thread implements Serializable {

		/**
		 * serialUID.
		 */
		private static final long serialVersionUID = -4721010421263413587L;

		private DistributeTask dtask;
		private HazelcastInstance hzcInstance;

		public InnerTask(DistributeTask dTask, HazelcastInstance hzcInstance) {
			this.dtask = dTask;
			this.hzcInstance = hzcInstance;
		}

		/**
		 * 
		 */
		@Override
		public void run() {
			ISemaphore sem = this.hzcInstance.getSemaphore(dtask
					.getOwnSemaphore());
			try {
				sem.acquire(); // 此处为了获取一个锁，实现不同进程之间的信号量互斥

				while (!isShutdown) {
					try {
						this.dtask.execute();
					} catch (Exception ex) {
						log.eLog("执行分布式任务时出错："+ex.getMessage(),ex);
					}
					try {
						// 间隔时间执行.
						Thread.sleep(1000L * dtask.getInterval());
					} catch (InterruptedException e) {
						// ignore this exception.
					}
				}

			} catch (Exception e1) {
				log.eLog("执行分布式任务时出错.", e1);
			} finally {
				if (!isShutdown)
					sem.release();
			}

		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final DistributeEnv de = new DistributeEnv();
		de.setClusterName("test-haz");
		de.setClusterMembers("localhost:5709,localhost:5710");
		de.setHost("127.0.0.1");
		de.setPort(5710);
		final HazelcastInstance haz = de.createHazelcastInstance();

		Thread t = new Thread() {
			@Override
			public void run() {

				ISemaphore sem = haz.getSemaphore("defaultSemaphore");
				try {
					System.out.println("获取信号量...");
					sem.acquire();
					while (true) {
						System.out.println(de.getPort() + "_信号量");
						try {
							Thread.sleep(5 * 1000L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				} catch (InterruptedException e1) {

					e1.printStackTrace();
				} finally {
					sem.release();
				}

			}
		};
		t.setName("t_" + de.getPort());
		t.start();
	}

}
