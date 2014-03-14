package net.gmx.teamterrian.CDsPluginPack.plugins;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.jsonParser;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.comphenix.protocol.events.PacketEvent;

public class NoMessage extends CDPlugin
{
	Log clog;
	
	public NoMessage(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	@CDPluginPacket(priority = 20, types = { "schat" })
	public void onPacket(PacketEvent e)
	{
		String json = e.getPacket().getChatComponents().read(0).getJson();
		String text = jsonParser.parse(json);
		if( text == null ||
			text.contains("§6Warping to") || 
			text.contains("§6Teleport") ||
			text.contains("teleported") ||
			text.contains("§6Received") ||
			text.equals("§4You don't have permission for this area.") ||
			text.equals("§4You are not permitted to enter this area.") ||
			text.equals("§4You are in a no-PvP area.")
			)
			e.setCancelled(true);
	}
	
	@CDPluginEvent
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		e.setJoinMessage(null);
	}
	@CDPluginEvent
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		e.setQuitMessage(null);
	}
	@CDPluginEvent
	public void onPlayerDeath(PlayerDeathEvent e)
	{
		e.setDeathMessage(null);
	}
	@CDPluginEvent
	public boolean onPlayerKick(PlayerKickEvent e)
	{
		e.setLeaveMessage(null);
		return false;
	}
}