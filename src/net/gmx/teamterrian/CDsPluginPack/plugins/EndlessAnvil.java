package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;

public class EndlessAnvil extends CDPlugin
{
	Log clog;
	
	public EndlessAnvil(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}
		
	@SuppressWarnings("deprecation")
	@CDPluginPacket(types = { "sworld_event" })
	public void onPacket(PacketEvent e)
	{
		PacketContainer pc = e.getPacket();
		if(pc.getIntegers().read(0) != 1020) return;
		StructureModifier<Integer> sm = pc.getIntegers();
		int x = sm.read(2),
			y = sm.read(3),
			z = sm.read(4);
		clog.log("Placing Anvil on x:" + x + ", y:" + y + ", z:" + z, this);
		Bukkit.getServer().getWorld("world").getBlockAt(x, y, z).setTypeIdAndData(Material.ANVIL.getId(), (byte) 0, false);
		e.setCancelled(true);
	}
}
