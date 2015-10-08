package com.mmdb.model.bean;


import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class Page<T> implements Serializable {
	
	private Integer totalCount;

	private List<T> datas;

	private Integer count;
	
	private Integer start;
	
	private Integer pageSize;
	
	public Page(List<T> datas) {
		super();
		this.datas = datas;
		this.count = datas.size();
	}
	
	public Page(){
		super();
	}
	public Page(List<T> datas,Integer count) {
		super();
		this.datas = datas;
		this.count = count;
	}


	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public List<T> getDatas() {
		return datas;
	}

	public void setDatas(List<T> datas) {
		this.datas = datas;
	}

	
	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	
	
}
