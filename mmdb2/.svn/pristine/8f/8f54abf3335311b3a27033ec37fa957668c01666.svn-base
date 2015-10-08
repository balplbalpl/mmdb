package com.mmdb.model.message;

import org.springframework.context.ApplicationEvent;

import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.model.info.CiInformation;

/**
 * 配置项数据的消息对象
 * 
 * @author XIE
 * 
 */
public class CiInfoMsg extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
	private CiInformation Information;
	/**
	 * 分类数据的增删改
	 */
	private ACTION action;
	private Boolean thread = true;

	public CiInfoMsg(CiInformation information) {
		super("");
		this.setInformation(information);
	}

	public Boolean getThread() {
		return thread;
	}

	public void setThread(Boolean thread) {
		this.thread = thread;
	}

	private void send() {
		SpringContextUtil.publishEvent(this, this.getThread());
	}

	public void ADD() {
		this.setAction(ACTION.ADD);
		this.send();
	}

	public void DLT() {
		this.setAction(ACTION.DLT);
		this.send();
	}

	public void UPD() {
		this.setAction(ACTION.UPD);
		this.send();
	}

	public void send(ACTION action) {
		this.setAction(action);
		this.send();
	}

	public CiInformation getInformation() {
		return Information;
	}

	public void setInformation(CiInformation information) {
		Information = information;
	}

	public ACTION getAction() {
		return action;
	}

	public void setAction(ACTION action) {
		this.action = action;
	}

	/**
	 * 枚举增删改
	 * 
	 * @author XIE
	 * 
	 */
	public static enum ACTION {
		ADD, DLT, UPD
	}
}
