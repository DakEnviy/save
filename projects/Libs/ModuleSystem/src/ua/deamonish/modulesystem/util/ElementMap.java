package ua.deamonish.modulesystem.util;

public class ElementMap<K,V> {
	protected K key;
	protected V value;

	protected ElementMap next;
	protected ElementMap back;

	public ElementMap (K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
}
}
