package net.gmx.teamterrian.CDsPluginPack.handle.hardDependencys;

import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public interface IMan
{	
	public void onEnable(CDPluginEnableEvent e);
	
	public Permission[] getPermissions();
	
	public void onCommand(CommandEvent e) throws CDInvalidArgsException;
	
	public void printUsage(CommandSender sender, String c, boolean showIfEmpty);
	
	public String[] getDirectorys();
}
