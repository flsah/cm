package com.ruisoft.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.ruisoft.cm.rbac.dao.BaseDAO;
import com.ruisoft.cm.rbac.entity.QueryEntity;

public class ParamGenerator {
	private static final Logger LOG = Logger.getLogger(ParamGenerator.class);
	
	private Map<String, ParamEntity> config = null;

	public Map<String, ParamEntity> getConfig() {
		return config;
	}

	public void setConfig(Map<String, ParamEntity> config) {
		this.config = config;
	}
	
	@Resource
	private BaseDAO baseDAO = null;
	
	public void setBaseDAO(BaseDAO baseDAO) {
		this.baseDAO = baseDAO;
	}

	private static final HashMap<String, String> cache = new HashMap<String, String>();
	
	public String generate(String id, JSONObject args) {
		if (!config.containsKey(id))
			throw new IllegalArgumentException("key=[".concat(id).concat("]未定义"));
		
		ParamEntity entity = config.get(id);
		
		try {
			if (entity instanceof SQLParamEntity) {
				return generate((SQLParamEntity) entity, args);
			} else if (entity instanceof ListParamEntity) {
				return generate((ListParamEntity) entity);
			}
		} catch (JSONException e) {
			
		}
		
		return null;
	}
	
	public String generate(SQLParamEntity entity, JSONObject args) throws JSONException {
		if (args == null || args.isNull("d")) {
			LOG.error("未提供目标参数定义名称");
			return null;
		}
		
		String dict = args.getString("d");
		// fetch from cache if it exists
		if (cache.containsKey(dict))
			return cache.get(dict);
		
		QueryEntity qEntity = entity.getSql();
		List<JSONObject> params = baseDAO.query(args, qEntity);
		
		
		return null;
	}
	
	public String generate(ListParamEntity entity) {
		return null;
	}
}
