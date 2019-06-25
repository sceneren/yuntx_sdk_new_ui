package com.yuntongxun.mcm.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {
	public static Gson gson = new GsonBuilder().serializeNulls().create();

	public static Object jsonToObj(String json, Class<?> c) {
		return gson.fromJson(json, c);
	}

	public static String bean2json(Object bean) {
		return gson.toJson(bean);
	}
	
	public static String toJson(Object obj){
		return gson.toJson(obj);
	}
}
