package com.mmdb.service.event.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.model.bean.User;
import com.mmdb.model.event.EventView;
import com.mmdb.model.event.storage.EventStorage;
import com.mmdb.model.event.storage.EventViewStorage;
import com.mmdb.service.event.IEventViewService;

@Component("eventViewService")
public class EventViewServiceImpl implements IEventViewService{
	@Autowired
	private EventViewStorage eventViewStorage;
	@Autowired
	private EventStorage eventStorage;
	
	@Override
	public List<EventView> getAll(User user) throws Exception {
		return eventViewStorage.getAll(user);
	}

	@Override
	public EventView getById(String id, User user) throws Exception {
		return eventViewStorage.getById(id, user);
	}
	
	@Override
	public EventView getByName(String name, User user) throws Exception {
		return eventViewStorage.getByName(name, user);
	}

	@Override
	public EventView save(EventView ev, User user) throws Exception {
		return eventViewStorage.save(ev, user);
	}

	@Override
	public EventView update(EventView ev, User user) throws Exception {
		return eventViewStorage.update(ev, user);
	}

	@Override
	public void delete(List<EventView> evs) throws Exception {
		eventViewStorage.delete(evs);
	}

	@Override
	public void deleteAll(User user) throws Exception {
		eventViewStorage.deleteAll(user);
	}

	@Override
	public List<Map<String, String>> getSeverityMap() throws Exception {
		return eventStorage.getSeverityMap();
	}

	@Override
	public List<Map<String, String>> getStatusMap() throws Exception {
		return eventStorage.getStatusMap();
	}

	@Override
	public Map<String, String> getTitleMap(String type, String dataType) throws Exception {
		return eventStorage.getTitleMap(type, dataType);
	}

}
