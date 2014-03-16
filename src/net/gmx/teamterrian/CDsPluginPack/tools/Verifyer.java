package net.gmx.teamterrian.CDsPluginPack.tools;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
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
			String hashStr = getString(createHash(getZipFiles(), "SHA-1"));
			byte[] hash = createHash(hashStr, "SHA-1");
			byte[] reByted = new byte[20];
			for(int i = 0; i < 20; i++)
				reByted[i] = (byte) (0xFF & hash[i]);
			byte[] reByted2 = new byte[20];
			for(int i = 0; i < 20; i++)
				reByted2[i] = (byte) (0xFF & givenhash[i]);
	        if(Arrays.equals(reByted, reByted2)) {
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
	/*private byte[] createHash(File file, String hashalg)
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
	}*/
	private byte[] createHash(String s, String hashalg)
	{
		return createHash(s.getBytes(), hashalg);
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
			
			z.close();
		}
		catch (IOException e) { return ""; }
		return sb.toString();
	}
	/*private String hashZip(ZipFile z, String filename)
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
	        {s
	        	b[i] = out.get(0);
	        	out.remove(0);
	        }
	        s.close();
	        return getString(createHash(b, "SHA-1"));
		}
		catch(Exception x) { return ""; }
	}*/
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
