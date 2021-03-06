package com.mmdb.service.role.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.model.bean.Company;
import com.mmdb.model.role.IRoleDao;
import com.mmdb.service.role.ICompanyService;

@Service
public class CompanyService implements ICompanyService {
	@Autowired
	private IRoleDao roleDao;

	// private IRoleDao roleDao = new RoleDao();

	@Override
	public boolean saveCompany(Company company) {
		boolean flag = true;
		flag = roleDao
				.saveObject("insert into tb_portal_company(companyname,companydesc) values('"
						+ company.getCompanyName()
						+ "','"
						+ company.getCompanyDesc() + "')");
		return flag;
	}

	@Override
	public boolean deleteCompany(String companyName) {
		boolean flag = true;
		flag = roleDao
				.saveObject("delete from tb_portal_company where companyname='"
						+ companyName + "'");
		return flag;
	}

	@Override
	public List<Company> getAllCompany() {
		List<Company> list = new ArrayList<Company>();
		ResultSet rs = roleDao.getAll("select * from tb_portal_company");
		try {
			while (rs.next()) {
				String companyName = rs.getString("companyname");
				String companyDesc = rs.getString("companydesc");
				Company company = new Company();
				company.setCompanyName(companyName);
				company.setCompanyDesc(companyDesc);
				list.add(company);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public Company getCompanyByName(String name) {
		ResultSet rs = roleDao
				.getAll("select * from tb_portal_company where companyname='"
						+ name + "'");
		Company company = null;
		try {
			while (rs.next()) {
				String companyName = rs.getString("companyname");
				String companyDesc = rs.getString("companydesc");
				company = new Company();
				company.setCompanyName(companyName);
				company.setCompanyDesc(companyDesc);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return company;
	}

	@Override
	public boolean updateCompany(Company company) {
		boolean flag = true;
		flag = roleDao
				.updateObject("update tb_portal_company set companydesc='"
						+ company.getCompanyDesc() + "' where companyname='"
						+ company.getCompanyName() + "'");
		return flag;
	}

}
