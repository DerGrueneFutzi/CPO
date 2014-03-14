package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.List;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.minecraft.server.v1_7_R1.WatchableObject;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class AntiDamageIndicator extends CDPlugin
{
	Log clog;
	
	public AntiDamageIndicator(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.adi.bypass", PermissionDefault.OP)
		};
	}
		
	@CDPluginPacket(types = { "sentity_metadata" })
	@SuppressWarnings("unchecked")
	public void onPacket(PacketEvent e)
	{
		try
		{
			PacketContainer pc = e.getPacket();
			if(e.getPlayer().hasPermission("cdpp.adi.bypass")) return;
			if((int) pc.getIntegers().read(0) == e.getPlayer().getEntityId()) return;
			Object mod = pc.getModifier().read(1);
			List<WatchableObject> list = (List<WatchableObject>) mod;
			WatchableObject wo = list.get(6);
			if(!checkWObject(wo)) return;
			WatchableObject nwo = new WatchableObject(wo.c(), wo.a(), wo.b());
			nwo.a(wo.d());
			nwo.a(1F);
			list.set(6, nwo);
			pc.getModifier().write(1, list);
		}
		catch (ClassCastException x) {}
		catch (IndexOutOfBoundsException x) {}
	}
	
	private boolean checkWObject(WatchableObject wo)
	{
		return
			(
				wo.a() == 6 &&
				wo.c() == 3 &&
				(float) wo.b() == 0F
			);
	}
}