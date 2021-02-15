package com.oreillyauto.storepurchaseorder.remote.rest;

import java.util.ArrayList;

public class ClassInfo {

	Class interfaceClass;
	ArrayList<MethodInfo> alMethodInfo = new ArrayList();

	public ClassInfo(Class clazz) {
		this.interfaceClass = clazz;
	}

	public ArrayList<String> verify() {
		ArrayList<String> alErrors = new ArrayList();

		for (MethodInfo mi : alMethodInfo) {
			mi.verify(alErrors);
		}
		return alErrors;
	}

}
