package net.gmx.teamterrian.CDsPluginPack;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;

public class CDPlugin implements Listener
{
	protected PluginHandler handler;
	public CDPlugin(PluginHandler handler) { this.handler = handler; }
	
	public OfflinePlayer globalPlayer = Bukkit.getOfflinePlayer("");
	public String[] getDirectorys() { return null; }
	public static final String getDir() { return "./plugins/CDsPluginPack/"; }
	public Permission[] getPermissions() { return new Permission[0]; }
	
	public static String getExclamation(ChatColor c) { return "" + c + ChatColor.BOLD + ChatColor.MAGIC + "!!!" + ChatColor.RESET; }
}
