package net.gmx.teamterrian.CDsPluginPack.plugins;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.sk89q.bukkit.util.DynamicPluginCommand;
import com.sk89q.worldedit.bukkit.selections.Selection;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginEvent;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

public class WorldEditWarning extends CDPlugin
{
	String mbeg = ChatColor.RED + "[WorldEditWaring] " + ChatColor.DARK_RED;
	Log clog;
	
	public WorldEditWarning(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}
	
	@CDPluginEvent(priority = 1)
	public void onCommand(PlayerCommandPreprocessEvent e)
	{
		if(e.getMessage().length() == 1) return;
		String cmd = e.getMessage().substring(1);
		String simpleCmd = VarTools.stringToArr(cmd, 0)[0];
		if(simpleCmd.equalsIgnoreCase("dobig")) {
			clog.log("Running probably big WorldEdit Operation '" + cmd + "' from " + e.getPlayer().getName() + ". Permissionhandling is done by WorldEdit", this);
			e.setMessage(VarTools.arrToString(VarTools.stringToArr(cmd, 1), 0));
			return;
		}
		Command c;
		if((c = handler.cRegister.commandMap.getCommand(simpleCmd)) == null ||
				!(c instanceof DynamicPluginCommand) ||
				((DynamicPluginCommand) c).getPlugin() != handler.dependencys.we) return;
		Player p = e.getPlayer();
		Selection s = handler.dependencys.we.getSelection(p);
		if(s == null || s.getHeight() * s.getLength() * s.getWidth() < 1000000) return;
		clog.log("Catched big WorldEdit operation '" + e.getMessage() + "' from " + p.getName(), this);
		p.sendMessage(mbeg + "You´ve selected a Area with over 1 Mio. Blocks");
		p.sendMessage(mbeg + "If this was your intention, please run this command with prefix /dobig");
		p.sendMessage(mbeg + "Example: /dobig //set 1");
		e.setCancelled(true);
	}
}
