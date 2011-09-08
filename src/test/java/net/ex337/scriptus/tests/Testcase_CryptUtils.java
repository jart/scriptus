package net.ex337.scriptus.tests;

import static net.ex337.scriptus.CryptUtils.decrypt;
import static net.ex337.scriptus.CryptUtils.detokenize;
import static net.ex337.scriptus.CryptUtils.encrypt;
import static net.ex337.scriptus.CryptUtils.fromHex;
import static net.ex337.scriptus.CryptUtils.hash;
import static net.ex337.scriptus.CryptUtils.tokenize;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import junit.framework.TestCase;
import net.ex337.scriptus.CryptUtils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class Testcase_CryptUtils extends TestCase {
	
	private static final String TOKEN_KEY = "TokenKeyMustBe32BytesToAvoid";//...padding
	private static final String TEST_TOKEN_VALUE = "this is a test token value of arbirary length";
	private SecretKeySpec key;

	@Override
	protected void setUp() throws Exception {
		this.key = new SecretKeySpec(hash("sha1", TOKEN_KEY), 0, 16, "AES");
	}

	public void test_01_encryption() {
		
		byte[] encrypted = encrypt("AES", TEST_TOKEN_VALUE.getBytes(), key);
		
		String toHex = CryptUtils.toHex(encrypted);
		
		byte[] fromHex = fromHex(toHex);
		
		assertTrue("hex encoding accurate", ArrayUtils.isEquals(encrypted, fromHex));
		
		byte[] decrypted = decrypt("AES", fromHex, key);
		
		assertTrue("decrypted correctly", ArrayUtils.isEquals(TEST_TOKEN_VALUE.getBytes(), decrypted));
		
	}

	public void test_02_tokenize() {

		byte[] encrypted = encrypt("AES", TEST_TOKEN_VALUE.getBytes(), key);
		
		String toHex = CryptUtils.toHex(encrypted);

		String token = tokenize(TEST_TOKEN_VALUE, key);
		
		assertEquals(toHex, token);

		byte[] fromHex = fromHex(toHex);
		
		assertTrue("hex encoding accurate", ArrayUtils.isEquals(encrypted, fromHex));
		
		byte[] decrypted = decrypt("AES", fromHex, key);
		
		assertTrue("decrypted correctly", ArrayUtils.isEquals(TEST_TOKEN_VALUE.getBytes(), decrypted));
		
		String result = detokenize(token, key);
		
		assertEquals(TEST_TOKEN_VALUE, result);
		assertTrue("Decrypted value equal to detokenized value", ArrayUtils.isEquals(decrypted, result.getBytes()));
	}
	
	public void test_03_sha() throws NoSuchAlgorithmException {
	
		String str = "foo";
		
		MessageDigest md = MessageDigest.getInstance("SHA1");
		md.update(str.getBytes());
		byte[] b = md.digest();
		System.out.println(b.length);
		
		System.out.println(CryptUtils.toHex(b));
		

	}
	
	public void test_mac() {
		
		String foo = "this is to be macced";
		
		Key key = null;
		
		byte[] b = CryptUtils.mac("HMAC-SHA256", foo.getBytes(), key);
		
	}
	
	public void test_04_UUID() {
		
		String uuidStr = "cfbf283e-2692-b150-5cf3-19b38276f0a7";
		UUID uuid = UUID.fromString(uuidStr);
		
		String hashStr = StringUtils.remove(uuidStr, "-");
		
		System.out.println("uuidStr="+uuidStr);
		System.out.println("uuid="+uuid);
		System.out.println("hashStr="+hashStr);
		
		System.out.println(CryptUtils.toHex(uuid));
		
		assertEquals(hashStr, CryptUtils.toHex(uuid));
		
	}

	
}