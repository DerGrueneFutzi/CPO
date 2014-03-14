package net.gmx.teamterrian.CDsPluginPack.tools;

import net.gmx.teamterrian.CDsPluginPack.PluginHandler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class Dependencys
{
	private Log clog;
	public Essentials ess;
	public WorldEditPlugin we;
	public boolean nbt;
	
	public Dependencys(PluginHandler handler)
	{
		clog = handler.clog;
	}
	
	public static enum Dependency
	{
		WORLDEDIT,
		ESSENTIALS,
		NBTEDITOR,
	}
	
	public boolean getDependencys()
	{
		boolean check = true;
		Plugin p = Bukkit.getPluginManager().getPlugin("WorldEdit");
		if(p == null) {
			clog.log("Failed to get WorldEdit", this);
			we = null;
			check = false;
		}
		else {
			clog.log("WorldEdit get", this);
			we = (WorldEditPlugin) p;
		}
		p = Bukkit.getPluginManager().getPlugin("Essentials");
		if(p == null) {
			clog.log("Failed to get Essentials", this);
			ess = null;
			check = false;
		}
		else {
			clog.log("Essentials get", this);
			ess = (Essentials) p;
		}
		p = Bukkit.getPluginManager().getPlugin("NBTEditor");
		if(p == null) {
			clog.log("Failed to get NBTEditor", this);
			nbt = false;
			check = false;
		}
		else {
			clog.log("NBTEditor get", this);
			nbt = true;
		}
		return check;
	}
	public boolean checkDepend(Dependency d) throws Exception
	{
		switch(d)
		{
			case ESSENTIALS: return ess != null;
			case WORLDEDIT: return we != null;
			case NBTEDITOR: return nbt;
		}
		throw new Exception("Unknown Dependency " + d.name());
	}
	public boolean doDepend(Dependency d, CommandSender sender)
	{
		boolean exist;
		try { exist = checkDepend(d); }
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			return false;
		}
		if(exist) return true;
		switch(d)
		{
			case NBTEDITOR:
				if(sender != null)
					sender.sendMessage(ChatColor.RED + "You can´t run this command, because NBTEditor is not available");
			case ESSENTIALS:
				if(sender != null)
					sender.sendMessage(ChatColor.RED + "You can´t run this command, because Essentials is not available");
			case WORLDEDIT:
				if(sender != null)
					sender.sendMessage(ChatColor.RED + "You can´t run this command, because WorldEdit is not available");
		}
		return false;
	}
}
