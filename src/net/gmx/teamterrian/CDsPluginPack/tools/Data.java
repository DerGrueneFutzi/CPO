package net.gmx.teamterrian.CDsPluginPack.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.gmx.teamterrian.CDsPluginPack.CDsPluginPack;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R1.NBTTagCompound;

public class Data
{
	static CDsPluginPack cdpp = CDsPluginPack.getInstance();
	static Log clog = cdpp.handler.clog;
	//static boolean unix = !System.getProperty("os.name").toLowerCase().contains("windows");
	
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
	
	public static long getTimestamp()
	{
		return System.currentTimeMillis() / 1000;
	}
	public static String getTime()
	{
		return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
	}
	
	public static String readFile(String path, Charset encoding)  throws IOException 
	{
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
}
