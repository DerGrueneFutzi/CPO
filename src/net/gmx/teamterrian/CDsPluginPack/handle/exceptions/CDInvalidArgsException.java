package net.gmx.teamterrian.CDsPluginPack.handle.exceptions;

import org.bukkit.ChatColor;

import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.hardDependencys.IMan;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class CDInvalidArgsException extends CDException
{
	private static final long serialVersionUID = 2315402008574479470L;
	private String command;
	
	public CDInvalidArgsException(String command)
	{
		this.command = command.toLowerCase();
	}
	
	public String getCommand()
	{
		return command;
	}

	@Override
	public void handle(PluginHandler handler, CommandEvent e)
	{
		e.getSender().sendMessage(VarTools.getExclamation(ChatColor.GOLD) + ChatColor.GOLD + "The Plugin don�t accept that count or kind of arguments");
		if(handler.clistener.checkCommand(e.getCommand().getName(), e.getSender()))
			((IMan) handler.getPlugin("Man")).printUsage(e.getSender(), e.getCommand().getName(), false);
	}
}
