package net.gmx.teamterrian.CDsPluginPack;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class CDsPluginPack extends JavaPlugin
{ 
	public PluginHandler handler = new PluginHandler(this);
	
	public void onLoad()
	{
		handler.clog.log("Loadin Pluging", this);
		handler.load();
	}
	
	public void onEnable()
	{
		handler.clog.log("Beginning Enabling", this);
		handler.enable();
	}
	
	public void onDisable()
	{
		handler.clog.log("Beginning Disabling", this);
		handler.disable();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		handler.clistener.onCommand(cmd, args, sender);
		return true;
	}
	
	public static CDsPluginPack getInstance()
	{
		return (CDsPluginPack) Bukkit.getPluginManager().getPlugin("CDsPluginPack");
	}
}
