package net.gmx.teamterrian.CDsPluginPack.tools;

import java.util.HashMap;

import org.bukkit.entity.Player;

public class CDHashMap extends HashMap<Object, Object>
{
	private static final long serialVersionUID = 7019909201564608954L;
	
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
		return false;
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
	
	public Object get(Object o)
	{
		if(!(o instanceof Player)) return super.get(o);
		Player p, po = (Player) o;
		for(Object key : this.values())
		{
			if(!(key instanceof Player)) return super.containsKey(o);
			p = (Player) key;
			if(p.getName().equals(po.getName())) return this.get(key);
		}
		return null;
	}
}
