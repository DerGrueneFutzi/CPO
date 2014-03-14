package net.gmx.teamterrian.CDsPluginPack.handle.exceptions;

import org.bukkit.ChatColor;

import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;

public class CDWorldNotFoundException extends CDException
{
	private static final long serialVersionUID = 4162951419658392695L;
	
	private String world;
	
	public CDWorldNotFoundException(String world)
	{
		this.world = world;
	}
	
	public void handle(PluginHandler handler, CommandEvent e) throws CDInvalidArgsException
	{
		e.getSender().sendMessage(ChatColor.RED + "The World '" + world + "' was not found");
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
}
