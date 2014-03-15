package net.gmx.teamterrian.CDsPluginPack.tools;

import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.entity.Player;

public class CDArrayList<E> extends ArrayList<E>
{
	private static final long serialVersionUID = -1198161659015956750L;
	
	public CDArrayList() { }
	public CDArrayList(Collection<E> col)
	{
		super(col);
	}
	public boolean contains(Object o)
	{
		if(!(o instanceof Player)) return super.contains(o);
		Player p, po = (Player) o;
		for(Object val : this)
		{
			if(!(val instanceof Player)) return super.contains(o);
			p = (Player) val;
			if(p.getName().equals(po.getName())) return true;
		}
		return false;
	}
	
	public int indexOf(Object o)
	{
		if(!(o instanceof Player)) return super.indexOf(o);
		Player p, po = (Player) o;
		for(int i = 0; i < this.size(); i++)
		{
			p = (Player) this.get(i);
			if(p.getName().equals(po.getName())) return i;
		}
		return -1;
	}
	public int lastIndexOf(Object o)
	{
		if(!(o instanceof Player)) return super.lastIndexOf(o);
		Player p, po = (Player) o;
		for(int i = this.size() - 1; i >= 0; i--)
		{
			p = (Player) this.get(i);
			if(p.getName().equals(po.getName())) return i;
		}
		return -1;
	}
	
	public boolean remove(Object o)
	{
		if(!(o instanceof Player)) return super.remove(o);
		Player p, po = (Player) o;
		for(int i = 0; i < this.size(); i++)
		{
			p = (Player) this.get(i);
			if(p.getName().equals(po.getName()))
			{
				this.remove(i);
				return true;
			}
		}
		return false;
	}
}
