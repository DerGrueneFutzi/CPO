package net.gmx.teamterrian.CDsPluginPack.handle.exceptions;

import org.bukkit.ChatColor;

import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class CDNullSelectionException extends CDException
{
	private static final long serialVersionUID = 414867063722955616L;

	@Override
	public void handle(PluginHandler handler, CommandEvent e)
	{
		e.getSender().sendMessage(VarTools.getExclamation(ChatColor.GOLD) + ChatColor.GOLD + "For this action you have to select a Region with WorldEdit");
	}
}
