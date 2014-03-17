package net.gmx.teamterrian.CDsPluginPack.plugins;

import java.io.File;
import java.util.ArrayList;

import net.gmx.teamterrian.CDsPluginPack.CDPlugin;
import net.gmx.teamterrian.CDsPluginPack.PluginHandler;
import net.gmx.teamterrian.CDsPluginPack.handle.CDPluginCommand;
import net.gmx.teamterrian.CDsPluginPack.handle.events.CommandEvent;
import net.gmx.teamterrian.CDsPluginPack.plugins.BlockCommand.TriggerType;
import net.gmx.teamterrian.CDsPluginPack.tools.Log;
import net.gmx.teamterrian.CDsPluginPack.tools.VarTools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

public class SSConv extends CDPlugin
{
	Log clog;
	
	public SSConv(PluginHandler handler)
	{
		super(handler);
		clog = handler.clog;
	}
	
	@CDPluginCommand(commands = { "ssc cdpp.ssc 1" })
	public void loadSigns(CommandEvent e)
    {
		BlockCommand bc = (BlockCommand) handler.plugins.get(BlockCommand.class);
		
		Location l;
		BCBlockData blockData;
		BCTriggerData triggerData;
		BCCommandData commandData;
		BCTimestamp timestamp;
		
		
		
        File dir = new File((new StringBuilder("plugins")).append(File.separator).append("ServerSigns").append(File.separator).append("signs").toString());
        dir.mkdirs();
        File files[] = dir.listFiles();
        File afile[];
        int k = (afile = files).length;
        for(int j = 0; j < k; j++)
        {
            File file = afile[j];
            if(file.getName().endsWith(".yml"))
                if(file.length() < 50L)
                {
                    clog.log((new StringBuilder("Could not load sign ")).append(file.getName()).append(". The file is empty, proceeding to next file.").toString(), this);
                } else
                {
                    YamlConfiguration yamlLoad = YamlConfiguration.loadConfiguration(file);
                    blockData = new BCBlockData();
                    triggerData = new BCTriggerData();
                    
                    l = new Location(Bukkit.getWorld(yamlLoad.getString("world")), (double) yamlLoad.getInt("X"), (double) yamlLoad.getInt("Y"), (double) yamlLoad.getInt("Z"));
                    timestamp = new BCTimestamp();
                    
                    timestamp.setGlobalCooldown(yamlLoad.getLong("globalCooldown"));
                    if(timestamp.getGlobalCooldown() == 0) timestamp.setGlobalCooldown(-1);
                    timestamp.setPlayerCooldown(yamlLoad.getLong("cooldown"));
                    if(timestamp.getPlayerCooldown() == 0) timestamp.setPlayerCooldown(-1);
                    triggerData.setBlockCooldowns(timestamp);
                    String s;
                    String[] cmd;
                    int i;
                    for(Object o : ((ArrayList<?>)yamlLoad.getList("commands")))
                    {
                    	s = (String) o;
                    	if(s.startsWith("<server>")) s = "@c " + s.substring(8);
                    	else if(s.startsWith("*")) s = "@o " + s.substring(1);
                    	else s = "@p " + s;
                		cmd = VarTools.stringToArr(s, 0);
                		i = cmd[0].startsWith("@") ? 1 : 0;
                    	if(cmd[i].startsWith("/")) cmd[i] = cmd[i].substring(1);
                    	
                    	commandData = new BCCommandData(cmd, -1, -1, TriggerType.CLICK);
                    	triggerData.addCommandData(commandData);
                    }
                    blockData.setTriggerData(TriggerType.CLICK, triggerData);
                    bc.places.put(l, blockData);
                }
        }
        e.getSender().sendMessage(ChatColor.GOLD + "Signs converted");

    }
}
