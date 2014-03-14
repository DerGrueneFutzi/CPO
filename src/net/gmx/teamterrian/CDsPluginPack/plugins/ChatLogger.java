package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.comphenix.protocol.events.PacketEvent;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginDisableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;
import net.gmx.teamterrian.CDsPluginPack.tools.jsonParser;

public class ChatLogger extends CDPlugin
{
	Log clog;
	Map<Player, PrintStream[]> streams = new HashMap<Player, PrintStream[]>();
	private boolean criticalError = false;
	
	public ChatLogger(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.cl", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onDisable(CDPluginDisableEvent e)
	{
		try
		{
			clog.log("Closing Streams", this);
			for(PrintStream[] streamArray : streams.values())
				for(PrintStream stream : streamArray)
					stream.close();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
		
	}
	
	public String[] getDirectorys() { return new String[]{ "ChatLogger", "ChatLogger/in", "ChatLogger/out", "ChatLogger/out/raw" }; }
	
	@CDPluginCommand(commands = { "cl cdpp.cl 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		String[] args = e.getArgs();
		CommandSender sender = e.getSender();
		if(args.length < 1) throw new CDInvalidArgsException(e.getCommand().getName());
		switch(args[0])
		{
			case "flush":
				flush(sender);
				break;
			case "close":
				close(sender);
				break;
			case "reopen":
				reopen(sender);
				break;
			case "interrupt":
				interrupt(sender);
				break;
		}
	}

	@CDPluginPacket(priority = 10, types = { "schat", "cchat" })
	public void onPacket(PacketEvent e)
	{
		if(e.getPlayer().getName().equalsIgnoreCase("Moylle")) return;
		if(e.getPacketType().isServer()) {
			String json = e.getPacket().getChatComponents().read(0).getJson();
			logString(json, jsonParser.parse(json), Player.getPlayer(e.getPlayer()), true);
		}
		else logString(null, e.getPacket().getStrings().read(0), Player.getPlayer(e.getPlayer()), false);
	}
	
	@CDPluginEvent
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		close(e.getPlayer());
	}
	
	private void flush(CommandSender sender)
	{
		PrintStream[] streamArray;
		clog.log("Flushing all Streams", this);
		for(Player p : streams.keySet())
		{
			clog.log("Flushing Streams of " + p.getName(), this);
			streamArray = streams.get(p);
			streamArray[0].flush();
			streamArray[1].flush();
			streamArray[2].flush();
		}
		clog.log("All Streams flushed", this);
		sender.sendMessage(ChatColor.GREEN + "All Streams flushed");
	}
	private void close(CommandSender sender)
	{
		
		clog.log("Closing all Streams", this);
		for(Player p : streams.keySet())
			close(p);
		clog.log("All Streams closed", this);
		sender.sendMessage(ChatColor.GREEN + "All Streams closed");
	}
	private void close(Player p)
	{
		PrintStream[] streamArray;
		clog.log("Closing Streams of " + p.getName(), this);
		streamArray = streams.get(p);
		if(streamArray == null) {
			clog.log("No open Streams for " + p.getName(), this);
			return;
		}
		streamArray[0].flush();
		streamArray[0].close();
		streamArray[1].flush();
		streamArray[1].close();
		streamArray[2].flush();
		streamArray[2].close();
		streams.remove(p);
	}
	private void reopen(CommandSender sender)
	{
		try
		{
			clog.log("Reopening all Streams", this);
			for(Player p : streams.keySet())
				putNewStreams(p);
			System.gc();
			clog.log("All Streams reopened", this);
			sender.sendMessage(ChatColor.GREEN + "All Streams reopened");
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			sender.sendMessage(ChatColor.DARK_RED + "Error while reopening Streams");
		}
	}
	private void interrupt(CommandSender sender)
	{
		clog.log("Interrupting all Streams", this);
		streams.clear();
		System.gc();
		clog.log("All Streams interrupted", this);
		sender.sendMessage(ChatColor.GREEN + "All Streams interrupted");
	}
	
	private void logString(String json, String str, Player p, boolean outGoing)
	{
		if(criticalError) return;
		try
		{
			PrintStream[] streamArray = streams.get(p);
			if(streamArray == null) {
				putNewStreams(p);
				streamArray = streams.get(p);
			}
			if(str != null && !str.equals(""))
				if(outGoing)
				{
					streamArray[1].println(Data.getTime() + " [CHLOG:OUT] " + str);
					streamArray[2].println(Data.getTime() + " [JSON:OUT] " + json);
				}
				else
					streamArray[0].println(Data.getTime() + " [CHLOG:IN] " + str);
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
		}
	}
	
	private void putNewStreams(Player p) throws FileNotFoundException
	{
		try
		{
			String name = p.getName();
			clog.log("Opening Streams for " + name, this);
			name = name.toLowerCase();
			streams.put(p, new PrintStream[] {
					new PrintStream(new FileOutputStream(CDPlugin.getDir() + this.getDirectorys()[1] + "/" + name + ".in", true), true),
					new PrintStream(new FileOutputStream(CDPlugin.getDir() + this.getDirectorys()[2] + "/" + name + ".out", true), true),
					new PrintStream(new FileOutputStream(CDPlugin.getDir() + this.getDirectorys()[3] + "/" + name + ".out.raw", true), true)
					});
		}
		catch (Exception x)
		{
			criticalError = true;
			EMStop.doKill(clog, this);
		}
	}
}
