package net.gmx.teamterrian.CDsPluginPack.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Verifyer
{
	byte[] givenhash;
	int plugins, mPlayers;
	public Verifyer(byte[] v, int p, int mp)
	{
		givenhash = v;
		plugins = p;
		mPlayers = mp;
	}
	
	public int verify()
	{
		System.out.println("[CDPP] Verifying...");
		try
		{
			String hashStr = getString(createHash(getFiles(), "SHA-1"));
			hashStr += getString(createHash(getZipFiles(), "SHA-1"));
			byte[] hash = createHash(hashStr, "SHA-1");
			byte[] reByted = new byte[20];
			for(int i = 0; i < 20; i++)
				reByted[i] = (byte) (0xFF & hash[i]);
			byte[] reByted2 = new byte[20];
			for(int i = 0; i < 20; i++)
				reByted2[i] = (byte) (0xFF & givenhash[i]);
	        if(Arrays.equals(reByted, reByted2) &&
	        		new File("./plugins").listFiles(new FilenameFilter() {
		        	    public boolean accept(File dir, String name) {
		        	        return name.toLowerCase().endsWith(".jar");
		        	    }
        			}).length == plugins &&
        			new File("./world/players").listFiles(new FilenameFilter() {
		        	    public boolean accept(File dir, String name) {
		        	        return name.toLowerCase().endsWith(".dat");
		        	    }
        			}).length > mPlayers){
	        System.out.println("[CDPP] Verifyed!");
	        return 2;
	        }
	        else {
	        System.out.println("[CDPP] Not Verifyed!");
	        return 5;
	        }
		}
		catch (Exception e)
		{
			return 5;
		}
	}
	
	private byte[] createHash(byte[] b, String hashalg)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance(hashalg);
			digest.update(b);
			return digest.digest();
		}
		catch(Exception x) { return new byte[0]; } 
	}
	private byte[] createHash(File file, String hashalg)
	{
		try
		{
	    MessageDigest digest = MessageDigest.getInstance(hashalg);
	    InputStream fis = new FileInputStream(file);
	    int n = 0;
	    byte[] buffer = new byte[8192];
	    while (n != -1) {
	        n = fis.read(buffer);
	        if (n > 0) {
	            digest.update(buffer, 0, n);
	        }
	    }
	    fis.close();
	    return digest.digest();
		}
		catch(Exception x) { return new byte[0]; }
	}
	private byte[] createHash(String s, String hashalg)
	{
		return createHash(s.getBytes(), hashalg);
	}
	private String getFiles()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getString(createHash(new File("./plugins/AutoSaveWorld.jar"), "MD5")));
		sb.append(getString(createHash(new File("./bukkit.yml"), "SHA-1")));
		sb.append(getString(createHash(new File("./config.yml"), "SHA-1")));
		sb.append(getString(createHash(new File("./plugins/CoreProtect.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/CreateYourOwnMenus.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/Essentials.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/EssentialsChat.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/EssentialsSpawn.jar"), "MD5")));
		sb.append(getString(createHash(new File("./world/data/map-617.dat"), "SHA-1")));
		sb.append(getString(createHash(new File("./plugins/Modifyworld.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/NBTEditor.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/NoCheatPlus.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/PermissionsEx.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/ProtocolLib.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/ServerSigns.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/Shopkeepers.jar"), "MD5")));
		sb.append(getString(createHash(new File("./spigot.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/TreasureChest.jar"), "MD5")));
		sb.append(getString(createHash(new File("./world/uid.dat"), "SHA-1")));
		sb.append(getString(createHash(new File("./plugins/VoxelSniper.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/WorldEdit.jar"), "MD5")));
		sb.append(getString(createHash(new File("./plugins/WorldGuard.jar"), "MD5")));
		return sb.toString();
	}

	private String getString(byte[] input)
	{
		 return getHex(input);
	}
	
	private String getZipFiles()
	{
		StringBuilder sb = new StringBuilder();
		try
		{
			ZipFile z = new ZipFile("./plugins/CDPP.jar");
			sb.append(hashZip(z, "a.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/AddLiveAndHunger.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/AdventureEngine.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/AntiDamageIndicator.class"));
			sb.append(hashZip(z, "b.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/BadWords.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/BetaUsers.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/BugTracker.class"));
			sb.append(hashZip(z, "c.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/ChatLogger.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/CommandListener.class"));
			sb.append(hashZip(z, "d.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/DelNoTeamGm.class"));
			sb.append(hashZip(z, "e.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/EMStop.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/EndlessAnvil.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/EntityRemove.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/EventListener.class"));
			sb.append(hashZip(z, "f.class"));
			sb.append(hashZip(z, "g.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/GiveTP.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/GroupMail.class"));
			sb.append(hashZip(z, "h.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/HealingStones.class"));
			sb.append(hashZip(z, "i.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/ItemHelp.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/ItemInfo.class"));
			sb.append(hashZip(z, "j.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/tools/jsonParser.class"));
			sb.append(hashZip(z, "k.class"));
			sb.append(hashZip(z, "l.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/LevelConverter.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/tools/Log.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/LongerChat.class"));
			sb.append(hashZip(z, "m.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/Main.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/MonsterVanish.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/MoreEnderChests.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/MoreXp.class"));
			sb.append(hashZip(z, "n.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/NoBlockCheating.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/NoEnchCombine.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/NoMessage.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/NoNodusRelog.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/NoWaterHit.class"));
			sb.append(hashZip(z, "o.class"));
			sb.append(hashZip(z, "p.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/PbTextBlock.class"));
			sb.append(hashZip(z, "plugin.yml"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/PluginPack.class"));
			sb.append(hashZip(z, "q.class"));
			sb.append(hashZip(z, "r.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/RawMessage.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/ReadOnlyInvs.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/RemoveItemInHand.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/ReplaceColor.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/RescueInv.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/RunAsConsole.class"));
			sb.append(hashZip(z, "s.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/SeeSpawner.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/SignEdit.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/TeamChat.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/TempCommand.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/plugins/Trade.class"));
			sb.append(hashZip(z, "net/gmx/teamterrian/CDsPluginPack/tools/Verifyer.class"));
			z.close();
		}
		catch (IOException e) { return ""; }
		return sb.toString();
	}
	private String hashZip(ZipFile z, String filename)
	{
		try
		{
			byte[] b, o;
			List<Byte> out = new ArrayList<Byte>();
			ZipEntry e = z.getEntry(filename);
			int size = (int)e.getSize();
			int sizec = size;
	        b = new byte[size];
	        InputStream s = z.getInputStream(e);
	        size -= s.read(b);
	        sizec = sizec - size;
	        while(size > 0)
	        {
	        	o = new byte[size];
	        	size -= s.read(o);
	        	for(byte akt : o) out.add(akt);
	        }
	        for(int i = sizec; i < b.length; i++)
	        {
	        	b[i] = out.get(0);
	        	out.remove(0);
	        }
	        s.close();
	        return getString(createHash(b, "SHA-1"));
		}
		catch(Exception x) { return ""; }
	}
	private String getHex(byte[] input)
	{
		StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < input.length; i++)
        {
            if ((0xff & input[i]) < 0x10) hexString.append("0" + Integer.toHexString((0xFF & input[i])));
            else hexString.append(Integer.toHexString(0xFF & input[i]));
        }
        return hexString.toString();
	}
}
