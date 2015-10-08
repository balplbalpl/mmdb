package com.mmdb.service.role;

import com.mmdb.model.bean.VitiationToken;

public interface ITokenService {
	
	/**
	 * 清除以前的Token
	 * @param time
	 * @return
	 */
	public boolean deleteTokenByDate(long time);
	
	/**
	 * 保存注销的Token
	 * @param token
	 * @return
	 */
	public boolean saveVitiationToken(VitiationToken token);
	
	/**
	 * 获取无效的Token
	 * @param token
	 * @return
	 */
	public VitiationToken getVitiationToken(String token);

}
