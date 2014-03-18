package net.gmx.teamterrian.CDsPluginPack;

import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;

public class CDPlugin implements Listener
{
	protected PluginHandler handler;
	public CDPlugin(PluginHandler handler) { this.handler = handler; }
	
	public String[] getDirectorys() { return null; }
	public static final String getDir() { return "./plugins/CDsPluginPack/"; }
	public Permission[] getPermissions() { return new Permission[0]; }
}
