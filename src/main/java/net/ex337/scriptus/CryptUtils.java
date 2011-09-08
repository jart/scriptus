package net.ex337.scriptus;

import java.security.Key;
import java.security.MessageDigest;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.Mac;

import net.ex337.scriptus.exceptions.ScriptusRuntimeException;

import org.apache.commons.lang.StringUtils;

public class CryptUtils {

	public static String tokenize(String value, Key key) {
		return tokenize(value.getBytes(), key);
	}
	public static String tokenize(byte[] bytes, Key key) {
		return toHex(encrypt("AES", bytes, key));
	}
	public static String detokenize(String value, Key key) {
		return new String(decrypt("AES", fromHex(value), key));
	}

	public static byte[] encrypt(String cipherScheme, byte[] plaintext, Key key) {
		try {
			Cipher cipher = Cipher.getInstance(cipherScheme);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] result = cipher.doFinal(plaintext);
			return result;
		} catch (Exception e) {
			throw new ScriptusRuntimeException("Problem with cipher", e);
		}
	}

	public static byte[] decrypt(String cipherScheme, byte[] ciphertext, Key key) {
		try {
			Cipher cipher = Cipher.getInstance(cipherScheme);
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(ciphertext);
		} catch (Exception e) {
			throw new ScriptusRuntimeException("problem with cipher", e);
		}
	}

	public static String toHex(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int v = (int) b[i];
			v = v < 0 ? 0x100 + v : v;
			String cc = Integer.toHexString(v);
			if (cc.length() == 1)
				sb.append('0');
			sb.append(cc);
		}
		return sb.toString();
	}
	public static byte[] fromHex(String hex) {
		return fromHex(hex, -1);
	}
	public static byte[] fromHex(String hex, int expectedLength) {
		
		if(hex == null) {
			throw new ScriptusRuntimeException("hex string should not be null");
		}
		if (hex.length() % 2 == 1) {
			RuntimeException e = new ScriptusRuntimeException("Hex.length="+hex.length());
			e.printStackTrace();
			throw e;
		}
		if(expectedLength != -1 && hex.length() / 2 != expectedLength) {
			throw new ScriptusRuntimeException("Expected "+expectedLength+" bytes, got "+hex.length() / 2);
		}
		byte[] result = new byte[hex.length() / 2];
		for (int i = 0; i != result.length; i++) {
			result[i] = Integer.valueOf(hex.substring(i * 2, i * 2 + 2), 16).byteValue();
		}
		return result;
	}

	public static final byte[] hash(String hash, String str) {
		try {
			MessageDigest md = MessageDigest.getInstance(hash);
			md.update(str.getBytes());
			return md.digest();
		} catch (Exception e) {
			throw new ScriptusRuntimeException("probmem with hash", e);
		}
	}
	public static String toHex(UUID uuid) {

		String msb = Long.toHexString(uuid.getMostSignificantBits());
		String lsb = Long.toHexString(uuid.getLeastSignificantBits());
		
		if(msb.length() < 16) {
			msb = StringUtils.repeat("0", 16 - msb.length()) + msb;
		}
		if(lsb.length() < 16) {
			lsb = StringUtils.repeat("0", 16 - lsb.length()) + lsb;
		}
		
		return msb+lsb;
	}

    public static byte[] mac(String macScheme, byte[] plaintext, Key key) {

    	try {

    		Mac mac = Mac.getInstance(macScheme);

    		mac.init(key);

    		return mac.doFinal(plaintext);
    		
            } catch (Exception e) {
                throw new ScriptusRuntimeException("problem with mac", e);
            }
       
        }
	
	
}