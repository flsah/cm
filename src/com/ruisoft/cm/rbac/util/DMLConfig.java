package com.ruisoft.cm.rbac.util;

import java.util.Map;

import com.ruisoft.cm.rbac.entity.AddEntity;
import com.ruisoft.cm.rbac.entity.DeleteEntity;
import com.ruisoft.cm.rbac.entity.QueryEntity;
import com.ruisoft.cm.rbac.entity.UpdateEntity;

public class DMLConfig {
	public static Map<String, QueryEntity> select = null;

	public void setSelect(Map<String, QueryEntity> select) {
		DMLConfig.select = select;
	}
	
	public static Map<String, AddEntity> add = null;

	public void setAdd(Map<String, AddEntity> add) {
		DMLConfig.add = add;
	}
	
	public static Map<String, UpdateEntity> update = null;

	public void setUpdate(Map<String, UpdateEntity> update) {
		DMLConfig.update = update;
	}
	
	public static Map<String, DeleteEntity> delete = null;

	public void setDelete(Map<String, DeleteEntity> delete) {
		DMLConfig.delete = delete;
	}
}
