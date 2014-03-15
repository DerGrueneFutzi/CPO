package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.sk89q.worldedit.bukkit.selections.Selection;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginDisableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDNoPermissionException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDNullSelectionException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDPlayerNotFoundException;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDUnsupportedPacketModifierException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys.Dependency;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;
import net.minecraft.server.v1_7_R1.NBTTagByte;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagInt;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagString;

public class FakeBlocks extends CDPlugin
{
	Dependencys d;
	Log clog;
	Map<Location, FBBlockData> blocks = new CDHashMap<Location, FBBlockData>();
	String mbeg = ChatColor.DARK_GREEN + "[FakeBlocks] " + ChatColor.BLUE;
	
	public FakeBlocks(PluginHandler handler)
	{
		super(handler);
		d = handler.dependencys;
		clog = handler.clog;
	}
	
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try
		{
			clog.log("Loading Blocks", this);
			load();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while loading Blocks", this);
			e.setSuccess(this, false);
		}
	}
	@CDPluginEvent
	public void onDisable(CDPluginDisableEvent e)
	{
		try
		{
			clog.log("Saving Blocks", this);
			save();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while saving Blocks", this);
			e.setSuccess(this, false);
		}
	}
	
	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.fb", PermissionDefault.OP),
			new Permission("cdpp.fb.io", PermissionDefault.OP)
		};
	}
	public String[] getDirectorys() { return new String[] { "FakeBlocks" }; }
	
	@CDPluginCommand(commands = { "fb cdpp.fb 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException, CDNullSelectionException, InvocationTargetException, CDPlayerNotFoundException, CDNoPermissionException
	{
		if(!d.doDepend(Dependency.WORLDEDIT, e.getSender())) return;
		String[] args = e.getArgs();
		if(args.length == 0) throw new CDInvalidArgsException(e.getCommand().getName());
		switch(args[0])
		{
			case "add":
				add(e);
				return;
			case "del":
				del(e);
				return;
			case "update":
				update(e);
				return;
			case "save":
				save(e.getSender());
				return;
			case "load":
				load(e.getSender());
				return;
		}
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
	private void add(CommandEvent e) throws CDNullSelectionException
	{
		String[] args = e.getArgs();
		FBBlockData blockData;
		int counter = 0;
		Selection s = d.we.getSelection((Player) e.getSender());
		if(s == null) throw new CDNullSelectionException();
		String perm = args.length == 2 ? args[1] : null;
		for(Location l : VarTools.getLocations(s))
		{
			if((blockData = blocks.get(l)) == null) blockData = new FBBlockData();
			blockData.addFakeBlock(perm, l);
			blocks.put(l, blockData);
			counter++;
		}
		e.getSender().sendMessage(mbeg + counter + " Blocks added");
		clog.log("Added " + counter + " Blocks from " + e.getSender().getName(), this);
		try { save(); }
		catch (Exception x) { e.getSender().sendMessage(mbeg + ChatColor.RED + "The Blocks were added, but there is a problem in saving the data"); }
	}
	private void del(CommandEvent e) throws CDNullSelectionException
	{
		int counter = 0;
		CommandSender sender = e.getSender();
		Selection s = d.we.getSelection((Player) sender);
		if(s == null) throw new CDNullSelectionException();
		for(Location l : VarTools.getLocations(s))
			if(blocks.containsKey(l)) {
				blocks.remove(l);
				counter++;
			}
		e.getSender().sendMessage(mbeg + counter + " Blocks removed");
		clog.log("Removed " + counter + " Blocks from " + sender.getName(), this);
		try { save(); }
		catch (Exception x) { e.getSender().sendMessage(mbeg + ChatColor.RED + "The Blocks were removed, but there is a problem in saving the data"); }
	}
	private void update(CommandEvent e) throws CDPlayerNotFoundException, CDInvalidArgsException, InvocationTargetException
	{
		int counter = 0;
		String[] args = e.getArgs();
		FBPermData permData;
		if(args.length != 8) throw new CDInvalidArgsException(e.getCommand().getName());
		Player p = Bukkit.getPlayer(args[1]);
		if(p == null) throw new CDPlayerNotFoundException(args[1]);
		for(Location l : VarTools.getLocations(VarTools.getBox(e.getArgs(), 2, p.getWorld())))
		{
			if(!blocks.containsKey(l)) continue;
			counter++;
			permData = blocks.get(l).getBlock(p);
			if(permData != null)
				sendBlock(p, permData, l);
		}
		clog.log(counter + " Blocks for " + p.getName() + " updated", this);
	}
	
	private void sendBlock(Player p, FBPermData permData, Location l) throws InvocationTargetException
	{
		PacketContainer blockPacket = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
		StructureModifier<Object> sm = blockPacket.getModifier();
		sm.write(0, l.getBlockX());
		sm.write(1, l.getBlockY());
		sm.write(2, l.getBlockZ());
		sm.write(3, getBlock(permData.blockData[0]));
		sm.write(4, (int) permData.blockData[1]);
		ProtocolLibrary.getProtocolManager().sendServerPacket(p, blockPacket);
		if(permData.tileData == null) return;
		ProtocolLibrary.getProtocolManager().sendServerPacket(p, permData.tileData);
	}
	@SuppressWarnings("deprecation")
	private net.minecraft.server.v1_7_R1.Block getBlock(int id)
	{
		return CraftMagicNumbers.getBlock(id);
	}

	private void save(CommandSender sender) throws CDNoPermissionException
	{
		if(!sender.hasPermission("cdpp.fb.io")) throw new CDNoPermissionException(true);
		try { save(); sender.sendMessage(mbeg + ChatColor.GREEN + "Data saved"); }
		catch (Exception x) { x.printStackTrace(clog.getStream()); sender.sendMessage(mbeg + ChatColor.RED + "Error while saving the Data"); }
	}
	private void save() throws CDUnsupportedPacketModifierException, IOException
	{
		NBTTagCompound base = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		NBTTagCompound locCpd;
		NBTTagCompound permCpd;
		NBTTagList locList;
		for(Location l : blocks.keySet())
		{
			locCpd = new NBTTagCompound();
			Data.writeCoords(locCpd, l);
			locList = new NBTTagList();
			for(FBPermData permData : blocks.get(l).blocks)
			{
				permCpd = Data.writePacket(permData.tileData);
				permCpd.set("blockID", new NBTTagInt(permData.blockData[0]));
				permCpd.set("blockData", new NBTTagInt(permData.blockData[1]));
				permCpd.set("perm", new NBTTagString(permData.perm == null ? "" : permData.perm));
				locList.add(permCpd);
			}
			locCpd.set("permData", locList);
			list.add(locCpd);
		}
		base.set("Data", list);
		Data.secureSave(base, getDir() + getDirectorys()[0] + "/data.dat", this);
	}
	private void load(CommandSender sender) throws CDNoPermissionException
	{
		if(!sender.hasPermission("cdpp.fb.io")) throw new CDNoPermissionException(true);
		try { load(); sender.sendMessage(mbeg + ChatColor.GREEN + "Data loaded"); }
		catch (Exception x) { x.printStackTrace(clog.getStream()); sender.sendMessage(mbeg + ChatColor.RED + "Error while loading the Data"); }
	}
	private void load() throws IOException
	{
		clog.log("Clearing intern list", this);
		blocks.clear();
		clog.log("Loading File", this);
		NBTTagCompound base = Data.load(getDir() + getDirectorys()[0] + "/data.dat", this);
		if(base == null) return;
		clog.log("Reading File", this);
		NBTTagList list = (NBTTagList) base.get("Data");
		NBTTagCompound locCpd;
		NBTTagList locList;
		NBTTagCompound permCpd;
		FBPermData permData;
		FBBlockData blockData;
		for(int i = 0; i < list.size(); i++)
		{
			locCpd = list.get(i);
			blockData = new FBBlockData();
			locList = (NBTTagList) locCpd.get("permData");
			for(int j = 0; j < locList.size(); j++)
			{
				permCpd = locList.get(j);
				permData = new FBPermData(permCpd.getString("perm"), new Integer[] { permCpd.getInt("blockID"), permCpd.getInt("blockData") });
				permData.tileData = Data.readPacket(permCpd);
				blockData.blocks.add(permData);
			}
			blocks.put(Data.readCoords(locCpd), blockData);
		}
		clog.log("All Data read", this);
	}
}

class FBBlockData
{
	List<FBPermData> blocks = new ArrayList<FBPermData>();
	
	public void addFakeBlock(String perm, Location l)
	{
		addFakeBlock(perm, l.getBlock());
	}
	public void addFakeBlock(String perm, Block b)
	{
		blocks.add(new FBPermData(perm, b));
	}
	public FBPermData getBlock(CommandSender sender)
	{
		FBPermData back = null;
		for(FBPermData permData : blocks)
			if(permData.perm == null || sender.hasPermission(permData.perm))
				back = permData;
		return back;
	}
}
class FBPermData
{
	String perm;
	PacketContainer tileData;
	Integer[] blockData;
	
	@SuppressWarnings("deprecation")
	public FBPermData(String perm, Block bukkitBlock)
	{
		this.perm = perm;
		this.blockData = new Integer[] { bukkitBlock.getTypeId(), (int) bukkitBlock.getData() };
		tileData = getTileDataPacket(bukkitBlock);
	}
	public FBPermData(String perm, Integer[] blockData)
	{
		this.perm = perm;
		this.blockData = blockData;
		this.tileData = null;
	}
	
	private PacketContainer getTileDataPacket(Block b)
	{
		BlockState bs = b.getState();
		if(bs instanceof Sign)
			return getTileDataPacket((Sign) bs);
		else if(bs instanceof Skull)
			return getTileDataPacket((Skull) bs);
		return null;
	}
	private PacketContainer getTileDataPacket(Sign s)
	{
		PacketContainer pc = new PacketContainer(PacketType.Play.Server.UPDATE_SIGN);
		writeCoords(pc, s.getLocation());
		pc.getStringArrays().write(0, s.getLines());
		return pc;
	}
	private PacketContainer getTileDataPacket(Skull s)
	{
		PacketContainer pc = new PacketContainer(PacketType.Play.Server.TILE_ENTITY_DATA);
		NBTTagCompound cpd = new NBTTagCompound();
		cpd.set("ExtraType", new NBTTagString(s.getOwner()));
		cpd.set("id", new NBTTagString("Skull"));
		cpd.set("SkullType", new NBTTagByte((byte) s.getSkullType().ordinal()));
		cpd.set("Rot", new NBTTagByte((byte) s.getRotation().ordinal()));
		Data.writeCoords(cpd, s.getLocation());
		writeCoords(pc, s.getLocation());
		pc.getIntegers().write(3, 4);
		pc.getModifier().write(4, cpd);
		return pc;
	}
	
	private void writeCoords(PacketContainer pc, Location l)
	{
		StructureModifier<Integer> smi = pc.getIntegers();
		smi.write(0, l.getBlockX());
		smi.write(1, l.getBlockY());
		smi.write(2, l.getBlockZ());
	}
}
