package com.mmdb.service.role;

import java.util.List;

import com.mmdb.model.bean.Company;

public interface ICompanyService {
	
	public boolean saveCompany(Company company);
	
	public boolean deleteCompany(String companyName);
	
	public boolean updateCompany(Company company);
	
	public List<Company> getAllCompany();
	
	public Company getCompanyByName(String name);

}
