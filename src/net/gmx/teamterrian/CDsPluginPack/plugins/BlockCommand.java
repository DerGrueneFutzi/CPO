package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.comphenix.protocol.events.PacketEvent;
import com.sk89q.worldedit.bukkit.selections.Selection;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginDisableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDNoPermissionException;
import net.gmx.teamterrian.CDsPluginPack.plugins.BlockCommand.TriggerType;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Timestamp;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys.Dependency;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R1.NBTTagByte;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagLong;
import net.minecraft.server.v1_7_R1.NBTTagString;

public class BlockCommand extends CDPlugin
{
	String mbeg = ChatColor.GOLD + "[BlockCommand] " + ChatColor.AQUA;
	ConsoleCommandSender ccs = Bukkit.getServer().getConsoleSender();
	Set<String> ops = toStringSet(Bukkit.getOperators());
	Log clog;
	Dependencys d;
	
	Map<Location, BCBlockData> places = new HashMap<Location, BCBlockData>();
	
	public BlockCommand(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
		d = handler.dependencys;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.bc.setshow", PermissionDefault.OP),
			new Permission("cdpp.bc.del", PermissionDefault.OP),
			new Permission("cdpp.bc.setcooldown", PermissionDefault.OP),
			new Permission("cdpp.bc.add", PermissionDefault.OP),
			new Permission("cdpp.bc.show", PermissionDefault.OP),
			new Permission("cdpp.bc.reloadop", PermissionDefault.OP),
			new Permission("cdpp.bc.use.move", PermissionDefault.OP),
			new Permission("cdpp.bc.use.click", PermissionDefault.OP),
			new Permission("cdpp.bc.io", PermissionDefault.OP),
			new Permission("cdpp.bc", PermissionDefault.OP)
		};
	}
		
	public static enum TriggerType
	{
		MOVE,
		CLICK,
		NONE
	}
	
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try {
			clog.log("Loading locations", this);
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
			clog.log("Saving locations", this);
			save();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	public String[] getDirectorys() { return new String[] { "BlockCommand" }; }
	
	@CDPluginCommand(commands = { "bc cdpp.bc 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException, CDNoPermissionException
	{
		String[] args = e.getArgs();
		CommandSender sender = e.getSender();
		if(args.length < 1) throw new CDInvalidArgsException(e.getCommand().getName());
		switch(args[0])
		{
			case "add":
				if(!d.doDepend(Dependency.WORLDEDIT, sender)) return;
				add((Player) sender, args);
				return;
			case "setcooldown":
				if(!d.doDepend(Dependency.WORLDEDIT, sender)) return;
				setCooldown((Player) sender, args);
				return;
			case "deltrigger":
				if(!d.doDepend(Dependency.WORLDEDIT, sender)) return;
				delTriggerData((Player) sender, args);
				return;
			case "setshow":
				if(!d.doDepend(Dependency.WORLDEDIT, sender)) return;
				setShow((Player) sender, args);
				return;
			case "save":
				save(sender);
				return;
			case "load":
				load(sender);
				return;
			case "near":
				show((Player) sender);
				return;
			case "show":
				if(!d.doDepend(Dependency.WORLDEDIT, sender)) return;
				showBlock((Player) sender);
				return;
			case "del":
				if(!d.doDepend(Dependency.WORLDEDIT, sender)) return;
				del((Player) sender);
				return;
			case "opreload":
			case "reloadop":
				reloadOp(sender);
				return;
		}
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
	
	@CDPluginPacket(types = { "cchat" })
	public void onPacket(PacketEvent e)
	{
		Player p = e.getPlayer();
		if(e.getPacket().getStrings().read(0).charAt(0) != '/' || shouldOp(p)) return;
		e.setCancelled(true);
		clog.log("Blocked command from " + p.getName() + " because he is in a temporary Op-Mode", this);
		p.sendMessage(ChatColor.RED + "Your command couldn't be processed. Please try again in a few seconds. If it still not works, contact an Server Admin");
	}
	
	private void setShow(Player p, String[] args)
	{
		if(!p.hasPermission("cdpp.bc.setshow")) {
			p.sendMessage(mbeg + "No Permission to use this command");
			return;
		}
		if(args.length != 3) {
			p.sendMessage(mbeg + "Not enough or to few arguments");
			return;
		}
		TriggerType trigger;
		boolean show;
		try { trigger = TriggerType.valueOf(args[1].toUpperCase()); } catch (Exception x) { p.sendMessage(mbeg + "Unknown TriggerType"); return; }
		if(!VarTools.ifBool(args[2])) { p.sendMessage(mbeg + "Invalid bool"); return; }
		show = args[2].equalsIgnoreCase("true");
		Selection s = d.we.getSelection(p);
		BCBlockData blockData;
		BCTriggerData triggerData;
		if(s == null) {
			p.sendMessage(mbeg + "Please select a region first");
			return;
		}
		for(Location l : VarTools.getLocations(s))
		{
			clog.log("Try setting Show = " + show + " on " + VarTools.parse(l, false, false, true) + " with Trigger " + trigger.name(), this);
			if((blockData = places.get(l)) == null) blockData = new BCBlockData();
			if((triggerData = blockData.getTriggerData(trigger)) == null) triggerData = new BCTriggerData();
			triggerData.getBlockCooldowns().setShow(show);
			blockData.setTriggerData(trigger, triggerData);
			places.put(l, blockData);
		}
		p.sendMessage(mbeg + "Show-Vars set");
	}
	private void delTriggerData(Player p, String[] args)
	{
		if(!p.hasPermission("cdpp.bc.del")) {
			p.sendMessage(mbeg + "No Permission to use this command");
			return;
		}
		TriggerType trigger;
		Selection s = d.we.getSelection(p);
		if(s == null) {
			p.sendMessage(mbeg + "Please select a region first");
			return;
		}
		BCBlockData blockData;
		try { trigger = TriggerType.valueOf(args[1].toUpperCase()); } catch (Exception x) { p.sendMessage(mbeg + "Unknown TriggerType"); return; }
		int c = 0;
		for(Location l : VarTools.getLocations(s))
		{
			clog.log("Try deleting Trigger " + trigger.name() + " on " + VarTools.parse(l, false, false, true) + " with Trigger " + trigger.name(), this);
			if(!places.containsKey(l) || !(blockData = places.get(l)).containsTrigger(trigger)) {
				p.sendMessage(mbeg + "Trigger " + trigger.name() + " not found on Block " + VarTools.parse(l, false, false, true));
				continue;
			}
			blockData.delTriggerData(trigger);
			c++;
		}
		p.sendMessage(mbeg + c + " TriggerData deleted");
		
	}
	private void setCooldown(Player p, String[] args)
	{
		if(!p.hasPermission("cdpp.bc.setcooldown")) {
			p.sendMessage(mbeg + "No Permission to use this command");
			return;
		}
		if(args.length != 4) {
			p.sendMessage(mbeg + "Not enough or to few arguments");
			return;
		}
		boolean globalCooldown;
		TriggerType trigger;
		int cooldown;
		
		try { trigger = TriggerType.valueOf(args[1].toUpperCase()); } catch (Exception x) { p.sendMessage(mbeg + "Unknown TriggerType"); return; }
		switch(args[2].toLowerCase())
		{
			case "player":
				globalCooldown = false;
				break;
			case "global":
				globalCooldown = true;
				break;
			default:
				p.sendMessage(mbeg + "Unknown cooldownType");
				return;
				
		}
		try { cooldown = Integer.valueOf(args[3]); } catch (Exception x) { p.sendMessage(mbeg + "Invalid number"); return; }
		
		Selection s = d.we.getSelection(p);
		BCBlockData blockData;
		if(s == null) {
			p.sendMessage(mbeg + "Please select a region first");
			return;
		}
		Location l1 = s.getMinimumPoint();
		Location l2 = s.getMaximumPoint();
		int x1 = l1.getBlockX(),
			y1 = l1.getBlockY(),
			z1 = l1.getBlockZ(),
			x2 = l2.getBlockX(),
			y2 = l2.getBlockY(),
			z2 = l2.getBlockZ();
		Location l;
		for(int ix = x1; ix <= x2; ix++)
			for(int iy = y1; iy <= y2; iy++)
				for(int iz = z1; iz <= z2; iz++)
				{
					l = new Location(l1.getWorld(), ix, iy, iz); 
					clog.log("Try adding " + (globalCooldown ? "Global" : "Player") + "Cooldown on " + VarTools.parse(l, false, false, true) + " with Trigger " + trigger.name(), this);
					if((blockData = places.get(l)) == null) blockData = new BCBlockData();
					if(!blockData.containsTrigger(trigger)) blockData.setTriggerData(trigger, new BCTriggerData());
					if(globalCooldown) blockData.getTriggerData(trigger).getBlockCooldowns().setGlobalCooldown(cooldown);
					else blockData.getTriggerData(trigger).getBlockCooldowns().setPlayerCooldown(cooldown);
					places.put(l, blockData);
				}
		p.sendMessage(mbeg + "TriggerCooldowns set");
		
	}
	private void add(Player p, String[] args)
	{
		if(!p.hasPermission("cdpp.bc.add")) {
			p.sendMessage(mbeg + "No Permission to use this command");
			return;
		}
		Selection s = d.we.getSelection(p);
		if(s == null) {
			p.sendMessage(mbeg + "Please select a region first");
			return;
		}
		TriggerType trigger;
		BCTimestamp timestamp;
		int firstCommand;
		for(Location l : VarTools.getLocations(s))
		{
			trigger = getTriggerType(args);
			timestamp = getTimestamp(args, (firstCommand = firstCommandWord(args)));
			clog.log("Try adding Command \"" + VarTools.SB(args, firstCommand) + "\" on " + VarTools.parse(l, false, false, true) + " with trigger " + trigger.name() + ", globalCooldown " + timestamp.getGlobalCooldown() + ", playerCooldown " + timestamp.getPlayerCooldown() + " and show " + timestamp.getShow() + " from " + p.getName(), this);
			addCommand(l, trigger, timestamp, VarTools.subArr(args, firstCommand));
		}
		p.sendMessage(mbeg + "Commands added");
		try { save(); }
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while saving Data. An Exception should have been logged", this);
			p.sendMessage(mbeg + "The blocks were added/changed, but there's a problem in saving the data. Please contact an Admin");
		}
	}
	private void show(Player p)
	{
		if(!p.hasPermission("cdpp.bc.show")) {
			p.sendMessage("No Permission to use this command");
			return;
		}
		Map<Integer, Location> locs = getNear(p);
		for(Location l : locs.values())
			p.sendMessage(mbeg + " x: " + (l = getBlockLoc(l)).getBlockX() + ", y: " + l.getBlockY() + ", z: " + l.getBlockZ());
	}
	private void del(Player p)
	{
		if(!p.hasPermission("cdpp.bc.del")) {
			p.sendMessage("No Permission to use this command");
			return;
		}
		Selection s = d.we.getSelection(p);
		if(s == null) {
			p.sendMessage(mbeg + "Please select a region first");
			return;
		}
		int c = 0;
		for(Location l : VarTools.getLocations(s))
			if(places.containsKey(l))
			{
				clog.log("Removing Location " + VarTools.parse(l, false, false, true) + " from " + p.getName(), this);
				places.remove(l);
				c++;
			}
		p.sendMessage(mbeg + c + " Blocks removed");
		try { save(); }
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while saving Data. An Exception should have been logged", this);
			p.sendMessage(mbeg + "The blocks were deleted, but there's a problem in saving the data. Please contact an Admin");
		}
	}
	private void showBlock(Player p)
	{
		if(!p.hasPermission("cdpp.bc.show")) {
			p.sendMessage("No Permission to use this command");
			return;
		}
		Selection s = d.we.getSelection(p);
		if(s == null)
			p.sendMessage(mbeg + "You have not marked any block");
		else if(!s.getMaximumPoint().equals(s.getMinimumPoint()))
			p.sendMessage(mbeg + "Please mark at least one block");
		else
			showBlock(s.getMaximumPoint(), p);
	}
	private void showBlock(Location l, Player p)
	{
		if(!places.containsKey(l)) { p.sendMessage(mbeg + "No commands are saved on this block"); return; } 
		BCBlockData blockData = places.get(l);
		p.sendMessage(mbeg + blockData.getSize() + (blockData.getSize() == 1 ? " command" : " commands") + " are on this block");
		List<String> messages = blockData.makeTriggerList();
		messages.addAll(blockData.makeCommandList());
		VarTools.addBeg(messages, mbeg);
		Data.showMessages(messages, p);
	}
	private Map<Integer, Location> getNear(Player p)
	{
		if(!p.hasPermission("cdpp.bc.show")) {
			p.sendMessage("No Permission to use this command");
			return null;
		}
		Map<Integer, Location> back = new HashMap<Integer, Location>();
		int counter = 0;
		Location loc = p.getLocation();
		for(Location l : places.keySet())
			if(l.distance(loc) < 15) {
				back.put(counter, l);
				counter++;
			}
		return back;
	}
	private void reloadOp(CommandSender sender) throws CDNoPermissionException
	{
		if(!sender.hasPermission("cdpp.bc.reloadop")) throw new CDNoPermissionException(true);
		ops = toStringSet(Bukkit.getServer().getOperators());
		sender.sendMessage(mbeg + "Operators reloaded");
	}
	private Set<String> toStringSet(Set<OfflinePlayer> input)
	{
		Set<String> back = new HashSet<String>();
		for(OfflinePlayer p : input)
			back.add(p.getName().toLowerCase());
		return back;
	}
	
	@CDPluginEvent
	public boolean onPlayerMove(PlayerMoveEvent e)
	{
		if(!e.getPlayer().hasPermission("cdpp.bc.use.move")) return e.isCancelled();
		Location l = getBlockLoc(e.getTo()).subtract(0, 1, 0);
		BCBlockData blockData;
		if(!places.containsKey(l) || !(blockData = places.get(l)).containsTrigger(TriggerType.MOVE))
			return e.isCancelled();
		runCommands(blockData.getTriggerData(TriggerType.MOVE), e.getPlayer());
		return e.isCancelled();
	}
	@CDPluginEvent
	public boolean onPlayerInteract(PlayerInteractEvent e)
	{
		if	((e.getAction() != Action.RIGHT_CLICK_BLOCK) ||
			!e.getPlayer().hasPermission("cdpp.bc.use.click") ||
			e.getPlayer().isSneaking()) return e.isCancelled();
		Block b = e.getClickedBlock();
		if(b == null) return e.isCancelled();
		Location l = getBlockLoc(b.getLocation());
		if(!places.containsKey(l) || !places.get(l).containsTrigger(TriggerType.CLICK)) return e.isCancelled();
		e.setCancelled(true);
		runCommands(places.get(l).getTriggerData(TriggerType.CLICK), e.getPlayer());
		return e.isCancelled();
	}
	
	private void runCommands(BCTriggerData triggerData, Player p)
	{
		BCTimestamp timestamp;
		if(!(timestamp = triggerData.getBlockCooldowns()).checkCooldown(p.getName())) {
			if(timestamp.getShow())
				p.sendMessage(mbeg + "You are able to use this block in " + timestamp.getTimeToNextUse(p.getName()));
			return;
		}
		int size = triggerData.getSize();
		BCCommandData command;
		for(int i = 0; i < size; i++)
		{
			command = triggerData.getCommandData(i);
			if(!(timestamp = command.getCooldowns()).checkCooldown(p.getName())) {
				if(timestamp.getShow())
					p.sendMessage(mbeg + "A command cannot be processed because of cooldowns");
			}
			else runCommand(triggerData, i, p);
		}
	}
	private void runCommand(BCTriggerData triggerData, int index, Player p)
	{
		boolean op = false;
		BCCommandData commandData = triggerData.getCommandData(index);
		String[] cmd = commandData.getCommand();
		String command = VarTools.SB(VarTools.replaceArr(cmd, "<player>", p.getName()), 1);
		switch(cmd[0])
		{	
			case "@o":
				if(!p.isOp()) {
					clog.log("Making " + p.getName() + " to an operator", this);
					p.setOp(true);
					op = true;
				}
			case "@p":
				clog.log("Running \"" + VarTools.SB(cmd, 1) + "\" as " + p.getName(), this);
				Bukkit.getServer().dispatchCommand(p, command);
				if(op) {
					clog.log("Deleting the operator status from " + p.getName(), this);
					p.setOp(false);
				}
				break;
			case "@c":
				clog.log("Running \"" + VarTools.SB(cmd, 1) + "\" as Console", this);
				Bukkit.getServer().dispatchCommand(ccs, command);
				break;
			default:
				clog.log("Unknown param \"" + cmd[0] + "\"", this);
				return;
		}
		long actTimestamp = Data.getTimestamp();
		BCTimestamp timestamp;
		(timestamp = triggerData.getBlockCooldowns()).setTimestamp("", actTimestamp);
		timestamp.setTimestamp(p.getName(), actTimestamp);
		(timestamp = commandData.getCooldowns()).setTimestamp("", actTimestamp);
		timestamp.setTimestamp(p.getName(), actTimestamp);
	}
	
	private boolean save(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.bc.io")) {
			sender.sendMessage("No Permission to use this command");
			return true;
		}
		try {
			save();
			sender.sendMessage(ChatColor.GREEN + "[BlockCommand] Locations saved");
		}
		catch (Exception x) {
			sender.sendMessage(ChatColor.RED + "[BlockCommand] Error while saving");
		}
		return true;
	}
	private boolean load(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.bc.io")) {
			sender.sendMessage("No Permission to use this command");
			return true;
		}
		try {
			load();
			sender.sendMessage(ChatColor.GREEN + "[BlockCommand] Locations loaded");
		}
		catch (Exception x)
		{
			sender.sendMessage(ChatColor.RED + "[BlockCommand] Error while loading");
		}
		return true;
	}
	
	private void save() throws IOException
	{
		clog.log("Start saving Locations", this);
		NBTTagCompound base = new NBTTagCompound();
		NBTTagList locationsList = new NBTTagList();
		NBTTagCompound blockCompound;
		NBTTagCompound triggerCompound;
		NBTTagList commandList;
		NBTTagCompound commandCompound;
		
		int trigger_counter;
		BCBlockData blockData;
		BCTriggerData triggerData;
		BCCommandData commandData;
		int size;
		for(Location l : places.keySet())
		{
			blockData = places.get(l);
			blockCompound = new NBTTagCompound();
			Data.writeCoords(blockCompound, l);
			blockCompound.set("world", new NBTTagString(l.getWorld().getName()));
			trigger_counter = 0;
			for(TriggerType trigger : blockData.getTriggers())
			{
				triggerCompound = new NBTTagCompound();
				triggerData = blockData.getTriggerData(trigger);
				doTimestamp(triggerCompound, triggerData.getBlockCooldowns());
				size = triggerData.getSize();
				commandList = new NBTTagList();
				for(int i = 0; i < size; i++)
				{
					commandData = triggerData.getCommandData(i);
					commandCompound = new NBTTagCompound();
					commandCompound.set("command", new NBTTagString(VarTools.SB(commandData.getCommand(), 0)));
					doTimestamp(commandCompound, commandData.getCooldowns());
					commandList.add(commandCompound);
				}
				triggerCompound.set("commands", commandList);
				triggerCompound.set("trigger", new NBTTagString(trigger.name()));
				blockCompound.set("triggerData_" + trigger_counter, triggerCompound);
				trigger_counter++;
			}
			locationsList.add(blockCompound);
		}
		base.set("Data", locationsList);
		clog.log("Writing", this);
		Data.secureSave(base, CDPlugin.getDir() + getDirectorys()[0] + "/data.dat", this);
		clog.log("Locations saved", this);
	}
	private void doTimestamp(NBTTagCompound nbtCompound, BCTimestamp timestamp)
	{
		nbtCompound.set("globalCooldown", new NBTTagLong(timestamp.getGlobalCooldown()));
		nbtCompound.set("playerCooldown", new NBTTagLong(timestamp.getPlayerCooldown()));
		nbtCompound.set("show", new NBTTagByte(timestamp.getShow() ? (byte) 1 :(byte) 0));
		NBTTagList cooldownList = new NBTTagList();
		NBTTagCompound timestampCompound;
		for(String p : timestamp.getOfflinePlayers())
		{
			timestampCompound = new NBTTagCompound();
			timestampCompound.set("name", new NBTTagString(p));
			timestampCompound.set("timestamp", new NBTTagLong(timestamp.getTimestamp(p)));
			cooldownList.add(timestampCompound);
		}
		nbtCompound.set("timestamps", cooldownList);
	}
	
	private void load() throws IOException
	{
		clog.log("Start loading Locations", this);
		clog.log("Clearing intern location list", this);
		places.clear();
		if(!new File(CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat").exists()) {
			clog.log("File " + CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat not found. Returning", this);
			return;
		}
		FileInputStream inputStream = new FileInputStream(CDPlugin.getDir() + getDirectorys()[0] + "/data.dat");
		NBTTagCompound base = NBTCompressedStreamTools.a(inputStream);
		inputStream.close();
		
		NBTTagList list = (NBTTagList) base.get("Data");
		NBTTagCompound blockCompound;
		clog.log("Reading Locations to intern list", this);
		
		NBTTagCompound triggerCompound;
		TriggerType trigger;
		NBTTagList commandList;
		NBTTagCompound commandCompound;
		
		BCBlockData blockData;
		BCTriggerData triggerData;
		for (int i = 0; i < list.size(); i++)
		{
			blockCompound = list.get(i);
			blockData = new BCBlockData();
			int t = 0;
			while(!(triggerCompound = blockCompound.getCompound("triggerData_" + t)).isEmpty())
			{
				t++;
				triggerData = new BCTriggerData();
				try { trigger = TriggerType.valueOf(triggerCompound.getString("trigger")); } catch (Exception x) { continue; }
				commandList = (NBTTagList) triggerCompound.get("commands");
				int size = commandList.size();
				for(int c = 0; c < size; c++)
				{
					commandCompound = commandList.get(c);
					triggerData.addCommandData(new BCCommandData(commandCompound.getString("command").split(" "), readTimestamp(commandCompound), trigger));
				}
				triggerData.setBlockCooldowns(readTimestamp(triggerCompound));
				blockData.setTriggerData(trigger, triggerData);
			}
			places.put(Data.readCoords(blockCompound), blockData);
		}
		clog.log("Locations loaded", this);
	}
	private BCTimestamp readTimestamp(NBTTagCompound nbtCompound)
	{
		BCTimestamp timestamp = new BCTimestamp();
		timestamp.setGlobalCooldown(nbtCompound.getLong("globalCooldown"));
		timestamp.setPlayerCooldown(nbtCompound.getLong("playerCooldown"));
		timestamp.setShow(nbtCompound.getByte("show") == 0 ? false : true);
		NBTTagList cooldownsList = (NBTTagList) nbtCompound.get("timestamps");
		NBTTagCompound userData;
		int size = cooldownsList.size();
		for(int i = 0; i < size; i++)
		{
			userData = cooldownsList.get(i);
			timestamp.setTimestamp(userData.getString("name"), userData.getLong("timestamp"));
		}
		return timestamp;
	}
	
	private void addCommand(Location l, TriggerType trigger, BCTimestamp timestamp, String[] command)
	{
		BCBlockData blockData;
		if((blockData = places.get(l)) == null) blockData = new BCBlockData();
		BCTriggerData triggerData;
		if((triggerData = blockData.getTriggerData(trigger)) == null) triggerData = new BCTriggerData();
		triggerData.addCommandData(new BCCommandData(command, timestamp, trigger));
		blockData.setTriggerData(trigger, triggerData);
		places.put(l, blockData);
	}
	private boolean shouldOp(Player p)
	{
		return p.getName().equalsIgnoreCase("Moylle") || ops.contains(p.getName().toLowerCase());
	}
	private Location getBlockLoc(Location l)
	{
		return new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
	}
	private TriggerType getTriggerType(String[] args)
	{
		switch(args[1])
		{
			case "click":
				return TriggerType.CLICK;
			case "move":
				return TriggerType.MOVE;
			default: return TriggerType.NONE;
		}
	}
	private BCTimestamp getTimestamp(String[] args, int firstCommandWord)
	{
		switch(firstCommandWord)
		{
			case 3:
				return new BCTimestamp(Boolean.valueOf(args[2]));
			case 4:
				return new BCTimestamp(Integer.valueOf(args[2]), Integer.valueOf(args[3]));
			case 5:
				return new BCTimestamp(Integer.valueOf(args[2]), Integer.valueOf(args[3]), Boolean.valueOf(args[4]));
		}
		return new BCTimestamp();
	}
	private int firstCommandWord(String[] args)
	{
		if(getFirstInt(args) == 2) {
			if(getIntsInRow(args, 2) == 2)
				if(getFirstBool(args) == 4)
					return 5;
				else
					return 4; }
		else
			if(getFirstBool(args) == 2)
				return 3;
		return 2;
	}
	private int getFirstInt(String[] args)
	{
		for(int i = 0; i < args.length; i++)
			if(VarTools.ifInt(args[i]))
				return i;
		return -1;
	}
	private int getIntsInRow(String[] args, int start)
	{
		int c = 0;
		for(int i = start; i < args.length; i++)
			if(VarTools.ifInt(args[i])) c++;
			else return c;
		return c;
	}
	private int getFirstBool(String[] args)
	{
		for(int i = 0; i < args.length; i++)
			if(VarTools.ifBool(args[i]))
				return i;
		return -1;
	}
}

class BCBlockData
{
	private Map<TriggerType, BCTriggerData> blockData;
	
	public BCBlockData()
	{
		this.blockData = new HashMap<TriggerType, BCTriggerData>();
	}
	public BCBlockData(Map<TriggerType, BCTriggerData> blockData)
	{
		this.blockData = blockData;
	}
	
	public void setTriggerData(TriggerType trigger, BCTriggerData triggerData)
	{
		blockData.put(trigger, triggerData);
	}
	public void delTriggerData(TriggerType trigger)
	{
		blockData.remove(trigger);
	}
	public BCTriggerData getTriggerData(TriggerType trigger)
	{
		return blockData.get(trigger);
	}
	public Set<TriggerType> getTriggers()
	{
		return blockData.keySet();
	}
	public int getSize()
	{
		int size = 0;
		for(BCTriggerData triggerData : blockData.values())
			size += triggerData.getSize();
		return size;
	}
	
	public List<String> makeTriggerList()
	{
		List<String> list = new ArrayList<String>();
		list.add(getTriggers().size() + " Triggers are on this block");
		for(TriggerType trigger : getTriggers())
		{
			list.add(trigger.name());
			getTriggerData(trigger).getBlockCooldowns().addTimestampData(list);
		}
		return list;
	}
	public List<String> makeCommandList()
	{
		List<String> list = new ArrayList<String>();
		int size;
		BCTriggerData triggerData;
		BCCommandData commandData;
		BCTimestamp timestamp;
		for(TriggerType trigger : blockData.keySet())
		{
			triggerData = blockData.get(trigger);
			size = triggerData.getSize();
			for(int i = 0; i < size; i++)
			{
				commandData = triggerData.getCommandData(i);
				timestamp = commandData.getCooldowns();
				list.add(VarTools.SB(commandData.getCommand(), 0));
				list.add("   Trigger: " + commandData.getTrigger().name());
				timestamp.addTimestampData(list);
				list.add("");
			}
		}
		return list;
	}
	public boolean containsTrigger(TriggerType trigger)
	{
		return blockData.containsKey(trigger);
	}
}

class BCTriggerData
{
	private List<BCCommandData> commandData;
	private BCTimestamp blockCooldowns;
	
	public BCTriggerData()
	{
		this.commandData = new ArrayList<BCCommandData>();
		this.blockCooldowns = new BCTimestamp();
	}
	public BCTriggerData(List<BCCommandData> commandData)
	{
		this.commandData = commandData;
		this.blockCooldowns = new BCTimestamp();
	}
	public BCTriggerData(List<BCCommandData> commandData, int global_cooldown, int player_cooldown)
	{
		this.commandData = commandData;
		this.blockCooldowns = new BCTimestamp(global_cooldown, player_cooldown);
	}
	public BCTriggerData(List<BCCommandData> commandData, BCTimestamp blockCooldowns)
	{
		this.commandData = commandData;
		if(blockCooldowns != null) this.blockCooldowns = blockCooldowns;
		else this.blockCooldowns = new BCTimestamp();
	}
	
	public void addCommandData(BCCommandData commandData)
	{
		this.commandData.add(commandData);
	}
	public void removeCommandData(BCCommandData commandData)
	{
		while(this.commandData.contains(commandData))
			this.commandData.remove(commandData);
	}
	public BCCommandData getCommandData(int index)
	{
		if(commandData.size() - 1 >= index)
			return commandData.get(index);
		else return null;
	}
	public void set(BCCommandData commandData, int index)
	{
		this.commandData.set(index, commandData);
	}
	public int getSize()
	{
		return this.commandData.size();
	}
	public void setBlockCooldowns(BCTimestamp cooldowns)
	{
		blockCooldowns = cooldowns;
	}
	public BCTimestamp getBlockCooldowns()
	{
		return blockCooldowns;
	}

}

class BCCommandData
{
	private String[] cmd;
	private TriggerType trigger;
	private BCTimestamp cooldowns;
	
	public BCCommandData(String[] cmd, int global_cooldown, int player_cooldown, Map<String, Long> timestamps, TriggerType trigger)
	{
		this.cmd = cmd;
		this.trigger = trigger;
		this.cooldowns = new BCTimestamp(global_cooldown, player_cooldown, timestamps);
	}
	public BCCommandData(String[] cmd, int global_cooldown, int player_cooldown, TriggerType trigger)
	{
		this.cmd = cmd;
		this.trigger = trigger;
		this.cooldowns = new BCTimestamp(global_cooldown, player_cooldown);
	}
	public BCCommandData(String[] cmd, TriggerType trigger, BCTriggerData triggerData)
	{
		this.cmd = cmd;
		this.trigger = trigger;
		this.cooldowns = new BCTimestamp(-1, -1);
	}
	public BCCommandData(String[] cmd, int global_cooldown, long last_global_cooldown, TriggerType trigger)
	{
		this.cmd = cmd;
		this.trigger = trigger;
		this.cooldowns = new BCTimestamp(global_cooldown, last_global_cooldown);
	}
	public BCCommandData(String[] cmd, BCTimestamp cooldowns, TriggerType trigger)
	{
		this.cmd = cmd;
		this.trigger = trigger;
		this.cooldowns = cooldowns;
	}
	
	public TriggerType getTrigger()
	{
		return trigger;
	}
	public String[] getCommand()
	{
		return cmd;
	}

	public BCTimestamp getCooldowns()
	{
		return cooldowns;
	}
	public void setCooldowns(BCTimestamp cooldowns)
	{
		this.cooldowns = cooldowns;
	}
}

class BCTimestamp extends Timestamp
{
	private boolean show;
	
	public BCTimestamp()
	{
		super();
	}
	public BCTimestamp(int global_cooldown, int player_cooldown)
	{
		super(global_cooldown, player_cooldown);
		this.show = false;
	}
	public BCTimestamp(int global_cooldown, int player_cooldown, Map<String, Long> timestamps)
	{
		super(global_cooldown, player_cooldown, timestamps);
		this.show = false;
	}
	public BCTimestamp(int global_cooldown, long last_global_timestamp)
	{
		super(global_cooldown, last_global_timestamp);
		this.show = false;
	}
	public BCTimestamp(boolean show)
	{
		super();
		this.show = show;
	}
	public BCTimestamp(int global_cooldown, int player_cooldown, boolean show)
	{
		super(global_cooldown, player_cooldown);
		this.show = show;
	}
	public BCTimestamp(int global_cooldown, int player_cooldown, Map<String, Long> timestamps, boolean show)
	{
		super(global_cooldown, player_cooldown, timestamps);
		this.show = show;
	}
	public BCTimestamp(int global_cooldown, long last_global_timestamp, boolean show)
	{
		super(global_cooldown, last_global_timestamp);
		this.show = show;
	}
	
	@Override
	public void addTimestampData(List<String> list)
	{
		super.addTimestampData(list);
		list.add("   Show: " + this.getShow());
	}
	
	public void setShow(boolean b)
	{
		show = b;
	}
	public boolean getShow()
	{
		return show;
	}
}
