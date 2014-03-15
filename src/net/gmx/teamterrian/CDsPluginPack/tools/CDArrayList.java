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
		if(o == null || !(o instanceof Player)) return super.contains(o);
		String name = ((Player) o).getName();
		for(Object val : this)
			if(val == null) continue;
			else if(!(val instanceof Player)) return super.contains(o);
		for(Object val : this)
			if(val == null) continue;
			else if(((Player) val).getName().equals(name)) return true;
		return false;
	}
	
	public int indexOf(Object o)
	{
		if(o == null || !(o instanceof Player)) return super.indexOf(o);
		String name = ((Player) o).getName();
		for(Object val : this)
			if(val == null) continue;
			else if(!(val instanceof Player)) return super.indexOf(o);
		for(int i = 0; i < this.size(); i++)
			if(this.get(i) == null) continue;
			else if(((Player) this).getName().equals(name)) return i;
		return -1;
	}
	public int lastIndexOf(Object o)
	{
		if(o == null || !(o instanceof Player)) return super.lastIndexOf(o);
		String name = ((Player) o).getName();
		for(Object val : this)
			if(val == null) continue;
			else if(!(val instanceof Player)) return super.lastIndexOf(o);
		for(int i = this.size() - 1; i >= 0; i--)
			if(this.get(i) == null) continue;
			else if(((Player) this).getName().equals(name)) return i;
		return -1;
	}
	
	public boolean remove(Object o)
	{
		if(!(o instanceof Player)) return super.remove(o);
		String name = ((Player) o).getName();
		for(Object val : this)
			if(val == null) continue;
			else if(!(val instanceof Player)) return super.remove(o);
		for(int i = 0; i < this.size(); i++)
			if(((Player) this.get(i)).getName().equals(name)) {
				this.remove(i);
				return true;
			}
		return false;
	}
}
