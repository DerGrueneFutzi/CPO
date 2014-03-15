package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.Bukkit;



import org.bukkit.entity.Player;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class ReplaceColor extends CDPlugin
{	
	public ReplaceColor(PluginHandler handler)
	{
		super(handler);
	}
	
	@CDPluginCommand(commands = { "rc essentials.chat.color 1" })
	public void onCommand(CommandEvent e)
	{
		Player p = (Player) e.getSender();
		String mes = VarTools.arrToString(e.getArgs(), 0);
		mes = mes.replace('&', '§');
		if(mes.charAt(0) == '/') Bukkit.dispatchCommand(p, mes.substring(1));
		else p.chat(mes);
	}
}
