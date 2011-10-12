package edu.arizona.cs.learn.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class DataMap {

	/** Bidirectional map for propositions and relations to an integer value */
	private static BiMap<String,Integer> _map = HashBiMap.create(200);
	
	
	/**
	 * If the key exists return the mapped value, and if it does
	 * not exist in the map, add it and return the mapped value.
	 * @param key
	 * @return
	 */
	public static Integer findOrAdd(String key) { 
		Integer result = _map.get(key);
		if (result == null) { 
			result = _map.size();
			_map.put(key, result);
		}
		return result;
	}
	
	/**
	 * Reverse lookup of the key from the integer value.
	 * @param value
	 * @return
	 */
	public static String getKey(Integer value) { 
		String key = _map.inverse().get(value);
		if (key == null) {
			throw new RuntimeException("Reverse lookup failed for integer: " + value);
		}
		return key;
	}
}
