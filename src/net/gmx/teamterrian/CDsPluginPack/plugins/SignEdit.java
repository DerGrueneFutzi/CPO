package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.util.List;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.handle.exceptions.CDInvalidArgsException;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys;
import net.gmx.teamterrian.CDsPluginPack.tools.Dependencys.Dependency;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.sk89q.worldedit.bukkit.selections.Selection;

public class SignEdit extends CDPlugin
{	
	Log clog;
	String mbeg = ChatColor.DARK_AQUA + "[SignEdit] " + ChatColor.AQUA;
	Dependencys d;
	
	public SignEdit(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
		d = handler.dependencys;
	}

	public Permission[] getPermissions()
	{
		return new Permission[]
		{
			new Permission("cdpp.se", PermissionDefault.OP)
		};
	}
		
	@CDPluginCommand(commands = { "se cdpp.se 1" })
	public void onCommand(CommandEvent e) throws CDInvalidArgsException
	{
		process(e.getArgs(), e.getSender(), e.getCommand());
	}
	
	private boolean process(String[] args, CommandSender sender, Command cmd) throws CDInvalidArgsException
	{
		if(!d.doDepend(Dependency.WORLDEDIT, sender)) return true;
		if(args.length < 2) throw new CDInvalidArgsException("se");
		Selection s = d.we.getSelection((Player) sender);
		if(s == null)
			sender.sendMessage(mbeg + "Please select a region with WorldEdit");
		return cSign(VarTools.getLocations(s), (Player) sender, args);
	}
	
	private boolean cSign(List<Location> locs, Player p, String[] args)
	{
		String s = VarTools.SB(args, 1);
		Block b;
		int line;
		try { line = Integer.valueOf(args[0]); } catch (Exception x) { x.printStackTrace(); return false; }
		if(s.length() > 15) { p.sendMessage(ChatColor.RED + "There are only 15 chars per line"); return true; }
		if(line > 4) { p.sendMessage(ChatColor.RED + "There are only 4 lines per sign"); return true; }
		BlockState bs;
		Sign si;
		for(Location l : locs)
		{
			b = p.getWorld().getBlockAt(l);
			if (!((b.getType().equals(Material.WALL_SIGN)) || (b.getType().equals(Material.SIGN_POST)))) continue;
			bs = b.getState();
			si = (Sign) bs;
			clog.log(p.getName() + " changing line " + l + " of sign on " + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + " from " + si.getLine(line - 1) + " to " + s, this);
			si.setLine(line - 1, s);
			bs.update(true);
		}
		return true;
	}
}