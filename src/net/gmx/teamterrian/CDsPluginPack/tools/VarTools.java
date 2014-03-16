package net.gmx.teamterrian.CDsPluginPack.tools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDUnsupportedPacketModifierException;
import net.minecraft.server.v1_7_R1.NBTBase;
import net.minecraft.server.v1_7_R1.NBTTagByte;
import net.minecraft.server.v1_7_R1.NBTTagByteArray;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagDouble;
import net.minecraft.server.v1_7_R1.NBTTagFloat;
import net.minecraft.server.v1_7_R1.NBTTagInt;
import net.minecraft.server.v1_7_R1.NBTTagIntArray;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagLong;
import net.minecraft.server.v1_7_R1.NBTTagShort;
import net.minecraft.server.v1_7_R1.NBTTagString;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class VarTools
{
	//static boolean unix = !System.getProperty("os.name").toLowerCase().contains("windows");
	
	private enum NBTType
	{
		INT,
		STRING,
		DOUBLE,
		FLOAT,
		BYTEARR,
		INTARR,
		COMPOUND,
		BYTE,
		SHORT,
		LONG,
		LIST,
		_STRINGARR
	}
	public static String arrToString(String[] input, int start)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = start; i < input.length; i++)
			sb.append(" " + input[i]);
		String s = sb.toString();
		if(s.length() != 0) s = s.substring(1);
		return s;
	}
	public static String[] stringToArr(String input, int start)
	{
		return subArr(input.split(" "), start);
	}
	
	public static String[] subArr(String[] input, int start)
	{
		if(start >= input.length) return new String[0];
		String[] back = new String[input.length - start];
		for(int i = start; i < input.length; i++)
			back[i - start] = input[i];
		return back;
	}
	public static String[] replaceArr(String[] input, String search, String replacement)
	{
		String[] back = new String[input.length];
		for(int i = 0; i < input.length; i++)
			back[i] = (input[i].equals(search) ? replacement : input[i]);
		return back;
	}
	
	public static String parse(Location l, boolean pitch, boolean yaw, boolean blockCoords)
	{
		StringBuilder sb = new StringBuilder();
		if(blockCoords) sb.append("x:" + l.getBlockX() + ", y:" + l.getBlockY() + ", z:" + l.getBlockZ());
		else sb.append("x:" + l.getX() + ", y:" + l.getY() + ", z:" + l.getZ());
		if(pitch) sb.append(", pitch:" + l.getPitch());
		if(yaw) sb.append(", yaw:" + l.getYaw());
		return sb.toString();
	}
	
	public static String[] toArray(List<String> list)
	{
		String[] back = new String[list.size()];
		for(int i = 0; i < list.size(); i++)
			back[i] = list.get(i);
		return back;
	}
	public static List<String> toList(String[] array)
	{
		List<String> back = new ArrayList<String>();
		for(String s : array)
			back.add(s);
		return back;
	}
	
	
	public static List<String[]> makeStringArrList(List<String> list)
	{
		List<String[]> back = new ArrayList<String[]>();
		for(int i = 0; i < list.size(); i++)
			back.add(VarTools.stringToArr(list.get(i), 0));
		return back;
	}
	public static List<String> makeStringList(List<String[]> list)
	{
		List<String> back = new ArrayList<String>();
		if(list != null)
			for(String[] sArr : list)
				back.add(VarTools.arrToString(sArr, 0));
		return back;
	}
	public static Map<String[], Boolean> combineLists(List<String[]> first, List<String[]> second)
	{
		Map<String[], Boolean> back = new HashMap<String[], Boolean>();
		if(first != null)
			for(String[] o : first)
				back.put(o, true);
		if(second != null)
			for(String[] o : second)
				back.put(o, false);
		return back;
	}
	public static List<Location> combineSets(Set<Location> first, Set<Location> second)
	{
		List<Location> back = new ArrayList<Location>(first);
		if(second != null)
			for(Location o : second)
				back.add(o);
		return back;
	}
	
	public static NBTTagList convertList(List<? extends NBTBase> list)
	{
		NBTTagList back = new NBTTagList();
		for(NBTBase b : list) back.add(b);
		return back;
	}
	
	public static void addBeg(List<String> list, String beg)
	{
		for(int i = 0; i < list.size(); i++)
			list.set(i, beg + list.get(i));
	}
	public static boolean ifInt(String input) { try { Integer.valueOf(input); return true; } catch (Exception x) { return false; } }
	public static boolean ifBool(String input) { return (input.equalsIgnoreCase("true") || input.equals("false")); }
	
	public static Inventory cloneInventory(Inventory i)
	{
		Inventory cloned = Bukkit.createInventory(i.getHolder(), i.getSize(), i.getName());
		cloned.setContents(i.getContents());
		return cloned;
	}
	
	public static ItemStack[] combineArray(ItemStack[] a1, ItemStack[] a2)
	{
		ItemStack[] back = new ItemStack[a1.length + a2.length];
		for(int i = 0; i < a1.length; i++)
			back[i] = a1[i];
		for(int i = a1.length; i < back.length; i++)
			back[i] = a2[i - a1.length];
		return back;
	}
	
	public static List<Location> getLocations(Selection s)
	{
		List<Location> back = new ArrayList<Location>();
		Location l1 = s.getMinimumPoint();
		Location l2 = s.getMaximumPoint();
		int x1 = l1.getBlockX(),
			y1 = l1.getBlockY(),
			z1 = l1.getBlockZ(),
			x2 = l2.getBlockX(),
			y2 = l2.getBlockY(),
			z2 = l2.getBlockZ();
		World w = l1.getWorld();
		for(int ix = x1; ix <= x2; ix++)
			for(int iy = y1; iy <= y2; iy++)
				for(int iz = z1; iz <= z2; iz++)
					back.add(new Location(w, ix, iy, iz));
		return back;
	}
	
	public static List<Entry<Object, Method>> reverseList(List<Entry<Object, Method>> input)
	{
		List<Entry<Object, Method>> back = new ArrayList<Entry<Object, Method>>();
		for(int i = input.size() - 1; i >= 0; i--)
			back.add(input.get(i));
		return back;
	}
	
	public static void sortCalls(Map<String, List<Entry<Object, Method>>> map, Class<? extends Annotation> a)
	{
		List<Entry<Object, Method>> val;
		List<Entry<Object, Method>> newval;
		for(String str : map.keySet())
		{
			val = map.get(str);
			newval = new ArrayList<Entry<Object, Method>>();
			for(int p = 0; !val.isEmpty() && p <= 10000; p++)
				for(Entry<Object, Method> entry : new ArrayList<Entry<Object, Method>>(val))
					if(getPriority(entry.getValue(), a) == p)
					{
						val.remove(entry);
						newval.add(entry);
					}
			map.put(str, VarTools.reverseList(newval));
		}
	}
	private static int getPriority(Method m, Class<? extends Annotation> a)
	{
		if(a.isAssignableFrom(CDPluginEvent.class))
			return m.getAnnotation(CDPluginEvent.class).priority();
		if(a.isAssignableFrom(CDPluginPacket.class))
			return m.getAnnotation(CDPluginPacket.class).priority();
		if(a.isAssignableFrom(CDPluginCommand.class))
			return m.getAnnotation(CDPluginCommand.class).priority();
		return -1;
	}
	
	public static Collection<Command> cloneCollection(Collection<Command> collection)
	{
		Collection<Command> back = new ArrayList<Command>();
		for(Command cmd : collection) back.add(cmd);
		return back;
	}
	public static Location[] getBox(String[] args, int start, World w)
	{
		Double[] points = new Double[] { null, null, null, null, null, null };
		for(int i = 0; i < 6; i++)
			points[i] = Double.valueOf(args[i + start]);
		return getBox(points, w);
	}
	public static Location[] getBox(Double[] points, World w)
	{
		return new Location[] { new Location(w, points[0], points[1], points[2]), new Location(w, points[3], points[4], points[5]) };
	}
	public static Location[] getBox(Location middle, int radius)
	{
		Location[] back = new Location[2];
		double
			x = middle.getX(),
			y = middle.getY(),
			z = middle.getZ();
		back[0] = new Location(middle.getWorld(), x - radius, y - radius, z - radius);
		back[1] = new Location(middle.getWorld(), x + radius, y + radius, z + radius);
		return back;
	}
	public static Location getPoint(String[] args, int start, World w)
	{
		double[] points = new double[3];
		for(int i = 0; i < 3; i++)
			points[i] = Double.valueOf(args[i + start]);
		return new Location(w, points[0], points[1], points[2]);
	}
	public static List<Location> getLocations(Location[] points)
	{
		List<Location> back = new ArrayList<Location>();
		int[] coords = new int[]
		{
			points[0].getBlockX(),
			points[1].getBlockX(),
			points[0].getBlockY(),
			points[1].getBlockY(),
			points[0].getBlockZ(),
			points[1].getBlockZ()
		};
		sortNumberPairs(coords);
		World w = points[0].getWorld();
		for(int ix = coords[0]; ix <= coords[1]; ix++)
			for(int iy = coords[2]; iy <= coords[3]; iy++)
				for(int iz = coords[4]; iz <= coords[5]; iz++)
					back.add(new Location(w, ix, iy, iz));
		return back;
	}
	public static void sortNumberPairs(int[] n)
	{
		int[] temp = new int[2];
		for(int i = 0; i + 1 < n.length; i += 2)
		{
			temp[0] = n[i];
			temp[1] = n[i + 1];
			sortNumberPair(temp);
			n[i] = temp[0];
			n[i + 1] = temp[1];
		}
			
	}
	public static void sortNumberPair(int[] n)
	{
		int temp;
		if(n[0] > n[1])
		{
			temp = n[0];
			n[0] = n[1];
			n[1] = temp;
		}
	}
	
	public static boolean isPlayer(Object o)
	{
		return (o instanceof Player);
	}
	public static void showMessages(List<String> messages, Player p)
	{
		for(String line : messages)
			p.sendMessage(line);
	}
	public static Location readCoords(NBTTagCompound cpd)
	{
		return new Location(Bukkit.getWorld(cpd.getString("world")), cpd.getInt("x"), cpd.getInt("y"), cpd.getInt("z"));
	}
	private static void readModifier(NBTBase o, NBTType t, PacketContainer pc, int id)
	{
		switch(t)
		{
			case INT:
				pc.getModifier().write(id, ((NBTTagInt) o).d());
				break;
			case STRING:
				pc.getModifier().write(id, ((NBTTagString) o).a_());
				break;
			case DOUBLE:
				pc.getModifier().write(id, ((NBTTagDouble) o).g());
				break;
			case FLOAT:
				pc.getModifier().write(id, ((NBTTagFloat) o).h());
				break;
			case BYTEARR:
				pc.getModifier().write(id, ((NBTTagByteArray) o).c());
				break;
			case INTARR:
				pc.getModifier().write(id, ((NBTTagIntArray) o).c());
				break;
			case COMPOUND:
				pc.getModifier().write(id, (NBTTagCompound) o);
				break;
			case BYTE:
				pc.getModifier().write(id, ((NBTTagByte) o).f());
				break;
			case LONG:
				pc.getModifier().write(id, ((NBTTagLong) o).c());
				break;
			case SHORT:
				pc.getModifier().write(id, ((NBTTagShort) o).e());
				break;
			case LIST:
				pc.getModifier().write(id, (NBTTagList) o);
			case _STRINGARR:
				NBTTagList strList = (NBTTagList) o;
				String[] strings = new String[strList.size()];
				for(int i = 0; i < strList.size(); i++)
					strings[i] = strList.f(i);
				pc.getModifier().write(id, strings);
				break;
		}
	}
	public static void writeCoords(NBTTagCompound cpd, Location l)
	{
		cpd.set("x", new NBTTagInt(l.getBlockX()));
		cpd.set("y", new NBTTagInt(l.getBlockY()));
		cpd.set("z", new NBTTagInt(l.getBlockZ()));
		cpd.set("world", new NBTTagString(l.getWorld().getName()));
	}
	private static void writeModifier(Object o, NBTType t, NBTTagCompound cpd)
	{
		switch(t)
		{
			case INT:
				cpd.set("Type", new NBTTagString("INT"));
				cpd.set("Value", new NBTTagInt((int) o));
				break;
			case STRING:
				cpd.set("Type", new NBTTagString("STRING"));
				cpd.set("Value", new NBTTagString((String) o));
				break;
			case DOUBLE:
				cpd.set("Type", new NBTTagString("DOUBLE"));
				cpd.set("Value", new NBTTagDouble((double) o));
				break;
			case FLOAT:
				cpd.set("Type", new NBTTagString("FLOAT"));
				cpd.set("Value", new NBTTagFloat((float) o));
				break;
			case BYTEARR:
				cpd.set("Type", new NBTTagString("BYTEARR"));
				cpd.set("Value", new NBTTagByteArray((byte[]) o));
				break;
			case INTARR:
				cpd.set("Type", new NBTTagString("INTARR"));
				cpd.set("Value", new NBTTagIntArray((int[]) o));
				break;
			case COMPOUND:
				cpd.set("Type", new NBTTagString("COMPOUND"));
				cpd.set("Value", (NBTTagCompound) o);
				break;
			case BYTE:
				cpd.set("Type", new NBTTagString("BYTE"));
				cpd.set("Value", new NBTTagByte((byte) o));
				break;
			case LONG:
				cpd.set("Type", new NBTTagString("LONG"));
				cpd.set("Value", new NBTTagLong((long) o));
				break;
			case SHORT:
				cpd.set("Type", new NBTTagString("SHORT"));
				cpd.set("Value", new NBTTagShort((short) o));
				break;
			case LIST:
				cpd.set("Type", new NBTTagString("LIST"));
				cpd.set("Value", (NBTTagList) o);
			case _STRINGARR:
				cpd.set("Type", new NBTTagString("_STRINGARR"));
				NBTTagList strList = new NBTTagList();
				for(String s : (String[]) o)
					strList.add(new NBTTagString(s));
				cpd.set("Value", strList);
				break;
			default:
				break;
		}
	}
	public static PacketType getPacketType(String type, boolean serverSide)
	{
		for(PacketType packetType : PacketType.values())
			if(packetType.isServer() != serverSide) continue;
			else if(packetType.name().equalsIgnoreCase(type))
				return packetType;
		return null;
	}
	private static NBTType getNBTType(Object o)
	{
		if(o instanceof Integer)
			return NBTType.INT;
		else if(o instanceof String)
			return NBTType.STRING;
		else if(o instanceof Double)
			return NBTType.DOUBLE;
		else if(o instanceof Float)
			return NBTType.FLOAT;
		else if(o instanceof byte[])
			return NBTType.BYTEARR;
		else if(o instanceof int[])
			return NBTType.INTARR;
		else if(o instanceof NBTTagCompound)
			return NBTType.COMPOUND;
		else if(o instanceof Byte)
			return NBTType.BYTE;
		else if(o instanceof Short)
			return NBTType.SHORT;
		else if(o instanceof Long)
			return NBTType.LONG;
		else if(o instanceof NBTTagList)
			return NBTType.LIST;
		else if(o instanceof String[])
			return NBTType._STRINGARR;
		else return null;
	}
	private static NBTType getNBTType(NBTTagCompound cpd)
	{
		return NBTType.valueOf(cpd.getString("Type").toUpperCase());
	}
	public static List<String> convertNBTList(NBTTagList nbtList)
	{
		List<String> back = new ArrayList<String>();
		for(int i = 0; i < nbtList.size(); i++)
			back.add(nbtList.f(i));
		return back;
	}
	public static NBTTagList convertNBTList(List<String> list)
	{
		NBTTagList back = new NBTTagList();
		for(String s : list)
			back.add(new NBTTagString(s));
		return back;
	}
	public static NBTTagCompound writePacket(PacketContainer pc) throws CDUnsupportedPacketModifierException
	{
		NBTTagCompound base = new NBTTagCompound();
		if(pc == null) return base;
		base.set("Type", new NBTTagString(pc.getType().name()));
		base.set("ServerSide", new NBTTagByte((byte) (pc.getType().isServer() ? 1 : 0)));
		NBTTagList modifier = new NBTTagList();
		NBTType type;
		NBTTagCompound modCpd;
		StructureModifier<Object> objects = pc.getModifier();
		int size = objects.getValues().size();
		Object o;
		
		for(int i = 0; i < size; i++)
		{
			o = objects.read(i);
			type = getNBTType(o);
			if(type == null) throw new CDUnsupportedPacketModifierException(o);
			
			modCpd = new NBTTagCompound();
			modCpd.set("id", new NBTTagInt(i));
			writeModifier(o, type, modCpd);
			modifier.add(modCpd);
		}
		base.set("Data", modifier);
		return base;
	}
	public static PacketContainer readPacket(NBTTagCompound cpd)
	{
		PacketType packetType = getPacketType(cpd.getString("Type"), cpd.getByte("ServerSide") == (byte) 1);
		if(packetType == null) return null;
		PacketContainer pc = new PacketContainer(packetType);
		NBTTagList data = (NBTTagList) cpd.get("Data");
		NBTTagCompound modCpd;
		NBTBase value;
		for(int i = 0; i < data.size(); i++)
		{
			modCpd = data.get(i);
			value = modCpd.get("Value");
			readModifier(value, getNBTType(modCpd), pc, modCpd.getInt("id"));
		}
		return pc;
	}
	
	public static String[] combineArray(String[] a1, String[] a2)
	{
		String[] back = new String[a1.length + a2.length];
		for(int i = 0; i < a1.length; i++)
			back[i] = a1[i];
		for(int i = 0; i < a2.length; i++)
			back[i + a1.length] = a2[i];
		return back;
	}
}

//static boolean unix = !System.getProperty("os.name").toLowerCase().contains("windows");

