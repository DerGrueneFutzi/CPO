package net.gmx.teamterrian.CDsPluginPack.handle.exceptions;

import org.bukkit.ChatColor;

import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class CDNoPermissionException extends CDException
{
	private static final long serialVersionUID = 134708763002176638L;
	public boolean notify;

	public CDNoPermissionException(boolean notify)
	{
		this.notify = notify;
	}
	
	@Override
	public void handle(PluginHandler handler, CommandEvent e)
	{
		handler.clog.log(e.getSender().getName() + " was denyed to run command '" + e.getCommand().getName() + " " + VarTools.arrToString(e.getArgs(), 0) + "'. The Player was " + (notify ? "" : "not ") + "notifyed", this);
		if(notify)
			e.getSender().sendMessage(VarTools.getExclamation(ChatColor.RED) + ChatColor.DARK_RED + "Der Brotkasten will nicht, dass du diesen Command benutzt");
	}
}
