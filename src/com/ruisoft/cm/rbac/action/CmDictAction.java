package com.ruisoft.cm.rbac.action;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ruisoft.cm.rbac.dao.BaseDAO;
import com.ruisoft.cm.rbac.util.DMLConfig;
import com.ruisoft.cm.rbac.util.JSONMap;

@Controller
@RequestMapping("/rbac/cmDict.do")
public class CmDictAction extends BaseAction {
	private final static Logger LOG = Logger.getLogger(CmDictAction.class);
	
	private static final String SQL_DEF_QUERY = "dict.def.query";
	
	private static final String SQL_ITEM_QUERY = "dict.item.query";
	
	private static final String SQL_DEF_ADD = "dict.def.add";
	
	private static final String SQL_DEF_UPDATE = "dict.def.update";
	
	private static final String SQL_DEF_DELETE = "dict.def.delete";
	
	private static final String SQL_ITEM_DELETE = "dict.item.delete";
	
	private static final String RESPONSE_STR = "{\"Rows\":{dict},\"Total\":\"{total}\"}";
	
	@Resource
	private BaseDAO baseDAO = null;
	
	public void setBaseDAO(BaseDAO baseDAO) {
		this.baseDAO = baseDAO;
	}

	@RequestMapping(params = "m=q")
	public String query(HttpServletRequest request, HttpServletResponse response) {
		LOG.debug("查询字典定义");
		LOG.debug(request.getQueryString());
		try {
			int page = Integer.valueOf(request.getParameter("p"));
			int pageSize = Integer.valueOf(request.getParameter("cp"));

			JSONObject cond = getReqData(request);
			LOG.debug("查询条件：" + cond);

			List<JSONObject> dictDefs = baseDAO.queryForPage(SQL_DEF_QUERY,
					cond, page, pageSize);

			String c = dictDefs.get(0).getString("total");
			dictDefs.remove(0);

			String dicts = RESPONSE_STR.replaceFirst("\\{dict\\}",
					new JSONArray(dictDefs).toString()).replaceFirst(
					"\\{total\\}", c);
			LOG.debug(dicts);
			response(request, response, dicts);
		} catch (Exception e) {
			LOG.error("查询字典定义发生错误", e);
		}
		return null;
	}
	
	/**
	 * 字典数据项加载缓存
	 */
	protected final static HashMap<String, String> DICT_ITEM_CACHE = new HashMap<String, String>();
	
	@RequestMapping(params = "m=gi")
	public String getDictItem(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			// 请求JSON对象
			JSONObject reqObj = getReqData(request);
			// 要转换的字典字义名称
			String dictName = reqObj.getString("d");

			if (DICT_ITEM_CACHE.containsKey(dictName)) { // 查找缓存
				response(request, response, DICT_ITEM_CACHE.get(dictName));
			} else {
				List<JSONObject> items = baseDAO.query(reqObj, SQL_ITEM_QUERY);
				JSONObject item = new JSONObject();
				for (JSONObject i : items) {
					item.put(i.getString("value"), i.toString());
				}
				
				String param = item.toString();
				LOG.debug(param);
				
				if (!items.isEmpty())
					DICT_ITEM_CACHE.put(dictName, param);
				
				response(request, response, param);
			}
		} catch (Exception e) {
			LOG.error("获取字典项定义发生错误", e);
		}
		return null;
	}
	
	@RequestMapping(params = "m=a")
	public String addDictDef(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			JSONObject reqData = getReqData(request);
			JSONMap<String, Object> json = new JSONMap<String, Object>(
					JSONMap.TYPE.OBJECT);
			int ex = baseDAO.count(DMLConfig.select.get(SQL_DEF_QUERY).getSql()
					.concat(" WHERE CODE='").concat(reqData.getString("code"))
					.concat("'"));
			int r = 0xFF;
			if (ex > 0) {
				r = -1;
			} else {
				r = baseDAO.add(reqData, SQL_DEF_ADD);
			}
			json.put("result", r);
			response(request, response, json);
		} catch (Exception e) {
			LOG.error("新增字典定义发生错误", e);
		}
		return null;
	}
	
	@RequestMapping(params = "m=u")
	public String updateDictDef(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			int r = baseDAO.update(getReqData(request), SQL_DEF_UPDATE);
			JSONMap<String, Object> json = new JSONMap<String, Object>(
					JSONMap.TYPE.OBJECT);
			json.put("result", r);
			response(request, response, json);
		} catch (Exception e) {
			LOG.error("更新字典定义发生错误", e);
		}
		return null;
	}
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	@RequestMapping(params = "m=d")
	public String deleteDictDef(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			JSONObject cond = getReqData(request);
			int r = baseDAO.delete("{\"def_id\":\"" + cond.getString("id") + "\"}", SQL_ITEM_DELETE);
			r = baseDAO.delete(cond, SQL_DEF_DELETE);
			JSONMap<String, Object> json = new JSONMap<String, Object>(
					JSONMap.TYPE.OBJECT);
			json.put("result", r);
			response(request, response, json);
		} catch (Exception e) {
			LOG.error("删除字典定义发生错误", e);
		}
		return null;
	}
	
	public String queryDictItemByTree(HttpServletRequest request,
			HttpServletResponse response) {
		
		return null;
	}
}
