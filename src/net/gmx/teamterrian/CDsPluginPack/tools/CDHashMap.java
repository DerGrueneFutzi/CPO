package net.gmx.teamterrian.CDsPluginPack.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.entity.Player;

public class CDHashMap<K, V> extends HashMap<K, V>
{
	private static final long serialVersionUID = 7019909201564608954L;
	private Boolean isKeyPlayer = null,
					isValPlayer = null;
	
	public CDHashMap() { }
	public CDHashMap(Map<K, V> map)
	{
		super(map);
	}

	public boolean containsKey(Object o)
	{
		if(!checkType(o, true)) return super.containsKey(o);
		String name = ((Player) o).getName();
		for(Object key : this.keySet())
			if(key == null) continue;
			else if(((Player) key).getName().equals(name)) return true;
		return false;
	}
	public boolean containsValue(Object o)
	{
		if(!checkType(o, false)) return super.containsValue(o);
		String name = ((Player) o).getName();
		for(Object val : this.values())
			if(val == null) continue;
			else if(((Player) val).getName().equals(name)) return true;
		return false;
	}
	
	public V get(Object o)
	{
		if(!checkType(o, true)) return super.get(o);
		String name = ((Player) o).getName();
		for(Object key : this.keySet())
			if(key == null) continue;
			else if(((Player) key).getName().equals(name)) return super.get(key);
		return super.get(o);
	}	
	public V remove(Object o)
	{
		if(!containsKey(o)) return null;
		if(!checkType(o, true)) return super.remove(o);
		String name = ((Player) o).getName();
		for(Object key : new HashSet<K>(this.keySet()))
			if(key == null) continue;
			else if(((Player) key).getName().equals(name)) return super.remove(key);
		return null;
	}
	public V put(K k, V v)
	{
		if(!checkType(k, true)) return super.put(k, v);
		String name = ((Player) k).getName();
		V ret;
		for(Object key : new HashSet<K>(this.keySet()))
			if(key == null) continue;
			else if(((Player) key).getName().equals(name))
			{
				ret = get(key);
				remove(key);
				super.put(k, v);
				return ret;
			}
		return super.put(k, v);
	}

	private boolean checkType(Object o, boolean isKey)
	{
		if(o == null) return false;
		Boolean b = (isKey ? isKeyPlayer : isValPlayer);
		if(b != null) return b;
		if(isKey) isKeyPlayer = o instanceof Player;
		else isValPlayer = o instanceof Player;
		return (isKey ? isKeyPlayer : isValPlayer);
	}
}
