package net.gmx.teamterrian.CDsPluginPack.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Log
{
	public String path;
	Logger log;
	private PrintStream p;
	
	public Log(String path, PluginHandler handler)
	{
		log = handler.log;
		archive();
		File f = new File(path);
		try
		{
			if(!f.exists()) f.mkdirs();
			p = new LogStream(new FileOutputStream(new File(path + "/latest.log"), true), false, "UTF-8");
		}
		catch (Exception x) { }
	}
	
	public void log(String message, Object c)
	{
		try
		{
			String name = (c == null ? "" : resolve(c.getClass().getName()));
			name = name.substring(name.lastIndexOf('.') + 1) + ": ";
			p.println(name + message);
			p.flush();
		}
		catch (Exception x) { }
	}
	private String resolve(String name)
	{
		switch(name)
		{
			case "d": return "HealingStones$InvulnerablingTask";
			case "g": return "NoMessage$TextPacketTask";
			default: return name;
		}
	}
	
	public PrintStream getStream()
	{
		notifyAdmins();
		return p;
	}
	public void close()
	{
		log("Closing Stream", this);
		p.close();
	}
	
	private void notifyAdmins()
	{
		for(Player p : Bukkit.getServer().getOnlinePlayers())
			if(p.hasPermission("cdpp.log.notify.error"))
				p.sendMessage(ChatColor.DARK_RED + "[Log] A intern plugin wants access to the LogStream. Probably an error occured.");
		log.warning("[CDPP] [Log] A intern plugin wants access to the LogStream. Probably an error occured.");
	}

	private void archive()
	{
		File f = new File("./plugins/CDsPluginPack/logs/latest.log"); 
		if(!f.exists()) return;
		try
		{
			String outFilename;
			try { outFilename = getFileName("./plugins/CDsPluginPack/logs/latest.log"); }
			catch (Exception x) {
				corrupt(f);
				return;
			}
	        String inFilename = "./plugins/CDsPluginPack/logs/latest.log";
	        archive(inFilename, outFilename);
            f.delete();
		}
		catch (Exception x)
		{
			x.printStackTrace();
		}
	}
	private void archive(String input, String output) throws IOException
	{
		BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(
                    new GZIPOutputStream(new FileOutputStream(output))
                ));

		BufferedReader bufferedReader = new BufferedReader(new FileReader(input));
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
		bufferedWriter.write(line);
		bufferedWriter.newLine();
		}
		bufferedWriter.flush();
		bufferedWriter.close();
		bufferedReader.close();
	}
	private void corrupt(File f) throws IOException
	{
		archive(f.getPath(), CDPlugin.getDir() + "logs/" + getCorruptFileName());
		f.delete();
	}
	private String getCorruptFileName()
	{
		return "corrupt_archived_on_" + new java.text.SimpleDateFormat("yyyy_MM_dd_ss-mm-HH").format(new java.util.Date()) + ".gz";
	}
	private String getFileName(String path) throws Exception
	{
		byte[] read = new byte[19];
		int left = 19;
		try
		{
			InputStream fs = new FileInputStream(path);
			while(left > 0)
			left -= fs.read(read, 19 - left, left);
			String back = "";
			for(int i = 0; i < read.length; i++)
		        back += (char)read[i];
			back = CDPlugin.getDir() + "logs/" + back.replace(":", "-").replace(' ', '_') + ".log.gz";
			fs.close();
			return back;
		}
		catch (Exception x)
		{
			System.out.println("Error while archiving the latest log. It is now marked as corrupt");
			System.out.println("Following the Stacktrace");
			x.printStackTrace();
			throw x;
		}
	}
}

class LogStream extends PrintStream
{
	public LogStream(OutputStream arg0, boolean arg1, String arg2) throws UnsupportedEncodingException
	{
		super(arg0, arg1, arg2);
	}
	
	@Override
	public void println(String s)
	{
		s = getBeg() + s;
		super.println(s);
	}
	@Override
	public void println(Object o)
	{
		o = getBeg() + o;
		super.println(o);
	}
	
	private String getBeg()
	{
		return Data.getTime() + " [CDPP] ";
	}
}
