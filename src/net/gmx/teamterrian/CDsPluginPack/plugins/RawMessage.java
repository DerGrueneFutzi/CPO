package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;

public class RawMessage extends CDPlugin
{
	Log clog;
	
	public RawMessage(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.rmsg", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "rmsg cdpp.rmsg 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		String[] args = e.getArgs();
		if(args.length <= 1) throw new CDInvalidArgsException(e.getCommand().getName());
		Player p = Player.getPlayer(Bukkit.getPlayer(args[0]));
		if(p == null) { e.getSender().sendMessage("Player not found"); return; }
		String message = "";
		for(String str : args) message += " " + str;
		message = message.substring(1);
		message = message.substring(message.indexOf(' ') + 1).replace('&', '§');
		clog.log("Sending RawMessage \"" + message + "\" to " + p.getName(), this);
		p.sendMessage(message);
	}
	
}
