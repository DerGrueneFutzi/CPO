﻿package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.comphenix.protocol.events.PacketEvent;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PbTextBlock extends CDPlugin
{
	Log clog;
	Logger log;
	String mbeg = ChatColor.GREEN + "[" + ChatColor.RED + "PbTB" + ChatColor.GREEN + "] " + ChatColor.GRAY;
	
	public PbTextBlock(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
		log = handler.log;
	}
	
	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.PbTB.disable", PermissionDefault.OP)
		};
	}
	
	enum MessageType
	{
		HIDE,
		NONE,
		FORBIDDEN,
		NORMAL,
		CMSG,
		BEGINNING,
		RULE,
		PUNC,
		END,
	}
	enum SearchType
	{
		NORMAL,
		BEGINNING,
		END,
		NONE,
	}
	
	@CDPluginPacket(types = { "cchat" }, priority = 20)
	public void onPacket(PacketEvent e)
	{
		if(doAction(e)) return;
		if(e.getPlayer().hasPermission("cdpp.PbTB.disable")) return;
		doBlock(e);
	}

	//[\\.,!\\?;:/\\\\\\(\\)]{4,}
	public boolean doAction(PacketEvent e)
	{
		try
		{
			int i = ((ItemHelp) handler.plugins.get(ItemHelp.class)).isHelp(null, e.getPlayer(), e);
			if(i == 2) return true;
			else if (i == 0) return false;
			e.setCancelled(true);
			if(i == 3) ((TempCommand) handler.plugins.get(TempCommand.class)).checkTask(e.getPlayer(), e.getPacket().getStrings().read(0).substring(1));
			else ((MoreEnderChests) handler.plugins.get(MoreEnderChests.class)).doInv(e.getPacket().getStrings().read(0));
			return true;
		}
		catch (Exception x) { return false; }
	}
	private boolean doBlock(PacketEvent e)
	{
		Player p = e.getPlayer();
		String message = e.getPacket().getStrings().read(0);
		PermissionUser u = PermissionsEx.getUser(p);
		String[] permsArr = u.getPermissions(p.getWorld().getName());
		for(PermissionGroup pg : u.getGroups())
			permsArr = VarTools.combineArray(permsArr, pg.getPermissions(p.getWorld().getName()));
		u.getGroups();
		String[] parr = new String[1];
		Object[] options;
		List<String> perms = VarTools.toList(permsArr);
		for(String perm : perms)
		{
			if(!perm.startsWith("cdpp.PbTB.block.") || perms.contains("-" + perm) || perm.length() < 19) continue;
			perm = perm.substring(16).replace('_', ' ');
			parr[0] = perm;
			options = getOptions(parr);
			if(search(message, parr[0], (SearchType) options[0]))
			{
				e.setCancelled(true);
				clog.log("Blocked ChatPacket \"" + message + "\" from " + p.getName(), this);
				log.info("[PbTB] Blocked ChatPacket \"" + message + "\" from " + p.getName());
				logBlock(message, parr[0], p, (SearchType) options[0]);
				showMessage(message, parr[0], p, (MessageType) options[1]);
				return true;
			}
		}
		return false;
	}
	
	private Object[] getOptions(String[] str)
	{
		Object[] back = new Object[2];
		String s = str[0];
		if(s == null || s.length() < 3) return null;
		MessageType mt;
		SearchType st;
		switch(s.charAt(0))
		{
			case '=': st = SearchType.NORMAL; break;
			case 'b': st = SearchType.BEGINNING; break;
			case 'e': st = SearchType.END; break;
			default: st = SearchType.NONE; break;
		}
		switch(s.charAt(1))
		{
			case '=': mt = MessageType.NORMAL; break;
			case 'c': mt = MessageType.CMSG; break;
			case 'h': mt = MessageType.HIDE; break;
			case 'f': mt = MessageType.FORBIDDEN; break;
			case 'r': mt = MessageType.RULE; break;
			case 'p': mt = MessageType.PUNC; break;
			case 'b': mt = MessageType.BEGINNING; break;
			case 'e': mt = MessageType.END; break;
			default: mt = MessageType.NONE; break;
		}
		str[0] = s.substring(2);
		back[0] = st;
		back[1] = mt;
		return back;
	}
	private boolean search(String text, String search, SearchType st)
	{
		switch(st)
		{
			case NORMAL: return Pattern.compile(search).matcher(text).matches();
			case BEGINNING:
				Matcher m = Pattern.compile(search).matcher(text);
				return (m.find() ? m.start() == 0 : false);
			case END:
				Matcher ma = Pattern.compile(search).matcher(text);
				int e = -1;
				while(ma.find()) e = ma.end();
				return (e == text.length());
			default:
				return false;
		}
		
	}
	private void logBlock(String message, String search, Player p, SearchType st)
	{
		switch(st)
		{
			case NORMAL:
				clog.log("Blocked Text \"" + search + " \" in \"" + message + "\" from " + p.getName(), this); break;
			case BEGINNING:
				clog.log("Blocked TextBeginning \"" + search + " \" in \"" + message + "\" from " + p.getName(), this); break;
			case END:
				clog.log("Blocked TextEnding \"" + search + " \" in \"" + message + "\" from " + p.getName(), this); break;
			default:
				clog.log("Blocked \"" + search + " \" in \"" + message + "\" from " + p.getName() + " without any reason", this); break;
		}
	}
	private void showMessage(String message, String search, Player p, MessageType mt)
	{
		switch(mt)
		{
			case NONE:
			case NORMAL:
				clog.log("Show blocked Text \"" + search + " \" in \"" + message + "\" from " + p.getName(), this);
				p.sendMessage(mbeg + ChatColor.GREEN + "[" + ChatColor.RED + "\"" + ChatColor.GRAY + search + ChatColor.RED + "\"" + ChatColor.GREEN + "]" + ChatColor.GRAY + " Text blocked"); break;
			case BEGINNING:
				clog.log("Show blocked TextBeginning \"" + search + " \" in \"" + message + "\" from " + p.getName(), this);
				p.sendMessage(mbeg + ChatColor.GREEN + "[" + ChatColor.RED + "\"" + ChatColor.GRAY + search + ChatColor.RED + "\"" + ChatColor.GREEN + "]" + ChatColor.GRAY + " TextBeginning blocked"); break;
			case END:
				clog.log("Show blocked TextEnding \"" + search + " \" in \"" + message + "\" from " + p.getName(), this);
				p.sendMessage(mbeg + ChatColor.GREEN + "[" + ChatColor.RED + "\"" + ChatColor.GRAY + search + ChatColor.RED + "\"" + ChatColor.GREEN + "]" + ChatColor.GRAY + " TextEnding blocked"); break;
			case HIDE:
				clog.log("Show blocked HelpCommand \"" + message + "\" from " + p.getName(), this); break;
			case FORBIDDEN:
				clog.log("Show blocked forbidden Text \"" + search + " \" in \"" + message + "\" from " + p.getName(), this);
				p.sendMessage(ChatColor.DARK_RED + "Because of a certain char sequence, the access to that chat text or command was denyed"); break;
			case CMSG:
				clog.log("Show blocked ConsoleMsg \"" + message + "\" from " + p.getName(), this);
				p.sendMessage(mbeg + "You can´t send a msg to Console");
				break;
			case RULE:
				clog.log("Show blocked spoiler text in \"" + message + "\" from " + p.getName() , this);
				p.sendMessage(mbeg + "Bitte beachte Regel Nr. 2 (Öffentliches Spoilern untersagt) und nerve andere Spieler nicht");
				break;
			case PUNC:
				clog.log("Show blocked punc \"" + message + "\" from " + p.getName(), this);
				p.sendMessage(mbeg + "Satzzeichen sind keine Rudeltiere!");
				break;
				
		}
	}
}
