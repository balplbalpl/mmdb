package com.mmdb.model.event;

import java.util.Map;

import com.mmdb.core.framework.neo4j.annotation.Space;

@Space("event")
public class Event {
	private String serial;
	private String duplicateSerial;
	private String ciCategory;
	private String ciid;
	private String ciHex;
	private String kpiCategory;
	private String kpi;
	private String kpiHex;
	private String kpiInstance;
	private String eventTitle;
	private String summary;
	private String status;
	private String severity;
	private Long firstOccurrence;
	private Long lastOccurrence;
	private Long closeTime;
	private String closeInfo;
	private String closeUid;
	private Long ackTime;
	private String ackInfo;
	private String ackUid;
	private Long tally;
	private String viewId;
	private String userName;
	private Map<String, String> data;
	
	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public String getDuplicateSerial() {
		return duplicateSerial;
	}
	public void setDuplicateSerial(String duplicateSerial) {
		this.duplicateSerial = duplicateSerial;
	}
	public String getCiCategory() {
		return ciCategory;
	}
	public void setCiCategory(String ciCategory) {
		this.ciCategory = ciCategory;
	}
	public String getCiid() {
		return ciid;
	}
	public void setCiid(String ciid) {
		this.ciid = ciid;
	}
	public String getKpiCategory() {
		return kpiCategory;
	}
	public void setKpiCategory(String kpiCategory) {
		this.kpiCategory = kpiCategory;
	}
	public String getKpi() {
		return kpi;
	}
	public void setKpi(String kpi) {
		this.kpi = kpi;
	}
	public String getKpiInstance() {
		return kpiInstance;
	}
	public void setKpiInstance(String kpiInstance) {
		this.kpiInstance = kpiInstance;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public Long getFirstOccurrence() {
		return firstOccurrence;
	}
	public void setFirstOccurrence(Long firstOccurrence) {
		this.firstOccurrence = firstOccurrence;
	}
	public Long getLastOccurrence() {
		return lastOccurrence;
	}
	public void setLastOccurrence(Long lastOccurrence) {
		this.lastOccurrence = lastOccurrence;
	}
	public Long getCloseTime() {
		return closeTime;
	}
	public void setCloseTime(Long closeTime) {
		this.closeTime = closeTime;
	}
	public String getCloseInfo() {
		return closeInfo;
	}
	public void setCloseInfo(String closeInfo) {
		this.closeInfo = closeInfo;
	}
	public Long getAckTime() {
		return ackTime;
	}
	public void setAckTime(Long ackTime) {
		this.ackTime = ackTime;
	}
	public String getAckInfo() {
		return ackInfo;
	}
	public void setAckInfo(String ackInfo) {
		this.ackInfo = ackInfo;
	}
	public Long getTally() {
		return tally;
	}
	public void setTally(Long tally) {
		this.tally = tally;
	}
	public Map<String, String> getData() {
		return data;
	}
	public void setData(Map<String, String> data) {
		this.data = data;
	}
	public String getEventTitle() {
		return eventTitle;
	}
	public void setEventTitle(String eventTitle) {
		this.eventTitle = eventTitle;
	}
	public String getCloseUid() {
		return closeUid;
	}
	public void setCloseUid(String closeUid) {
		this.closeUid = closeUid;
	}
	public String getAckUid() {
		return ackUid;
	}
	public void setAckUid(String ackUid) {
		this.ackUid = ackUid;
	}
	public String getCiHex() {
		return ciHex;
	}
	public void setCiHex(String ciHex) {
		this.ciHex = ciHex;
	}
	public String getKpiHex() {
		return kpiHex;
	}
	public void setKpiHex(String kpiHex) {
		this.kpiHex = kpiHex;
	}
	public String getViewId() {
		return viewId;
	}
	public void setViewId(String viewId) {
		this.viewId = viewId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
