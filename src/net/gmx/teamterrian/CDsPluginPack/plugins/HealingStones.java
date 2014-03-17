package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.List;
import java.util.Map;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.CDsPluginPack;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginPacket;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.CDArrayList;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.minecraft.server.v1_7_R1.WatchableObject;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ClientCommand;

public class HealingStones extends CDPlugin
{
	Log clog;
	Inventory tinv;
	public List<Entity> invulnerableAll = new CDArrayList<Entity>();
	public List<Entity> invulnerableEntity = new CDArrayList<Entity>();
	public Map<String, Location> reviveData = new CDHashMap<String, Location>();
	
	public HealingStones(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
		tinv = Bukkit.createInventory(null, 9);
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.hs", PermissionDefault.TRUE),
			new Permission("cdpp.hs.revive", PermissionDefault.TRUE)
		};
	}
		
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try
		{
			clog.log("Scheduling Healing Task", this);
			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(handler.getCDPP(), getHealingTask(), 200, 40);
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	@CDPluginCommand(commands = { "revive cdpp.hs.revive 1" })
	public void onCommand(CommandEvent e)
	{
		Player p = (Player) e.getSender();
		doRevive(p, reviveData.get(p.getName()));
	}

	@CDPluginPacket(types = { "supdate_health", "sentity_metadata" })
	public void onPacket(PacketEvent e)
	{
		PacketContainer pc = e.getPacket();
		if(pc.getType() == PacketType.Play.Server.UPDATE_HEALTH) {
			if(e.getPacket().getFloat().read(0) == 0F) {
				respawnPlayer(e.getPlayer());
				e.setCancelled(true);
			}
		}
		else
		{
			@SuppressWarnings("unchecked")
			WatchableObject wo = ((List<WatchableObject>) pc.getModifier().read(1)).get(0);
			if(wo.a() == 6 && wo.c() == 3 && ((Float) wo.b()).equals(0F))
				e.setCancelled(true);
		}
	}
	
	private Runnable getHealingTask()
	{
		return new Runnable()
		{
			public void run()
			{
				Player[] players = Bukkit.getServer().getOnlinePlayers();
				ItemStack i;
				String s, n1, n2;
				char c;
				int co;
				List<String> sarr;
				ItemMeta m;
				Double h;
				Short dur;
				int stch;
				for(Player p : players)
				{
					if(!p.hasPermission("cdpp.hs") || p.getGameMode() == GameMode.CREATIVE) continue;
					try
					{
						if((i = p.getItemInHand()) == null || !checkItem(i)) continue;
						if(i.getDurability() >= 1000)
						{
							clog.log("Stone from " + p.getName() + " broke", this);
							p.setItemInHand(null);
							continue;
						}
						if((m = i.getItemMeta()) == null || (sarr = m.getLore()) == null || sarr.size() == 0) continue;
						s = sarr.get(0);
						if(s.substring(s.length() - 2).equals("§1")) stch = 1;
						else if(s.substring(s.length() - 2).equals("§2")) stch = 2;
						else continue;
						if(!procItem(i, p.getInventory())) continue;
						if(i.getAmount() > 1)
						{
							p.sendMessage(ChatColor.RED + "[HealingStones] Bitte nur einen Stein in der Hand halten");
							continue;
						}
						n1 = "";
						n2 = "";
						co = 1;
						n1 += s.charAt(1);
						while((c = s.charAt(co += 2)) != 'r') n1 += c;
						while((c = s.charAt(co += 2)) != 'r') n2 += c;
						if(p.isDead()) continue;
						switch(stch)
						{
							case 1:
								h = ((Damageable) p).getHealth() + Double.valueOf(n1);
								if(((Damageable) p).getHealth() >= p.getHealthScale()) continue;
								if(h > p.getHealthScale())
									p.setHealth(p.getHealthScale());
								else p.setHealth(h);
								break;
							case 2:
								if(p.getFoodLevel() >= 20) continue;
								p.setFoodLevel(p.getFoodLevel() + Integer.valueOf(n1));
								break;
						}
						dur = (short) (i.getDurability() + Short.valueOf(n2));
						i.setDurability(dur);
						int uses = calcUses(i, Double.valueOf(n2));
						m.setLore(setUses(uses, sarr));
						i.setItemMeta(m);
						if(i.getDurability() >= 1000) p.setItemInHand(null);
					}
					catch (Exception x){}
				}
			}; };
	}
	
	@CDPluginEvent(priority = 0, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent e)
	{
		if(!invulnerableAll.contains(e.getEntity())) return;
		e.setCancelled(true);
		doFire(e);
			
	}
	@CDPluginEvent(priority = 0)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e)
	{
		if(invulnerableEntity.contains(e.getEntity()) || invulnerableAll.contains(e.getEntity()))
			e.setCancelled(true);
	}
	@CDPluginEvent(priority = 0)
	public void onEntityDamageByBlock(EntityDamageByBlockEvent e)
	{
		if(!invulnerableAll.contains(e.getEntity())) return;
		e.setCancelled(true);
		doFire(e);
	}
	private void doFire(EntityDamageEvent e)
	{
		DamageCause dc = e.getCause();
		if(dc == DamageCause.FIRE || dc == DamageCause.FIRE_TICK)
			e.getEntity().setFireTicks(0);
	}
	
	private boolean checkItem(ItemStack i)
	{
		Material m = i.getType();
		return !(m == Material.POTION || m.isBlock() || m == Material.DIAMOND_AXE || m == Material.DIAMOND_BOOTS ||
				m == Material.DIAMOND_CHESTPLATE || m == Material.DIAMOND_HELMET || m == Material.DIAMOND_HOE ||
				m == Material.DIAMOND_LEGGINGS || m == Material.DIAMOND_PICKAXE || m == Material.DIAMOND_SWORD);
	}
	
	private void makeInvulnerable(final Player p, long time)
	{
		invulnerableAll.add((Entity) p);
		Bukkit.getScheduler().scheduleSyncDelayedTask(handler.getCDPP(), new Runnable(){
			public void run()
			{
				clog.log("Ending invulnerabling of " + p.getName(), this);
				invulnerableAll.remove((Entity) p);
			}
		}, time);
	}
	public void makeInvulnerable(Player p)
	{
		clog.log("Making " + p.getName() + " invulnerable for Entitys", this);
		invulnerableEntity.add(p);
	}
	public void remInvulnerable(Player p)
	{
		clog.log("Removing invulnerabling of " + p.getName(), this);
		while(invulnerableEntity.contains(p))
			invulnerableEntity.remove(p);
	}
	
	private void respawnPlayer(Player p)
	{
		Location l = p.getLocation();
        PacketContainer packet = new PacketContainer(PacketType.Play.Client.CLIENT_COMMAND);
        packet.getClientCommands().write(0, ClientCommand.PERFORM_RESPAWN);
        try
        {
        	if(canRevive(p) != -1) {
        		p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[HealingStones] You had an item of reviving in your inventory on Death");
        		p.sendMessage(ChatColor.AQUA + "" +  ChatColor.BOLD + "[HealingStones] If you want to be revived, type " + ChatColor.ITALIC + "/revive");
        		reviveData.put(p.getName(), l.clone());
        	}
        	else reviveData.put(p.getName(), null);
            ProtocolLibrary.getProtocolManager().recieveClientPacket(p, packet);
        }
        catch (Exception e) {
            throw new RuntimeException("Cannot recieve packet.", e);
        }
    }
	private void doRevive(Player p, int itemSlot, Location l)
	{
		String s, n1, n2;
		char ch;
		List<String> sarr;
		ItemMeta m;
		Double h;
		Short dur;
		int i1 = -1;
		Inventory r_inv = p.getInventory();
		ItemStack i = r_inv.getItem(itemSlot);;
		
			try
			{
				if(!procItem(i, r_inv)) {
					clog.log(p.getName() + " should be revived, but his inventory is full", this);
					p.sendMessage(ChatColor.DARK_RED + "[HealingStones] Your Inventory is full. So we cannot split your HealingStones and also not revive.");
					return;
				}
				i.setAmount(1);
				if((m = i.getItemMeta()) == null || (sarr = m.getLore()) == null || sarr.size() == 0) return;
				s = sarr.get(0);
				
				n1 = "";
				n2 = "";
				i1 = 1;
				n1 += s.charAt(1);
				while((ch = s.charAt(i1 += 2)) != 'r') n1 += ch;
				while((ch = s.charAt(i1 += 2)) != 'r') n2 += ch;
				
				int uses = calcUses(i, Double.valueOf(n2)) - 1;
				h = Double.valueOf(n1);
				if(h > p.getHealthScale()) h = p.getHealthScale();
				
				clog.log("Allowed ressurrection of " + p.getName() + " with " + h + " Health", this);
				
				clog.log("Ressurrect " + p.getName() + " with " + h + " health", this);
				clog.log("Making " + p.getName() + " invulnerable", this);
				
				makeInvulnerable(p, 100);
				
				clog.log("Teleporting " + p.getName() + " : " + p.teleport(l), this);
				
				p.setHealth(h);
				reviveData.put(p.getName(), null);
				
				clog.log("Ressurrected " + p.getName(), this);
				p.sendMessage(ChatColor.DARK_AQUA + "§l[HealingStones] Du wurdest wiederbelebt.");
				p.sendMessage(ChatColor.DARK_AQUA + "§l[HealingStones] " + uses + " Benutzungen verbleiben.");
				p.sendMessage(ChatColor.GOLD + "§l[HealingStones] Du bist für 5 Sekunden unverwundbar");
				
				dur = (short) (i.getDurability() + Short.valueOf(n2));
				i.setDurability(dur);
				m.setLore(setUses(uses, sarr));
				i.setItemMeta(m);
				if(i.getDurability() >= 1000) r_inv.setItem(itemSlot, null);
			}
			catch (Exception x) {
				x.printStackTrace(clog.getStream());
			}
	}
	private void doRevive(Player p, Location l)
	{
		if(l == null) {
			p.sendMessage(ChatColor.RED + "[HealingStones] You cannot be revived, because you had not a stone of reviving in your Inventory on Death");
			return;
		}
		int i = canRevive(p);
		if(i == -1) {
			p.sendMessage(ChatColor.RED + "[HealingStones] You cannot be revived, because the stone of reviving is not in your inventory anymore");
			return;
		}
		doRevive(p, i, l);
	}
	private int canRevive(Player p)
	{
		if(!p.hasPermission("cdpp.hs.revive")) return -1;
		int sco = -1, co = -1;
		boolean found = false;
		ItemMeta m;
		List<String> sarr;
		String s;
		ItemStack[] inv = p.getInventory().getContents();
		for(ItemStack item : inv)
		{
			co++;
			if(item == null || (m = item.getItemMeta()) == null || (sarr = m.getLore()) == null || sarr.size() == 0) continue;
			s = sarr.get(0);
			if(s.endsWith("§3"))
			{
				if(item.getAmount() > 1) { sco = co; continue; }
				found = true;
				break;
			}
		}
		return (found ? co : sco);
	}
	
	public boolean checkName(String s)
	{
		try { s = s.substring(s.indexOf(' ') + 1); }
		catch (Exception x) { return false; }
		((Trade) CDsPluginPack.getInstance().handler.plugins.get(Trade.class)).prefix = s;
		return true;
	}
	private boolean procItem(ItemStack i, Inventory inv)
	{
		int fe;
		if(i.getAmount() == 1) return true;
		ItemStack icl = i.clone();
		icl.setAmount(i.getAmount() - 1);
		if((fe = inv.firstEmpty()) == -1) return false;
		else inv.setItem(fe, icl);
		i.setAmount(1);
		return true;
	}
	private int calcUses(ItemStack i, double n2)
	{
		double d = (1000 - i.getDurability()) /  Double.valueOf(n2);
		return (int) (d + (d % 1 == 0 ? 0 : 1));
	}
	private List<String> setUses(int uses, List<String> lores)
	{
		if(lores.size() < 2) {
			lores.add("§f" + uses + " Benutzungen");
			return lores;
		}
		String s = lores.get(1);
		int co = 0;
		while(s.charAt(co) == '§') co += 2;
		int co2 = co;
		while(s.length() - 1 != co2 && s.charAt(co2) != ' ') co2++;
		lores.set(1, s.substring(0, co) + uses + s.substring(co2));
		return lores;
	}
	
}