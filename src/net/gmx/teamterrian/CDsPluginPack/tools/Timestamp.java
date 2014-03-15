package net.gmx.teamterrian.CDsPluginPack.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Timestamp
{
	private long global_cooldown;
	private long player_cooldown;
	private Map<String, Long> timestamps;
	
	public Timestamp()
	{
		this.global_cooldown = -1;
		this.player_cooldown = -1;
		this.timestamps = new HashMap<String, Long>();
	}
	public Timestamp(int global_cooldown, int player_cooldown)
	{
		this.global_cooldown = global_cooldown;
		this.player_cooldown = player_cooldown;
		this.timestamps = new HashMap<String, Long>();
	}
	public Timestamp(int global_cooldown, int player_cooldown, Map<String, Long> timestamps)
	{
		this.global_cooldown = global_cooldown;
		this.player_cooldown = player_cooldown;
		this.timestamps = timestamps;
	}
	public Timestamp(int global_cooldown, long last_global_timestamp)
	{
		this.global_cooldown = global_cooldown;
		this.timestamps = new HashMap<String, Long>();
		this.timestamps.put("", last_global_timestamp);
		this.player_cooldown = -1;
	}
	
	public void setGlobalCooldown(long l)
	{
		this.global_cooldown = l;
	}
	public void setPlayerCooldown(long player_cooldown)
	{
		this.player_cooldown = player_cooldown;
	}
	public void setTimestamps(Map<String, Long> timestamps)
	{
		this.timestamps = timestamps;
	}
	public long getGlobalCooldown()
	{
		return global_cooldown;
	}
	public long getPlayerCooldown()
	{
		return player_cooldown;
	}
	public void setTimestamp(String p, long l)
	{
		timestamps.put(p, l);
	}
	
	
	
	public long getTimestamp(String p)
	{
		Long l = timestamps.get(p);
		if(l != null) return l;
		else return 0;
	}
	public Set<String> getOfflinePlayers()
	{
		return timestamps.keySet();
	}
	
	public void addTimestampData(List<String> list)
	{
		list.add("   PlayerCooldown: " + this.getPlayerCooldown());
		list.add("   GlobalCooldown: " + this.getGlobalCooldown());
		list.add("      Time to next global use (in seconds): " + this.getTimeToNextGlobalUse());
	}
	
	public boolean checkCooldown(String p)
	{
		long timestamp = Data.getTimestamp();
		return
			(
				(
					global_cooldown == -1 ||
					timestamp - getTimestamp("") > global_cooldown
				)
				&&
				(
					player_cooldown == -1 ||
					timestamp - getTimestamp(p) > player_cooldown
				)
			);
	}
	public long getTimeToNextGlobalUse()
	{
		if(global_cooldown == -1 || timestamps.get("") == null) return 0;
		long i = global_cooldown - (Data.getTimestamp() - this.getTimestamp("")) + 1;
		return (i < 0 ? 0 : i);
	}
	public long getTimeToNextUse(String p)
	{
		long i;
		if(player_cooldown == -1 || timestamps.get(p) == null) i = 0;
		else i = player_cooldown - (Data.getTimestamp() - this.getTimestamp(p)) + 1;
		long t = this.getTimeToNextGlobalUse();
		i = (i > t ? i : t); 
		return (i < 0 ? 0 : i);
	}
}
