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
		if(!(o instanceof Player)) return super.containsKey(o);
		Player p, po = (Player) o;
		for(Object key : this.keySet())
		{
			if(!(key instanceof Player)) return super.containsKey(o);
			p = (Player) key;
			if(p.getName().equals(po.getName())) return true;
		}
		return true;
	}
	public boolean containsValue(Object o)
	{
		if(!(o instanceof Player)) return super.containsKey(o);
		Player p, po = (Player) o;
		for(Object val : this.values())
		{
			if(!(val instanceof Player)) return super.containsValue(o);
			p = (Player) val;
			if(p.getName().equals(po.getName())) return true;
		}
		return false;
	}
	
	public V get(Object o)
	{
		if(!(o instanceof Player)) return super.get(o);
		Player p, po = (Player) o;
		for(Object key : this.values())
		{
			if(!(key instanceof Player)) return super.get(o);
			p = (Player) key;
			if(p.getName().equals(po.getName())) return this.get(key);
		}
		return null;
	}	
	public V remove(Object o)
	{
		if(!containsKey(o)) return null;
		if(o instanceof Player) return super.remove(o);
		Player p, po = (Player) o;
		for(Object key : new HashSet<K>(this.keySet()))
		{
			if(!(key instanceof Player)) return super.remove(o);
			p = (Player) key;
			if(p.getName().equals(po.getName())) return remove(key);
		}
		return null;
	}
	public V put(K k, V v)
	{
		if(k instanceof Player) return super.put(k, v);
		Player p, po = (Player) k;
		V ret;
		for(Object key : new HashSet<K>(this.keySet()))
		{
			if(!(key instanceof Player)) return super.put(k, v);
			p = (Player) key;
			if(p.getName().equals(po.getName()))
			{
				ret = get(key);
				remove(key);
				super.put(k, v);
				return ret;
			}
		}
		return null;
	}
}
