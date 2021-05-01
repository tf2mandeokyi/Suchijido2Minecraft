package com.mndk.kvm2m.core.util;

import java.util.HashMap;
import java.util.Set;

@SuppressWarnings("serial")
public class KeyRestrictedMap<K, V> extends HashMap<K, V> {

	private Set<K> allowedKeys;
	
	public KeyRestrictedMap(Set<K> allowedKeys) {
		super();
		this.allowedKeys = allowedKeys;
	}
	
	@Override
	public V put(K key, V value) {
		if(!allowedKeys.contains(key)) throw new IllegalArgumentException("Unpermitted key: " + key);
		return super.put(key, value);
	}
	
	@Override
	public V get(Object key) {
		if(!allowedKeys.contains(key)) throw new IllegalArgumentException("Unpermitted key: " + key);
		return super.get(key);
	}
	
	public Set<K> getAllowedKeys() {
		return this.allowedKeys;
	}
	
}
