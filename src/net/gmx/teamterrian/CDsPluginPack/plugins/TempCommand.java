package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.earth2me.essentials.User;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDNoPermissionException;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys.Dependency;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class TempCommand extends CDPlugin
{
	Log clog;
	Map<Runnable, Object[]> data = new HashMap<Runnable, Object[]>();
	Dependencys d;
	String mbeg = "[TempCommand] ";
	
	public TempCommand(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
		d = handler.dependencys;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.tempcmd.fly.maxtime.bypass", PermissionDefault.OP),
			new Permission("cdpp.tempcmd.god.maxtime.bypass", PermissionDefault.OP),
			new Permission("cdpp.tempcmd", PermissionDefault.OP)
		};
	}
		
	enum command
	{
		FLY,
		GOD,
	}
	
	@CDPluginCommand(commands = { "temp cdpp.tempcmd 1" })
	public void onCommand(CommandEvent e) throws CDNoPermissionException, CDInvalidArgsException
	{
		String[] args = e.getArgs();
		CommandSender sender = e.getSender();
		if(args.length < 3) throw new CDInvalidArgsException(e.getCommand().getName());
		if(!d.doDepend(Dependency.ESSENTIALS, sender)) return;
		long time;
		if(Bukkit.getPlayerExact(args[1]) == null) {
			sender.sendMessage(ChatColor.RED + mbeg + "Player not found");
			return;
		}
		args[1] = Bukkit.getPlayerExact(args[1]).getName();
		try { time = Long.valueOf(args[2]); }
		catch (Exception x) {
			sender.sendMessage(ChatColor.RED + mbeg + "Cannot cast \"" + args[2] + "\" to a number");
			return;
		}
		if(!sender.hasPermission("cdpp.tempcmd." + args[0])) throw new CDNoPermissionException(true);
		switch(args[0])
		{
			case "fly":
				startTask(args[1], command.FLY, sender.hasPermission("cdpp.tempcmd.fly.maxtime.bypass") ? time : time % 12001);
				return;
			case "god":
				startTask(args[1], command.GOD, sender.hasPermission("cdpp.tempcmd.god.maxtime.bypass") ? time : time % 6001);
				return;
		}
		sender.sendMessage(ChatColor.RED + mbeg + "Unknown command \"" + args[0] + "\"");
	}
	
	private void startTask(String p, command c, long time)
	{
		if(time == 0) {	
			if(endTask(p, c))
				Bukkit.getPlayer(p).sendMessage(ChatColor.GOLD + mbeg + "Your " + c.name() + "-Time was prematurely broken");
			return;
		}
		clog.log("Starting Task for " + p + " with Command " + c.name() + " for " + time / 20 + " seconds", this);
		Runnable r = getRunnable(c);
		addTask(r, p, c);
		if(!doCommand(c, p)) {
			clog.log("Doing the command was not successfull. Removing Runnable", this);
			removeTask(r);
			return;
		}
		messagePlayer(Player.getPlayer(Bukkit.getPlayer(p)), c, time);
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(handler.getCDPP(), r, time);
	}
	
	private boolean doCommand(command c, String p)
	{
		User u = d.ess.getUser(p);
		if(u == null) return false;
		clog.log("Set " + c.name() + "-Mode true for " + p, this);
		switch(c)
		{
			case FLY:
				u.setAllowFlight(true);
				break;
			case GOD:
				u.setGodModeEnabled(true);
				break;
		}
		return true;
	}
	
	private void messagePlayer(Player p, command c, long time)
	{
		if(p == null) return;
		switch(c)
		{
			case FLY:
				p.sendMessage(ChatColor.GREEN + "[TempCommand] Flying is now enabled for you for " + time / 20 + " seconds");
				break;
			case GOD:
				p.sendMessage(ChatColor.GREEN + "[TempCommand] GodMode is now enabled for you for " + time / 20 + " seconds");
				break;
		}
	}
	private void messagePlayer(Player p, command c)
	{
		if(p == null) return;
		switch(c)
		{
			case FLY:
				p.sendMessage(ChatColor.GOLD + "[TempCommand] The time for your fly mode is now expired");
				break;
			case GOD:
				p.sendMessage(ChatColor.GOLD + "[TempCommand] The time for your god mode is now expired");
				break;
		}
	}
	
	public boolean checkTask(Player p, final String s)
	{
		String cmd = VarTools.stringToArr(s, 0)[0];
		Collection<Command> col = (handler.cmdRegister.cmds == null ? handler.cmdRegister.commandMap.getCommands() : handler.cmdRegister.cmds);
		for(final Command c : col)
			if(c.getName().equalsIgnoreCase(cmd))
			{
				if(c instanceof PluginCommand)
					((PluginCommand) c).getPlugin().onCommand(p, c, c.getLabel(), VarTools.stringToArr(s, 1));
				else if(c instanceof CDCommand)
					handler.clistener.onCommand(c, VarTools.stringToArr(s, 1), p);
				else
					c.execute(p, "", VarTools.stringToArr(s, 1));
				/*else
					Bukkit.getScheduler().runTask(
							handler.getCDPP(), new Runnable() { public void run() {
								c.execute(Bukkit.getServer().getConsoleSender(), c.getLabel(), VarTools.stringToArr(s, 1)); }});*/
				return true;
			}
		return true;
	}
	private void addTask(Runnable r, String p, command c)
	{
		removeTask(p, c);
		clog.log("Adding Runnable with {" + p + ", " + c.name() + "}", this);
		data.put(r, new Object[] { p, c });
	}
	private boolean removeTask(String p, command c)
	{
		boolean removed = false;
		Object[] o;
		String pl;
		command cm;
		for(Runnable rn : data.keySet())
		{
			o = data.get(rn);
			pl = (String) o[0];
			cm = (command) o[1];
			if(pl.equals(p) && cm.equals(c))
			{
				clog.log("Removing overridden Runnable with {" + pl + ", " + cm.name() + "}", this);
				removeTask(rn);
				removed = true;
			}
		}
		return removed;
	}
	private boolean removeTask(Runnable r)
	{
		boolean removed = false;
		Object[] o = data.get(r);
		if(o == null) clog.log("Object to Remove is NULL", this);
		else {
			clog.log("Removing Runnable with {" + o[0] + ", " + ((command) o[1]).name() + "}", this);
			data.remove(r);
			removed = true;
		}
		return removed;
	}
	
	private Runnable getRunnable(final command c)
	{
		return new Runnable()
		{
			public void run()
			{
				endTask(this, c, true);
			}
		};
	}
	private boolean endTask(String p, command c)
	{
		boolean removed = false;
		Object[] o;
		String pl;
		command cm;
		for(Runnable rn : data.keySet())
		{
			o = data.get(rn);
			pl = (String) o[0];
			cm = (command) o[1];
			if(pl.equals(p) && cm.equals(c))
				removed |= endTask(rn, c, false);
		}
		return removed;
	}
	private boolean endTask(Runnable r, command c, boolean messagePlayer)
	{
		Object[] o = data.get(r);
		if(o == null) {
			clog.log("Error: Object Array was null", this);
			return false;
		}
		String p = (String) o[0];
		clog.log("Set " + c.name() +  "-Mode false for " + p, this);
		if(messagePlayer) messagePlayer(Player.getPlayer(Bukkit.getPlayerExact(p)), (command) o[1]);
		switch(c)
		{
			case FLY:
				d.ess.getUser(p).setAllowFlight(false);
			case GOD:
				d.ess.getUser(p).setGodModeEnabled(false);
		}
		return removeTask(r);
	}
}
