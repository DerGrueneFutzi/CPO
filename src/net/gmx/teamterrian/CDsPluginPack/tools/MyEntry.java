package net.gmx.teamterrian.CDsPluginPack.tools;

import java.util.Map;

public class MyEntry<K, V> implements Map.Entry<K, V>
{
	private final K key;
	private V value;
	
	public MyEntry(K key, V value)
	{
		this.key=key;
		this.value=value;
	}
	
	public K getKey()
	{
		return key;
	}
	public V getValue()
	{
		return value;
	}
	public V setValue(V value)
	{
		V old = this.value;
		this.value = value;
		return old;
	}
}
