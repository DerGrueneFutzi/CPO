package net.gmx.teamterrian.CDsPluginPack.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.entity.Player;

public class CDHashMap<K, V> extends HashMap<K, V>
{
	private static final long serialVersionUID = 7019909201564608954L;
	
	public CDHashMap() { }
	public CDHashMap(Map<K, V> map)
	{
		super(map);
	}

	public boolean containsKey(Object o)
	{
		if(o == null || !(o instanceof Player)) return super.containsKey(o);
		String name = ((Player) o).getName();
		for(Object key : this.keySet())
			if(key == null) continue;
			else if(!(key instanceof Player)) return super.containsKey(key);
			else break;
		for(Object key : this.keySet())
			if(key == null) continue;
			else if(((Player) key).getName().equals(name)) return true;
		return false;
	}
	public boolean containsValue(Object o)
	{
		if(o == null || !(o instanceof Player)) return super.containsValue(o);
		String name = ((Player) o).getName();
		for(Object val : this.values())
			if(val == null) continue;
			else if(!(val instanceof Player)) return super.containsValue(o);
			else break;
		for(Object val : this.values())
			if(val == null) continue;
			else if(((Player) val).getName().equals(name)) return true;
		return false;
	}
	
	public V get(Object o)
	{
		if(o == null || !(o instanceof Player)) return super.get(o);
		String name = ((Player) o).getName();
		for(Object key : this.keySet())
			if(key == null) continue;
			else if(!(key instanceof Player)) return super.get(o);
			else break;
		for(Object key : this.keySet())
			if(key == null) continue;
			else if(((Player) key).getName().equals(name)) return super.get(key);
		return super.get(o);
	}	
	public V remove(Object o)
	{
		if(!containsKey(o)) return null;
		if(o == null || !(o instanceof Player)) return super.remove(o);
		String name = ((Player) o).getName();
		for(Object key : this.keySet())
			if(key == null) continue;
			else if(!(key instanceof Player)) return super.remove(o);
			else break;
		for(Object key : new HashSet<K>(this.keySet()))
			if(key == null) continue;
			else if(((Player) key).getName().equals(name)) return super.remove(key);
		return null;
	}
	public V put(K k, V v)
	{
		if(k == null || !(k instanceof Player)) return super.put(k, v);
		String name = ((Player) k).getName();
		V ret;
		for(Object key : this.keySet())
			if(key == null) continue;
			else if(!(key instanceof Player)) return super.put(k, v);
			else break;
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
}
