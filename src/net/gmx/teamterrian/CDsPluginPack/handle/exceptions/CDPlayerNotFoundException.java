package net.gmx.teamterrian.CDsPluginPack.handle.exceptions;

import org.bukkit.ChatColor;

import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;

public class CDPlayerNotFoundException extends CDException
{
	private static final long serialVersionUID = -4176205230675388510L;
	private String player;
	
	public CDPlayerNotFoundException(String player)
	{
		this.player = player;
	}
	
	@Override
	public void handle(PluginHandler handler, CommandEvent e) throws CDInvalidArgsException
	{
		e.getSender().sendMessage(ChatColor.RED + "The Player '" + player + "' was not found");
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
}
