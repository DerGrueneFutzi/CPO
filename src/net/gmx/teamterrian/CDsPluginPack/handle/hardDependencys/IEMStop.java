package net.gmx.teamterrian.CDsPluginPack.handle.hardDependencys;

import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

import org.bukkit.permissions.Permission;

public interface IEMStop
{
	public Permission[] getPermissions();
		
	public void onCommand(CommandEvent e);
	public void doCommand(String args);
	public void doKill(Log clog, Object c);
}
