package com.ruisoft.cm.rbac.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import com.ruisoft.cm.rbac.entity.AddEntity;
import com.ruisoft.cm.rbac.entity.DMLEntity;
import com.ruisoft.cm.rbac.entity.DeleteEntity;
import com.ruisoft.cm.rbac.entity.QueryEntity;
import com.ruisoft.cm.rbac.entity.UpdateEntity;
import com.ruisoft.cm.rbac.util.KeyGenerator;
import com.ruisoft.cm.rbac.util.SysCache;

public class BaseDAO {
	private static final Logger LOG = Logger.getLogger(BaseDAO.class);
	
	protected JdbcTemplate tpl = null;
	
	public void setTpl(JdbcTemplate tpl) {
		this.tpl = tpl;
	}
	
	@Resource
	protected PlatformTransactionManager transactionManager = null;
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	@Resource
	protected HttpServletRequest request = null;
	
	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	@Resource
	protected KeyGenerator keyGenerator = null;

	public KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	public void setKeyGenerator(KeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}
	
	private QueryEntity qEntity = null;
	private AddEntity aEntity = null;
	private UpdateEntity uEntity = null;
	private DeleteEntity dEntity = null;

	public List<JSONObject> query(JSONObject json, QueryEntity query)
			throws JSONException {
		String sql = query.getPreparedSQL();
		LOG.debug("执行查询:" + sql);
		Object[] info = getPreparedParam(json, query);
		
		if (tpl == null)
			tpl = new JdbcTemplate(getDataSource());
		
		if (QueryEntity.COND.equals(query.getDmlType())) {
			return tpl.query(sql, new JSONMapper());
		}
		return tpl.query(sql, info, new JSONMapper());
	}
	
	public List<JSONObject> query(JSONObject json, String id)
			throws JSONException {
		return query(json, SysCache.get(id, qEntity));
	}
	
	public List<JSONObject> query(String str, QueryEntity query)
			throws JSONException {
		return query(new JSONObject(str), query);
	}
	
	public List<JSONObject> query(String str, String id)
			throws JSONException {
		return query(new JSONObject(str), id);
	}
	
	public List<JSONObject> queryForPage(QueryEntity query, JSONObject qCond,
			int page, int pageSize) throws Exception {
		if (page < 1 || pageSize < 1)
			throw new Exception("分页信息有误");

		String sql = null;
		if (DMLEntity.COND.equals(query.getDmlType()))
			sql = query.getSql(qCond);
		else
			sql = query.getPreparedSQL();
		
		LOG.debug(sql);

		final int rStart = (page - 1) * pageSize;
		final int rEnd = rStart + pageSize;
		if (tpl == null)
			tpl = new JdbcTemplate(getDataSource());
		
		JSONPagerMapper rowMapper = new JSONPagerMapper();
		rowMapper.setrStart(rStart);
		rowMapper.setrEnd(rEnd);
		List<JSONObject> results = null;
		if (!DMLEntity.COND.equals(query.getDmlType())) {
			Object[] info = getPreparedParam(new JSONObject(qCond), query);
			results = tpl.query(sql, info, rowMapper);
		} else { 
			results = tpl.query(sql, rowMapper);
		}

		int c = count(sql);
		JSONObject countJ = new JSONObject("{\"total\":\"" + c + "\"}");
		results.add(0, countJ);

		return results;
	}
	
	public List<JSONObject> queryForPage(QueryEntity query, String qCond,
			int page, int pageSize) throws Exception {
		return queryForPage(query, new JSONObject(qCond), page, pageSize);
	}
	
	public List<JSONObject> queryForPage(String entityName, String qCond,
			int page, int pageSize) throws Exception {
		return queryForPage(SysCache.get(entityName, qEntity), new JSONObject(
				qCond), page, pageSize);
	}
	
	public List<JSONObject> queryForPage(String entityName, JSONObject qCond,
			int page, int pageSize) throws Exception {
		return queryForPage(SysCache.get(entityName, qEntity), qCond, page, pageSize);
	}
	
	public int count(String sql) {
		return tpl.queryForInt("SELECT COUNT(1) NUM FROM (".concat(sql).concat(") TC"));
	}
	
	public int count(String sql, Object... args) {
		return tpl.queryForInt("SELECT COUNT(1) NUM FROM (".concat(sql).concat(") TC"), args);
	}
	
	public int add(JSONObject values, AddEntity add) throws Exception {
		String sql = add.getPreparedSQL();
		LOG.debug("执行新增：" + sql);
		Object[] params = getPreparedParam(values, add);

		if (tpl == null)
			tpl = new JdbcTemplate(getDataSource());

		return tpl.update(sql, params);
	}
	
	public int add(JSONObject values, String id) throws Exception {
		return add(values, SysCache.get(id, aEntity));
	}
	
	public int add(String values, AddEntity add) throws Exception {
		return add(new JSONObject(values), add);
	}
	
