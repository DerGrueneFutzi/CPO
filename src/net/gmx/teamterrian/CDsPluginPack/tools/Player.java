package net.gmx.teamterrian.CDsPluginPack.tools;

import net.minecraft.server.v1_7_R1.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;

public class Player extends CraftPlayer
{
	public org.bukkit.entity.Player real;
	
	private Player(CraftServer server, EntityPlayer entity, org.bukkit.entity.Player real)
	{
		super(server, entity);
		this.real = real;
	}
	
	public boolean equals(Object obj)
	{
		if (!(obj instanceof OfflinePlayer))
            return false;
        OfflinePlayer other = (OfflinePlayer) obj;
        if ((this.getName() == null) || (other.getName() == null))
            return false;
        return this.getName().equalsIgnoreCase(other.getName());
	}
	
	public static Player getPlayer(Object o)
	{
		if(o == null) return null;
		org.bukkit.entity.Player p = (org.bukkit.entity.Player) o;
		return new Player((CraftServer) Bukkit.getServer(), ((CraftPlayer) p).getHandle(), p);
	}
	public static Player[] getPlayers(org.bukkit.entity.Player[] players)
	{
		Player[] back = new Player[players.length];
		for(int i = 0; i < players.length; i++)
			back[i] = getPlayer(players[i]);
		return back;
	}
	public static boolean isPlayer(Object o)
	{
		return o instanceof org.bukkit.entity.Player;
	}
}
