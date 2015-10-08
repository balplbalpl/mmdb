package com.mmdb.service.role;

import java.util.List;

import com.mmdb.model.bean.Dept;

public interface IDeptService {
	
	public boolean saveDept(Dept dept);
	
	public boolean deleteDept(String deptName);
	
	public boolean updateDept(Dept dept);
	
	public List<Dept> getAllDept();
	
	public Dept getDeptByName(String name);

}
