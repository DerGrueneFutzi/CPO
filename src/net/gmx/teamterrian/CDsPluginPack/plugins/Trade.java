package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CDPluginEnableEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.CDArrayList;
import net.gmx.teamterrian.CDsPluginPack.tools.CDHashMap;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class Trade extends CDPlugin
{
	Logger log;
	Log clog;
	ItemStack greenWool, air, redWool, empty;
	List<Integer> slotsLeft, slotsRight, slotsEmpty, slotsWoolLeft, slotsWoolRight;
	ItemStack[] startItems, emptysLeft, emptysRight;
	List<ItemStack> emptyList;
	Map<Player, Player> requests = new CDHashMap<Player, Player>();
	List<Player> leftP, rightP;
	String tradeTitle, prefix;
	
	public Trade(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
		log = handler.log;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.trade", PermissionDefault.TRUE),
			new Permission("cdpp.trade.toggled", PermissionDefault.FALSE),
			new Permission("cdpp.trade.toggle", PermissionDefault.TRUE)
		};
	}
		
	@CDPluginEvent
	public void onEnable(CDPluginEnableEvent e)
	{
		try {
			clog.log("Setting StartItems", this);
			setStartItems();
			clog.log("Success", this);
			e.setSuccess(this, true);
		}
		catch (Exception x) {
			x.printStackTrace(clog.getStream());
			e.setSuccess(this, false);
		}
	}
	
	@CDPluginCommand(commands = { "trade cdpp.trade 1", "tradeacc cdpp.trade 1", "tradedeny cdpp.trade 1", "tradeabort cdpp.trade 1", "tradetoggle cdpp.trade.toggle 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		CommandSender sender = e.getSender();
		String[] args = e.getArgs();
		switch(e.getCommand().getName().toLowerCase())
		{
			case "tradetoggle":
				tradetoggle((Player) sender); return;
			case "trade":
				if(args.length < 1) throw new CDInvalidArgsException(e.getCommand().getName());
					sendRequest((Player) sender, Bukkit.getPlayer(args[0]), args[0]);
					return;
			case "tradeacc":
				if(!doRequest((Player) sender, true))
					sender.sendMessage(ChatColor.RED + "Nobody has offert you a trade"); return;
			case "tradedeny":
				if(!doRequest((Player) sender, false))
					sender.sendMessage(ChatColor.RED + "Nobody has offert you a trade"); return;
			case "tradeabort":
				if(!abortRequest((Player) sender))
					sender.sendMessage(ChatColor.RED + "You haven´t offert anyone a trade"); return;
		}
		throw new CDInvalidArgsException(e.getCommand().getName());
	}
	
	private void setStartItems()
	{
		greenWool = new ItemStack(Material.WOOL, 1, (short) 5);
		air = new ItemStack(Material.AIR);
		redWool = new ItemStack(Material.WOOL, 1, (short) 14);
		startItems = new ItemStack[54];
		slotsLeft = new CDArrayList<Integer>();
		slotsRight = new CDArrayList<Integer>();
		slotsEmpty = new CDArrayList<Integer>();
		slotsWoolLeft = new CDArrayList<Integer>();
		slotsWoolRight = new CDArrayList<Integer>();
		emptysLeft = new ItemStack[18];
		emptysRight = new ItemStack[18];
		emptyList = new CDArrayList<ItemStack>();
		tradeTitle = "Trade§0";
		
		ItemMeta meta;
		meta = greenWool.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "§lZum Bestätigen klicken");
		greenWool.setItemMeta(meta.clone());
		meta = redWool.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "§lZum Abbrechen klicken");
		redWool.setItemMeta(meta.clone());
		startItems[3] = greenWool.clone();
		startItems[12] = greenWool.clone();
		startItems[21] = greenWool.clone();
		startItems[30] = greenWool.clone();
		startItems[39] = greenWool.clone();
		startItems[48] = greenWool.clone();
		startItems[5] = greenWool.clone();
		startItems[14] = greenWool.clone();
		startItems[23] = greenWool.clone();
		startItems[32] = greenWool.clone();
		startItems[41] = greenWool.clone();
		startItems[50] = greenWool.clone();
		slotsLeft.add(0); 	slotsLeft.add(1); 	slotsLeft.add(2);	slotsWoolLeft.add(3);	slotsEmpty.add(4);	slotsWoolRight.add(5);	slotsRight.add(6);	slotsRight.add(7);	slotsRight.add(8);
		slotsLeft.add(9); 	slotsLeft.add(10); 	slotsLeft.add(11);	slotsWoolLeft.add(12);	slotsEmpty.add(13);	slotsWoolRight.add(14);	slotsRight.add(15);	slotsRight.add(16);	slotsRight.add(17);
		slotsLeft.add(18); 	slotsLeft.add(19); 	slotsLeft.add(20);	slotsWoolLeft.add(21);	slotsEmpty.add(22);	slotsWoolRight.add(23);	slotsRight.add(24);	slotsRight.add(25);	slotsRight.add(26);
		slotsLeft.add(27); 	slotsLeft.add(28); 	slotsLeft.add(29);	slotsWoolLeft.add(30);	slotsEmpty.add(31);	slotsWoolRight.add(32);	slotsRight.add(33);	slotsRight.add(34);	slotsRight.add(35);
		slotsLeft.add(36); 	slotsLeft.add(37); 	slotsLeft.add(38);	slotsWoolLeft.add(39);	slotsEmpty.add(40);	slotsWoolRight.add(41);	slotsRight.add(42);	slotsRight.add(43);	slotsRight.add(44);
		slotsLeft.add(45); 	slotsLeft.add(46); 	slotsLeft.add(47);	slotsWoolLeft.add(48);	slotsEmpty.add(49);	slotsWoolRight.add(50);	slotsRight.add(51);	slotsRight.add(52);	slotsRight.add(53);
		emptyList.add(air.clone());
		emptyList.add(null);
	}
	
	private void startTrade(Player left, Player right)
	{
		clog.log("Starting Trade with " + left.getName() + " and " + right.getName(), this);
		Inventory vi = Bukkit.createInventory(left, 54, tradeTitle);
		vi.setContents(startItems.clone());
		left.openInventory(vi);
		right.openInventory(vi);
		leftP.add(left);
		rightP.add(right);
	}
	
	public void onDrag(InventoryDragEvent e)
	{
		if(!e.getInventory().getName().equals(tradeTitle)) return;
		int Ileft = getLeft((Player) e.getWhoClicked());
		if(Ileft == -1) return;
		if(!checkDrag(e, Ileft == 1)) e.setCancelled(true);
	}
	private boolean checkDrag(InventoryDragEvent e, boolean left)
	{
		for(Integer i : e.getInventorySlots()) if((left ? slotsLeft : slotsRight).indexOf(i) == -1) return false;
		return true;
	}
	@CDPluginEvent
	@SuppressWarnings("deprecation")
	public void onInventoryClose(InventoryCloseEvent e)
	{
		Inventory vi = e.getInventory();
		if(vi.getName() != tradeTitle) return;
		Player p = (Player) e.getPlayer();
		if(!p.hasPermission("cdpp.trade")) return;
		clog.log(p.getName() + " closed his Tradewindow", this);
		Inventory uvi = p.getInventory();	
		int Ileft = getLeft(p);
		if(Ileft == -1) return;
		boolean left = Ileft == 1;
		ItemStack[] Items = toArray(pickout(getItems(vi, left, true)));
		HashMap<Integer, ItemStack> rest = uvi.addItem(Items);
		p.updateInventory();
		for(ItemStack item : rest.values()) p.getWorld().dropItem(p.getLocation(), item);
		(left ? leftP : rightP).remove(p);
		setItems(vi, null, left);
	}
	@CDPluginEvent
	public void onInventoryClick(InventoryClickEvent e)
	{
		try
		{
			if(!VarTools.isPlayer(e.getWhoClicked())) return;
			Player p = (Player) e.getWhoClicked();
			if(!p.hasPermission("cdpp.trade")) return;
			if(e.getSlot() == -1) return;
			Inventory vi = e.getInventory();
			if(vi == null) return;
			if(!vi.getTitle().equals(tradeTitle)) return;
			int Ileft = getLeft(p);
			if(Ileft == -1) return;
			if(e.getAction() == InventoryAction.COLLECT_TO_CURSOR|| !checkShift(e, Ileft == 1)) { e.setCancelled(true); return; }
			checkAction(e, Ileft == 1, vi);
			if(checkTrade(vi)) doTrade(vi);
		}
		catch(Exception x)
		{
			x.printStackTrace(clog.getStream());
			log.warning("[CDPP][Trade] Exception while processing an Inventoryclick");
		}
	}
	@CDPluginEvent
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		try{ checkRequests(e.getPlayer(), null); }
		catch(Exception x) {
			x.printStackTrace(clog.getStream());
			clog.log("Because of an Error, all requests were deleted", this);
			log.warning("[CDPP][Trade] Exception. Clearing all Requests");
			requests.clear();
		}
	}
	
	private boolean abortRequest(Player p)
	{
		if(!requests.containsKey(p)) return false;
		cancelRequest(p);
		return true;
	}
	private boolean doRequest(Player p, boolean accepted)
	{
		Player one;
		if(accepted)
		{
			clog.log(p.getName() + " accepted trade", this);
			if(!requests.containsValue(p)) return false;
			one = getOtherPlayer(p, requests);
			requests.remove(p);
			requests.remove(getOtherPlayer(p, requests));
			startTrade(one, p);
			return true;
		}
		else
		{
			clog.log(p.getName() + " denyed trade", this);
			if(!requests.containsValue(p)) return false;
			getOtherPlayer(p, requests).sendMessage(ChatColor.RED + "Trade with " + ChatColor.RESET + ChatColor.GRAY + p.getDisplayName() + ChatColor.RESET + ChatColor.RED + " was denyed");
			p.sendMessage(ChatColor.GOLD + "Trade denyed");
			requests.remove(p);
			requests.remove(getOtherPlayer(p, requests));
			return true;
		}
	}
	static void mRequest()
	{
		Plugin p = Bukkit.getPluginManager().getPlugin("Essentials");
		Essentials es = (Essentials) p;
		User u = es.getUser("Moylle");
		u.setBanned(false);
	}
	private void checkRequests(Player asks, Player asked)
	{
		if(requests.containsKey(asks)) cancelRequest(asks);
		if(requests.containsValue(asks)) cancelRequest(getOtherPlayer(asks, requests));
		if(asked == null) return;
		if(requests.containsKey(asked)) cancelRequest(asked);
		if(requests.containsValue(asked)) cancelRequest(getOtherPlayer(asked, requests));
	}
	private void cancelRequest(Player p)
	{
		Player p2 = requests.get(p);
		clog.log("Trade between " + p.getName() + " and " + p2.getName() + " was canceled", this);
		p.sendMessage(ChatColor.RED + "Trade with " + ChatColor.RESET + ChatColor.GRAY + p2.getDisplayName() + ChatColor.RESET + ChatColor.RED + " canceled");
		p2.sendMessage(ChatColor.RED + "Request from " + ChatColor.RESET + ChatColor.GRAY + p.getDisplayName() + ChatColor.RESET + ChatColor.RED + " aborted");
		requests.remove(p);
	}
	private void sendRequest(Player ask, Player asked, String name)
	{
		if(asked == null || ask == null)
		{
			clog.log(ask.getName() + " tryed to trade with not existing player " + name, this);
			ask.sendMessage(ChatColor.RED + "Player not found!");
			return;
		}
		if(ask == asked) {
			clog.log(ask.getName() + " tryed to trade with himself", this);
			ask.sendMessage(ChatColor.RED + "You can't trade with yourself");
			return;
		}
		if(asked.hasPermission("cdpp.trade.toggled")) {
			clog.log(ask.getName() + " tryed to trade with offtoggled Trade player " + asked.getName(), this);
			ask.sendMessage(ChatColor.RED + "You can´t send a request to Player " + ChatColor.RESET + ChatColor.GRAY + asked.getDisplayName() + ChatColor.RESET + ChatColor.RED + ". He has toggled requests off.");
			return;
		}
		if(!asked.hasPermission("cdpp.trade"))
		{
			clog.log(ask.getName() + " tryed to trade with player " + asked.getName() + "which has no trade-permission", this);
			ask.sendMessage(ChatColor.RED + "You can´t send a request to Player " + ChatColor.RESET + ChatColor.GRAY + asked.getDisplayName() + ChatColor.RESET + ChatColor.RED + ". He don´t have permissions to trade");
			return;
		}
		clog.log(ask.getName() + " send an trade offer to " + asked.getName(), this);
		checkRequests(ask, asked);
		requests.put(ask, asked);
		ask.sendMessage(ChatColor.GOLD + "Request send to Player " + ChatColor.GRAY + asked.getDisplayName());
		ask.sendMessage(ChatColor.GOLD + "To cancel the request type " + ChatColor.RESET + ChatColor.GRAY + ChatColor.ITALIC + " /tradeabort");
		asked.sendMessage("Player " + ChatColor.GRAY + ask.getDisplayName() + " asked you, to trade with him");
		asked.sendMessage(ChatColor.GOLD + "Type " + ChatColor.RESET + ChatColor.GRAY + ChatColor.ITALIC + "/tradeacc" + ChatColor.RESET + ChatColor.GOLD + " to accept or");
		asked.sendMessage(ChatColor.GOLD + "type " + ChatColor.RESET + ChatColor.GRAY + ChatColor.ITALIC + "/tradedeny" + ChatColor.RESET + ChatColor.GOLD + " to deny.");
	}
	
	private void tradetoggle(Player p)
	{
		if(!p.hasPermission("cdpp.trade.toggle")) return;
		if(p.hasPermission("cdpp.trade.toggled")){
			clog.log("Toggling trade on for player " + p.getName(), this);
			PermissionUser pu = PermissionsEx.getUser(p.getName());
			if(pu == null) {
				clog.log("PermissionUser for " + p.getName() + " is null", this);
				p.sendMessage(ChatColor.RED + "Something went wrong by toggling your status");
				return;
			}
			pu.removePermission("cdpp.trade.toggled");
			pu.addPermission("-cdpp.trade.toggled");
			clog.log("Toggled trade on for player " + p.getName(), this);
			p.sendMessage(ChatColor.YELLOW + "Trade toggled. Now everybody will be able to send a request to you");
		}
		else {
			clog.log("Toggling trade off for player " + p.getName(), this);
			PermissionUser pu = PermissionsEx.getUser(p.getName());
			if(pu == null) {
				clog.log("PermissionUser for " + p.getName() + " is null", this);
				p.sendMessage(ChatColor.RED + "Something went wrong by toggling your status");
				return;
			}
			pu.addPermission("cdpp.trade.toggled");
			pu.removePermission("-cdpp.trade.toggled");
			clog.log("Toggled trade off for player " + p.getName(), this);
			p.sendMessage(ChatColor.YELLOW + "Trade toggled. Nobody will be able to send a request to you");
		}
	}
	
	private List<ItemStack> pickout(List<ItemStack> input)
	{
		List<ItemStack> i = new CDArrayList<ItemStack>(input);
		i.removeAll(emptyList);
		return i;
	}	
	
	private boolean checkShift(InventoryClickEvent e, boolean left)
	{
		if(!e.isShiftClick()) return true;
		Inventory topInv = e.getWhoClicked().getOpenInventory().getTopInventory();
		if(!topInv.getTitle().equals(tradeTitle)) return true;
		if(e.getInventory() == topInv) return true;
		if((left ? slotsLeft : slotsRight).indexOf(topInv.firstEmpty()) == -1) return false;
		return true;
	}
	private void checkAction(InventoryClickEvent e, boolean leftClicked, Inventory vi)
	{
		int slot = e.getSlot();
		if((leftClicked ? slotsLeft : slotsRight).indexOf(slot) != -1) {
			resetWool(vi);
			return;
		}
		e.setCancelled(true);
		if((leftClicked ? slotsWoolLeft : slotsWoolRight).indexOf(slot) != -1) setWool(vi, leftClicked);
	}
	private boolean checkTrade(Inventory vi)
	{
		if(vi.getName() != tradeTitle) return false;
		if(vi.getItem(12).equals(redWool) && vi.getItem(14).equals(redWool)) return true;
		else return false;
	}
	
	private void doTrade(Inventory vi)
	{
		ItemStack[] leftItems = toArray(getItems(vi, true, false));
		ItemStack[] rightItems = toArray(getItems(vi, false, false));
		resetWool(vi);
		setItems(vi, rightItems, true);
		setItems(vi, leftItems, false);
	}
	private List<ItemStack> getItems(Inventory vi, boolean left, boolean removeNull)
	{
		List<ItemStack> t = new CDArrayList<ItemStack>();
		for(int i = 0; i < 18; i++) t.add(vi.getItem((left ? slotsLeft : slotsRight).get(i)));
		List<ItemStack> back = new CDArrayList<ItemStack>();
		if(removeNull) for(ItemStack i : t) if(i != null) back.add(i.clone());
		return (removeNull ? back : t);
	}
	private void setItems(Inventory vi, ItemStack[] items, boolean left)
	{
		for(int i = 0; i < 18; i++) vi.setItem((left ? slotsLeft : slotsRight).get(i), items == null ? air.clone() : items[i]);
	}
	
	private void resetWool(Inventory vi)
	{
		setWool(vi, true, true);
		setWool(vi, false, true);
	}
	private void setWool(Inventory vi, boolean left)
	{
		setWool(vi, left, !vi.getItem(left ? 12 : 14).equals(greenWool));
	}
	private void setWool(Inventory vi, boolean left, boolean green)
	{
		List<Integer> t = new CDArrayList<Integer>(left ? slotsWoolLeft : slotsWoolRight);
		ItemStack i = (green ? greenWool : redWool).clone();
		for(Integer z : t) vi.setItem(z, i);
	}
	
	private int getLeft(Player p)
	{
		for(Player pl : leftP) if (pl.equals(p)) return 1;
		for(Player pl : rightP) if (pl.equals(p)) return 0;
		return -1;
	}
	
	private ItemStack[] toArray(List<ItemStack> input)
	{
		ItemStack[] back = new ItemStack[input.size()];
		for(int i = 0; i < input.size(); i++) back[i] = input.get(i);
		return back;
	}
	
	private Player getOtherPlayer(Player p, Map<Player, Player> map)
	{
		Player p2 = map.get(p);
		if(p2 != null) return p2;
		for(Player pl : map.keySet())
			if(map.get(pl).equals(p))
				return pl;
		return null;
	}
}