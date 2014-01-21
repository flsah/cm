package com.ruisoft.cm.rbac.util;

import java.util.UUID;

public class KeyGenerator {
	public static final String getUUID() {
		return UUID.randomUUID().toString();
	}
	
	public static final String get32UUID() {
		return getUUID().replaceAll("-", "");
	}
}
