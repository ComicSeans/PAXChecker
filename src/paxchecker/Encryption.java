/*******************************************************************************
 * PAXCheckerCMD
 *
 * This software is created under an MIT License. Originally created by
 * Sunnybat, this version has been forked and modified by ComicSeans.
 *
 * Contributors:
 *		SunnyBat
 *		ComicSeans
 *******************************************************************************/

package paxchecker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Credit goes to Johannes Brodwall on StackOverflow for providing this class.
 * Submitted back in '09, I found it and decided it would be a good idea to
 * protect the password, even if it's reversible. The idea is that memory
 * sniffers won't be able to decrypt it without knowing about this program.
 *
 * @author Johannes Brodwall
 */
public class Encryption {

	private static final byte[] SALT = { (byte) 0xde, (byte) 0x33, (byte) 0x10,
			(byte) 0x12, (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, };

	public static String encrypt(String property)
			throws GeneralSecurityException, UnsupportedEncodingException {
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(System
				.getProperty("java.version").toCharArray()));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher
				.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
	}

	private static String base64Encode(byte[] bytes) {
		// NB: This class is internal, and you probably should use another impl
		return new BASE64Encoder().encode(bytes); // Need to replace with a
													// different encoder
	}

	public static String decrypt(String property)
			throws GeneralSecurityException, IOException {
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(System
				.getProperty("java.version").toCharArray()));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher
				.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
	}

	private static byte[] base64Decode(String property) throws IOException {
		// NB: This class is internal, and you probably should use another impl
		return new BASE64Decoder().decodeBuffer(property); // Need to replace
															// with a different
															// encoder
	}

}
