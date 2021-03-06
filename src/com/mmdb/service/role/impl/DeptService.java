package com.mmdb.service.role.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.model.bean.Dept;
import com.mmdb.model.role.IRoleDao;
import com.mmdb.service.role.IDeptService;

@Service
public class DeptService implements IDeptService {
	@Autowired
	private IRoleDao roleDao;

	@Override
	public boolean saveDept(Dept dept) {
		boolean flag = true;
		flag = roleDao
				.saveObject("insert into tb_portal_dept(companyname,deptname,deptdesc) values('"
						+ dept.getCompanyName()
						+ "','"
						+ dept.getDeptName()
						+ "','" + dept.getDeptDesc() + "')");
		return flag;

	}

	@Override
	public boolean deleteDept(String deptName) {
		boolean flag = true;
		flag = roleDao
				.deleteObject("delete from tb_portal_dept where deptname='"
						+ deptName + "'");
		return flag;
	}

	@Override
	public boolean updateDept(Dept dept) {
		boolean flag = true;
		flag = roleDao.updateObject("update tb_portal_dept set companyname='"
				+ dept.getCompanyName() + "',deptdesc='" + dept.getDeptDesc()
				+ "' where deptname='" + dept.getDeptName() + "'");
		return flag;
	}

	@Override
	public List<Dept> getAllDept() {
		List<Dept> list = new ArrayList<Dept>();
		ResultSet rs = roleDao
				.getAll("select * from tb_portal_dept order by companyname");
		try {
			while (rs.next()) {
				String companyName = rs.getString("companyname");
				String deptName = rs.getString("deptname");
				String deptDesc = rs.getString("deptdesc");
				Dept dept = new Dept();
				dept.setCompanyName(companyName);
				dept.setDeptName(deptName);
				dept.setDeptDesc(deptDesc);
				list.add(dept);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public Dept getDeptByName(String name) {
		Dept dept = null;
		ResultSet rs = roleDao
				.getAll("select * from tb_portal_dept where deptname='" + name
						+ "'");
		try {
			while (rs.next()) {
				String companyName = rs.getString("companyname");
				String deptName = rs.getString("deptname");
				String deptDesc = rs.getString("deptdesc");
				dept = new Dept();
				dept.setCompanyName(companyName);
				dept.setDeptName(deptName);
				dept.setDeptDesc(deptDesc);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dept;
	}

}
