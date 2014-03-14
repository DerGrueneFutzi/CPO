package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagString;

public class BadWords extends CDPlugin
{
	Log clog;
	private String[] badWords;
	private List<String> read;
	
	public BadWords(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.bw.disable", PermissionDefault.OP),
			new Permission("cdpp.bw.add", PermissionDefault.OP),
			new Permission("cdpp.bw.io", PermissionDefault.OP),
			new Permission("cdpp.bw", PermissionDefault.OP)
		};
	}
		
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try
		{
			clog.log("Loading BadWords", this);
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
	
	public String[] getDirectorys() { return new String[] { "BadWords" }; }
	
	@CDPluginCommand(commands = { "badwords cdpp.bw 1", "bw cdpp.bw 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		String[] args = e.getArgs();
		CommandSender sender = e.getSender();
		if(args.length < 1) throw new CDInvalidArgsException(e.getCommand().getName());
		switch(args[0])
		{
			case "save":
				save(sender);
				return;
			case "load":
				load(sender);
				return;
			case "add":
				if(add(args, sender)) return;
		}
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
	
	@CDPluginPacket(types = { "cchat" })
	public void onPacket(PacketEvent e)
	{
		PacketContainer pc = e.getPacket();
		Player p = Player.getPlayer(e.getPlayer());
		if(p.hasPermission("cdpp.bw.disable")) return;
		String s;
		try { if((s = replaceString(pc.getStrings().read(0).toLowerCase(), badWords)) != null)
		{
			clog.log("Replaced Chatmessage of " + p.getName() + " from \"" + pc.getStrings().read(0) + "\" to \"" + s + "\"", this);
			pc.getStrings().write(0, s);
			e.getPlayer().sendMessage(ChatColor.DARK_RED + "Don´t use such words!");
		}}
		catch (Exception x) { }
	}
	
	private String replaceString(String input, String[] badWords)
	{
		if(input == null || input.length() == 0) return null;
		String copy = new String(input);
		for(String s : badWords)
			copy = copy.replaceAll(s, "***");
		return (copy.equals(input) ? null : copy);
	}
	
	private boolean add(String[] args, CommandSender sender)
	{
		if(args.length < 2) return false;
		if(!sender.hasPermission("cdpp.bw.add")) {
			sender.sendMessage("No Permission");
			return true;
		}
		try
		{
			clog.log("Trying to add word \"" + args[1] + "\"", this);
			add(args[1]);
			clog.log("Word \"" + args[1] + "\" add", this);
			sender.sendMessage(ChatColor.GREEN + "[BadWords] Word temporary added");
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while adding word \"" + args[1] + "\"", this);
			sender.sendMessage(ChatColor.RED + "[BadWords] Error while adding word");
		}
		return true;
	}
	private void add(String input)
	{
		String[] s = new String[badWords.length + 1];
		for(int i = 0; i < badWords.length; i++)
			s[i] = badWords[i];
		s[s.length - 1] = processWord(input);
		badWords = s;
		read.add(input);
	}
	
	private boolean load(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.bw.io")) {
			sender.sendMessage("No Permission");
			return true;
		}
		try
		{
			clog.log("Force loading", this);
			load();
			clog.log("Loaded", this);
			sender.sendMessage(ChatColor.GREEN + "[BadWords] Words loaded");
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while saving", this);
			sender.sendMessage(ChatColor.RED + "[BadWords] Error while loading words");
		}
		return true;
	}
	private boolean save(CommandSender sender)
	{
		if(!sender.hasPermission("cdpp.bw.io")) {
			sender.sendMessage("No Permission");
			return true;
		}
		try
		{
			clog.log("Force saving", this);
			save();
			clog.log("Saved", this);
			sender.sendMessage(ChatColor.GREEN + "[BadWords] Words saved");
		}
		catch (Exception x)
		{
			x.printStackTrace(clog.getStream());
			clog.log("Error while saving", this);
			sender.sendMessage(ChatColor.RED + "[BadWords] Error while saving words");
		}
		return true;
	}
	
	private void load() throws IOException
	{
		if(!new File(CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat").exists()) {
			clog.log("File " + CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat not found. Returning", this);
			return;
		}
		NBTTagCompound base;
		FileInputStream inputStream = new FileInputStream(CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat");
		base = NBTCompressedStreamTools.a(inputStream);
		inputStream.close();
		NBTTagList list = (NBTTagList) base.get("Words");
		badWords = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			badWords[i] = list.f(i);
		read = VarTools.toList(badWords.clone());
		processWords(badWords);
		clog.log("Words read", this);
	}
	private void save() throws IOException
	{
		clog.log("Begin saving bad words", this);
		NBTTagCompound base = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for(String word : read)
			list.add(new NBTTagString(word));
		base.set("Words", list);
	    Data.secureSave(base, CDPlugin.getDir() + this.getDirectorys()[0] + "/data.dat", this);
	    clog.log("Finished saving", this);
	}
	
	public void processWords(String[] words)
	{
		for(int i = 0; i < words.length; i++)
			words[i] = processWord(words[i].toLowerCase());
	}
	private String processWord(String word)
	{
		String s = "";
		for(int i = 0; i < word.length(); i++)
			s += "[ -_,\\.!=" + getLeet(word.charAt(i)) + "]*" + word.charAt(i) + "{1,}";
		if(word.length() != 0)
			s = s.substring(11);
		return s;
	}
	private String getLeet(char input)
	{
		switch(input)
		{
			case 'o':
				return "0";
			case '0':
				return "o";
			case 'i':
				return "1";
			case '1':
				return "i";
			case 'z':
				return "2";
			case '2':
				return "z";
			case 'e':
				return "3";
			case '3':
				return "e";
			case 'a':
				return "4";
			case '4':
				return "a";
			case 's':
				return "5$";
			case '5':
				return "s";
			case 't':
				return "7";
			case '7':
				return "t";
			case 'c':
				return "\\[\\(";
		}
		return "";
	}
}
