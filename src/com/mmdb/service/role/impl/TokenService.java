package com.mmdb.service.role.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.bean.VitiationToken;
import com.mmdb.model.role.IRoleDao;
import com.mmdb.service.role.ITokenService;

@Service
public class TokenService implements ITokenService {

	@Autowired
	private IRoleDao dao;
	// private IRoleDao dao = new RoleDao();

	private Log log = LogFactory.getLogger("TokenService");

	@Override
	public boolean deleteTokenByDate(long time) {
		log.dLog("清除Token!");
		return dao.deleteObject("delete tb_token_info where `createtime`<="
				+ time);
	}

	@Override
	public boolean saveVitiationToken(VitiationToken token) {
		String sql = "insert into tb_token_info(`token`,`createtime`) values('"
				+ token.getToken() + "'," + token.getCreateTime() + ")";
		log.dLog("注销的Token: " + token.getToken());
		return dao.saveObject(sql);
	}

	@Override
	public VitiationToken getVitiationToken(String token) {
		VitiationToken vToken = null;
		ResultSet rs = dao
				.getObjectById("select * from tb_token_info where `token`='"
						+ token + "'");
		try {
			while (rs.next()) {
				String vtoken = rs.getString("token");
				long time = rs.getLong("createtime");
				vToken = new VitiationToken(vtoken, time);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vToken;
	}

}
