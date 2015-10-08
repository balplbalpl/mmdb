package com.mmdb.model.db.storage;

import java.util.List;

import org.springframework.stereotype.Component;

import com.mmdb.core.framework.neo4j.storage.NodeStorage;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.db.DataBase;

/**
 * 数据库配置 - 存储
 *
 * @author XIE
 */
@Component("dataBaseStorage")
public class DataBaseStorage extends NodeStorage<DataBase> {
    private Log log = LogFactory.getLogger("DataBaseStorage");

    /**
     * 根据id获取db配置
     *
     * @param id 分类id（当前分类中唯一）
     * @return
     * @throws Exception
     */
    public DataBase getById(String id) throws Exception {
        List<DataBase> list = this.getByProperty("id", id);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            log.eLog("数据库[" + id + "]不唯一");
            throw new Exception("数据库[" + id + "]不唯一");
        } else {
            return null;
        }
    }
}
