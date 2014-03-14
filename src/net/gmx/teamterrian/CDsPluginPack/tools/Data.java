package net.gmx.teamterrian.CDsPluginPack.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

import net.gmx.teamterrian.CDsPluginPack.CDsPluginPack;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDUnsupportedPacketModifierException;
import net.minecraft.server.v1_7_R1.NBTBase;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
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

public class Data
{
	static CDsPluginPack cdpp = CDsPluginPack.getInstance();
	static Log clog = cdpp.handler.clog;
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

	public static NBTTagCompound load(String path, Object o) throws IOException
	{
		if(!new File(path).exists()) {
			clog.log("File " + path + " not found. Returning", o);
			return null;
		}
		FileInputStream inputStream = new FileInputStream(path);
		NBTTagCompound base = NBTCompressedStreamTools.a(inputStream);
		inputStream.close();
		return base;
	}
	public static void secureSave(NBTTagCompound nbtData, String path, Object o) throws IOException
	{
		FileOutputStream outputStream = new FileOutputStream(path + "~");
	    NBTCompressedStreamTools.a(nbtData, outputStream);
	    outputStream.flush();
	    outputStream.close();
	    clog.log("Finished saving", o);
	    clog.log("Moving " + path + "~ to " + path, o);
	    File f = new File(path + "~");
	    clog.log("Deleting old file", o);
	    File old = new File(path);
	    if(!old.exists())
	    	clog.log("File does not exists", o);
	    else if(!old.delete()) {
	    	clog.log("Error while deleting old file. Throwing exception", o);
	    	throw new IOException("Cannot delete File " + path);
	    }
	    else clog.log("Old File deleted", o);
	    f.renameTo(old);
	    clog.log("Moved", o);
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
	public static long getTimestamp()
	{
		return System.currentTimeMillis() / 1000;
	}
	public static void showMessages(List<String> messages, Player p)
	{
		for(String line : messages)
			p.sendMessage(line);
	}
	
	public static String getTime()
	{
		return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
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
	public static PacketType getPacketType(String type, boolean serverSide)
	{
		for(PacketType packetType : PacketType.values())
			if(packetType.isServer() != serverSide) continue;
			else if(packetType.name().equalsIgnoreCase(type))
				return packetType;
		return null;
	}
	private static NBTType getNBTType(NBTTagCompound cpd)
	{
		return NBTType.valueOf(cpd.getString("Type").toUpperCase());
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
	public static Location readCoords(NBTTagCompound cpd)
	{
		return new Location(Bukkit.getWorld(cpd.getString("world")), cpd.getInt("x"), cpd.getInt("y"), cpd.getInt("z"));
	}
	
	public static String readFile(String path, Charset encoding)  throws IOException 
	{
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
}
