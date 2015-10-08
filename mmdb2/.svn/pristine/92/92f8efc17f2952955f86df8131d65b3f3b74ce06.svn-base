package com.mmdb.service.mq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.springframework.stereotype.Service;

import com.mmdb.core.utils.SysProperties;

@Service
public class MonitorActiveMqService {
	/**
	 * 
	 */
	private Map<String, Map<String, Object>> config;

	public MonitorActiveMqService() {
		config = new HashMap<String, Map<String, Object>>();
		try {

			String brokerNames = SysProperties.get("jmx.amq.brokerNames");
			String domains = SysProperties.get("jmx.amq.domains");
			String hosts = SysProperties.get("jmx.amq.hosts");
			String ports = SysProperties.get("jmx.amq.ports");
			String paths = SysProperties.get("jmx.amq.paths");
			String[] bns = brokerNames.split(",");
			String[] dms = domains.split(",");
			String[] hts = hosts.split(",");
			String[] pts = ports.split(",");
			String[] phs = paths.split(",");
			int tLen = bns.length;
			if (dms.length == tLen && hts.length == tLen && pts.length == tLen
					&& phs.length == tLen) {
				for (int i = 0; i < tLen; i++) {
					try {
						String brokerName = bns[i].trim();
						String domain = dms[i].trim();
						String host = hts[i].trim();
						int port = Integer.parseInt(pts[i].trim());
						String path = phs[i].trim();
						if (brokerName == null || "".equals(brokerName)
								|| domain == null || "".equals(domain)
								|| host == null || "".equals(host)
								|| path == null || "".equals(path)) {
							continue;
						} else {
							Map<String, Object> value = new HashMap<String, Object>();
							value.put("brokerName", brokerName);
							value.put("domain", domain);
							value.put("host", host);
							value.put("port", port + "");
							if (!path.startsWith("/")) {
								path = "/" + path;
							}
							value.put("path", path);
							config.put(brokerName, value);
						}
					} catch (Exception e) {
					}

				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 获取全部队列的数据.
	 * 
	 * @return
	 */
	public Map<String, List<Map<String, String>>> getQueuesData() {
		Map<String, List<Map<String, String>>> ret = new HashMap<String, List<Map<String, String>>>();
		Set<String> keySet = config.keySet();
		for (String brokerName : keySet) {
			Map<String, Object> map = config.get(brokerName);
			String domain = (String) map.get("domain");
			String host = (String) map.get("host");
			String port = (String) map.get("port");
			String path = (String) map.get("path");
			List<Map<String, String>> queues = getQueues(brokerName, domain,
					host, port, path);
			if (queues != null) {
				ret.put(brokerName, queues);
			}
		}
		return ret;
	}

	public void delQueue(String brokerName, String queueName) throws Exception {
		Map<String, Object> map = config.get(brokerName);
		if (map != null) {
			String domain = (String) map.get("domain");
			String host = (String) map.get("host");
			String port = (String) map.get("port");
			String path = (String) map.get("path");
			delQueue(brokerName, domain, host, port, path, queueName);
		} else {
			throw new Exception("activeMq服务[" + brokerName + "]不存在");
		}
	}

	/**
	 * 通过配置文件获取到相对应的队列信息
	 * 
	 * @param brokerName
	 * @param domain
	 * @param port
	 * @param path
	 *            "/jmxrmi"
	 */
	private List<Map<String, String>> getQueues(String brokerName,
			String domain, String host, String port, String path) {
		String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port
				+ path;
		JMXServiceURL jmxUrl;
		JMXConnector connector = null;

		try {
			jmxUrl = new JMXServiceURL(url);
			connector = JMXConnectorFactory.connect(jmxUrl);
			MBeanServerConnection mbsc = connector.getMBeanServerConnection();
			String name = null;
			Set<ObjectInstance> queryMBeans = mbsc.queryMBeans(null, null);
			for (ObjectInstance objectInstance : queryMBeans) {
				if("org.apache.activemq.broker.jmx.BrokerView".equals(objectInstance.getClassName())){
					name=objectInstance.getObjectName().toString();
					break;
				}
			}
			ObjectName mbeanName = new ObjectName(name);
			BrokerViewMBean mBean = MBeanServerInvocationHandler
					.newProxyInstance(mbsc, mbeanName, BrokerViewMBean.class,
							true);
			ObjectName[] queues = mBean.getQueues();
			List<Map<String, String>> ret = new ArrayList<Map<String, String>>();

			for (ObjectName objectName : queues) {
				QueueViewMBean queueViewMBean = MBeanServerInvocationHandler
						.newProxyInstance(mbsc, objectName,
								QueueViewMBean.class, true);
				HashMap<String, String> temp = new HashMap<String, String>();
				temp.put("queueName", queueViewMBean.getName());
				temp.put("pendingMessages", queueViewMBean.getQueueSize() + "");
				temp.put("numberOfConsumers", queueViewMBean.getConsumerCount()
						+ "");
				temp.put("messagesEnqueued", queueViewMBean.getEnqueueCount()
						+ "");
				temp.put("messagesDequeued", queueViewMBean.getDequeueCount()
						+ "");
				// temp.put("队列名字", queueViewMBean.getName());
				// temp.put("未处理消息数", queueViewMBean.getQueueSize() + "");
				// temp.put("处理的服务数", queueViewMBean.getConsumerCount() + "");
				// temp.put("处理中消息数量", queueViewMBean.getEnqueueCount() + "");
				// temp.put("处理过的消息", queueViewMBean.getDequeueCount() + "");
				ret.add(temp);
			}
			return ret;
		} catch (Exception e) {
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private boolean addQueues(String brokerName, String domain, String host,
			String port, String path, String queueName) throws Exception {
		String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port
				+ path;
		JMXServiceURL jmxUrl;
		JMXConnector connector = null;
		try {
			jmxUrl = new JMXServiceURL(url);
			connector = JMXConnectorFactory.connect(jmxUrl);

			MBeanServerConnection mbsc = connector.getMBeanServerConnection();
			String name = domain + ":brokerName=" + brokerName + ",type=Broker";
			ObjectName mbeanName = new ObjectName(name);
			BrokerViewMBean mBean = MBeanServerInvocationHandler
					.newProxyInstance(mbsc, mbeanName, BrokerViewMBean.class,
							true);
			mBean.addQueue(queueName);
		} catch (Exception e) {
			throw new Exception("队列名不存在");
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * 删除一个队列,会将数据一起删掉--!
	 */
	private void delQueue(String brokerName, String domain, String host,
			String port, String path, String queueName) throws Exception {
		if (queueName == null || "".equals(queueName)) {
			throw new Exception("队列名不存在");
		}
		String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port
				+ path;
		JMXServiceURL jmxUrl;
		JMXConnector connector = null;
		try {
			jmxUrl = new JMXServiceURL(url);
			connector = JMXConnectorFactory.connect(jmxUrl);

			MBeanServerConnection mbsc = connector.getMBeanServerConnection();
			String name = domain + ":brokerName=" + brokerName + ",type=Broker";
			ObjectName mbeanName = new ObjectName(name);
			BrokerViewMBean mBean = MBeanServerInvocationHandler
					.newProxyInstance(mbsc, mbeanName, BrokerViewMBean.class,
							true);
			mBean.removeQueue(queueName);
		} catch (Exception e) {
			throw new Exception("队列名不存在");
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
