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
import net.minecraft.server.v1_7_R1.NBTBase;
import net.minecraft.server.v1_7_R1.NBTTagList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.bukkit.selections.Selection;

public class VarTools
{
	public static String SB(String[] input, int start)
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
				back.add(VarTools.SB(sArr, 0));
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
		return o instanceof Player;
	}
}
