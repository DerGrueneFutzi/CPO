package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Data;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.Player;

public class ChatRegime extends CDPlugin
{
	Logger log;
	Log clog;
	List<Character> bigs = new ArrayList<Character>();
	Map<Player, CRChatData> data = new HashMap<Player, CRChatData>();
	
	public ChatRegime(PluginHandler handler)
	{
		super(handler);
		log = handler.log;
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.cr.capsblock", PermissionDefault.NOT_OP),
			new Permission("cdpp.cr.spamblock", PermissionDefault.NOT_OP)
		};
	}
		
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try
		{
			clog.log("Setting StartItems", this);
			setStartItems();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch(Exception x)
		{
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	@CDPluginPacket(types = { "cchat" })
	public void onPacket(PacketEvent e)
	{
		if(doSpam(e)) return;
		doCaps(e);
	}
	
	private boolean doCaps(PacketEvent e)
	{
		Player p = Player.getPlayer(e.getPlayer());
		StructureModifier<String> sm = e.getPacket().getStrings();
		String message = sm.read(0);
		if(p.hasPermission("cdpp.cr.capsblock"))
		{
			if(message.length() > 3)
			{
				int c = 0;
				char[] m = message.toCharArray();
				for(char akt : m) if(bigs.indexOf(akt) != -1) c++;
				if(message.length() / 2 <= c)
				{
					clog.log("Blocked caps \"" + sm.read(0) + "\" from " + p.getName(), this);
					log.info("[ChatRegime] Blocked caps \"" + sm.read(0) + "\" from " + p.getName());
					e.getPacket().getStrings().write(0, message.toLowerCase());
				}
			}
		}
		return false;
	}
	private boolean doSpam(PacketEvent e)
	{
		Player p = Player.getPlayer(e.getPlayer());
		StructureModifier<String> sm = e.getPacket().getStrings();
		String message = sm.read(0);
		long actTime = Data.getTimestamp();
		CRChatData chatData;
		if(message.startsWith("/")) return false;
		if(p.hasPermission("cdpp.cr.spamblock"))
		{
			if(!data.containsKey(p))
				data.put(p, new CRChatData(message, actTime));
			else
			{
				chatData = data.get(p);
				if(!chatData.check(message))
				{
					clog.log("Blocked Spam \"" + sm.read(0) + "\" from " + p.getName(), this);
					log.info("[ChatRegime] Blocked Spam \"" + sm.read(0) + "\" from " + p.getName());
					e.setCancelled(true);
					p.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + ChatColor.MAGIC + "!!!" + ChatColor.RESET + ChatColor.DARK_RED + ChatColor.BOLD + "Spamme und wiederhole dich nicht!");
					return true;
				}
				
				chatData.setMsg1(message);
				chatData.setTime1(actTime);
				data.put(p, chatData);
			}
		}
		return false;
	}
	
	private void setStartItems()
	{
		bigs.add('A');
		bigs.add('B');
		bigs.add('C');
		bigs.add('D');
		bigs.add('E');
		bigs.add('F');
		bigs.add('G');
		bigs.add('H');
		bigs.add('I');
		bigs.add('J');
		bigs.add('K');
		bigs.add('L');
		bigs.add('M');
		bigs.add('N');
		bigs.add('O');
		bigs.add('P');
		bigs.add('Q');
		bigs.add('R');
		bigs.add('S');
		bigs.add('T');
		bigs.add('U');
		bigs.add('V');
		bigs.add('W');
		bigs.add('X');
		bigs.add('Y');
		bigs.add('Z');
		bigs.add('Ü');
		bigs.add('Ä');
		bigs.add('Ö');
	}
}

class CRChatData
{
	private String msg1, msg2;
	private long time1, time2;
	
	public CRChatData(String msg1, String msg2, long time1, long time2)
	{
		this.msg1 = msg1;
		this.msg2 = msg2;
		this.time1 = time1;
		this.time2 = time2;
	}
	public CRChatData(String msg1, String msg2)
	{
		this.msg1 = msg1;
		this.msg2 = msg2;
		this.time1 = 0;
		this.time2 = 0;
	}
	public CRChatData(String msg1, long time1)
	{
		this.msg1 = msg1;
		this.msg2 = "";
		this.time1 = time1;
		this.time2 = 0;
	}
	public CRChatData()
	{
		this.msg1 = "";
		this.msg2 = "";
		this.time1 = 0;
		this.time2 = 0;
	}
	
	public void setMsg1(String msg1)
	{
		this.msg2 = this.msg1;
		this.msg1 = msg1;
	}
	public void setTime1(long time1)
	{
		this.time2 = this.time1;
		this.time1 = time1;
	}
	
	public String getMsg1()
	{
		return msg1;
	}
	public String getMsg2()
	{
		return msg2;
	}
	public long getTime1()
	{
		return time1;
	}
	public long getTime2()
	{
		return time2;
	}
	
	public boolean check(String text)
	{
		return checkDifference() || (checkTime() && checkText(text));
	}
	public boolean checkText(String text)
	{
		if(compareStrings(text, msg1) > text.length() / 2) return false;
		if(compareStrings(text, msg2) > text.length() / 2) return false;
		return true;
	}
	public boolean checkTime()
	{
		long act = Data.getTimestamp();
		if(act - time1 <= 2) return false;
		if(act - time2 <= 7) return false;
		return true;
	}
	public boolean checkDifference()
	{
		if(Data.getTimestamp() - time1 > 90) return true;
		return false;
	}
	private int compareStrings(String one, String two)
	{
		one = one.toLowerCase();
		two = two.toLowerCase();
		int c = 0;
		int found;
		int l;
		while(one.length() >= 3)
		{
			found = -1;
			for(int i = 3; i <= one.length(); i++)
				if(i == one.length())
					if(two.contains(one)) found = i;
					else break;
				else
					if(two.contains(one.substring(0, i))) found = i;
					else break;
			if(found != -1)
			{
				l = two.length();
				if(found == one.length()) two = two.replace(one, "");
				else two = two.replace(one.substring(0, found), "");
				c += l - two.length();
			}
			if(one.length() == (found == -1 ? 3 : found)) one = "";
			else one = one.substring((found == -1 ? 3 : found));
		}
		return c;
	}
}
