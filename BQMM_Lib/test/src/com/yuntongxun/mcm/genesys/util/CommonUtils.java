package com.yuntongxun.mcm.genesys.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.genesyslab.platform.commons.collections.KeyValueCollection;
import com.genesyslab.platform.commons.collections.KeyValuePair;
import com.genesyslab.platform.commons.collections.ValueType;

public class CommonUtils {
	
	private static final Logger logger = LogManager.getLogger(CommonUtils.class);
	
	private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	
	public static String getUTC8DateTime(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		cal.add(java.util.Calendar.MILLISECOND, +(zoneOffset + dstOffset));
		return dateTimeFormat.format(cal.getTime());
	}
	
	public static boolean isEmpty(String str) {
		try {
			return str == null || str.length() == 0;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static synchronized Map<String, String> convertKvcToMap(
			final KeyValueCollection data) {

		Map<String, String> map = new HashMap<String, String>();
		if (data != null) {
			for (Object o : data) {
				KeyValuePair pair = (KeyValuePair) o;

				ValueType type = pair.getValueType();
				Object value = pair.getValue();

				if (value != null
						&& (type == ValueType.INT || type == ValueType.FLOAT
								|| type == ValueType.LONG
								|| type == ValueType.STRING || type == ValueType.WIDE_STRING)) {
					map.put(pair.getStringKey(), value.toString());

				}
			}
		}

		return map;
	}
	
	public static synchronized void setParam(Class<?> clazz, Object obj,
			Map<String, Map<String, String>> params) {
		if (params != null && params.isEmpty() == false) {
			Iterator<Entry<String, Map<String, String>>> iterator = params
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Map<String, String>> entry = iterator.next();
				String key = entry.getKey();
				Map<String, String> values = entry.getValue();
				String method = "set" + key;
				if (values != null && values.isEmpty() == false) {
					try {
						Method methodExtensions = clazz.getMethod(method,
								KeyValueCollection.class);
						methodExtensions.invoke(obj, convertMapToKvc(values));
					} catch (IllegalAccessException e) {
						logger.error(e.getMessage(), e);
					} catch (InvocationTargetException e) {
						logger.error(e.getMessage(), e);
					} catch (NoSuchMethodException e) {
						logger.warn(clazz.getSimpleName()
								+ " does not exist the method : " + method
								+ "(). ");
					}
				}
			}
		}
	}

	public static synchronized KeyValueCollection convertMapToKvc(
			Map<String, String> map) {
		KeyValueCollection collection = new KeyValueCollection();
		Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			collection.addString(entry.getKey(), entry.getValue());
		}
		return collection;
	}
	
	public static KeyValueCollection mapToKeyValueCollection(Map map) {
		if (map == null || map.isEmpty())
			return null;

		KeyValueCollection list = new KeyValueCollection();
		Set set = map.keySet();
		Iterator iter = set.iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			Object value = map.get(key);

			if (value == null)
				continue;

			list.addString(key.toString(), value.toString());
		}
		return list;
	}
	
	
}
