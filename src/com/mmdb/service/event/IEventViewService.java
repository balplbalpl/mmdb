package com.mmdb.service.event;

import java.util.List;
import java.util.Map;

import com.mmdb.model.bean.User;
import com.mmdb.model.event.EventView;

public interface IEventViewService {
	public List<EventView> getAll(User user) throws Exception;
	
	public EventView getById(String id, User user) throws Exception;
	
	public EventView getByName(String name, User user) throws Exception;
	
	public EventView save(EventView ev, User user) throws Exception;
	
	public EventView update(EventView ev, User user) throws Exception;
	
	public void delete(List<EventView> evs) throws Exception;
	
	public void deleteAll(User user) throws Exception;
	
	public List<Map<String, String>> getSeverityMap() throws Exception;
	
	public List<Map<String, String>> getStatusMap() throws Exception;
	
	public Map<String, String> getTitleMap(String type, String dataType) throws Exception;
}