	public int add(String values, String id) throws Exception {
		return add(new JSONObject(values), SysCache.get(id, aEntity));
	}
	
	public int update(JSONObject values, UpdateEntity update) throws Exception {
		String sql = update.getPreparedSQL();
		LOG.debug("执行更新：" + sql);
		Object[] params = getPreparedParam(values, update);

		if (tpl == null)
			tpl = new JdbcTemplate(getDataSource());

		return tpl.update(sql, params);
	}
	
	public int update(JSONObject values, String id) throws Exception {
		return update(values, SysCache.get(id, uEntity));
	}
	
	public int update(String values, UpdateEntity update) throws Exception {
		return update(new JSONObject(values), update);
	}
	
	public int update(String values, String id) throws Exception {
		return update(new JSONObject(values), SysCache.get(id, uEntity));
	}
	
	public int delete(JSONObject values, DeleteEntity delete) throws Exception {
		String sql = null;
		if (tpl == null)
			tpl = new JdbcTemplate(getDataSource());
		
		if (DMLEntity.COND.equals(delete.getDmlType())) {
			sql = delete.getSql(values);
			LOG.debug("执行删除：" + sql);
			return tpl.update(sql);
		} else {
			sql = delete.getPreparedSQL();
			LOG.debug("执行删除：" + sql);
			Object[] params = getPreparedParam(values, delete);
			return tpl.update(sql, params);
		}
	}
	
	public int delete(JSONObject values, String id) throws Exception {
		return delete(values, SysCache.get(id, dEntity));
	}
	
	public int delete(String values, DeleteEntity delete) throws Exception {
		return delete(new JSONObject(values), delete);
	}
	
	public int delete(String values, String id) throws Exception {
		return delete(new JSONObject(values), SysCache.get(id, dEntity));
	}
	
	protected Object[] getPreparedParam(JSONObject json, DMLEntity entity)
			throws JSONException {
		Map<String, String> cond = entity.getConditions();
		if (cond == null)
			return new Object[0];
		
		Object[] params = new Object[cond.size()];
		int i = 0;
		String datatype = null;
		Object val = null;
		for (String k : cond.keySet()) {
			datatype = cond.get(k);
			
			if (k.startsWith("#"))
				val = parseSharp(k.substring(1));
			else if (json.isNull(k))
				val = null;
			else if ("str".equals(datatype))
				val = json.getString(k);
			else if ("int".equals(datatype))
				val = json.getInt(k);
			else if ("double".equals(datatype))
				val = json.getDouble(k);
			else if ("long".equals(datatype))
				val = json.getLong(k);
			else if ("bool".equals(datatype))
				val = json.getBoolean(k);
			
			params[i++] = val;
		}

		return params;
	}
	
	private Object parseSharp(String key) {
		if (key.equals("uuid")) {
			return KeyGenerator.getUUID();
		} else if (key.equals("uuid32")) {
			return KeyGenerator.get32UUID();
		} else {
			// 生成主键
			return keyGenerator.getKeyByRule(key);
		}
	}
	
	protected DataSource getDataSource() {
		return (DataSource) new XmlBeanFactory(new ClassPathResource(
				"/applicationContext.xml")).getBean("dataSource");
	}
	
	public class JSONMapper implements RowMapper<JSONObject> {
		@Override
		public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
			ResultSetMetaData meta = rs.getMetaData();
			int i = meta.getColumnCount();
			String col = null;
			JSONObject json = new JSONObject();
			for (int j = 1; j <= i; j++) {
				col = meta.getColumnLabel(j);
				try {
					json.put(col.toLowerCase(),
							rs.getString(col));
				} catch (JSONException e) {
					throw new SQLException(e);
				}
			}

			return json;
		}
	}
	
	public class JSONPagerMapper implements RowMapper<JSONObject> {
		private int rStart = 0;
		
		private int rEnd = 0;
		
		public int getrStart() {
			return rStart;
		}

		public void setrStart(int rStart) {
			this.rStart = rStart;
		}

		public int getrEnd() {
			return rEnd;
		}

		public void setrEnd(int rEnd) {
			this.rEnd = rEnd;
		}

		@Override
		public JSONObject mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			if (rStart > 1 && rowNum == 1) {
				rs.absolute(rStart);
			}

			ResultSetMetaData meta = rs.getMetaData();
			int i = meta.getColumnCount();
			String col = null, val = null;
			JSONObject json = new JSONObject();
			for (int j = 1; j <= i; j++) {
				col = meta.getColumnLabel(j);
				val = rs.getString(col);

				try {
					json.put(col.toLowerCase(), (val == null ? "null" : val));
				} catch (JSONException e) {
					throw new SQLException(e);
				}
			}
			
			if (rowNum + 1 == rEnd) {
				rs.afterLast();
			}

			return json;
		}
	}
}

