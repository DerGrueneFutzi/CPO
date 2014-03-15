package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.earth2me.essentials.User;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginDisableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDNoPermissionException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys.Dependency;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagString;

public class GroupMail extends CDPlugin
{
	Log clog;
	Map<String, List<User>> groups = new CDHashMap<String, List<User>>();
	Dependencys d;
	String mbeg = ChatColor.DARK_GREEN + "[GroupMail] " + ChatColor.WHITE;
	
	public GroupMail(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
		d = handler.dependencys;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.gm.send", PermissionDefault.OP),
			new Permission("cdpp.gm.member.add", PermissionDefault.OP),
			new Permission("cdpp.gm.member.delete", PermissionDefault.OP),
			new Permission("cdpp.gm.group.add", PermissionDefault.OP),
			new Permission("cdpp.gm.group.delete", PermissionDefault.OP),
			new Permission("cdpp.gm.listusergroups", PermissionDefault.OP),
			new Permission("cdpp.gm.listgroups", PermissionDefault.OP),
			new Permission("cdpp.gm.listmembers", PermissionDefault.OP),
			new Permission("cdpp.gm.io", PermissionDefault.OP),
			new Permission("cdpp.gm", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try
		{
			clog.log("Loading groups", this);
			load();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	@CDPluginEvent
	public void onDisable(CDPluginDisableEvent e)
	{
		try
		{
			clog.log("Saving groups", this);
			save();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	public String[] getDirectorys(){ return new String[] { "GroupMail" }; }
	
	@CDPluginCommand(commands = { "gmail cdpp.gm 1" })
	public void onCommand(CommandEvent e) throws CDException
	{
		String args[] = e.getArgs();
		CommandSender sender = e.getSender();
		if(args.length < 1) throw new CDInvalidArgsException(e.getCommand().getName());
		switch(args[0])
		{
			case "send":
				send(args, sender); return;
			case "group":
				if(args.length < 2) throw new CDInvalidArgsException(e.getCommand().getName());
				if(args.length == 2)
					{ showMembers(args[1], sender); return; }
				if(args.length < 3) throw new CDInvalidArgsException(e.getCommand().getName());
				switch(args[2])
				{
					case "create":
						gAdd(args, sender); return;
					case "delete":
					case "remove":
						gDel(args, sender); return;
					case "add":
					case "join":
						if(args.length < 4) throw new CDInvalidArgsException(e.getCommand().getName());
						memAdd(args, sender, 3); return;
					case "leave":
						if(args.length < 4) throw new CDInvalidArgsException(e.getCommand().getName());
						memDel(args, sender, 3); return;
					case "users":
						showMembers(args[1], sender); return;
				}
				throw new CDInvalidArgsException(e.getCommand().getName());
			case "player":
			case "user":
				if(args.length < 2) throw new CDInvalidArgsException(e.getCommand().getName());
				if(args.length == 2)
					{ showGroups(args[1], sender); return; }
				if(args.length < 3) throw new CDInvalidArgsException(e.getCommand().getName());
				switch(args[2])
				{
					case "join":
					case "add":
						if(args.length < 4) throw new CDInvalidArgsException(e.getCommand().getName());
						memAdd(args[3], args[1], sender); return;
					case "leave":
						if(args.length < 4) throw new CDInvalidArgsException(e.getCommand().getName());
						memDel(args[3], args[1], sender); return;
					case "groups":
						showGroups(args[1], sender); return;
				}
			case "groups":
				showGroups(sender); return;
			case "save":
				save(sender); return;
			case "load":
				load(sender); return;
		}
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
	
	private void send(String[] args, CommandSender sender) throws CDException
	{
		List<User> members;
		if(!sender.hasPermission("cdpp.gm.send")) throw new CDNoPermissionException(true);
		if(args.length < 2) throw new CDInvalidArgsException("gmail");
		if((members = groups.get(args[1])) == null)
			sender.sendMessage(mbeg + ChatColor.RED + "Group does not exist");
		else {
			String message = SB(args, 2);
			for(User pl : members)
				pl.addMail(sender.getName() + ": " + message);
			sender.sendMessage(mbeg + ChatColor.GREEN + "Mails sent");
		}
	}
	private void memAdd(String[] args, CommandSender sender, int start) throws CDNoPermissionException
	{
		for(String name : subArray(args, start))
			memAdd(args[1], name, sender);
	}
	private void memAdd(String group, String name, CommandSender sender) throws CDNoPermissionException
	{
		if(!d.doDepend(Dependency.ESSENTIALS, sender)) return;
		List<User> members;
		User p;
		if(!sender.hasPermission("cdpp.gm.member.add")) throw new CDNoPermissionException(true);
		if((members = groups.get(group)) == null)
			sender.sendMessage(mbeg + ChatColor.RED + "Group does not exist");
		else if((p = d.ess.getOfflineUser(name)) == null)
			sender.sendMessage(mbeg + ChatColor.RED + "Player " + ChatColor.ITALIC + name + ChatColor.RESET + ChatColor.RED + " does not exist");
		else if(members.contains((User) p))
			sender.sendMessage(mbeg + ChatColor.RED + "Player " + ChatColor.ITALIC + name + ChatColor.RESET + ChatColor.RED + " is already in this group");
		else {
			sender.sendMessage(mbeg + ChatColor.GREEN + "Player " + ChatColor.ITALIC + name + ChatColor.RESET + ChatColor.GREEN + " added");
			members.add(p);
		}
	}
	private void memDel(String[] args, CommandSender sender, int start) throws CDNoPermissionException
	{
		for(String name : subArray(args, start))
			memDel(args[1], name, sender);
	}
	private void memDel(String group, String name, CommandSender sender) throws CDNoPermissionException
	{
		if(!d.doDepend(Dependency.ESSENTIALS, sender)) return;
		List<User> members;
		User p;
		if(!sender.hasPermission("cdpp.gm.member.delete")) throw new CDNoPermissionException(true);
		if((members = groups.get(group)) == null)
			sender.sendMessage(mbeg + ChatColor.RED + "Group does not exist");
		else if((p =  d.ess.getOfflineUser(name)) == null)
			sender.sendMessage(mbeg + ChatColor.RED + "Player " + ChatColor.ITALIC + name + ChatColor.RESET + ChatColor.RED + " does not exist");
		else if(!members.contains(p))
			sender.sendMessage(mbeg + ChatColor.RED + "Player " + ChatColor.ITALIC + name + ChatColor.RESET + ChatColor.RED + " is not in this group");
		else {
			sender.sendMessage(mbeg + ChatColor.GREEN + "Player " + ChatColor.ITALIC + name + ChatColor.RESET + ChatColor.GREEN + " removed");
			members.remove(p);
		}
	}
	@SuppressWarnings("unchecked")
	public void pDel(String name, CommandSender sender)
	{
		SimpleCommandMap cmdmap = handler.cmdRegister.commandMap;
		handler.cmdRegister.cmds = VarTools.cloneCollection(((SimpleCommandMap) handler.cmdRegister.commandMap).getCommands());
		Map<String, Command> knownCommands;
		try {
			Field f = cmdmap.getClass().getDeclaredField("knownCommands");
			f.setAccessible(true);
			knownCommands = (Map<String, Command>) f.get(cmdmap);
		}
		catch (Exception x) { x.printStackTrace(); return; }
		for (Map.Entry<String, Command> entry : knownCommands.entrySet()) {
            entry.getValue().unregister(cmdmap);
        }
        knownCommands.clear();
	}
	private void gAdd(String[] args, CommandSender sender) throws CDNoPermissionException, CDInvalidArgsException
	{
		if(!sender.hasPermission("cdpp.gm.group.add")) throw new CDNoPermissionException(true);
		if(args.length < 2) throw new CDInvalidArgsException("gmail");
		if(groups.get(args[1]) != null)
			sender.sendMessage(mbeg + ChatColor.RED + "Group already exists");
		else {
			groups.put(args[1], new ArrayList<User>());
			sender.sendMessage(mbeg + ChatColor.GREEN + "Group created");
		}
	}
	private void gDel(String[] args, CommandSender sender) throws CDInvalidArgsException, CDNoPermissionException
	{
		if(!sender.hasPermission("cdpp.gm.group.delete")) throw new CDNoPermissionException(true);
		if(args.length < 2) throw new CDInvalidArgsException("gmail");
		if((groups.get(args[1])) == null)
			sender.sendMessage(mbeg + ChatColor.RED + "Group does not exist");
		else {
			groups.remove(args[1]);
			sender.sendMessage(mbeg + ChatColor.GREEN + " Group removed");
		}
	}
	private void showGroups(String name, CommandSender sender) throws CDNoPermissionException
	{
		if(!sender.hasPermission("cdpp.gm.listusergroups")) throw new CDNoPermissionException(true);
		sender.sendMessage(mbeg + "Groups of Player " + name);
		for(String group : groups.keySet())
			for(User u : groups.get(group))
				if(u.getName().equalsIgnoreCase(name)) sender.sendMessage(mbeg + "    " + group);
	}
	private void showGroups(CommandSender sender) throws CDNoPermissionException
	{
		if(!sender.hasPermission("cdpp.gm.listgroups")) throw new CDNoPermissionException(true);
		sender.sendMessage(mbeg + "Groups");
		for(String group : groups.keySet())
			sender.sendMessage(mbeg + "    " + group + ": " + groups.get(group).size());
	}
	private void showMembers(String group, CommandSender sender) throws CDNoPermissionException
	{
		if(!sender.hasPermission("cdpp.gm.listmembers")) throw new CDNoPermissionException(true);
		List<User> members;
		if((members = groups.get(group)) == null)
			sender.sendMessage(mbeg + ChatColor.RED + "Group does not exist");
		else
		{
			sender.sendMessage(mbeg + "Members of Group " + group);
			for(User u : members)
				sender.sendMessage(mbeg + "    " + u.getName());
		}
	}
	
	private void save(CommandSender sender) throws CDNoPermissionException
	{
		if(!sender.hasPermission("cdpp.gm.io")) throw new CDNoPermissionException(true);
		try {
			save();
			sender.sendMessage(mbeg + ChatColor.GREEN + "[GroupMail] Groups saved");
		}
		catch (Exception x) { sender.sendMessage(mbeg + ChatColor.RED + "[GroupMail] Error while saving"); }
	}
	private void load(CommandSender sender) throws CDNoPermissionException
	{
		if(!sender.hasPermission("cdpp.gm.io")) throw new CDNoPermissionException(true);
		try {
			load();
			sender.sendMessage(mbeg + ChatColor.GREEN + "Groups loaded");
		}
		catch (Exception x) { sender.sendMessage(mbeg + ChatColor.RED + "Error while loading"); }
	}
	
	private void save() throws IOException
	{
		clog.log("Start saving Groups", this);
		NBTTagCompound base = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for(String group : groups.keySet())
		{
			NBTTagCompound groupCompound = new NBTTagCompound();
			NBTTagList groupList = new NBTTagList();
			for(User p : groups.get(group))
				groupList.add(new NBTTagString(p.getName()));
			groupCompound.set("Members", groupList);
			groupCompound.set("Name", new NBTTagString(group));
			list.add(groupCompound);
		}
		base.set("Data", list);
		Data.secureSave(base, CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat", this);
	    clog.log("Groups saved", this);
	}
	private void load() throws Exception
	{
		if(!d.checkDepend(Dependency.ESSENTIALS)) {
			clog.log("Can´t load Groups. Plugin Essentials not found", this);
			return;
		}
		clog.log("Start loading groups", this);
		clog.log("Clearing intern group list", this);
		groups.clear();
		NBTTagCompound base = Data.load(getDir() + getDirectorys()[0] + "/data.dat", this);
		if(base == null) return;
		NBTTagList list = (NBTTagList) base.get("Data");
		clog.log("Reading groups to intern group list", this);
		NBTTagCompound groupCompound;
		NBTTagList groupList;
		List<User> playerList;
		User p;
		for (int i = 0; i < list.size(); i++)
		{
			groupCompound = list.get(i);
			groupList = (NBTTagList) groupCompound.get("Members");
			playerList = new ArrayList<User>();
			for(int ii = 0; ii < groupList.size(); ii++)
			{
				if((p = d.ess.getOfflineUser(groupList.f(ii))) == null) throw new Exception("Player " + groupList.f(ii) + " not found");
				playerList.add(p);
			}
			groups.put(groupCompound.getString("Name"), playerList);
		}
		clog.log("Groups loaded", this);
	}
	
	private String SB(String[] args, int start)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = start; i < args.length; i++)
			sb.append(" " + args[i]);
		String back = sb.toString();
		if(back.length() != 0) back = back.substring(1);
		return back;
	}
	private List<String> subArray(String[] args, int start)
	{
		List<String> back = new ArrayList<String>();
		for(int i = start; i < args.length; i++)
			back.add(args[i]);
		return back;
	}
}
